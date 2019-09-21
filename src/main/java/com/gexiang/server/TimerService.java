package com.gexiang.server;

import com.gexiang.Util.Helper;
import com.gexiang.constant.ConstValue;
import com.gexiang.io.IoFactory;
import com.gexiang.repository.entity.TimerReq;
import com.gexiang.repository.mapper.JobDao;
import com.gexiang.repository.mapper.LockerDao;
import com.gexiang.repository.mapper.NotifyDao;
import com.gexiang.repository.mapper.TimeDao;
import com.gexiang.vo.GxTimerRequest;
import com.gexiang.vo.GxTimerResponse;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class TimerService {
    private static final int JOB_RATE = 250;
    private static final int NOTIFY_RATE = 500;
    private static final int MAX_QUEUE_SIZE = 10000;
    private static Logger logger = LoggerFactory.getLogger(TimerService.class);
    private final LockerDao lockerDao;
    private final JobDao jobDao;
    private final NotifyDao notifyDao;
    private final TimeDao timeDao;
    private String owner;
    private GxLock jobLock;
    private GxLock notifyLock;
    private WorkerPool<TimerConsumer> jobWorker;
    private WorkerPool<TimerConsumer> notifyWorker;
    private ProducerConsumer<TimerReq> jobProducerConsumer;
    private ProducerConsumer<TimerReq> notifyProducerConsumer;

    @Autowired
    public TimerService(LockerDao lockerDao, JobDao jobDao, NotifyDao notifyDao, TimeDao timeDao){
        int cpuNum = Runtime.getRuntime().availableProcessors();
        this.lockerDao = lockerDao;
        this.jobDao = jobDao;
        this.notifyDao = notifyDao;
        this.timeDao = timeDao;
        /**初始化**/
        JobTimeMgr.getInstance().setTimeDao(timeDao);
        Preconditions.checkArgument((lockerDao != null), "Locker Dao is null");
        Preconditions.checkArgument((jobDao != null), "jobDao is null");
        Preconditions.checkArgument((notifyDao != null), "notifyDao is null");

        owner = Helper.randomString(16);
        logger.info("Owner:{}, Cpu num:{}", owner, cpuNum);
        jobWorker = new WorkerPool<>(cpuNum, JOB_RATE, MAX_QUEUE_SIZE, "job", this::handleJob);
        notifyWorker = new WorkerPool<>(2, NOTIFY_RATE, MAX_QUEUE_SIZE, "notify", this::handleNotify);
        jobProducerConsumer = new ProducerConsumer<>("job", 1000L, this::jobProducer, this::jobConsumer);
        notifyProducerConsumer = new ProducerConsumer<>("notify", 1000L, this::notifyProducer, this::nofityConsumer);
    }

    public void stop(){
        jobWorker.setStop();
        notifyWorker.setStop();
        jobProducerConsumer.setStop();
        notifyProducerConsumer.setStop();
    }

    public Mono<GxTimerResponse> submitJob(GxTimerRequest request){
        return Mono.create(new TimerConsumer(request, jobWorker));
    }

    public Mono<GxTimerResponse> submitNotify(GxTimerRequest request){
        return Mono.create(new TimerConsumer(request, notifyWorker));
    }

    private void handleJob(TimerConsumer timerConsumer){
        TimerReq timerReq = Helper.transFromRequest(timerConsumer.getRequest());
        Long now  = JobTimeMgr.getInstance().getNow(ConstValue.TIMER_JOB_NAME);
        timerReq.setNextTime(JobTimeMgr.calNextJobTime(now));
        Long timeLong = timerReq.getEndTime();
        if(timeLong == null){
            timeLong = ConstValue.DEF_JOB_TIME_LONG;
        }
        Long end = Long.valueOf(now.longValue() + timeLong.longValue());
        timerReq.setEndTime(JobTimeMgr.calNextJobTime(end));
        try{
            jobDao.add(timerReq);
        }catch (Throwable t){
            logger.warn("Insert job {}.{} exceptions:{}", timerReq.getAppId(), timerReq.getJobId(), t.getMessage());
        }
    }

    private void handleNotify(TimerConsumer timerConsumer){
//        TimerReq timerReq = Helper.transFromRequest(timerConsumer.getRequest());
//        try{
//            notifyDao.add(timerReq);
//        }catch (Throwable t){
//            logger.warn("Insert notify {}.{} exceptions:{}", timerReq.getAppId(), timerReq.getJobId(), t.getMessage());
//        }
    }

    private void jobProducer(ProducerConsumer<TimerReq> producerConsumer){
        /**单线程, 可以这样**/
        if(jobLock == null){
            jobLock = new GxLock("timer","job.producer", owner, lockerDao);
        }

        if(jobLock.tryLock() == false){
            return;
        }

        try{
            JobTimeMgr.getInstance().beginJob(ConstValue.TIMER_JOB_NAME);
            Long last = JobTimeMgr.getInstance().getJobLastTime(ConstValue.TIMER_JOB_NAME);
            Long now  = JobTimeMgr.getInstance().getNow(ConstValue.TIMER_JOB_NAME);
            if((last == null) || (now == null)){
                return;
            }

            long offsetId = 0;
            while(true){
                List<TimerReq> reqList = jobDao.getDelta(offsetId, last.longValue(), now.longValue(), ConstValue.MAX_QUEUE_SIZE);
                if(reqList == null || reqList.isEmpty()){
                    break;
                }

                offsetId = reqList.get(reqList.size() - 1).getId();
                if(producerConsumer.addPool(reqList) != 0){
                    reqList.clear();
                    while (true){
                        if((producerConsumer.getPoolSize() < ConstValue.MAX_QUEUE_SIZE/2)){
                            break;
                        }
                    }
                    Helper.sleepMills(1000);
                }else{
                    reqList.clear();
                }

                if(reqList.size() < ConstValue.MAX_QUEUE_SIZE){
                    break;
                }
            }

            JobTimeMgr.getInstance().saveJob(ConstValue.TIMER_JOB_NAME);
        }catch (Throwable t){
            logger.warn("Get delta job {} exceptions:{}", ConstValue.TIMER_JOB_NAME, t.getMessage());
        }finally {
            jobLock.releaseLock();
        }
    }

    private void jobConsumer(TimerReq timerReq){
        try{
            timerReq.setStatus(ConstValue.JOB_SUCCESS_STATE);
            IoFactory.getInstance().forward(timerReq);
        }catch (Throwable t){
            logger.warn("Update job {}.{} exceptions:{}", timerReq.getAppId(), timerReq.getJobId(), t.getMessage());
        }
    }

    private void notifyProducer(ProducerConsumer<TimerReq> producerConsumer){
        /**单线程, 可以这样**/
        if(notifyLock == null){
            notifyLock = new GxLock("timer","notify.producer", owner, lockerDao);
        }

        if(notifyLock.tryLock() == false){
            return;
        }

        try{
            JobTimeMgr.getInstance().beginJob(ConstValue.TIMER_NOTIFY_NAME);
            Long last = JobTimeMgr.getInstance().getJobLastTime(ConstValue.TIMER_NOTIFY_NAME);
            Long now  = JobTimeMgr.getInstance().getNow(ConstValue.TIMER_NOTIFY_NAME);
            if((last == null) || (now == null)){
                return;
            }

            long offsetId = 0;
            while(true){
                List<TimerReq> reqList = notifyDao.getDelta(offsetId, last.longValue(), now.longValue(), ConstValue.MAX_QUEUE_SIZE);
                if(reqList == null || reqList.isEmpty()){
                    break;
                }

                offsetId = reqList.get(reqList.size() - 1).getId();
                if(producerConsumer.addPool(reqList) != 0){
                    reqList.clear();
                    while (true){
                        if((producerConsumer.getPoolSize() < ConstValue.MAX_QUEUE_SIZE/2)){
                            break;
                        }
                    }
                    Helper.sleepMills(1000);
                }else{
                    reqList.clear();
                }

                if(reqList.size() < ConstValue.MAX_QUEUE_SIZE){
                    break;
                }
            }

            JobTimeMgr.getInstance().saveJob(ConstValue.TIMER_NOTIFY_NAME);
        }catch (Throwable t){
            logger.warn("Get delta job {} exceptions:{}", ConstValue.TIMER_NOTIFY_NAME, t.getMessage());
        }finally {
            notifyLock.releaseLock();
        }
    }

    private void nofityConsumer(TimerReq timerReq){

    }
}

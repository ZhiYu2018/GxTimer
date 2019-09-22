package com.gexiang.server;

import com.gexiang.Util.Helper;
import com.gexiang.Util.TimerBackoff;
import com.gexiang.constant.ConstValue;
import com.gexiang.io.CDRWriter;
import com.gexiang.io.IoFactory;
import com.gexiang.repository.entity.TimerReq;
import com.gexiang.repository.mapper.JobDao;
import com.gexiang.repository.mapper.LockerDao;
import com.gexiang.repository.mapper.NotifyDao;
import com.gexiang.repository.mapper.TimeDao;
import com.gexiang.vo.GxResult;
import com.gexiang.vo.GxTimeCbRequest;
import com.gexiang.vo.GxTimerRequest;
import com.gexiang.vo.GxTimerResponse;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class TimerService implements AutoCloseable{
    private static final int JOB_RATE = 250;
    private static final int NOTIFY_RATE = 500;
    private static final int MAX_QUEUE_SIZE = 2000;
    private static Logger logger = LoggerFactory.getLogger(TimerService.class);
    private final LockerDao lockerDao;
    private final JobDao jobDao;
    private final NotifyDao notifyDao;
    private String owner;
    private GxLock jobLock;
    private GxLock notifyLock;
    private WorkerPool<TimerConsumer> jobWorker;
    private WorkerPool<TimerConsumer> notifyWorker;
    private WorkerPool<GxResult<TimerReq, Object>> jobResultWorker;
    private WorkerPool<GxResult<TimerReq, Object>> notifyResultWorker;
    private ProducerConsumer<TimerReq> jobProducerConsumer;
    private ProducerConsumer<TimerReq> notifyProducerConsumer;

    @Autowired
    public TimerService(LockerDao lockerDao, JobDao jobDao, NotifyDao notifyDao, TimeDao timeDao){
        int cpuNum = Runtime.getRuntime().availableProcessors();
        this.lockerDao = lockerDao;
        this.jobDao = jobDao;
        this.notifyDao = notifyDao;
        /**初始化**/
        JobTimeMgr.getInstance().setTimeDao(timeDao);
        CDRWriter.getInstance().init("./cdr/");
        Preconditions.checkArgument((lockerDao != null), "Locker Dao is null");
        Preconditions.checkArgument((jobDao != null), "jobDao is null");
        Preconditions.checkArgument((notifyDao != null), "notifyDao is null");

        owner = Helper.randomString(16);
        logger.info("Owner:{}, Cpu num:{}", owner, cpuNum);
        jobWorker = new WorkerPool<>(cpuNum, JOB_RATE, MAX_QUEUE_SIZE, "job", this::handleJob);
        notifyWorker = new WorkerPool<>(2, NOTIFY_RATE, MAX_QUEUE_SIZE, "notify", this::handleNotify);
        jobResultWorker = new WorkerPool<>(cpuNum, JOB_RATE, MAX_QUEUE_SIZE, "job.return", this::handJobReturn);
        notifyResultWorker = new WorkerPool<>(cpuNum, JOB_RATE, MAX_QUEUE_SIZE, "notify.return", this::handNotifyReturn);
        jobProducerConsumer = new ProducerConsumer<>("job", 1000L, this::jobProducer, this::jobConsumer);
        notifyProducerConsumer = new ProducerConsumer<>("notify", 1000L, this::notifyProducer, this::nofityConsumer);
    }

    @Override
    public void close(){
        logger.info("TimerService is close");
        jobWorker.setStop();
        notifyWorker.setStop();
        jobProducerConsumer.setStop();
        notifyProducerConsumer.setStop();
        CDRWriter.getInstance().close();
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
        /**直接调用**/
        TimerReq timerReq = Helper.transFromRequest(timerConsumer.getRequest());
        IoFactory.getInstance().forward(timerReq, notifyResultWorker);
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
        timerReq.setTimes(timerReq.getTimes() + 1);
        IoFactory.getInstance().forward(timerReq, jobResultWorker);
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
        timerReq.setTimes(timerReq.getTimes() + 1);
        IoFactory.getInstance().forward(timerReq, notifyResultWorker);
    }

    private void handJobReturn(GxResult<TimerReq, Object> result){
        if(result.getResult() instanceof String){
            /***对方有成功反应，算是成功**/
            String body = String.valueOf(result.getResult());
            if(!Helper.isStrEmpty(result.getData().getCbUrl())){
                GxTimeCbRequest gxcb = new GxTimeCbRequest();
                gxcb.setStatus(HttpStatus.OK.value());
                gxcb.setMsg(HttpStatus.OK.getReasonPhrase());
                gxcb.setAppId(result.getData().getAppId());
                gxcb.setJobId(result.getData().getJobId());
                gxcb.setBody(body);
                IoFactory.getInstance().callBack(gxcb, result.getData().getCbUrl());
            }
            /**处理成功**/
            result.getData().setStatus(ConstValue.JOB_SUCCESS_STATE);
            try{
                jobDao.updateStatus(result.getData());
            }catch (Throwable t){
                logger.error("Update appId:{}, jobId:{}, Status:{}, exceptions:{}",
                             result.getData().getAppId(), result.getData().getJobId(), ConstValue.JOB_SUCCESS_STATE,
                             t.getMessage());
            }
            CDRWriter.getInstance().write("job", result.getData().getAppId(),
                                          result.getData().getJobId(), "OK", body);
        }else{
            long nextSecond = TimerBackoff.defaultBackOff.nextBackOffSecond(result.getData().getTimes());
            Long now  = JobTimeMgr.getInstance().getNow(ConstValue.TIMER_JOB_NAME);
            long nextTime   = JobTimeMgr.calNextJobTime(now + nextSecond);
            if(nextTime > result.getData().getEndTime()){
                /**失败**/
                if(!Helper.isStrEmpty(result.getData().getCbUrl())){
                    GxTimeCbRequest gxcb = new GxTimeCbRequest();
                    gxcb.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                    gxcb.setMsg(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
                    gxcb.setAppId(result.getData().getAppId());
                    gxcb.setJobId(result.getData().getJobId());
                    gxcb.setBody("Time over");
                    IoFactory.getInstance().callBack(gxcb, result.getData().getCbUrl());
                }
                result.getData().setStatus(ConstValue.JOB_PENDDING_STATE);
                CDRWriter.getInstance().write("job", result.getData().getAppId(),
                                              result.getData().getJobId(), "Service Unavailable", "Time over");
            }else{
                result.getData().setStatus(ConstValue.JOB_PENDDING_STATE);
            }
            result.getData().setNextTime(nextTime);
            try{
                jobDao.updateStatus(result.getData());
            }catch (Throwable t){
                logger.error("Update appId:{}, jobId:{}, Status:{}, exceptions:{}",
                        result.getData().getAppId(), result.getData().getJobId(), ConstValue.JOB_SUCCESS_STATE,
                        t.getMessage());
            }
        }
        result.setData(null);
    }

    private void handNotifyReturn(GxResult<TimerReq, Object> result){
        if(result.getResult() instanceof String){
            /***对方有成功反应，算是成功**/
            String body = String.valueOf(result.getResult());
            if(!Helper.isStrEmpty(result.getData().getCbUrl())){
                GxTimeCbRequest gxcb = new GxTimeCbRequest();
                gxcb.setStatus(HttpStatus.OK.value());
                gxcb.setMsg(HttpStatus.OK.getReasonPhrase());
                gxcb.setAppId(result.getData().getAppId());
                gxcb.setJobId(result.getData().getJobId());
                gxcb.setBody(body);
                IoFactory.getInstance().callBack(gxcb, result.getData().getCbUrl());
            }
            /**处理成功**/
            result.getData().setStatus(ConstValue.JOB_SUCCESS_STATE);
            try{
                notifyDao.insertUpdate(result.getData());
            }catch (Throwable t){
                logger.error("Update appId:{}, jobId:{}, Status:{}, exceptions:{}",
                        result.getData().getAppId(), result.getData().getJobId(), ConstValue.JOB_SUCCESS_STATE,
                        t.getMessage());
            }
            CDRWriter.getInstance().write("notify", result.getData().getAppId(),
                    result.getData().getJobId(), "OK", body);
        }else{
            long nextSecond = TimerBackoff.defaultBackOff.nextBackOffSecond(result.getData().getTimes());
            Long now  = JobTimeMgr.getInstance().getNow(ConstValue.TIMER_JOB_NAME);
            long nextTime   = JobTimeMgr.calNextJobTime(now + nextSecond);
            if(nextTime > result.getData().getEndTime()){
                /**失败**/
                if(!Helper.isStrEmpty(result.getData().getCbUrl())){
                    GxTimeCbRequest gxcb = new GxTimeCbRequest();
                    gxcb.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                    gxcb.setMsg(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
                    gxcb.setAppId(result.getData().getAppId());
                    gxcb.setJobId(result.getData().getJobId());
                    gxcb.setBody("Time over");
                    IoFactory.getInstance().callBack(gxcb, result.getData().getCbUrl());
                }
                result.getData().setStatus(ConstValue.JOB_PENDDING_STATE);
                CDRWriter.getInstance().write("notify", result.getData().getAppId(),
                        result.getData().getJobId(), "Service Unavailable", "Time over");
            }else{
                result.getData().setStatus(ConstValue.JOB_PENDDING_STATE);
            }
            result.getData().setNextTime(nextTime);
            try{
                notifyDao.insertUpdate(result.getData());
            }catch (Throwable t){
                logger.error("Update appId:{}, jobId:{}, Status:{}, exceptions:{}",
                        result.getData().getAppId(), result.getData().getJobId(), ConstValue.JOB_SUCCESS_STATE,
                        t.getMessage());
            }
        }
        result.setData(null);
    }
}

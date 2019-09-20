package com.gexiang.server;

import com.gexiang.repository.entity.TimerJobTime;
import com.gexiang.repository.mapper.TimeDao;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class JobTimeMgr {
    private static Logger logger = LoggerFactory.getLogger(JobTimeMgr.class);
    private static class Holder{
        private final static JobTimeMgr jobTimeMgr = new JobTimeMgr();
    }

    private ConcurrentHashMap<String, Long> jobLastTime;
    private ConcurrentHashMap<String, Long> jobNowTime;
    private TimeDao timeDao;
    private JobTimeMgr(){
        jobLastTime = new ConcurrentHashMap<>();
        jobNowTime  = new ConcurrentHashMap<>();
    }

    public static JobTimeMgr getInstance(){
        return Holder.jobTimeMgr;
    }

    public void setTimeDao(TimeDao timeDao){
        this.timeDao = timeDao;
    }


    public Long getJobLastTime(String name){
        Long v = jobLastTime.get(name);
        return v;
    }

    public long getNow(String name){
        Long v = jobNowTime.get(name);
        return v;
    }

    public void beginJob(String name){
        try{
            Long last = timeDao.queryJobTime(name);
            if(last == null){
                last = timeDao.getToday();
                logger.info("Get today time:{} sec", last);
            }

            if(last != null) {
                Long now = timeDao.getNow();
                jobLastTime.put(name, last);
                jobNowTime.put(name, now);
            }
        }catch (Throwable t){
            logger.warn("begin {} job time exceptions:{}", name, t.getMessage());
        }
    }

    public void saveJob(String name){
        Long now = getNow(name);
        Preconditions.checkNotNull(now, String.format("%s job find now error", name));
        try {
            TimerJobTime timerJobTime = new TimerJobTime();
            timerJobTime.setJobName(name);
            timerJobTime.setLastTime(now);
            timeDao.insertOrUpdate(timerJobTime);
        }catch (Throwable t){
            logger.warn("save job time {} error:{}", name, t.getMessage());
        }
    }

    public static Long calNextJobTime(Long now){
        /**担心数据库负载高，导致的时间不对，采用1秒延迟**/
        return Long.valueOf(now.longValue() + 1);
    }
}

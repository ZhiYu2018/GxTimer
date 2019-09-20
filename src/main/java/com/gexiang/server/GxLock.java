package com.gexiang.server;

import com.gexiang.repository.entity.TimerLock;
import com.gexiang.repository.mapper.LockerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;


public class GxLock {
    private static Logger logger = LoggerFactory.getLogger(GxLock.class);
    private final static Integer STATE_LOCK = Integer.valueOf(1);
    private final static Integer STATE_UNLOCK = Integer.valueOf(0);
    private final TimerLock timerLock;
    private final LockerDao lockerDao;
    public GxLock(String appId, String name, String owner, LockerDao lockerDao){
        this.timerLock = new TimerLock();
        timerLock.setAppId(appId);
        timerLock.setLockOwner(owner);
        timerLock.setName(name);
        timerLock.setStatus(STATE_UNLOCK);
        this.lockerDao = lockerDao;
        try{
            int v = this.lockerDao.create(timerLock);
            logger.debug("Create lock:{}.{} return:{}", timerLock.getAppId(), timerLock.getName(), v);
        }catch (Throwable t){
            if(t instanceof SQLException) {
                SQLException sqlException = (SQLException)t;
                logger.warn("Create lock {}.{} failed:{}:{}", timerLock.getAppId(), timerLock.getName(),
                            sqlException.getSQLState(), sqlException.getErrorCode());
            }
        }
    }

    public boolean tryLock(){
        if(lockerDao == null){
            return false;
        }
        try {
            int v = lockerDao.lock(timerLock);
            logger.debug("lock lock:{}.{} return:{}", timerLock.getAppId(), timerLock.getName(), v);
            return true;
        }catch (Throwable t){
            logger.warn("lock lock {}.{} failed:{}", timerLock.getAppId(), timerLock.getName(), t.getMessage());
        }

        return false;
    }

    public void releaseLock(){
        if(lockerDao == null){
            return;
        }
        try {
            int v = lockerDao.unlock(timerLock);
            logger.debug("release lock:{}.{} return:{}", timerLock.getAppId(), timerLock.getName(), v);
        }catch (Throwable t){
            logger.warn("release lock {}.{} failed:{}", timerLock.getAppId(), timerLock.getName(), t.getMessage());
        }
    }
}

package com.gexiang.repository.mapper;

import com.gexiang.repository.entity.TimerLock;
import org.springframework.stereotype.Repository;

@Repository
public interface LockerDao {
    int create(TimerLock lock);
    int lock(TimerLock lock);
    int unlock(TimerLock lock);
}

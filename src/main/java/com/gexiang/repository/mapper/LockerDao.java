package com.gexiang.repository.mapper;

import com.gexiang.repository.entity.TimerLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LockerDao {
    Integer create(@Param("lock")TimerLock lock);
    Integer lock(@Param("lock")TimerLock lock);
    Integer unlock(@Param("lock")TimerLock lock);
}

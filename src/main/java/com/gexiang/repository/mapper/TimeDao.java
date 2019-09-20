package com.gexiang.repository.mapper;

import com.gexiang.repository.entity.TimerJobTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TimeDao {
    Long getNow();
    Long getToday();
    Integer insertOrUpdate(@Param("jobTime") TimerJobTime jobTime);
    Long queryJobTime(@Param("jobName") String jobName);
}

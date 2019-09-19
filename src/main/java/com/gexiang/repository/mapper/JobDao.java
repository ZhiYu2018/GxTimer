package com.gexiang.repository.mapper;

import com.gexiang.repository.entity.TimerReq;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobDao {
    int add(@Param("req") TimerReq req);
    List<TimerReq> getDelta(@Param("start") long start, @Param("end") long end, @Param("limit")int limit);
    int updatePending(@Param("req") TimerReq req);
    int updateStatus(@Param("req") TimerReq req);
}

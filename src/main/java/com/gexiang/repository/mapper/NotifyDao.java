package com.gexiang.repository.mapper;

import com.gexiang.repository.entity.TimerReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotifyDao {
    Integer add(@Param("req") TimerReq req);
    List<TimerReq> getDelta(@Param("offsetId")long offset, @Param("start") long start,
                            @Param("end") long end, @Param("limit")int limit);
    Integer updatePending(@Param("req") TimerReq req);
    Integer updateStatus(@Param("req") TimerReq req);
    Integer insertUpdate(@Param("req") TimerReq req);
}

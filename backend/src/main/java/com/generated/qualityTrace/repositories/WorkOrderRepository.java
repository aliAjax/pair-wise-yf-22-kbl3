package com.generated.qualityTrace.repositories;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generated.qualityTrace.models.WorkOrder;

@Mapper
public interface WorkOrderRepository extends BaseMapper<WorkOrder> {

  @Select("SELECT * FROM work_order WHERE order_no = #{orderNo}")
  WorkOrder selectByOrderNo(@Param("orderNo") String orderNo);
}

package com.generated.qualityTrace.repositories;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generated.qualityTrace.models.DefectRecord;

@Mapper
public interface DefectRecordRepository extends BaseMapper<DefectRecord> {

  /** 某批次全部不良记录（重算读全量，处置状态参与判定） */
  @Select("SELECT * FROM defect_record WHERE batch_id = #{batchId} ORDER BY id")
  List<DefectRecord> findByBatchId(@Param("batchId") Long batchId);
}

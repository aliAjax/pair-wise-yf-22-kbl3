package com.generated.qualityTrace.repositories;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generated.qualityTrace.models.InspectionItemResult;

@Mapper
public interface InspectionItemResultRepository extends BaseMapper<InspectionItemResult> {

  /** 某批次全部已生效检验项（重算只读这个集合） */
  @Select("SELECT * FROM inspection_item_result WHERE batch_id = #{batchId} AND effective = true ORDER BY id")
  List<InspectionItemResult> findEffectiveByBatchId(@Param("batchId") Long batchId);

  /** 某批次某检验项的已生效记录（同一 item_code 至多一条） */
  @Select("SELECT * FROM inspection_item_result WHERE batch_id = #{batchId} AND item_code = #{itemCode} AND effective = true")
  InspectionItemResult findEffective(@Param("batchId") Long batchId, @Param("itemCode") String itemCode);

  /** 某批次某检验项的最大版本号，用于新版本号递增 */
  @Select("SELECT COALESCE(MAX(version), 0) FROM inspection_item_result WHERE batch_id = #{batchId} AND item_code = #{itemCode}")
  Integer maxVersion(@Param("batchId") Long batchId, @Param("itemCode") String itemCode);

  /** 把某批次某检验项的旧记录置为失效（只留最新一次） */
  @Update("UPDATE inspection_item_result SET effective = false WHERE batch_id = #{batchId} AND item_code = #{itemCode} AND effective = true")
  int markSuperseded(@Param("batchId") Long batchId, @Param("itemCode") String itemCode);
}

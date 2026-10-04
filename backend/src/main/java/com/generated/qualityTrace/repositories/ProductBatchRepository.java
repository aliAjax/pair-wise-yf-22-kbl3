package com.generated.qualityTrace.repositories;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generated.qualityTrace.models.ProductBatch;

@Mapper
public interface ProductBatchRepository extends BaseMapper<ProductBatch> {

  @Select("SELECT * FROM product_batch WHERE batch_no = #{batchNo}")
  ProductBatch selectByBatchNo(@Param("batchNo") String batchNo);

  /**
   * 按批次号加行锁（SELECT ... FOR UPDATE）。
   * 同一批次的并发重算在数据库层面串行化：后到的事务等待前一个事务提交后再读最新已生效数据，
   * 保证同时提交同一批次只让一次重算真正写结论，且不会读到旧快照覆盖新结论。
   */
  @Select("SELECT * FROM product_batch WHERE batch_no = #{batchNo} FOR UPDATE")
  ProductBatch lockByBatchNo(@Param("batchNo") String batchNo);

  @Select("SELECT * FROM product_batch WHERE id = #{id} FOR UPDATE")
  ProductBatch lockById(@Param("id") Long id);
}

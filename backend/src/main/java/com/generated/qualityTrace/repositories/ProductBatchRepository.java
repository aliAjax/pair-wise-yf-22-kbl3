package com.generated.qualityTrace.repositories;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.ProductBatch;

@Repository
public class ProductBatchRepository {
  private final AtomicLong ids = new AtomicLong(0);
  private final ConcurrentHashMap<Long, ProductBatch> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> idByBatchNo = new ConcurrentHashMap<>();

  public synchronized ProductBatch save(ProductBatch b) {
    if (b.id == null) {
      b.id = ids.incrementAndGet();
    }
    byId.put(b.id, b);
    idByBatchNo.put(b.batchNo, b.id);
    return b;
  }

  public Optional<ProductBatch> findById(Long id) {
    return Optional.ofNullable(byId.get(id));
  }

  public Optional<ProductBatch> findByBatchNo(String batchNo) {
    Long id = idByBatchNo.get(batchNo);
    return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
  }

  public List<ProductBatch> findAll() {
    return List.copyOf(byId.values());
  }
}

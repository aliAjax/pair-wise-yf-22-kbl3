package com.generated.qualityTrace.repositories;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.QualityInspection;

@Repository
public class QualityInspectionRepository {
  private final AtomicLong ids = new AtomicLong(0);
  private final ConcurrentHashMap<Long, QualityInspection> byId = new ConcurrentHashMap<>();

  public synchronized QualityInspection save(QualityInspection q) {
    if (q.id == null) {
      q.id = ids.incrementAndGet();
    }
    byId.put(q.id, q);
    return q;
  }

  public Optional<QualityInspection> findById(Long id) {
    return Optional.ofNullable(byId.get(id));
  }

  public List<QualityInspection> findByBatchId(String batchId) {
    return byId.values().stream().filter(q -> Objects.equals(q.batchId, batchId)).toList();
  }

  public List<QualityInspection> findAll() {
    return List.copyOf(byId.values());
  }
}

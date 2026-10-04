package com.generated.qualityTrace.repositories;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.DefectRecord;

@Repository
public class DefectRecordRepository {
  private final AtomicLong ids = new AtomicLong(0);
  private final ConcurrentHashMap<Long, DefectRecord> byId = new ConcurrentHashMap<>();

  public synchronized DefectRecord save(DefectRecord d) {
    if (d.id == null) {
      d.id = ids.incrementAndGet();
    }
    byId.put(d.id, d);
    return d;
  }

  public Optional<DefectRecord> findById(Long id) {
    return Optional.ofNullable(byId.get(id));
  }

  public List<DefectRecord> findByBatchId(String batchId) {
    return byId.values().stream().filter(d -> Objects.equals(d.batchId, batchId)).toList();
  }

  public List<DefectRecord> findAll() {
    return List.copyOf(byId.values());
  }
}

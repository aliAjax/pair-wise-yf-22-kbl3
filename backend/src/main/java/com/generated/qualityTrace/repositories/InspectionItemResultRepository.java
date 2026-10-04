package com.generated.qualityTrace.repositories;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.InspectionItemResult;

@Repository
public class InspectionItemResultRepository {
  private final AtomicLong ids = new AtomicLong(0);
  // one effective record per (inspectionId, itemCode): a repeated submission replaces the previous one
  private final ConcurrentHashMap<String, InspectionItemResult> latestByKey = new ConcurrentHashMap<>();

  private static String key(String inspectionId, String itemCode) {
    return inspectionId + "|" + itemCode;
  }

  /**
   * Keeps only the latest submission per (inspectionId, itemCode).
   * Returns the replaced record, or null when this is the first submission of the item.
   */
  public synchronized InspectionItemResult upsert(InspectionItemResult r) {
    String k = key(r.inspectionId, r.itemCode);
    InspectionItemResult previous = latestByKey.get(k);
    r.id = previous == null ? ids.incrementAndGet() : previous.id;
    latestByKey.put(k, r);
    return previous;
  }

  public List<InspectionItemResult> findEffectiveByInspectionIds(Collection<String> inspectionIds) {
    Set<String> wanted = new HashSet<>(inspectionIds);
    return latestByKey.values().stream().filter(r -> wanted.contains(r.inspectionId)).toList();
  }

  public List<InspectionItemResult> findEffectiveByInspectionId(String inspectionId) {
    return latestByKey.values().stream().filter(r -> Objects.equals(r.inspectionId, inspectionId)).toList();
  }

  public List<InspectionItemResult> findAll() {
    return List.copyOf(latestByKey.values());
  }
}

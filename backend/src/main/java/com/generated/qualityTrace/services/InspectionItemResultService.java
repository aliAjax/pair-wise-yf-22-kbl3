package com.generated.qualityTrace.services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.exceptions.NotFoundException;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;

/**
 * 检验项结果查询服务。检验项随检验单提交（见 QualityInspectionService），
 * 同一检验项重复提交只留最新一次（effective=true）。
 */
@Service
public class InspectionItemResultService {

  private final InspectionItemResultRepository repo;
  private final ProductBatchRepository batchRepo;

  public InspectionItemResultService(InspectionItemResultRepository repo, ProductBatchRepository batchRepo) {
    this.repo = repo;
    this.batchRepo = batchRepo;
  }

  public List<InspectionItemResult> list() {
    return repo.selectList(null);
  }

  /** 某批次全部已生效检验项（重算只读这个集合） */
  public List<InspectionItemResult> listEffectiveByBatch(String batchNo) {
    ProductBatch b = batchRepo.selectByBatchNo(batchNo);
    if (b == null) {
      throw new NotFoundException("batch not found: " + batchNo);
    }
    return repo.findEffectiveByBatchId(b.getId());
  }
}

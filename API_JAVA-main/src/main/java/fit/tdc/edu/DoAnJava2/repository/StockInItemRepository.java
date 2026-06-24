package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.StockInItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockInItemRepository extends JpaRepository<StockInItem, Long> {
    // Hàm này rất quan trọng để sau này lấy danh sách chi tiết của 1 phiếu nhập cụ thể
    List<StockInItem> findByStockInId(Long stockInId);
}
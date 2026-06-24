package fit.tdc.edu.DoAnJava2.repository;

import fit.tdc.edu.DoAnJava2.model.StockOutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockOutItemRepository extends JpaRepository<StockOutItem, Long> {
    List<StockOutItem> findByStockOutId(Long stockOutId);
}
package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.Product;
import fit.tdc.edu.DoAnJava2.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 🌟 HÀM PHÂN TRANG TOÀN NĂNG: VỪA DANH SÁCH MẶC ĐỊNH, VỪA TÌM KIẾM
    public Page<Product> getProductsWithPage(String keyword, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by("name").ascending()
                : Sort.by("name").descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }

        return productRepository.searchMultiFields(keyword, pageable);
    }

    // --- CÁC HÀM CRUD CƠ BẢN ---
    public List<Product> getAllProducts() { return productRepository.findAll(); }
    public Product getProductById(Long id) { return productRepository.findById(id).orElse(null); }
    public Product saveProduct(Product product) { return productRepository.save(product); }
    public void deleteProduct(Long id) { productRepository.deleteById(id); }
    public boolean isNameExists(String name) { return productRepository.existsByName(name); }

    // --- CÁC HÀM NGHIỆP VỤ MỞ RỘNG (GIỮ NGUYÊN) ---
    public List<Product> getLowStockProducts() { return productRepository.findProductsBelowMinQuantity(); }
    public List<Product> getProductsByCategory(Long categoryId) { return productRepository.findByCategoryId(categoryId); }
    public List<Product> searchByPriceRange(double minPrice, double maxPrice) { return productRepository.findByExportPriceBetween(minPrice, maxPrice); }

    // Cập nhật tồn kho và Tính toán lại Giá nhập (Bình quân gia quyền)
    public Product updateStockAndPrice(Long productId, int importQuantity, double newImportPrice) {
        Product product = getProductById(productId);
        if (product != null) {
            int oldQuantity = product.getQuantity();
            double oldPrice = product.getImportPrice();
            double calculatedNewPrice = 0;
            int newTotalQuantity = oldQuantity + importQuantity;

            if (newTotalQuantity > 0) {
                double totalOldValue = oldQuantity * oldPrice;
                double totalNewValue = importQuantity * newImportPrice;
                calculatedNewPrice = (totalOldValue + totalNewValue) / newTotalQuantity;
            }

            product.setQuantity(newTotalQuantity);
            product.setImportPrice(Math.round(calculatedNewPrice));
            return productRepository.save(product);
        }
        return null;
    }

    // Xuất kho

    // Cập nhật tồn kho, tăng tổng đã bán và Tính toán lại Giá xuất (Bình quân gia quyền tích lũy)
    public Product updateStockAndExportPrice(Long productId, int exportQuantity, double actualExportPrice) {
        Product product = getProductById(productId);
        if (product != null) {
            int oldQuantity = product.getQuantity();
            int newQuantity = oldQuantity - exportQuantity;
            if (newQuantity < 0) {
                throw new RuntimeException("Lỗi: Tồn kho của sản phẩm '" + product.getName() + "' không đủ để thực hiện giao dịch!");
            }
            
            int oldSold = product.getTotalSold();
            double oldExportPrice = product.getExportPrice();
            double calculatedNewExportPrice = 0;
            int newTotalSold = oldSold + exportQuantity;

            if (newTotalSold > 0) {
                double totalOldValue = oldSold * oldExportPrice;
                double totalNewValue = exportQuantity * actualExportPrice;
                calculatedNewExportPrice = (totalOldValue + totalNewValue) / newTotalSold;
            } else {
                calculatedNewExportPrice = oldExportPrice;
            }

            product.setQuantity(newQuantity);
            product.setTotalSold(newTotalSold);
            product.setExportPrice(Math.round(calculatedNewExportPrice));
            return productRepository.save(product);
        }
        return null;
    }
}
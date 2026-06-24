package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.dto.StockOutItemRequest;
import fit.tdc.edu.DoAnJava2.dto.StockOutRequest;
import fit.tdc.edu.DoAnJava2.model.StockOut;
import fit.tdc.edu.DoAnJava2.model.StockOutItem;
import fit.tdc.edu.DoAnJava2.repository.StockOutItemRepository;
import fit.tdc.edu.DoAnJava2.repository.StockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.model.Product;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockOutService {

    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private StockOutItemRepository stockOutItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    public List<StockOut> getAllStockOuts() {
        return stockOutRepository.findAll();
    }

    public List<StockOutItem> getStockOutItems(Long stockOutId) {
        return stockOutItemRepository.findByStockOutId(stockOutId);
    }

    public StockOut getById(Long id) {
        StockOut stockOut = stockOutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất với ID: " + id));

        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                if (user != null && !stockOut.getUserId().equals(user.getId())) {
                    throw new RuntimeException("Lỗi: Bạn không có quyền truy cập phiếu xuất này!");
                }
            }
        }
        return stockOut;
    }

    // LẬP PHIẾU XUẤT MỚI: Mặc định là PENDING và KHÔNG trừ kho ngay
    @Transactional
    public StockOut createStockOut(StockOutRequest request) {
        if (request == null) {
            throw new RuntimeException("Lỗi: Dữ liệu yêu cầu không hợp lệ!");
        }
        if (request.getCustomerId() == null) {
            throw new RuntimeException("Lỗi: Vui lòng chọn đối tác khách hàng!");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("Lỗi: Không tìm thấy thông tin người dùng lập phiếu!");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Lỗi: Phiếu xuất chưa có mặt hàng nào!");
        }

        // 1. Kiểm tra tồn kho trước khi cho phép khởi tạo phiếu
        for (StockOutItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new RuntimeException("Lỗi: Mã sản phẩm không hợp lệ!");
            }
            if (itemReq.getQuantity() <= 0) {
                throw new RuntimeException("Lỗi: Số lượng xuất của từng sản phẩm phải lớn hơn 0!");
            }
            if (itemReq.getPrice() < 0) {
                throw new RuntimeException("Lỗi: Đơn giá xuất của từng sản phẩm không được nhỏ hơn 0!");
            }

            Product product = productService.getProductById(itemReq.getProductId());
            if (product == null) {
                throw new RuntimeException("Sản phẩm với ID " + itemReq.getProductId() + " không tồn tại!");
            }
            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Lỗi: Số lượng tồn kho của sản phẩm '" + product.getName() +
                        "' không đủ (Còn: " + product.getQuantity() + ", Yêu cầu: " + itemReq.getQuantity() + ")");
            }
        }

        // 2. Khởi tạo phiếu xuất nếu tất cả sản phẩm đều đủ tồn kho
        StockOut stockOut = new StockOut();
        stockOut.setCustomerId(request.getCustomerId());
        stockOut.setUserId(request.getUserId());
        stockOut.setCreatedAt(LocalDateTime.now());
        stockOut.setStatus("PENDING");
        StockOut savedStockOut = stockOutRepository.save(stockOut);

        for (StockOutItemRequest itemReq : request.getItems()) {
            StockOutItem item = new StockOutItem();
            item.setStockOutId(savedStockOut.getId());
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            stockOutItemRepository.save(item);
        }
        return savedStockOut;
    }

    // DUYỆT HOẶC HỦY PHIẾU XUẤT KHO: Thực hiện trừ tồn kho nếu COMPLETED
    @Transactional
    public StockOut changeStatus(Long id, String newStatus) {
        StockOut stockOut = stockOutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất!"));

        if (!stockOut.getStatus().equals("PENDING")) {
            throw new RuntimeException("Phiếu này đã được xử lý từ trước, không thể đổi trạng thái!");
        }

        stockOut.setStatus(newStatus.toUpperCase());

        // NẾU DUYỆT THÌ MỚI CHÍNH THỨC TRỪ TỒN KHO
        if (newStatus.equalsIgnoreCase("COMPLETED")) {
            List<StockOutItem> items = stockOutItemRepository.findByStockOutId(id);
            for (StockOutItem item : items) {
                // Sử dụng hàm nghiệp vụ mới để trừ kho, tăng tổng bán và tính giá xuất trung bình
                productService.updateStockAndExportPrice(item.getProductId(), item.getQuantity(), item.getPrice());
            }
        }
        return stockOutRepository.save(stockOut);
    }

    // PHÂN TRANG VÀ TÌM KIẾM THÔNG MINH BIẾT DỊCH TIẾNG VIỆT
    public Page<StockOut> getStockOutsWithPage(String keyword, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by("id").descending() : Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = false;
        Long currentUserId = null;

        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                if (user != null) {
                    currentUserId = user.getId();
                } else {
                    isAdmin = true; // Fallback
                }
            }
        } else {
            isAdmin = true; // Fallback if no auth token (e.g. internal calls or local calls)
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            if (isAdmin) {
                return stockOutRepository.findAll(pageable);
            } else {
                return stockOutRepository.findByUserId(currentUserId, pageable);
            }
        }

        String rawKeyword = keyword.trim();

        // Nếu gõ kiểu #PX-1 hoặc PX1 thì ép tìm đích danh ID phiếu
        if (rawKeyword.toUpperCase().startsWith("#PX-") || rawKeyword.toUpperCase().startsWith("PX")) {
            try {
                Long exactId = Long.parseLong(rawKeyword.replaceAll("(?i)^#?PX-?", ""));
                if (isAdmin) {
                    return stockOutRepository.findByExactId(exactId, pageable);
                } else {
                    return stockOutRepository.findByExactIdAndUserId(exactId, currentUserId, pageable);
                }
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        // Rửa từ khóa trạng thái tiếng Việt sang tiếng Anh lưu trong DB
        String searchStatus = "UNKNOWN_STATUS";
        String cleanKeyword = rawKeyword;
        if (cleanKeyword.equalsIgnoreCase("đã duyệt") || cleanKeyword.equalsIgnoreCase("duyệt")) searchStatus = "COMPLETED";
        else if (cleanKeyword.equalsIgnoreCase("đã hủy") || cleanKeyword.equalsIgnoreCase("hủy")) searchStatus = "CANCELLED";
        else if (cleanKeyword.equalsIgnoreCase("chờ") || cleanKeyword.equalsIgnoreCase("chờ duyệt")) searchStatus = "PENDING";

        if (isAdmin) {
            return stockOutRepository.searchMultiFields(cleanKeyword, searchStatus, pageable);
        } else {
            return stockOutRepository.searchMultiFieldsByUserId(currentUserId, cleanKeyword, searchStatus, pageable);
        }
    }

    // Kéo danh sách chờ duyệt cho ngăn kéo Offcanvas
    public List<StockOut> getPendingStockOuts() {
        return stockOutRepository.findByStatus("PENDING");
    }
}
package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.dto.StockInItemRequest;
import fit.tdc.edu.DoAnJava2.dto.StockInRequest;
import fit.tdc.edu.DoAnJava2.model.StockIn;
import fit.tdc.edu.DoAnJava2.model.StockInItem;
import fit.tdc.edu.DoAnJava2.model.Product;
import fit.tdc.edu.DoAnJava2.model.User;
import fit.tdc.edu.DoAnJava2.repository.StockInItemRepository;
import fit.tdc.edu.DoAnJava2.repository.StockInRepository;
import fit.tdc.edu.DoAnJava2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockInService {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @Autowired
    private ProductService productService; // Gọi sang ProductService để cộng tồn kho

    @Autowired
    private UserRepository userRepository;

    // Lấy danh sách phiếu nhập
    public List<StockIn> getAllStockIns() {
        return stockInRepository.findAll();
    }

    // Lấy chi tiết các mặt hàng của 1 phiếu nhập
    public List<StockInItem> getStockInItems(Long stockInId) {
        return stockInItemRepository.findByStockInId(stockInId);
    }

    // LOGIC NHẬP KHO QUAN TRỌNG NHẤT
    @Transactional
    public StockIn createStockIn(StockInRequest request) {
        if (request == null) {
            throw new RuntimeException("Lỗi: Dữ liệu yêu cầu không hợp lệ!");
        }
        if (request.getSupplierId() == null) {
            throw new RuntimeException("Lỗi: Vui lòng chọn đối tác nhà cung cấp!");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("Lỗi: Không tìm thấy thông tin người dùng lập phiếu!");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Lỗi: Phiếu nhập chưa có mặt hàng nào!");
        }

        for (StockInItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new RuntimeException("Lỗi: Mã sản phẩm không hợp lệ!");
            }
            if (itemReq.getQuantity() <= 0) {
                throw new RuntimeException("Lỗi: Số lượng nhập của từng sản phẩm phải lớn hơn 0!");
            }
            if (itemReq.getPrice() < 0) {
                throw new RuntimeException("Lỗi: Đơn giá nhập của từng sản phẩm không được nhỏ hơn 0!");
            }

            Product product = productService.getProductById(itemReq.getProductId());
            if (product == null) {
                throw new RuntimeException("Lỗi: Sản phẩm với ID " + itemReq.getProductId() + " không tồn tại!");
            }
        }

        StockIn stockIn = new StockIn();
        stockIn.setSupplierId(request.getSupplierId());
        stockIn.setUserId(request.getUserId());
        stockIn.setCreatedAt(LocalDateTime.now());

        // Mặc định tạo mới là PENDING
        stockIn.setStatus("PENDING");
        StockIn savedStockIn = stockInRepository.save(stockIn);

        for (StockInItemRequest itemReq : request.getItems()) {
            StockInItem item = new StockInItem();
            item.setStockInId(savedStockIn.getId());
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            stockInItemRepository.save(item);

            // LƯU Ý: Đã XÓA dòng productService.updateStock ở đây!
        }
        return savedStockIn;
    }
    // 🌟 THÊM HÀM NÀY VÀO CLASS StockInService
    public StockIn getById(Long id) {
        StockIn stockIn = stockInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với ID: " + id));

        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                if (user != null && !stockIn.getUserId().equals(user.getId())) {
                    throw new RuntimeException("Lỗi: Bạn không có quyền truy cập phiếu nhập này!");
                }
            }
        }
        return stockIn;
    }
    // --- FILE: StockInService.java ---

    @Transactional
    public StockIn changeStatus(Long id, String newStatus) {
        StockIn stockIn = stockInRepository.findById(id).orElse(null);
        if (stockIn == null) throw new RuntimeException("Không tìm thấy phiếu nhập!");

        if (!stockIn.getStatus().equals("PENDING")) {
            throw new RuntimeException("Chỉ có thể thay đổi trạng thái khi phiếu đang ở mức PENDING!");
        }

        stockIn.setStatus(newStatus.toUpperCase());

        // NẾU LÀ 'COMPLETED' THÌ TIẾN HÀNH CỘNG KHO VÀ CHỐT GIÁ
        if (newStatus.equalsIgnoreCase("COMPLETED")) {
            List<StockInItem> items = stockInItemRepository.findByStockInId(id);
            for (StockInItem item : items) {
                // SỬA DÒNG NÀY: Gọi hàm updateStockAndPrice và truyền thêm item.getPrice()
                productService.updateStockAndPrice(item.getProductId(), item.getQuantity(), item.getPrice());
            }
        }

        return stockInRepository.save(stockIn);
    }

    // 🌟 HÀM PHÂN TRANG VÀ TÌM KIẾM CỰC KỲ THÔNG MINH (ĐÃ FIX LỖI TÌM TIẾNG ANH)
    public Page<StockIn> getStockInsWithPage(String keyword, int page, int size, String sortDir) {
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
                return stockInRepository.findAll(pageable);
            } else {
                return stockInRepository.findByUserId(currentUserId, pageable);
            }
        }

        String rawKeyword = keyword.trim();

        // 1. NẾU GÕ "#PN-..." HOẶC "PN..." THÌ CHỈ TÌM ĐÍCH DANH ID
        if (rawKeyword.toUpperCase().startsWith("#PN-") || rawKeyword.toUpperCase().startsWith("PN")) {
            try {
                Long exactId = Long.parseLong(rawKeyword.replaceAll("(?i)^#?PN-?", ""));
                if (isAdmin) {
                    return stockInRepository.findByExactId(exactId, pageable);
                } else {
                    return stockInRepository.findByExactIdAndUserId(exactId, currentUserId, pageable);
                }
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        // 2. PHÂN LUỒNG TỪ KHÓA: Tách riêng Keyword thường và Trạng thái
        String searchStatus = "UNKNOWN_STATUS"; // 🌟 Mặc định gán trạng thái ảo để chặn đứng việc lỡ tìm trúng "CANCELLED"
        String cleanKeyword = rawKeyword;

        // Nếu đại ca gõ đúng các từ này, hệ thống mới gán trạng thái thật để tìm kiếm
        if (cleanKeyword.equalsIgnoreCase("đã duyệt") || cleanKeyword.equalsIgnoreCase("duyệt")) {
            searchStatus = "COMPLETED";
        } else if (cleanKeyword.equalsIgnoreCase("đã hủy") || cleanKeyword.equalsIgnoreCase("hủy")) {
            searchStatus = "CANCELLED";
        } else if (cleanKeyword.equalsIgnoreCase("chờ") || cleanKeyword.equalsIgnoreCase("chờ duyệt")) {
            searchStatus = "PENDING";
        }

        // Gọi DB: Tìm "Tên" bằng cleanKeyword, tìm "Trạng thái" bằng searchStatus
        if (isAdmin) {
            return stockInRepository.searchMultiFields(cleanKeyword, searchStatus, pageable);
        } else {
            return stockInRepository.searchMultiFieldsByUserId(currentUserId, cleanKeyword, searchStatus, pageable);
        }
    }
    // 🌟 Lấy danh sách toàn bộ phiếu đang CHỜ DUYỆT
    public List<StockIn> getPendingStockIns() {
        return stockInRepository.findByStatus("PENDING");
    }
}
package fit.tdc.edu.DoAnJava2.controller;

import fit.tdc.edu.DoAnJava2.model.Customer;
import fit.tdc.edu.DoAnJava2.security.LogAction;
import fit.tdc.edu.DoAnJava2.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/page")
    public Page<Customer> getPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort) {
        return customerService.getCustomersWithPage(keyword, page, size, sort);
    }

    @GetMapping
    public List<Customer> getAll() { return customerService.getAllCustomers(); }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id) { return customerService.getCustomerById(id); }

    // 🌟 1. XỬ LÝ THÊM MỚI: CHẶN TRÙNG SỐ ĐIỆN THOẠI
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "CREATE_CUSTOMER", description = "Thêm mới khách hàng")
    public ResponseEntity<?> create(@RequestBody Customer customer) {
        String phone = customer.getPhone();

        // Nếu người dùng có nhập SĐT thì mới kiểm tra trùng
        if (phone != null && !phone.trim().isEmpty()) {
            if (customerService.isPhoneExists(phone.trim())) {
                return ResponseEntity.badRequest().body("Số điện thoại '" + phone + "' đã tồn tại trên hệ thống!");
            }
        }

        return ResponseEntity.ok(customerService.saveCustomer(customer));
    }

    // 🌟 2. XỬ LÝ CẬP NHẬT: CHẶN ĐỔI SANG SĐT CỦA NGƯỜI KHÁC
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "UPDATE_CUSTOMER", description = "Cập nhật thông tin khách hàng")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Customer customer) {
        Customer existingCustomer = customerService.getCustomerById(id);
        if (existingCustomer == null) {
            return ResponseEntity.badRequest().body("Lỗi: Không tìm thấy khách hàng!");
        }

        String newPhone = customer.getPhone();

        // Nếu có nhập SĐT mới VÀ số mới này KHÁC số cũ của chính họ
        if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.trim().equals(existingCustomer.getPhone())) {
            // Kiểm tra xem số mới định đổi có bị ai khác chiếm dụng chưa
            if (customerService.isPhoneExists(newPhone.trim())) {
                return ResponseEntity.badRequest().body("Lỗi: Số điện thoại '" + newPhone + "' đã được sử dụng bởi một khách hàng khác!");
            }
        }

        // Nếu hợp lệ thì tiến hành cập nhật
        existingCustomer.setName(customer.getName());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setAddress(customer.getAddress());
        return ResponseEntity.ok(customerService.saveCustomer(existingCustomer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogAction(actionType = "DELETE_CUSTOMER", description = "Xóa khách hàng ID:")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok("Đã xóa khách hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}
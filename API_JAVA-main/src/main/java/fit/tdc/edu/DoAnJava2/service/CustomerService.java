package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.Customer;
import fit.tdc.edu.DoAnJava2.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // 🌟 THÊM HÀM NÀY: Kiểm tra số điện thoại đã tồn tại chưa
    public boolean isPhoneExists(String phone) {
        return customerRepository.existsByPhone(phone);
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN ---
    public List<Customer> getAllCustomers() { return customerRepository.findAll(); }
    public Customer getCustomerById(Long id) { return customerRepository.findById(id).orElse(null); }
    public Customer saveCustomer(Customer customer) { return customerRepository.save(customer); }
    public void deleteCustomer(Long id) { customerRepository.deleteById(id); }
    public Page<Customer> getCustomersWithPage(String keyword, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by("name").ascending() : Sort.by("name").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) return customerRepository.findAll(pageable);
        return customerRepository.searchMultiFields(keyword, pageable);
    }
}
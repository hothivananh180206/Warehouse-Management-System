package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.Supplier;
import fit.tdc.edu.DoAnJava2.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public boolean isPhoneExists(String phone) { return supplierRepository.existsByPhone(phone); }

    public List<Supplier> getAllSuppliers() { return supplierRepository.findAll(); }
    public Supplier getSupplierById(Long id) { return supplierRepository.findById(id).orElse(null); }
    public Supplier saveSupplier(Supplier supplier) { return supplierRepository.save(supplier); }
    public void deleteSupplier(Long id) { supplierRepository.deleteById(id); }

    public Page<Supplier> getSuppliersWithPage(String keyword, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by("name").ascending() : Sort.by("name").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) return supplierRepository.findAll(pageable);
        return supplierRepository.searchMultiFields(keyword, pageable);
    }
}
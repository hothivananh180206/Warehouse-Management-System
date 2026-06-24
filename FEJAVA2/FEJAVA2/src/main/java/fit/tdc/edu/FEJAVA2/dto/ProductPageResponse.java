package fit.tdc.edu.FEJAVA2.dto;

import java.util.List;

public class ProductPageResponse {
    private List<Product> content; // Danh sách sản phẩm của trang hiện tại
    private int totalPages;        // Tổng số trang
    private long totalElements;    // Tổng số sản phẩm trong DB
    private int number;            // Số trang hiện tại (Bắt đầu từ số 0)
    private int size;              // Kích thước số lượng bản ghi trên 1 trang

    // --- GETTER VÀ SETTER ---
    public List<Product> getContent() { return content; }
    public void setContent(List<Product> content) { this.content = content; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
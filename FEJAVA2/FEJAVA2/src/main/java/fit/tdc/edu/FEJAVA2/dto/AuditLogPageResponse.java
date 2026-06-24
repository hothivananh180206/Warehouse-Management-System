package fit.tdc.edu.FEJAVA2.dto;

import java.util.List;

public class AuditLogPageResponse {
    private List<AuditLog> content;
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;

    // --- GETTER VÀ SETTER ---
    public List<AuditLog> getContent() { return content; }
    public void setContent(List<AuditLog> content) { this.content = content; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}

package fit.tdc.edu.FEJAVA2.dto;
import java.util.List;

public class StockInPageResponse {
    private List<StockInDTO> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;

    // Getter & Setter
    public List<StockInDTO> getContent() { return content; }
    public void setContent(List<StockInDTO> content) { this.content = content; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
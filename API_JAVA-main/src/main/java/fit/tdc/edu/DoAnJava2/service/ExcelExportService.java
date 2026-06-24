package fit.tdc.edu.DoAnJava2.service;

import fit.tdc.edu.DoAnJava2.model.*;
import fit.tdc.edu.DoAnJava2.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private StockOutItemRepository stockOutItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 🌟 HÀM TỰ ĐỘNG CÂN CHỈNH ĐỘ RỘNG CỘT & CỘNG THÊM PADDING AN TOÀN CHỐNG LỖI ###
     * @param sheet Trang tính Excel cần cân chỉnh
     * @param numColumns Số lượng cột của bảng tính
     */
    private void autoSizeAndPadColumns(Sheet sheet, int numColumns) {
        for (int col = 0; col < numColumns; col++) {
            // Tự động đo độ dài chữ của ô lớn nhất trong cột để đặt kích thước cột tương ứng
            sheet.autoSizeColumn(col);
            int currentWidth = sheet.getColumnWidth(col);
            // 🌟 Tăng thêm 35% độ rộng + 1200 đơn vị padding đệm an toàn để tránh lỗi hiển thị '###' do lệch font chữ Việt Nam
            int paddedWidth = (int) (currentWidth * 1.35 + 1200);
            if (paddedWidth < 3500) {
                paddedWidth = 3500; // Đảm bảo độ rộng tối thiểu để nhìn rõ tiêu đề cột
            }
            sheet.setColumnWidth(col, paddedWidth);
        }
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH PHIẾU NHẬP KHO (Dạng luồng ByteArrayInputStream)
     * @param start Thời gian bắt đầu lọc
     * @param end Thời gian kết thúc lọc
     * @param status Trạng thái phiếu cần lọc
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportStockInToExcel(LocalDateTime start, LocalDateTime end, String status) throws IOException {
        // 1. Lấy dữ liệu từ Database theo khoảng thời gian và trạng thái
        List<StockIn> list;
        if ("ALL".equalsIgnoreCase(status)) {
            list = stockInRepository.findByCreatedAtBetween(start, end);
        } else {
            list = stockInRepository.findByCreatedAtBetweenAndStatus(start, end, status);
        }

        // 2. Tạo Map lưu trữ tạm thời các đối tượng liên quan (Supplier, User, Product) để tránh vòng lặp query DB liên tục (N+1 query)
        Map<Long, String> supplierMap = new HashMap<>();
        for (Supplier s : supplierRepository.findAll()) {
            supplierMap.put(s.getId(), s.getName());
        }

        Map<Long, String> userMap = new HashMap<>();
        for (User u : userRepository.findAll()) {
            userMap.put(u.getId(), u.getFullName() != null ? u.getFullName() : u.getUsername());
        }

        Map<Long, String> productMap = new HashMap<>();
        for (Product p : productRepository.findAll()) {
            productMap.put(p.getId(), p.getName());
        }

        // 3. Khởi tạo SXSSFWorkbook (Streaming Workbook) giữ tối đa 100 dòng trên bộ nhớ RAM để tối ưu dung lượng bộ nhớ
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Nhập Kho");
            sheet.trackAllColumnsForAutoSizing();

            // 1. Title Banner
            createTitleBanner(workbook, sheet, "BÁO CÁO CHI TIẾT CHỨNG TỪ NHẬP KHO", start, end);

            // 2. Header Style (Cyberpunk Slate Blue)
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Column names
            String[] columns = {"STT", "Số Phiếu (ID)", "Nhà Cung Cấp", "Người Lập", "Ngày Tạo", "Trạng Thái", "Tổng Tiền (VNĐ)"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            // Styles for cells
            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle numberCellStyle = createNumberStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle statusPendingStyle = createStatusStyle(workbook, IndexedColors.GOLD.getIndex());
            CellStyle statusCompletedStyle = createStatusStyle(workbook, IndexedColors.GREEN.getIndex());
            CellStyle statusCancelledStyle = createStatusStyle(workbook, IndexedColors.RED.getIndex());

            int rowIndex = 5;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (StockIn item : list) {
                Row row = sheet.createRow(rowIndex++);

                // Cột 1: STT
                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                // Cột 2: Mã Phiếu
                Cell cellId = row.createCell(1);
                cellId.setCellValue("#PN-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                // Cột 3: Nhà cung cấp
                Cell cellSupplier = row.createCell(2);
                cellSupplier.setCellValue(supplierMap.getOrDefault(item.getSupplierId(), "Chưa phân loại"));
                cellSupplier.setCellStyle(defaultCellStyle);

                // Cột 4: Người lập
                Cell cellUser = row.createCell(3);
                cellUser.setCellValue(userMap.getOrDefault(item.getUserId(), "Hệ thống"));
                cellUser.setCellStyle(defaultCellStyle);

                // Cột 5: Ngày tạo
                Cell cellDate = row.createCell(4);
                cellDate.setCellValue(item.getCreatedAt() != null ? item.getCreatedAt().format(formatter) : "-");
                cellDate.setCellStyle(centerCellStyle);

                // Cột 6: Trạng thái (Tô màu badge)
                Cell cellStatus = row.createCell(5);
                String itemStatus = item.getStatus();
                cellStatus.setCellValue(itemStatus);
                if ("COMPLETED".equalsIgnoreCase(itemStatus)) {
                    cellStatus.setCellStyle(statusCompletedStyle);
                } else if ("PENDING".equalsIgnoreCase(itemStatus)) {
                    cellStatus.setCellStyle(statusPendingStyle);
                } else {
                    cellStatus.setCellStyle(statusCancelledStyle);
                }

                // Cột 7: Tính tổng tiền các sản phẩm trong phiếu nhập
                double total = 0;
                List<StockInItem> details = stockInItemRepository.findByStockInId(item.getId());
                for (StockInItem detail : details) {
                    total += detail.getQuantity() * detail.getPrice();
                }
                Cell cellTotal = row.createCell(6);
                cellTotal.setCellValue(total);
                cellTotal.setCellStyle(numberCellStyle);
            }

            // 3. Dòng tổng kết có công thức Excel động
            if (rowIndex > 5) {
                Row totalRow = sheet.createRow(rowIndex);
                Cell labelCell = totalRow.createCell(0);
                labelCell.setCellValue("TỔNG CỘNG");
                labelCell.setCellStyle(createTotalLabelStyle(workbook));
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex, rowIndex, 0, 5));

                // Nối các cell trống đã merge với style borders
                for (int i = 1; i <= 5; i++) {
                    totalRow.createCell(i).setCellStyle(createTotalLabelStyle(workbook));
                }

                Cell sumCell = totalRow.createCell(6);
                sumCell.setCellFormula(String.format("SUM(G6:G%d)", rowIndex));
                sumCell.setCellStyle(createTotalAmountStyle(workbook));
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH PHIẾU XUẤT KHO (Dạng luồng ByteArrayInputStream)
     * @param start Thời gian bắt đầu lọc
     * @param end Thời gian kết thúc lọc
     * @param status Trạng thái phiếu cần lọc
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportStockOutToExcel(LocalDateTime start, LocalDateTime end, String status) throws IOException {
        List<StockOut> list;
        if ("ALL".equalsIgnoreCase(status)) {
            list = stockOutRepository.findByCreatedAtBetween(start, end);
        } else {
            list = stockOutRepository.findByCreatedAtBetweenAndStatus(start, end, status);
        }

        Map<Long, String> customerMap = new HashMap<>();
        for (Customer c : customerRepository.findAll()) {
            customerMap.put(c.getId(), c.getName());
        }

        Map<Long, String> userMap = new HashMap<>();
        for (User u : userRepository.findAll()) {
            userMap.put(u.getId(), u.getFullName() != null ? u.getFullName() : u.getUsername());
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Xuất Kho");
            sheet.trackAllColumnsForAutoSizing();

            // 1. Title Banner
            createTitleBanner(workbook, sheet, "BÁO CÁO CHI TIẾT CHỨNG TỪ XUẤT KHO", start, end);

            // 2. Header Style (Cyberpunk Slate Blue)
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"STT", "Số Phiếu (ID)", "Khách Hàng", "Người Lập", "Ngày Tạo", "Trạng Thái", "Tổng Tiền (VNĐ)"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle numberCellStyle = createNumberStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle statusPendingStyle = createStatusStyle(workbook, IndexedColors.GOLD.getIndex());
            CellStyle statusCompletedStyle = createStatusStyle(workbook, IndexedColors.GREEN.getIndex());
            CellStyle statusCancelledStyle = createStatusStyle(workbook, IndexedColors.RED.getIndex());

            int rowIndex = 5;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (StockOut item : list) {
                Row row = sheet.createRow(rowIndex++);

                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                Cell cellId = row.createCell(1);
                cellId.setCellValue("#PX-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                Cell cellCustomer = row.createCell(2);
                cellCustomer.setCellValue(customerMap.getOrDefault(item.getCustomerId(), "Chưa phân loại"));
                cellCustomer.setCellStyle(defaultCellStyle);

                Cell cellUser = row.createCell(3);
                cellUser.setCellValue(userMap.getOrDefault(item.getUserId(), "Hệ thống"));
                cellUser.setCellStyle(defaultCellStyle);

                Cell cellDate = row.createCell(4);
                cellDate.setCellValue(item.getCreatedAt() != null ? item.getCreatedAt().format(formatter) : "-");
                cellDate.setCellStyle(centerCellStyle);

                Cell cellStatus = row.createCell(5);
                String itemStatus = item.getStatus();
                cellStatus.setCellValue(itemStatus);
                if ("COMPLETED".equalsIgnoreCase(itemStatus)) {
                    cellStatus.setCellStyle(statusCompletedStyle);
                } else if ("PENDING".equalsIgnoreCase(itemStatus)) {
                    cellStatus.setCellStyle(statusPendingStyle);
                } else {
                    cellStatus.setCellStyle(statusCancelledStyle);
                }

                double total = 0;
                List<StockOutItem> details = stockOutItemRepository.findByStockOutId(item.getId());
                for (StockOutItem detail : details) {
                    total += detail.getQuantity() * detail.getPrice();
                }
                Cell cellTotal = row.createCell(6);
                cellTotal.setCellValue(total);
                cellTotal.setCellStyle(numberCellStyle);
            }

            // 3. Sum Formula
            if (rowIndex > 5) {
                Row totalRow = sheet.createRow(rowIndex);
                Cell labelCell = totalRow.createCell(0);
                labelCell.setCellValue("TỔNG CỘNG");
                labelCell.setCellStyle(createTotalLabelStyle(workbook));
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex, rowIndex, 0, 5));

                for (int i = 1; i <= 5; i++) {
                    totalRow.createCell(i).setCellStyle(createTotalLabelStyle(workbook));
                }

                Cell sumCell = totalRow.createCell(6);
                sumCell.setCellFormula(String.format("SUM(G6:G%d)", rowIndex));
                sumCell.setCellStyle(createTotalAmountStyle(workbook));
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 🌟 TẠO BANNER TIÊU ĐỀ CHO FILE EXCEL (phiên bản có khoảng ngày lọc)
     * @param workbook Workbook hiện tại
     * @param sheet Sheet hiện tại
     * @param titleText Nội dung tiêu đề chính
     * @param start Ngày bắt đầu
     * @param end Ngày kết thúc
     */
    private void createTitleBanner(SXSSFWorkbook workbook, Sheet sheet, String titleText, LocalDateTime start, LocalDateTime end) {
        Row row0 = sheet.createRow(0);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue("HỆ THỐNG QUẢN LÝ KHO");
        Font titleBrandFont = workbook.createFont();
        titleBrandFont.setBold(true);
        titleBrandFont.setFontHeightInPoints((short) 13);
        titleBrandFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle brandStyle = workbook.createCellStyle();
        brandStyle.setFont(titleBrandFont);
        cell0.setCellStyle(brandStyle);

        Row row1 = sheet.createRow(1);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue(titleText);
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 15);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        cell1.setCellStyle(titleStyle);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Row row2 = sheet.createRow(2);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue(String.format("Khoảng ngày lọc: Từ %s đến %s", start.format(dtf), end.format(dtf)));
        Font subFont = workbook.createFont();
        subFont.setItalic(true);
        subFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        CellStyle subStyle = workbook.createCellStyle();
        subStyle.setFont(subFont);
        cell2.setCellStyle(subStyle);
    }

    /**
     * 🌟 TẠO BANNER TIÊU ĐỀ CHO FILE EXCEL (phiên bản không có khoảng ngày lọc)
     * @param workbook Workbook hiện tại
     * @param sheet Sheet hiện tại
     * @param titleText Nội dung tiêu đề chính
     */
    private void createTitleBanner(SXSSFWorkbook workbook, Sheet sheet, String titleText) {
        Row row0 = sheet.createRow(0);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue("HỆ THỐNG QUẢN LÝ KHO");
        Font titleBrandFont = workbook.createFont();
        titleBrandFont.setBold(true);
        titleBrandFont.setFontHeightInPoints((short) 13);
        titleBrandFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle brandStyle = workbook.createCellStyle();
        brandStyle.setFont(titleBrandFont);
        cell0.setCellStyle(brandStyle);

        Row row1 = sheet.createRow(1);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue(titleText);
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 15);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        cell1.setCellStyle(titleStyle);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        Row row2 = sheet.createRow(2);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue(String.format("Thời gian xuất báo cáo: %s", LocalDateTime.now().format(dtf)));
        Font subFont = workbook.createFont();
        subFont.setItalic(true);
        subFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        CellStyle subStyle = workbook.createCellStyle();
        subStyle.setFont(subFont);
        cell2.setCellStyle(subStyle);
    }

    /**
     * 🌟 TẠO STYLE CHO TIÊU ĐỀ CỘT BẢNG (Header: Nền xanh navy, chữ trắng, in đậm, border viền)
     * @param workbook Workbook hiện tại
     * @return CellStyle cho header
     */
    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Arial");

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    /**
     * 🌟 TẠO STYLE MẶC ĐỊNH CHO CÁC Ô CHỮ (Căn lề trái, có đường viền mỏng bao quanh)
     * @param workbook Workbook hiện tại
     * @return CellStyle mặc định
     */
    private CellStyle createDefaultStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 🌟 TẠO STYLE CHO CÁC Ô SỐ TIỀN/TIỀN TỆ (Căn lề phải, định dạng tiền tệ vi-VN có đuôi ₫)
     * @param workbook Workbook hiện tại
     * @return CellStyle cho số
     */
    private CellStyle createNumberStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        return style;
    }

    /**
     * 🌟 TẠO STYLE CHO TRẠNG THÁI (Căn giữa, in đậm, áp dụng màu chữ động dựa trên trạng thái phiếu)
     * @param workbook Workbook hiện tại
     * @param colorIndex Index màu của chữ
     * @return CellStyle cho trạng thái
     */
    private CellStyle createStatusStyle(SXSSFWorkbook workbook, short colorIndex) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(colorIndex);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 🌟 TẠO STYLE CHO NHÃN "TỔNG CỘNG"
     * @param workbook Workbook hiện tại
     * @return CellStyle cho nhãn tổng cộng
     */
    private CellStyle createTotalLabelStyle(SXSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 🌟 TẠO STYLE CHO Ô TỔNG SỐ TIỀN
     * @param workbook Workbook hiện tại
     * @return CellStyle cho ô tổng tiền
     */
    private CellStyle createTotalAmountStyle(SXSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        return style;
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH SẢN PHẨM
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportProductsToExcel() throws IOException {
        List<Product> list = productRepository.findAll();
        Map<Long, String> categoryMap = new HashMap<>();
        for (Category c : categoryRepository.findAll()) {
            categoryMap.put(c.getId(), c.getName());
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Sản Phẩm");
            sheet.trackAllColumnsForAutoSizing();

            createTitleBanner(workbook, sheet, "BÁO CÁO DANH SÁCH SẢN PHẨM TRÊN HỆ THỐNG");

            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] columns = {"STT", "Mã Sản Phẩm", "Tên Sản Phẩm", "Danh Mục", "Giá Nhập", "Giá Bán", "Số Lượng Tồn", "Lũy Kế Bán"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle numberCellStyle = createNumberStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            int rowIndex = 5;
            for (Product item : list) {
                Row row = sheet.createRow(rowIndex++);

                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                Cell cellId = row.createCell(1);
                cellId.setCellValue("#SP-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                Cell cellName = row.createCell(2);
                cellName.setCellValue(item.getName());
                cellName.setCellStyle(defaultCellStyle);

                Cell cellCategory = row.createCell(3);
                cellCategory.setCellValue(categoryMap.getOrDefault(item.getCategoryId(), "Chưa phân loại"));
                cellCategory.setCellStyle(defaultCellStyle);

                Cell cellImport = row.createCell(4);
                cellImport.setCellValue(item.getImportPrice());
                cellImport.setCellStyle(numberCellStyle);

                Cell cellExport = row.createCell(5);
                cellExport.setCellValue(item.getExportPrice());
                cellExport.setCellStyle(numberCellStyle);

                Cell cellQty = row.createCell(6);
                cellQty.setCellValue(item.getQuantity());
                cellQty.setCellStyle(numberCellStyle);

                Cell cellSold = row.createCell(7);
                cellSold.setCellValue(item.getTotalSold());
                cellSold.setCellStyle(numberCellStyle);
            }

            if (rowIndex > 5) {
                Row totalRow = sheet.createRow(rowIndex);
                Cell labelCell = totalRow.createCell(0);
                labelCell.setCellValue("TỔNG CỘNG");
                labelCell.setCellStyle(createTotalLabelStyle(workbook));
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex, rowIndex, 0, 3));

                for (int i = 1; i <= 3; i++) {
                    totalRow.createCell(i).setCellStyle(createTotalLabelStyle(workbook));
                }

                Cell sumImport = totalRow.createCell(4);
                sumImport.setCellFormula(String.format("SUM(E6:E%d)", rowIndex));
                sumImport.setCellStyle(createTotalAmountStyle(workbook));

                Cell sumExport = totalRow.createCell(5);
                sumExport.setCellFormula(String.format("SUM(F6:F%d)", rowIndex));
                sumExport.setCellStyle(createTotalAmountStyle(workbook));

                Cell sumQty = totalRow.createCell(6);
                sumQty.setCellFormula(String.format("SUM(G6:G%d)", rowIndex));
                sumQty.setCellStyle(createTotalAmountStyle(workbook));

                Cell sumSold = totalRow.createCell(7);
                sumSold.setCellFormula(String.format("SUM(H6:H%d)", rowIndex));
                sumSold.setCellStyle(createTotalAmountStyle(workbook));
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH DANH MỤC SẢN PHẨM
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportCategoriesToExcel() throws IOException {
        List<Category> list = categoryRepository.findAll();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Danh Mục");
            sheet.trackAllColumnsForAutoSizing();

            createTitleBanner(workbook, sheet, "BÁO CÁO DANH SÁCH DANH MỤC SẢN PHẨM");

            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] columns = {"STT", "Mã Danh Mục", "Tên Danh Mục"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            int rowIndex = 5;
            for (Category item : list) {
                Row row = sheet.createRow(rowIndex++);

                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                Cell cellId = row.createCell(1);
                cellId.setCellValue("#DM-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                Cell cellName = row.createCell(2);
                cellName.setCellValue(item.getName());
                cellName.setCellStyle(defaultCellStyle);
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH KHÁCH HÀNG
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportCustomersToExcel() throws IOException {
        List<Customer> list = customerRepository.findAll();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Khách Hàng");
            sheet.trackAllColumnsForAutoSizing();

            createTitleBanner(workbook, sheet, "BÁO CÁO DANH SÁCH KHÁCH HÀNG");

            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] columns = {"STT", "Mã Khách Hàng", "Tên Khách Hàng", "Số Điện Thoại", "Địa Chỉ"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            int rowIndex = 5;
            for (Customer item : list) {
                Row row = sheet.createRow(rowIndex++);

                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                Cell cellId = row.createCell(1);
                cellId.setCellValue("#KH-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                Cell cellName = row.createCell(2);
                cellName.setCellValue(item.getName());
                cellName.setCellStyle(defaultCellStyle);

                Cell cellPhone = row.createCell(3);
                cellPhone.setCellValue(item.getPhone() != null ? item.getPhone() : "-");
                cellPhone.setCellStyle(centerCellStyle);

                Cell cellAddress = row.createCell(4);
                cellAddress.setCellValue(item.getAddress() != null ? item.getAddress() : "-");
                cellAddress.setCellStyle(defaultCellStyle);
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH NHÀ CUNG CẤP
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportSuppliersToExcel() throws IOException {
        List<Supplier> list = supplierRepository.findAll();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Nhà Cung Cấp");
            sheet.trackAllColumnsForAutoSizing();

            createTitleBanner(workbook, sheet, "BÁO CÁO DANH SÁCH NHÀ CUNG CẤP");

            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] columns = {"STT", "Mã Nhà Cung Cấp", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Địa Chỉ"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            int rowIndex = 5;
            for (Supplier item : list) {
                Row row = sheet.createRow(rowIndex++);

                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                Cell cellId = row.createCell(1);
                cellId.setCellValue("#NCC-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                Cell cellName = row.createCell(2);
                cellName.setCellValue(item.getName());
                cellName.setCellStyle(defaultCellStyle);

                Cell cellPhone = row.createCell(3);
                cellPhone.setCellValue(item.getPhone() != null ? item.getPhone() : "-");
                cellPhone.setCellStyle(centerCellStyle);

                Cell cellAddress = row.createCell(4);
                cellAddress.setCellValue(item.getAddress() != null ? item.getAddress() : "-");
                cellAddress.setCellStyle(defaultCellStyle);
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 🌟 XUẤT FILE EXCEL DANH SÁCH NHÂN VIÊN
     * @return ByteArrayInputStream chứa dữ liệu file Excel
     * @throws IOException Lỗi khi ghi file
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportUsersToExcel() throws IOException {
        List<User> list = userRepository.findAll();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Nhân Viên");
            sheet.trackAllColumnsForAutoSizing();

            createTitleBanner(workbook, sheet, "BÁO CÁO DANH SÁCH NHÂN VIÊN & TÀI KHOẢN");

            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] columns = {"STT", "Mã Nhân Viên", "Tài Khoản", "Họ và Tên", "Số Điện Thoại", "Email", "Vai Trò", "Trạng Thái"};
            Row headerRow = sheet.createRow(4);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle defaultCellStyle = createDefaultStyle(workbook);
            CellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.cloneStyleFrom(defaultCellStyle);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle statusPendingStyle = createStatusStyle(workbook, IndexedColors.GOLD.getIndex());
            CellStyle statusCompletedStyle = createStatusStyle(workbook, IndexedColors.GREEN.getIndex());
            CellStyle statusCancelledStyle = createStatusStyle(workbook, IndexedColors.RED.getIndex());

            int rowIndex = 5;
            for (User item : list) {
                Row row = sheet.createRow(rowIndex++);

                Cell cellStt = row.createCell(0);
                cellStt.setCellValue(rowIndex - 5);
                cellStt.setCellStyle(centerCellStyle);

                Cell cellId = row.createCell(1);
                cellId.setCellValue("#NV-" + item.getId());
                cellId.setCellStyle(centerCellStyle);

                Cell cellUsername = row.createCell(2);
                cellUsername.setCellValue(item.getUsername());
                cellUsername.setCellStyle(defaultCellStyle);

                Cell cellFullName = row.createCell(3);
                cellFullName.setCellValue(item.getFullName() != null ? item.getFullName() : "Hệ thống");
                cellFullName.setCellStyle(defaultCellStyle);

                Cell cellPhone = row.createCell(4);
                cellPhone.setCellValue(item.getPhone() != null ? item.getPhone() : "-");
                cellPhone.setCellStyle(centerCellStyle);

                Cell cellEmail = row.createCell(5);
                cellEmail.setCellValue(item.getEmail() != null ? item.getEmail() : "-");
                cellEmail.setCellStyle(defaultCellStyle);

                Cell cellRole = row.createCell(6);
                cellRole.setCellValue(item.getRole());
                cellRole.setCellStyle(centerCellStyle);

                Cell cellStatus = row.createCell(7);
                String userStatus = item.getStatus();
                cellStatus.setCellValue(userStatus);
                if ("ACTIVE".equalsIgnoreCase(userStatus)) {
                    cellStatus.setCellStyle(statusCompletedStyle);
                } else if ("PENDING".equalsIgnoreCase(userStatus)) {
                    cellStatus.setCellStyle(statusPendingStyle);
                } else {
                    cellStatus.setCellStyle(statusCancelledStyle);
                }
            }

            // Auto-size & Pad columns
            autoSizeAndPadColumns(sheet, columns.length);

            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
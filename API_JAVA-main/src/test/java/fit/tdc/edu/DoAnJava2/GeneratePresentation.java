package fit.tdc.edu.DoAnJava2;

import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TableCell;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;

public class GeneratePresentation {

    @Test
    public void generateSlides() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        ppt.setPageSize(new java.awt.Dimension(960, 540)); // 16:9 widescreen
        
        // Slide 1: Title
        XSLFSlide slide1 = ppt.createSlide();
        XSLFAutoShape bg1 = slide1.createAutoShape();
        bg1.setShapeType(ShapeType.RECT);
        bg1.setAnchor(new java.awt.Rectangle(0, 0, 960, 540));
        bg1.setFillColor(new Color(10, 15, 25));
        bg1.setLineColor(null);
        
        XSLFAutoShape border = slide1.createAutoShape();
        border.setShapeType(ShapeType.RECT);
        border.setAnchor(new java.awt.Rectangle(30, 30, 900, 480));
        border.setFillColor(null);
        border.setLineColor(new Color(0, 242, 255, 150));
        border.setLineWidth(2.0);
        
        XSLFTextBox titleBox = slide1.createTextBox();
        titleBox.setAnchor(new java.awt.Rectangle(80, 150, 800, 120));
        XSLFTextParagraph p1 = titleBox.addNewTextParagraph();
        p1.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
        XSLFTextRun r1 = p1.addNewTextRun();
        r1.setText("BÁO CÁO DỰ ÁN HỆ THỐNG QUẢN LÝ KHO");
        r1.setFontColor(new Color(0, 242, 255));
        r1.setFontSize(36.0);
        r1.setFontFamily("Segoe UI");
        r1.setBold(true);

        XSLFTextBox subTitleBox = slide1.createTextBox();
        subTitleBox.setAnchor(new java.awt.Rectangle(80, 260, 800, 80));
        XSLFTextParagraph p2 = subTitleBox.addNewTextParagraph();
        p2.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
        XSLFTextRun r2 = p2.addNewTextRun();
        r2.setText("Thiết kế kiến trúc, nghiệp vụ xuất nhập kho và phân quyền bảo mật");
        r2.setFontColor(new Color(255, 170, 0));
        r2.setFontSize(18.0);
        r2.setFontFamily("Segoe UI");
        r2.setItalic(true);

        XSLFTextBox authorBox = slide1.createTextBox();
        authorBox.setAnchor(new java.awt.Rectangle(80, 400, 800, 80));
        XSLFTextParagraph p3 = authorBox.addNewTextParagraph();
        p3.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
        XSLFTextRun r3 = p3.addNewTextRun();
        r3.setText("Công nghệ sử dụng: Spring Boot, Spring Security, MySQL, Thymeleaf\nNgày báo cáo: Tháng 6/2026");
        r3.setFontColor(new Color(160, 160, 160));
        r3.setFontSize(14.0);
        r3.setFontFamily("Segoe UI");

        // Slide 2: Overview
        XSLFSlide slide2 = createBaseSlide(ppt, "TỔNG QUAN & MỤC TIÊU DỰ ÁN");
        addBulletPoints(slide2, 80, 130, 800, 350, new String[]{
            "Chuyển đổi số hoạt động quản lý kho hàng: Số hóa quy trình Nhập kho và Xuất kho một cách chặt chẽ và nhất quán.",
            "Giám sát tồn kho thực tế: Cung cấp số liệu chính xác theo thời gian thực (Real-time tracking), chống thất thoát hàng hóa.",
            "Bảo mật và Phân quyền nghiêm ngặt: Tách biệt rõ ràng vai trò ADMIN (Quản trị) và STAFF (Nhân viên vận hành).",
            "Giao diện trực quan tối tân: Xây dựng giao diện Cyberpunk HUD Dashboard hiển thị KPIs và biểu đồ động (doanh thu, chi phí, cơ cấu mặt hàng).",
            "Công cụ quản lý bổ trợ chuyên nghiệp: Hỗ trợ tải tệp báo cáo Excel định dạng chuyên nghiệp tự động giãn cột và chèn công thức tính tổng động."
        }, 18.0);

        // Slide 3: Architecture
        XSLFSlide slide3 = createBaseSlide(ppt, "KIẾN TRÚC HỆ THỐNG PHÂN RÃ");
        
        XSLFTextBox feBox = slide3.createTextBox();
        feBox.setAnchor(new java.awt.Rectangle(80, 140, 380, 300));
        XSLFTextParagraph feTitle = feBox.addNewTextParagraph();
        XSLFTextRun feTitleRun = feTitle.addNewTextRun();
        feTitleRun.setText("FRONTEND (PORT 8081)");
        feTitleRun.setFontColor(new Color(255, 170, 0));
        feTitleRun.setFontSize(20.0);
        feTitleRun.setBold(true);
        feTitleRun.setFontFamily("Segoe UI");
        
        String[] fePoints = new String[]{
            "Spring Boot + Thymeleaf Engine",
            "Mô hình ứng dụng trang đơn (SPA) chạy AJAX mượt mà không reload trang",
            "Giao diện tối tân (Dark Mode Cyberpunk HUD)",
            "Đồng bộ hóa ảnh sản phẩm & Lightbox modal"
        };
        for (String pt : fePoints) {
            XSLFTextParagraph p = feBox.addNewTextParagraph();
            p.setBullet(true);
            p.setBulletFontColor(new Color(0, 242, 255));
            XSLFTextRun r = p.addNewTextRun();
            r.setText("  " + pt);
            r.setFontColor(new Color(224, 224, 224));
            r.setFontSize(15.0);
            r.setFontFamily("Segoe UI");
        }
        
        XSLFTextBox beBox = slide3.createTextBox();
        beBox.setAnchor(new java.awt.Rectangle(500, 140, 380, 300));
        XSLFTextParagraph beTitle = beBox.addNewTextParagraph();
        XSLFTextRun beTitleRun = beTitle.addNewTextRun();
        beTitleRun.setText("BACKEND (PORT 8080) & DB");
        beTitleRun.setFontColor(new Color(255, 170, 0));
        beTitleRun.setFontSize(20.0);
        beTitleRun.setBold(true);
        beTitleRun.setFontFamily("Segoe UI");
        
        String[] bePoints = new String[]{
            "Spring Boot RESTful API",
            "Spring Security + Xác thực JWT token không trạng thái (Stateless)",
            "Spring Data JPA truy xuất MySQL Server",
            "MySQL Database chứa các bảng: users, products, categories, suppliers, customer, stock_in/out, audit_logs"
        };
        for (String pt : bePoints) {
            XSLFTextParagraph p = beBox.addNewTextParagraph();
            p.setBullet(true);
            p.setBulletFontColor(new Color(0, 242, 255));
            XSLFTextRun r = p.addNewTextRun();
            r.setText("  " + pt);
            r.setFontColor(new Color(224, 224, 224));
            r.setFontSize(15.0);
            r.setFontFamily("Segoe UI");
        }

        // Slide 4: Table Matrix
        XSLFSlide slide4 = createBaseSlide(ppt, "MA TRẬN PHÂN QUYỀN (AUTHORIZATION MATRIX)");
        XSLFTable table = slide4.createTable();
        table.setAnchor(new java.awt.Rectangle(80, 130, 800, 350));
        
        String[][] tableData = {
            {"MÔ ĐUN / CHỨC NĂNG", "VAI TRÒ ADMIN", "VAI TRÒ STAFF (NHÂN VIÊN)"},
            {"Sản Phẩm & Danh Mục", "Toàn quyền (Xem, Thêm, Sửa, Xóa)", "Chỉ được phép Xem (Read-Only)"},
            {"Khách Hàng & Nhà CC", "Toàn quyền (Xem, Thêm, Sửa, Xóa)", "Chỉ được phép Xem (Read-Only)"},
            {"Lập Phiếu Nhập / Xuất", "Cho phép lập phiếu nháp PENDING", "Cho phép lập phiếu nháp PENDING"},
            {"Phê Duyệt Phiếu", "Duyệt (COMPLETED) hoặc Hủy (CANCELLED)", "Bị chặn hoàn toàn (Ẩn nút & Chặn API)"},
            {"Nhật Ký Hệ Thống", "Xem & Bộ lọc Audit Logs", "Bị chặn hoàn toàn (Chặn truy cập)"},
            {"Phạm Vi Giao Dịch", "Xem và phê duyệt phiếu của mọi tài khoản", "Chỉ thấy các phiếu do chính mình tạo"}
        };
        
        for (int i = 0; i < tableData.length; i++) {
            XSLFTableRow row = table.addRow();
            row.setHeight(40);
            for (int j = 0; j < tableData[i].length; j++) {
                XSLFTableCell cell = row.addCell();
                cell.setText(tableData[i][j]);
                cell.setFillColor(i == 0 ? new Color(0, 242, 255, 40) : new Color(10, 15, 25, 80));
                
                cell.setBorderColor(TableCell.BorderEdge.bottom, new Color(0, 242, 255, 80));
                cell.setBorderWidth(TableCell.BorderEdge.bottom, 1.0);
                cell.setBorderColor(TableCell.BorderEdge.top, new Color(0, 242, 255, 30));
                cell.setBorderWidth(TableCell.BorderEdge.top, 0.5);
                cell.setBorderColor(TableCell.BorderEdge.left, new Color(0, 242, 255, 30));
                cell.setBorderWidth(TableCell.BorderEdge.left, 0.5);
                cell.setBorderColor(TableCell.BorderEdge.right, new Color(0, 242, 255, 30));
                cell.setBorderWidth(TableCell.BorderEdge.right, 0.5);
                
                XSLFTextParagraph p = cell.getTextParagraphs().get(0);
                p.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
                XSLFTextRun r = p.getTextRuns().get(0);
                r.setFontColor(i == 0 ? new Color(0, 242, 255) : new Color(224, 224, 224));
                r.setFontSize(i == 0 ? 14.0 : 13.0);
                r.setFontFamily("Segoe UI");
                r.setBold(i == 0);
            }
        }
        
        table.setColumnWidth(0, 250);
        table.setColumnWidth(1, 275);
        table.setColumnWidth(2, 275);

        // Slide 5: Core Operations
        XSLFSlide slide5 = createBaseSlide(ppt, "NGHIỆP VỤ NHẬP & XUẤT KHO CỐT LÕI");
        addBulletPoints(slide5, 80, 130, 800, 350, new String[]{
            "Nguyên tắc Khóa Số Liệu Trực Tiếp: Khóa chi tiết tồn kho, giá nhập và giá bán trên trang Sản phẩm. Chỉ cho phép cập nhật số liệu tự động thông qua phê duyệt phiếu nhập/xuất để tránh sai lệch số liệu thủ công.",
            "Cơ chế Kiểm soát Tồn Kho (Stock Validation): Kiểm tra số lượng tồn kho khả dụng trước khi lập phiếu xuất. Nếu số lượng xuất vượt quá tồn kho khả dụng, hệ thống cảnh báo tức thì ở cả Client-side (Frontend) và Server-side (Backend), chặn đứng hành vi tạo phiếu.",
            "Phân chia Trạng Thái Phiếu: Phiếu khởi tạo mặc định là PENDING (chờ duyệt) và chưa tác động đến tồn kho. Chỉ khi ADMIN phê duyệt chuyển trạng thái sang COMPLETED thì Backend mới chính thức cộng/trừ tồn kho.",
            "Tính giá xuất bình quan gia quyền tích lũy: Khi phiếu xuất kho được duyệt thành công (COMPLETED), Backend tự động tính toán lại Giá xuất trung bình dựa trên tổng lượng đã bán và lũy kế giá trị xuất để cập nhật giá trị tài chính chính xác."
        }, 16.0);

        // Slide 6: Tech Features
        XSLFSlide slide6 = createBaseSlide(ppt, "ĐẶC TÍNH KỸ THUẬT & TỐI ƯU HÓA");
        
        XSLFTextBox techLeft = slide6.createTextBox();
        techLeft.setAnchor(new java.awt.Rectangle(80, 140, 380, 300));
        XSLFTextParagraph lTitle = techLeft.addNewTextParagraph();
        XSLFTextRun lTitleRun = lTitle.addNewTextRun();
        lTitleRun.setText("HIỆU NĂNG & TRẠI NGHIỆM");
        lTitleRun.setFontColor(new Color(255, 170, 0));
        lTitleRun.setFontSize(20.0);
        lTitleRun.setBold(true);
        lTitleRun.setFontFamily("Segoe UI");
        
        String[] lPoints = new String[]{
            "Excel Streaming (SXSSFWorkbook): Ghi nén đĩa trực tiếp với bộ đệm 100 dòng, giải quyết triệt để rủi ro tràn bộ nhớ RAM (OutOfMemoryError) khi xuất dữ liệu lớn.",
            "Trải nghiệm SPA (AJAX Engine): Tải trang không reload thông qua nạp phân mảnh Thymeleaf kết hợp với AJAX, giúp hệ thống phản hồi cực nhanh.",
            "Xuất báo cáo Excel chuyên nghiệp: Tự động giãn cột, căn chỉnh cột dữ liệu, định dạng tiền tệ chuyên sâu và gán công thức tính tổng sum động của Excel."
        };
        for (String pt : lPoints) {
            XSLFTextParagraph p = techLeft.addNewTextParagraph();
            p.setBullet(true);
            p.setBulletFontColor(new Color(0, 242, 255));
            XSLFTextRun r = p.addNewTextRun();
            r.setText("  " + pt);
            r.setFontColor(new Color(224, 224, 224));
            r.setFontSize(14.0);
            r.setFontFamily("Segoe UI");
        }
        
        XSLFTextBox techRight = slide6.createTextBox();
        techRight.setAnchor(new java.awt.Rectangle(500, 140, 380, 300));
        XSLFTextParagraph rTitle = techRight.addNewTextParagraph();
        XSLFTextRun rTitleRun = rTitle.addNewTextRun();
        rTitleRun.setText("BẢO MẬT & DỌN DẸP");
        rTitleRun.setFontColor(new Color(255, 170, 0));
        rTitleRun.setFontSize(20.0);
        rTitleRun.setBold(true);
        rTitleRun.setFontFamily("Segoe UI");
        
        String[] rPoints = new String[]{
            "JWT Authentication (Stateless): Xác thực qua JWT token đính kèm ở Header. Không dùng HttpSession giúp API dễ dàng mở rộng sang các hệ thống khác.",
            "Đồng bộ ảnh & Dọn dẹp đĩa cứng: Khi cập nhật ảnh mới hoặc xóa sản phẩm, hệ thống tự động xóa file vật lý cũ trong thư mục uploads để tránh lãng phí dung lượng ổ cứng.",
            "Tomcat Temp folder fix: Khắc phục lỗi Tomcat temp directory bằng cơ chế Java NIO Files.copy ghi đĩa tuyệt đối."
        };
        for (String pt : rPoints) {
            XSLFTextParagraph p = techRight.addNewTextParagraph();
            p.setBullet(true);
            p.setBulletFontColor(new Color(0, 242, 255));
            XSLFTextRun r = p.addNewTextRun();
            r.setText("  " + pt);
            r.setFontColor(new Color(224, 224, 224));
            r.setFontSize(14.0);
            r.setFontFamily("Segoe UI");
        }

        // Slide 7: Upgrades
        XSLFSlide slide7 = createBaseSlide(ppt, "CÁC NÂNG CẤP MỚI HOÀN THÀNH GẦN ĐÂY");
        addBulletPoints(slide7, 80, 130, 800, 350, new String[]{
            "Khắc phục lỗi Tomcat Temp Directory (Tải ảnh): Sử dụng Java NIO ghi đĩa tuyệt đối, giải quyết triệt để lỗi FileNotFoundException khi Tomcat tự động dọn dẹp thư mục tạm.",
            "Cột hiển thị thông tin kép: Thêm cột 'Tài Khoản' hiển thị username kế bên cột 'Người Lập' hiển thị fullName trên cả hai bảng Nhập kho và Xuất kho.",
            "Tự động điền & Kiểm tra Tồn Kho Real-time: Thêm ô 'Tồn kho hiện tại' (bị vô hiệu hóa, chữ cam phát sáng) tự động cập nhật tồn kho khi chọn sản phẩm ở Modal Xuất kho. Chặn ngay tại chỗ (Client-side) nếu người dùng nhập số lượng vượt quá tồn kho khả dụng.",
            "Phân quyền theo tài khoản nhân viên (User Scoping): Nhân viên vận hành chỉ thấy và thao tác được các phiếu Nhập/Xuất kho do chính mình tạo ra. ADMIN có quyền xem và phê duyệt toàn bộ chứng từ hệ thống.",
            "Audit log mở rộng: Thêm cột 'Họ Tên' lấy fullName kế bên 'Tài Khoản' (username) trong danh sách Nhật ký hoạt động hệ thống."
        }, 15.0);

        // Slide 8: Operations
        XSLFSlide slide8 = createBaseSlide(ppt, "HƯỚNG DẪN VẬN HÀNH & KẾT LUẬN");
        addBulletPoints(slide8, 80, 130, 800, 350, new String[]{
            "Bước 1: Khởi động MySQL database server cục bộ trên cổng 3306.",
            "Bước 2: Chạy dự án Backend API ('API_JAVA-main') trên cổng 8080. Đảm bảo cấu hình biến môi trường JWT_SECRET_KEY.",
            "Bước 3: Chạy dự án Frontend UI ('FEJAVA2') trên cổng 8081.",
            "Bước 4: Truy cập ứng dụng qua trình duyệt tại địa chỉ http://localhost:8081/login.",
            "Kết luận: Dự án hoàn thành xuất sắc tất cả yêu cầu về bảo mật phân quyền, kiểm soát dữ liệu tự động chống sai lệch, xuất file Excel dung lượng lớn hiệu năng cao, giao diện đồng bộ hiện đại Cyberpunk mang lại trải nghiệm người dùng tối ưu."
        }, 16.0);

        // Save presentation to files
        String[] paths = {
            "C:/Users/Thi Duong/.gemini/antigravity/brain/65c8ad5a-0a4d-45cf-a39c-f9ae922a4d79/bao_cao_du_an.pptx",
            "D:/java2/API_JAVA-main/bao_cao_du_an.pptx",
            "D:/FEJAVA2/bao_cao_du_an.pptx"
        };

        for (String path : paths) {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
                System.out.println(">>> PPTX saved to: " + path);
            }
        }
        ppt.close();
    }

    private XSLFSlide createBaseSlide(XMLSlideShow ppt, String titleText) {
        XSLFSlide slide = ppt.createSlide();
        
        // Background
        XSLFAutoShape bg = slide.createAutoShape();
        bg.setShapeType(ShapeType.RECT);
        bg.setAnchor(new java.awt.Rectangle(0, 0, 960, 540));
        bg.setFillColor(new Color(10, 15, 25));
        bg.setLineColor(null);
        
        // Title
        if (titleText != null && !titleText.isEmpty()) {
            XSLFTextBox titleBox = slide.createTextBox();
            titleBox.setAnchor(new java.awt.Rectangle(50, 30, 860, 60));
            XSLFTextParagraph p = titleBox.addNewTextParagraph();
            XSLFTextRun r = p.addNewTextRun();
            r.setText(titleText.toUpperCase());
            r.setFontColor(new Color(0, 242, 255));
            r.setFontSize(24.0);
            r.setFontFamily("Segoe UI");
            r.setBold(true);
            
            XSLFAutoShape line = slide.createAutoShape();
            line.setShapeType(ShapeType.RECT);
            line.setAnchor(new java.awt.Rectangle(50, 90, 860, 2));
            line.setFillColor(new Color(0, 242, 255, 100));
            line.setLineColor(null);
        }
        
        return slide;
    }

    private void addBulletPoints(XSLFSlide slide, double x, double y, double w, double h, String[] points, double fontSize) {
        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new java.awt.Rectangle((int)x, (int)y, (int)w, (int)h));
        
        for (String pt : points) {
            XSLFTextParagraph p = textBox.addNewTextParagraph();
            p.setBullet(true);
            p.setBulletFontColor(new Color(255, 170, 0));
            
            XSLFTextRun r = p.addNewTextRun();
            r.setText("  " + pt);
            r.setFontColor(new Color(224, 224, 224));
            r.setFontSize(fontSize);
            r.setFontFamily("Segoe UI");
        }
    }
}

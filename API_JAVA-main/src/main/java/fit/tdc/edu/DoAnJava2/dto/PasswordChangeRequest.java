package fit.tdc.edu.DoAnJava2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PasswordChangeRequest {

    @Schema(description = "Mật khẩu hiện tại", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    @Schema(description = "Mật khẩu mới", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    public PasswordChangeRequest() {
    }

    public PasswordChangeRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

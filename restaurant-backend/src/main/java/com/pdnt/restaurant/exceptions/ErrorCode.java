package com.pdnt.restaurant.exceptions;

public enum ErrorCode {
    USERNAME_NOT_FOUND(1001, "Không đúng tên đăng nhập."),
    AUTHENTICATION_FAILED(4005,"Đăng nhập không thành công"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    REVIEW_UPDATE_FORBIDDEN(1111, "You cannot update someone else's review"),
    REVIEW_DELETE_FORBIDDEN(1112, "You cannot delete someone else's review"),
    UNAUTHORIZED(4001, "Người dùng chưa đăng nhập"),
    ORDER_REJECTED(5001, "Trong OrderGroup có đơn bị hủy, không thể thanh toán!"),
    PASSWORD_INCORRECT(5002, "Mật khẩu không đúng!!!"),
    FORBIDDEN(4003, "Bạn không có quyền thực hiện hành động này!"); // giống HTTP 403

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

package com.pdnt.restaurant.exceptions;

public enum ErrorCode {
    USER_EXISTED(1001, "User existed."),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    REVIEW_UPDATE_FORBIDDEN(1111, "You cannot update someone else's review"),
    REVIEW_DELETE_FORBIDDEN(1112, "You cannot delete someone else's review"),
    UNAUTHORIZED(4001, "Người dùng chưa đăng nhập"),
    ORDER_REJECTED(5001, "Trong OrderGroup có đơn bị hủy, không thể thanh toán!"),
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

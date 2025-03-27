package com.wiinventjava.Enums;

import lombok.Getter;

@Getter
public enum AttendanceMessage {
    ALREADY_CHECKED_IN("Bạn đã điểm danh hôm nay rồi!"),
    CHECK_IN_SUCCESS("Điểm danh thành công"),
    REACHED_MAX_DAYS("Bạn đã đạt giới hạn điểm danh 7 ngày trong tháng"),
    SYSTEM_BUSY("Hệ thống đang bận, vui lòng thử lại"),
    SYSTEM_ERROR("Lỗi hệ thống, vui lòng thử lại sau!"),
    WRONG_TIME("Bạn chỉ có thể điểm danh từ 09:00-11:00 hoặc 19:00-21:00!");

    private final String message;

    AttendanceMessage(String message) {
        this.message = message;
    }
}

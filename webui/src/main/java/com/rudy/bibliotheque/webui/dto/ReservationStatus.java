package com.rudy.bibliotheque.webui.dto;

import com.rudy.bibliotheque.webui.util.Constant;

public enum ReservationStatus {
    ONGOING(Constant.STATUS_ONGOING_CODE , Constant.STATUS_ONGOING),
    FINISHED(Constant.STATUS_FINISHED_CODE, Constant.STATUS_FINISHED),
    CANCELLED(Constant.STATUS_CANCELLED_CODE, Constant.STATUS_CANCELLED);

    private String code;
    private String name;

    ReservationStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

}

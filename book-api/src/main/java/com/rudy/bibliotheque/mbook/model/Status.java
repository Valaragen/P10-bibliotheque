package com.rudy.bibliotheque.mbook.model;

import com.rudy.bibliotheque.mbook.util.Constant;

public enum Status {
    PENDING(Constant.STATUS_PENDING_CODE, Constant.STATUS_PENDING),
    ONGOING(Constant.STATUS_ONGOING_CODE ,Constant.STATUS_ONGOING),
    LATE(Constant.STATUS_LATE_CODE, Constant.STATUS_LATE),
    FINISHED(Constant.STATUS_FINISHED_CODE, Constant.STATUS_FINISHED),
    CANCELLED(Constant.STATUS_CANCELLED_CODE, Constant.STATUS_CANCELLED);

    private Long code;
    private String name;

    Status(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

}

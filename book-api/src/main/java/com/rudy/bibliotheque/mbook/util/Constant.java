package com.rudy.bibliotheque.mbook.util;

import org.springframework.beans.factory.annotation.Value;

// TODO Update
public class Constant {

    //ROLES
    public static final String USER_ROLE_NAME = "ROLE_USER";
    public static final String STAFF_ROLE_NAME = "ROLE_STAFF";
    public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    //PATH
    public static final Object SLASH = "/";
    public static final Object REDIRECT = "redirect:";
    public static final String SLASH_ID_PATH = "/{id}";
    public static final String SLASH_STRING_PATH = "/{string}";

    public static final String USERS_PATH = "/users";
    public static final String BOOKS_PATH = "/books";
    public static final String LOANS_PATH = "/loans";
    public static final String COPIES_PATH = "/copies";
    public static final String RESERVATIONS_PATH = "/reservations";

    public static final String EXTEND_PATH = "/extend";
    public static final String VALIDATE_PATH = "/validate";
    public static final String RETURNED_PATH = "/returned";
    public static final String CURRENT_PATH = "/current";
    public static final String NON_RETURNED_EXPIRED_LOANS_PATH = "/non-returned-and-expired";
    public static final String COPY_CODE_PATH = "/code";

    //LOAN STATUS
    public static final String LOAN_STATUS_PENDING = "pending";
    public static final String LOAN_STATUS_ONGOING = "ongoing";
    public static final String LOAN_STATUS_LATE = "late";
    public static final String LOAN_STATUS_FINISHED = "finished";

    //MESSAGE
    //Validation

}

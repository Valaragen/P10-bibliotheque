package com.rudy.bibliotheque.batch.util;

public class Constant {
    //API-PATH
    public static final String BOOK_API_PATH = "/book-api";

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
    public static final String MAILS_PATH = "/mails";

    public static final String ADD_PATH = "/add";
    public static final String MODIFY_PATH = "/modify";
    public static final String DELETE_PATH = "/delete";

    public static final String EXTEND_PATH = "/extend";
    public static final String MY_LOANS_PATH = "/my-loans";
    public static final String ONGOING_PATH = "/ongoing";
    public static final String PENDING_PATH = "/pending";
    public static final String CURRENT_PATH = "/current";
    public static final String SEARCH_PATH = "/search";
    public static final String PENDING_MAIL_PATH = "/pending-mail";

    //LOAN STATUS
    public static final String LOAN_STATUS_PENDING = "pending";
    public static final String LOAN_STATUS_ONGOING = "ongoing";
    public static final String LOAN_STATUS_LATE = "late";
    public static final String LOAN_STATUS_FINISHED = "finished";

    //MESSAGE
    //Validation
    public static final String MSG_WRONG_ISBN_REGEX = "L''ISBN doit être un nombre de 10 ou 13 chiffres";
}

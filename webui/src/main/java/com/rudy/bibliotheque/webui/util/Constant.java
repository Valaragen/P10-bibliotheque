package com.rudy.bibliotheque.webui.util;

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
    public static final String RESERVATIONS_PATH = "/reservations";

    public static final String ADD_PATH = "/add";
    public static final String MODIFY_PATH = "/modify";
    public static final String DELETE_PATH = "/delete";

    public static final String MY_LOANS_PATH = "/my-loans";
    public static final String MY_RESERVATIONS_PATH = "/my-reservations";
    public static final String EXTEND_PATH = "/extend";
    public static final String ONGOING_PATH = "/ongoing";
    public static final String PENDING_PATH = "/pending";
    public static final String CURRENT_PATH = "/current";
    public static final String SEARCH_PATH = "/search";

    //PAGES
    public static final String HOME_PAGE = "homePage";
    public static final String BOOKS_LIST_PAGE = "booksListPage";
    public static final String LOANS_LIST_PAGE = "loansListPage";
    public static final String LOANS_USER_PAGE = "loansUserPage";
    public static final String RESERVATIONS_USER_PAGE = "reservationsUserPage";
    public static final String BOOK_DETAILS_PAGE = "bookDetailsPage";
    public static final String BOOK_ADD_PAGE = "bookAddPage";
    public static final String BOOK_MODIFY_PAGE = "bookModifyPage";

    //STATUS
    public static final String STATUS_PENDING_CODE = "P";
    public static final String STATUS_ONGOING_CODE = "O";
    public static final String STATUS_LATE_CODE = "L";
    public static final String STATUS_FINISHED_CODE = "F";
    public static final String STATUS_CANCELLED_CODE = "C";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_LATE = "late";
    public static final String STATUS_FINISHED = "finished";
    public static final String STATUS_CANCELLED = "cancelled";

    //MESSAGE
    //Validation
    public static final String MSG_WRONG_ISBN_REGEX = "L''ISBN doit être un nombre de 10 ou 13 chiffres";
}

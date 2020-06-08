package com.rudy.bibliotheque.mbook.helper;

import com.rudy.bibliotheque.mbook.model.*;

import java.util.*;

public abstract class ModelObjectBuilderHelper {

    // SAMPLE USERINFOS
    public static UserInfo getSampleUserInfo1() {
        return buildUserInfo("ed2afb4e-c552-47e9-9fab-48b59cd544b1", "user1", "user1@gmail.com",
                "user1firstname", "user1lastname", "0645854765", "user1address");
    }

    public static UserInfo getSampleUserInfo2() {
        return buildUserInfo("39c76e0b-d7b2-4447-9dad-1702cf4df4c0", "user2", "user2@gmail.com",
                "user2firstname", "user2lastname", "0645854765", "user2address");
    }

    // SAMPLE BOOKS
    public static Book getSampleBook1() {
        return buildBook(1L, "1223345432214", "Black and White",
                "Description of Black and White", "Penneck Daniel",
                "MyBook", new Date(), 1, 0);
    }

    public static Book getSampleBook2() {
        return buildBook(2L, "2568945236542", "Harry Potter and the Order of the Phoenix",
                "Description of Harry Potter and the Order of the Phoenix",
                "J.K. Rowling", "Publishitor", new Date(), 2, 1);
    }

    public static Book getSampleBook3() {
        return buildBook(3L, "5486982563524  ", "A Game of Thrones (A Song of Ice and Fire, #1)",
                "Description of A Game of Thrones (A Song of Ice and Fire, #1)",
                "George R.R. Martin", "Publishitor", new Date(), 1, 1);
    }

    //SAMPLE COPIES
    public static Copy getSampleCopy1ofBook1() {
        return buildCopy("YUBCEZUVEZ", getSampleBook1(), "neuf", "abimé", true);
    }

    public static Copy getSampleCopy1ofBook2() {
        return buildCopy("HUBEUYBUEFZ", getSampleBook2(), "neuf", "neuf", true);
    }

    public static Copy getSampleCopy2ofBook2() {
        return buildCopy("AAUDNZEFFFF", getSampleBook2(), "neuf", "neuf", false);
    }

    public static Copy getSampleCopy1ofBook3() {
        return buildCopy("UNEUVUUEUZZZ", getSampleBook3(), "neuf", "neuf", false);
    }

    //SAMPLE BORROWS
    public static Borrow getSampleOngoingBorrow1() {
        return buildBorrow(1L, getSampleCopy1ofBook1(), getSampleUserInfo1(), false,
                new GregorianCalendar(2020, Calendar.MARCH, 15).getTime(),
                new GregorianCalendar(2020, Calendar.MARCH, 17, 13, 0, 0).getTime(),
                new GregorianCalendar(2020, Calendar.MARCH, 17).getTime(),
                new GregorianCalendar(2020, Calendar.APRIL, 15).getTime(),
                null, "abimé", null);
    }

    public static Borrow getSamplePendingBorrow1() {
        return buildBorrow(1L, getSampleCopy1ofBook2(), getSampleUserInfo2(), false,
                new GregorianCalendar(2020, Calendar.MARCH, 15).getTime(),
                new GregorianCalendar(2020, Calendar.MARCH, 17, 13, 0, 0).getTime(),
                null,
                null,
                null, "neuf", null);
    }

    //SAMPLE RESERVATIONS
    public static Reservation getSampleOngoingReservation() {
        return buildReservation(1L, getSampleBook1(), getSampleUserInfo2(),
                new GregorianCalendar(2020, Calendar.MARCH, 15).getTime(),
                null,
                ReservationStatus.ONGOING);
    }

    //BUILDERS
    public static Book buildBook(Long id, String isbn, String name, String description, String author, String publisher,
                                 Date releaseDate, Integer copyNumber, Integer availableCopyNumber) {
        Book book = new Book();
        book.setId(id);
        book.setIsbn(isbn);
        book.setName(name);
        book.setDescription(description);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setReleaseDate(releaseDate);
        book.setCopyNumber(copyNumber);
        book.setAvailableCopyNumber(availableCopyNumber);
        return book;
    }

    public static Borrow buildBorrow(Long id, Copy copy, UserInfo userInfo, boolean hasDurationExtended, Date loanRequestDate, Date deadlineToRetrieve,
                                     Date loanStartDate, Date loanEndDate, Date returnedOn, String stateBeforeBorrow,
                                     String stateAfterBorrow) {
        Borrow borrow = new Borrow();
        borrow.setId(id);
        borrow.setCopy(copy);
        borrow.setUserInfo(userInfo);
        borrow.setHasDurationExtended(hasDurationExtended);
        borrow.setLoanRequestDate(loanRequestDate);
        borrow.setDeadlineToRetrieve(deadlineToRetrieve);
        borrow.setLoanStartDate(loanStartDate);
        borrow.setLoanEndDate(loanEndDate);
        borrow.setReturnedOn(returnedOn);
        borrow.setStateBeforeBorrow(stateBeforeBorrow);
        borrow.setStateAfterBorrow(stateAfterBorrow);

        return borrow;
    }

    public static UserInfo buildUserInfo(String id, String username, String email, String firstName, String lastName,
                                         String phone, String address) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setUsername(username);
        userInfo.setEmail(email);
        userInfo.setFirstName(firstName);
        userInfo.setLastName(lastName);
        userInfo.setPhone(phone);
        userInfo.setAddress(address);

        return userInfo;
    }

    public static Copy buildCopy(String code, Book book, String stateAtPurchase, String currentState,
                                 boolean borrowed) {
        Copy copy = new Copy();
        copy.setCode(code);
        copy.setBook(book);
        copy.setStateAtPurchase(stateAtPurchase);
        copy.setCurrentState(currentState);
        copy.setBorrowed(borrowed);
        return copy;
    }

    public static Reservation buildReservation(Long id, Book book, UserInfo userInfo, Date reservationStartDate,
                                               Date reservationEndDate, ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setBook(book);
        reservation.setUserInfo(userInfo);
        reservation.setReservationStartDate(reservationStartDate);
        reservation.setReservationEndDate(reservationEndDate);
        reservation.setStatus(status);
        return reservation;
    }
}

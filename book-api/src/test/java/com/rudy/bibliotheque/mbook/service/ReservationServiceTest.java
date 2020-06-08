package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.dto.ReservationCreateDTO;
import com.rudy.bibliotheque.mbook.helper.ModelObjectBuilderHelper;
import com.rudy.bibliotheque.mbook.model.*;
import com.rudy.bibliotheque.mbook.repository.ReservationRepository;
import com.rudy.bibliotheque.mbook.web.exception.InvalidIdException;
import com.rudy.bibliotheque.mbook.web.exception.NotFoundException;
import com.rudy.bibliotheque.mbook.web.exception.ProhibitedActionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ReservationServiceTest {

    private ReservationRepository reservationRepository = Mockito.mock(ReservationRepository.class);
    private UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
    private BookService bookService = Mockito.mock(BookService.class);
    private BorrowService borrowService = Mockito.mock(BorrowService.class);

    private ReservationService objectToTest = new ReservationService(reservationRepository, userInfoService, bookService, borrowService);


    @Nested
    class createNewReservationTest {
        @Test
        void createNewReservation_createDTOBookIdIsNotFoundInDb_throwInvalidIdException() {
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(InvalidIdException.class);
        }

        @Test
        void createNewReservation_createDTOUserIdIsNotFoundInDb_throwInvalidIdException() {
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(1L);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(InvalidIdException.class);
        }

        @Test
        void createNewReservation_createDTOBookIdIsNull_throwNotFoundException() {
            Long notFoundBookId = 1L;
            String userUUID = "user";
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(notFoundBookId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(notFoundBookId)).thenReturn(null);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void createNewReservation_bookGotNoCopyToReserve_throwProhibitedActionException() {
            Long bookWithoutCopyId = 1L;
            String userUUID = "user";
            Book bookWithoutCopy = ModelObjectBuilderHelper.getSampleBook1();
            bookWithoutCopy.setCopyNumber(0);
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(bookWithoutCopyId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(bookWithoutCopyId)).thenReturn(bookWithoutCopy);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("This book got no copy to reserve");
        }

        @Test
        void createNewReservation_bookGotAvailableCopies_throwProhibitedActionException() {
            Long bookWithAvailableCopyId = 1L;
            String userUUID = "user";
            Book bookWithAvailableCopies = ModelObjectBuilderHelper.getSampleBook1();
            bookWithAvailableCopies.setAvailableCopyNumber(1);
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(bookWithAvailableCopyId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(bookWithAvailableCopyId)).thenReturn(bookWithAvailableCopies);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("Reservations are only available when all copies of the book are currently borrowed");
        }

        @Test
        void createNewReservation_bookMaxNumberOfReservations_throwProhibitedActionException() {
            Long bookId = 1L;
            String userUUID = "user";
            Book book = ModelObjectBuilderHelper.getSampleBook1();
            List<Reservation> ongoingReservations = new ArrayList<>();
            ongoingReservations.add(new Reservation());
            ongoingReservations.add(new Reservation());
            book.setOngoingReservations(ongoingReservations);
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(bookId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(bookId)).thenReturn(book);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("The max number of concurrent reservations has been reached");
        }

        @Test
        void createNewReservation_userAlreadyGotAReservation_throwProhibitedActionException() {
            UserInfo userInfo = ModelObjectBuilderHelper.getSampleUserInfo1();
            Long bookId = 1L;
            String userUUID = userInfo.getId();
            Book book = ModelObjectBuilderHelper.getSampleBook1();
            List<Reservation> ongoingReservations = new ArrayList<>();
            Reservation ongoingReservationByCurrentUser = ModelObjectBuilderHelper.getSampleOngoingReservation();
            ongoingReservationByCurrentUser.setUserInfo(userInfo);
            ongoingReservations.add(ongoingReservationByCurrentUser);
            book.setOngoingReservations(ongoingReservations);
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(bookId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(bookId)).thenReturn(book);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("You can't reserve the same book twice");
        }

        @Test
        void createNewReservation_userAlreadyBorrowingBook_throwProhibitedActionException() {
            UserInfo userInfo = ModelObjectBuilderHelper.getSampleUserInfo1();
            Long bookId = 1L;
            String userUUID = userInfo.getId();
            Book book = ModelObjectBuilderHelper.getSampleBook1();
            book.setOngoingReservations(new ArrayList<>());
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(bookId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(bookId)).thenReturn(book);
            Mockito.when(borrowService.isUserBorrowingBook(reservationCreateDTO.getUserId(), reservationCreateDTO.getBookId()))
                    .thenReturn(true);

            Assertions.assertThatThrownBy(() -> objectToTest.createNewReservation(reservationCreateDTO))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("You can't reserve a book that you are borrowing");
        }

        @Test
        void createNewReservation_methodCalledWithRightParameters_shouldLinkBookAndUserInfoUpdateToOngoingAndSave() {
            UserInfo userInfo = ModelObjectBuilderHelper.getSampleUserInfo1();
            Long bookId = 1L;
            String userUUID = userInfo.getId();
            Book book = ModelObjectBuilderHelper.getSampleBook1();
            book.setOngoingReservations(new ArrayList<>());
            ReservationCreateDTO reservationCreateDTO = new ReservationCreateDTO();
            reservationCreateDTO.setBookId(bookId);
            reservationCreateDTO.setUserId(userUUID);
            Mockito.when(bookService.getBookById(bookId)).thenReturn(book);
            Mockito.when(borrowService.isUserBorrowingBook(reservationCreateDTO.getUserId(), reservationCreateDTO.getBookId()))
                    .thenReturn(false);
            Mockito.when(userInfoService.getUserInfoById(reservationCreateDTO.getUserId())).thenReturn(userInfo);
            Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

            Reservation resultReservation = objectToTest.createNewReservation(reservationCreateDTO);

            Assertions.assertThat(resultReservation.getUserInfo()).isNotNull();
            Assertions.assertThat(resultReservation.getBook()).isNotNull();
            Assertions.assertThat(resultReservation.getStatus()).isEqualTo(ReservationStatus.ONGOING);
            Mockito.verify(reservationRepository).save(resultReservation);
        }
    }

    @Nested
    class cancelReservationTest {
        @Test
        void cancelReservation_alreadyCancelledReservation_throwProhibitedActionException() {
            Reservation reservation = new Reservation();
            reservation.setStatus(ReservationStatus.CANCELLED);

            Assertions.assertThatThrownBy(() -> objectToTest.cancelReservation(reservation))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("This reservation is already cancelled");
        }

        @Test
        void cancelReservation_alreadyFinishedReservation_throwProhibitedActionException() {
            Reservation reservation = new Reservation();
            reservation.setStatus(ReservationStatus.FINISHED);

            Assertions.assertThatThrownBy(() -> objectToTest.cancelReservation(reservation))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("Can't cancel a finished reservation");
        }

        @Test
        void cancelReservation_whenMethodCalled_shouldCancelReservationAndSave() {
            Reservation reservation = new Reservation();
            Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

            objectToTest.cancelReservation(reservation);

            Assertions.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            Mockito.verify(reservationRepository).save(reservation);
        }
    }

    @Nested
    class validateReservationTest {
        @Test
        void validateReservation_alreadyCancelledReservation_throwProhibitedActionException() {
            Reservation reservation = new Reservation();
            reservation.setStatus(ReservationStatus.CANCELLED);

            Assertions.assertThatThrownBy(() -> objectToTest.validateReservation(reservation))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("Can't validate a cancelled reservation");
        }

        @Test
        void validateReservation_alreadyFinishedReservation_throwProhibitedActionException() {
            Reservation reservation = new Reservation();
            reservation.setStatus(ReservationStatus.FINISHED);

            Assertions.assertThatThrownBy(() -> objectToTest.validateReservation(reservation))
                    .isInstanceOf(ProhibitedActionException.class)
                    .hasMessageContaining("This reservation is already finished");
        }

        @Test
        void validateReservation_whenMethodCalled_shouldCancelReservationAndSave() {
            Reservation reservation = new Reservation();
            Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

            objectToTest.validateReservation(reservation);

            Assertions.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.FINISHED);
            Mockito.verify(reservationRepository).save(reservation);
        }
    }

    @Nested
    class createLoanFromBookReservationsTest {
        @Test
        void createLoanFromBookReservations_noOngoingReservationForBook_doNothingAndLog() {
            Book book = new Book();
            book.setOngoingReservations(new ArrayList<>());

            objectToTest.createLoanFromBookReservations(book);

            Mockito.verify(borrowService, Mockito.never()).saveANewLoan(Mockito.any(LoanCreateDTO.class));
        }

        @Test
        void createLoanFromBookReservations_whenBookGotOngoingReservations_shouldTakeTheFirstMadeReservationByDateAndCreateALoanFromIt() {
            //ARRANGE
            Book book = new Book();
            book.setId(1L);

            Reservation reservation1 = new Reservation();
            reservation1.setReservationStartDate(new GregorianCalendar(2020, Calendar.MARCH, 14).getTime());
            Reservation reservationThatShouldBeUsed = new Reservation();
            reservationThatShouldBeUsed.setReservationStartDate(new GregorianCalendar(2020, Calendar.MARCH, 1).getTime());
            reservationThatShouldBeUsed.setUserInfo(ModelObjectBuilderHelper.getSampleUserInfo1());
            Reservation reservation2 = new Reservation();
            reservation2.setReservationStartDate(new GregorianCalendar(2020, Calendar.MARCH, 15).getTime());

            List<Reservation> ongoingReservations = new ArrayList<>();
            ongoingReservations.add(reservation1);
            ongoingReservations.add(reservation2);
            ongoingReservations.add(reservationThatShouldBeUsed);
            book.setOngoingReservations(ongoingReservations);

            LoanCreateDTO expectedLoanCreateDTO = new LoanCreateDTO();
            expectedLoanCreateDTO.setBookId(book.getId());
            expectedLoanCreateDTO.setUserId(reservationThatShouldBeUsed.getUserInfo().getId());
            Mockito.when(borrowService.saveANewLoan(Mockito.any(LoanCreateDTO.class))).thenReturn(new Borrow());
            Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenAnswer(i -> i.getArguments()[0]);

            //ACT
            objectToTest.createLoanFromBookReservations(book);

            //ASSERT
            Mockito.verify(borrowService).saveANewLoan(expectedLoanCreateDTO);
        }
    }
}

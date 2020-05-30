package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.dto.ReservationCreateDTO;
import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.model.Reservation;
import com.rudy.bibliotheque.mbook.model.ReservationStatus;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.repository.ReservationRepository;
import com.rudy.bibliotheque.mbook.search.ReservationSearch;
import com.rudy.bibliotheque.mbook.web.controller.UserController;
import com.rudy.bibliotheque.mbook.web.controller.util.ControllerUtil;
import com.rudy.bibliotheque.mbook.web.exception.CRUDIssueException;
import com.rudy.bibliotheque.mbook.web.exception.InvalidIdException;
import com.rudy.bibliotheque.mbook.web.exception.NotFoundException;
import com.rudy.bibliotheque.mbook.web.exception.ProhibitedActionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ReservationService {
    private ReservationRepository reservationRepository;

    private UserInfoService userInfoService;
    private BookService bookService;
    private BorrowService borrowService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, UserInfoService userInfoService, BookService bookService, BorrowService borrowService) {
        this.reservationRepository = reservationRepository;
        this.userInfoService = userInfoService;
        this.bookService = bookService;
        this.borrowService = borrowService;
    }


    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getAllReservationsBySearch(ReservationSearch reservationSearch) {
        return reservationRepository.findAllBySearch(reservationSearch);
    }

    public List<Reservation> getAllReservationsByBookId(Long id) {
        return reservationRepository.findAllByBookId(id);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    public Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation createNewReservation(ReservationCreateDTO reservationCreateDTO) {
        if (reservationCreateDTO.getBookId() == null) {
            throw new InvalidIdException("Book id has not been provided");
        }

        Book linkedBook = bookService.getBookById(reservationCreateDTO.getBookId());
        if (linkedBook == null) {
            throw new NotFoundException("No book with id " + reservationCreateDTO.getBookId());
        }
        if (linkedBook.getCopyNumber() == 0) {
            throw new ProhibitedActionException("This book got no copy to reserve");
        }
        if (linkedBook.getAvailableCopyNumber() > 0) {
            throw new ProhibitedActionException("Reservations are only available when all copies of the book are currently borrowed");
        }
        if (linkedBook.getOngoingReservations().size() >= (2 * linkedBook.getCopyNumber())) {
            throw new ProhibitedActionException("The max number of concurrent reservations has been reached");
        }
        //Check if the user already got a reservation on the book
        boolean hasAlreadyAReservation = linkedBook.getOngoingReservations().stream()
                .map(Reservation::getUserInfo).anyMatch(userInfo -> userInfo.getId().equals(ControllerUtil.getUserIdFromToken()));
        if (hasAlreadyAReservation) {
            throw new ProhibitedActionException("You can't reserve the same book twice");
        }
        if (borrowService.isUserBorrowingBook(reservationCreateDTO.getUserId(), reservationCreateDTO.getBookId())) {
            throw new ProhibitedActionException("You can't reserve a book that you are borrowing");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(linkedBook);
        log.debug("Book linked to the reservation");
        UserInfo linkedUserInfo = userInfoService.getUserInfoById(reservationCreateDTO.getUserId());
        if (linkedUserInfo == null) {
            //Create the userInfo in database
            linkedUserInfo = UserController.getUserInfoFromToken();
            linkedUserInfo = userInfoService.saveUserInfo(linkedUserInfo);
            if (linkedUserInfo == null) {
                throw new CRUDIssueException("Can't create user_info entity");
            }
        }

        reservation.setUserInfo(linkedUserInfo);
        log.debug("User linked to the reservation");

        reservation.setStatus(ReservationStatus.ONGOING);

        log.debug("Saving the reservation");
        Reservation newReservation = saveReservation(reservation);
        if (newReservation == null) throw new CRUDIssueException("Can't' create reservation");

        return newReservation;
    }

    @Transactional
    public void cancelReservation(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ProhibitedActionException("This reservation is already cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.FINISHED) {
            throw new ProhibitedActionException("Can't cancel a finished reservation");
        }

        reservationToCancelledLogic(reservation);
        Reservation newReservation = saveReservation(reservation);
        if (newReservation == null) {
            throw new CRUDIssueException("Can't update reservation");
        }
    }

    @Transactional
    public void validateReservation(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.FINISHED) {
            throw new ProhibitedActionException("This reservation is already finished");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ProhibitedActionException("Can't validate a cancelled reservation");
        }

        reservationToFinishedLogic(reservation);
        Reservation newReservation = saveReservation(reservation);
        if (newReservation == null) {
            throw new CRUDIssueException("Can't update reservation");
        }
    }

    @Transactional
    public void createLoanFromBookReservations(Book book) {
        if (!book.getOngoingReservations().isEmpty()) {
            Reservation reservation = book.getOngoingReservations().stream().min(Comparator.comparing(Reservation::getReservationStartDate)).orElseThrow(() -> new IllegalStateException("No min date has been found"));
            LoanCreateDTO loanCreateDTO = new LoanCreateDTO();
            loanCreateDTO.setUserId(reservation.getUserInfo().getId());
            loanCreateDTO.setBookId(book.getId());
            borrowService.saveANewLoan(loanCreateDTO);
            log.info("New loan on book " + book.getName() + " created for user " + loanCreateDTO.getUserId());
            validateReservation(reservation);
        } else {
            log.info("No reservations on book, no loan has been created");
        }
    }

    private void reservationToFinishedLogic(Reservation reservation) {
        reservation.setStatus(ReservationStatus.FINISHED);
        reservation.setReservationEndDate(new Date());
    }

    private void reservationToCancelledLogic(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setReservationEndDate(new Date());
    }
}

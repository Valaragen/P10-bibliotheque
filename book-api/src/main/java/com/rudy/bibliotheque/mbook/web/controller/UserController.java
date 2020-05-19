package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.dto.ReservationCreateDTO;
import com.rudy.bibliotheque.mbook.model.*;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.service.*;
import com.rudy.bibliotheque.mbook.util.Constant;
import com.rudy.bibliotheque.mbook.web.controller.util.ControllerUtil;
import com.rudy.bibliotheque.mbook.web.exception.CRUDIssueException;
import com.rudy.bibliotheque.mbook.web.exception.InvalidIdException;
import com.rudy.bibliotheque.mbook.web.exception.NotFoundException;
import com.rudy.bibliotheque.mbook.web.exception.ProhibitedActionException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constant.USERS_PATH)
public class UserController {

    private BookService bookService;
    private ReservationService reservationService;
    private BorrowService borrowService;
    private CopyService copyService;
    private UserInfoService userInfoService;

    @Autowired
    public UserController(BookService bookService, ReservationService reservationService, BorrowService borrowService, CopyService copyService, UserInfoService userInfoService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
        this.borrowService = borrowService;
        this.copyService = copyService;
        this.userInfoService = userInfoService;
    }

    /**
     * Return ongoing loans of the current user
     *
     * @return List of loans
     */
    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @GetMapping(Constant.CURRENT_PATH + Constant.LOANS_PATH)
    public List<Borrow> getLoansOfCurrentUser(@ModelAttribute("loanSearch") LoanSearch loanSearch) {
        loanSearch.setUserId(ControllerUtil.getUserIdFromToken());
        return borrowService.getLoansBySearch(loanSearch);
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PostMapping(Constant.CURRENT_PATH + Constant.LOANS_PATH)
    public ResponseEntity<Borrow> createLoanForCurrentUser(@RequestBody LoanCreateDTO loanCreateDTO) {
        log.info("Start method createLoanForCurrentUser");
        loanCreateDTO.setUserId(ControllerUtil.getUserIdFromToken());
        loanCreateDTO.setCode(null);

        Borrow newBorrow = borrowService.saveANewLoan(loanCreateDTO);

        log.info("Method ended");
        return new ResponseEntity<>(newBorrow, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PutMapping(Constant.CURRENT_PATH + Constant.LOANS_PATH + Constant.SLASH_ID_PATH + Constant.EXTEND_PATH)
    public ResponseEntity<Borrow> extendMyLoan(@PathVariable Long id) throws ParseException {
        String tokenSubjectId = ControllerUtil.getUserIdFromToken();
        Borrow currentLoan = borrowService.getLoanById(id);
        if(currentLoan == null) {
            throw new NotFoundException("No loan with id " + id + " has been found");
        }
        if (!tokenSubjectId.equals(currentLoan.getUserInfo().getId())) {
            log.warn("user with id " + tokenSubjectId + " tried to extend a loan he does not have");
            throw new ProhibitedActionException("Prohibited action");
        }
        if (currentLoan.isHasDurationExtended()) {
            throw new ProhibitedActionException("Can't extend duration twice");
        }
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();
        Date todayWithZeroTime = formatter.parse(formatter.format(today));
        if (currentLoan.getLoanEndDate().before(todayWithZeroTime)) {
            throw new ProhibitedActionException("Can't extend duration when the loan end date has passed");
        }

        long loanTimeInTimestamp = (currentLoan.getLoanEndDate().getTime() - currentLoan.getLoanStartDate().getTime());

        currentLoan.setLoanEndDate(new Date(currentLoan.getLoanEndDate().getTime() + loanTimeInTimestamp));
        currentLoan.setHasDurationExtended(true);
        Borrow newLoan = borrowService.saveLoan(currentLoan);
        if(newLoan == null) throw new CRUDIssueException("Can't update the loan");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PostMapping(Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH)
    public ResponseEntity<Reservation> createReservationForCurrentUser(@RequestBody ReservationCreateDTO reservationCreateDTO) {
        log.info("Start method createReservationForCurrentUser");
        reservationCreateDTO.setUserId(ControllerUtil.getUserIdFromToken());

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
        boolean hasAlreadyAReservation = linkedBook.getOngoingReservations().stream()
                .map(Reservation::getUserInfo).anyMatch(userInfo -> userInfo.getId().equals(getCurrentUserIdFromToken()));
        if (hasAlreadyAReservation) {
            throw new ProhibitedActionException("You can't reserve the same book twice");
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

        Reservation newReservation = reservationService.saveReservation(reservation);
        if (newReservation == null) throw new CRUDIssueException("Can't' create reservation");

        log.info("Method ended");
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
    }

    public static UserInfo getUserInfoFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        KeycloakAuthenticationToken kp = (KeycloakAuthenticationToken) authentication;
        AccessToken token = kp.getAccount().getKeycloakSecurityContext().getToken();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(token.getSubject());
        userInfo.setUsername(token.getPreferredUsername());
        userInfo.setEmail(token.getEmail());
        userInfo.setFirstName(token.getGivenName());
        userInfo.setLastName(token.getFamilyName());
        userInfo.setPhone(token.getPhoneNumber());
        return userInfo;
    }

    public static String getCurrentUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        KeycloakAuthenticationToken kp = (KeycloakAuthenticationToken) authentication;
        AccessToken token = kp.getAccount().getKeycloakSecurityContext().getToken();

        return token.getSubject();
    }
}

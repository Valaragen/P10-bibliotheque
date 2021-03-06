package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.dto.ReservationCreateDTO;
import com.rudy.bibliotheque.mbook.model.*;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.search.ReservationSearch;
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

    private ReservationService reservationService;
    private BorrowService borrowService;

    @Autowired
    public UserController(ReservationService reservationService, BorrowService borrowService) {
        this.reservationService = reservationService;
        this.borrowService = borrowService;
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

        if(loanCreateDTO.getBookId() == null) {
            throw new InvalidIdException("A book id need to be provided");
        }
        if(borrowService.isUserBorrowingBook(loanCreateDTO.getUserId(), loanCreateDTO.getBookId())) {
            throw new ProhibitedActionException("You can't borrow the same book twice");
        }

        Borrow newBorrow = borrowService.saveANewLoan(loanCreateDTO);

        log.info("Method ended");
        return new ResponseEntity<>(newBorrow, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PutMapping(Constant.CURRENT_PATH + Constant.LOANS_PATH + Constant.SLASH_ID_PATH + Constant.EXTEND_PATH)
    public ResponseEntity<Borrow> extendMyLoan(@PathVariable Long id) throws ParseException {
        String tokenSubjectId = ControllerUtil.getUserIdFromToken();
        Borrow currentLoan = borrowService.getLoanById(id);
        if (currentLoan == null) {
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
    @GetMapping(Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH)
    public List<Reservation> getReservationsOfCurrentUser(@ModelAttribute("loanSearch") ReservationSearch reservationSearch) {
        reservationSearch.setUserId(ControllerUtil.getUserIdFromToken());
        return reservationService.getAllReservationsBySearch(reservationSearch);
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PostMapping(Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH)
    public ResponseEntity<Reservation> createReservationForCurrentUser(@RequestBody ReservationCreateDTO reservationCreateDTO) {
        log.info("Start method createReservationForCurrentUser");
        reservationCreateDTO.setUserId(ControllerUtil.getUserIdFromToken());

        Reservation newReservation = reservationService.createNewReservation(reservationCreateDTO);

        log.info("Method ended");
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PutMapping(Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH + Constant.SLASH_ID_PATH + Constant.CANCEL_PATH)
    public ResponseEntity<Reservation> cancelMyReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            throw new NotFoundException("Can't find reservation with id + " + id);
        }
        if (!reservation.getUserInfo().getId().equals(ControllerUtil.getUserIdFromToken())) {
            throw new ProhibitedActionException("You can't cancel a reservation you don't own");
        }
        reservationService.cancelReservation(reservation);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
}

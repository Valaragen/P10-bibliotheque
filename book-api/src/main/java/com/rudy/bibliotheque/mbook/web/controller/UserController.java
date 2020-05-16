package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.model.Copy;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.service.BorrowService;
import com.rudy.bibliotheque.mbook.service.CopyService;
import com.rudy.bibliotheque.mbook.service.UserInfoService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constant.USERS_PATH)
public class UserController {

    private BorrowService borrowService;
    private CopyService copyService;
    private UserInfoService userInfoService;

    @Autowired
    public UserController(BorrowService borrowService, CopyService copyService, UserInfoService userInfoService) {
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

        if (loanCreateDTO.getBookId() == null) {
            throw new InvalidIdException("Book code has not been provided");
        }
        //link the copy
        Copy linkedCopy = copyService.getAnAvailableCopyByBookId(loanCreateDTO.getBookId());
        if (linkedCopy == null) {
            throw new NotFoundException("No copy available for book with id " + loanCreateDTO.getBookId());
        }

        Borrow borrow = new Borrow();
        borrow.setCopy(linkedCopy);
        log.debug("Copy linked to the loan");
        UserInfo linkedUserInfo = userInfoService.getUserInfoById(loanCreateDTO.getUserId());
        if (linkedUserInfo == null) {
            //link the userInfos
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            KeycloakAuthenticationToken kp = (KeycloakAuthenticationToken) authentication;
            AccessToken token = kp.getAccount().getKeycloakSecurityContext().getToken();

            linkedUserInfo = new UserInfo();
            linkedUserInfo.setId(token.getSubject());
            linkedUserInfo.setUsername(token.getPreferredUsername());
            linkedUserInfo.setEmail(token.getEmail());
            linkedUserInfo.setFirstName(token.getGivenName());
            linkedUserInfo.setLastName(token.getFamilyName());
            linkedUserInfo.setPhone(token.getPhoneNumber());
            linkedUserInfo = userInfoService.saveUserInfo(linkedUserInfo);
            if (linkedUserInfo == null) {
                throw new CRUDIssueException("Can't create user_info entity");
            }
        }

        borrow.setUserInfo(linkedUserInfo);
        log.debug("User linked to the loan");

        LoanController.loanToPendingLogic(borrow);

        Borrow newBorrow = borrowService.saveLoan(borrow);
        if (newBorrow == null) throw new CRUDIssueException("Can't' create loan");

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
}

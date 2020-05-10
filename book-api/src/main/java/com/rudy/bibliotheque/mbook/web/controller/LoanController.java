package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.config.ApplicationPropertiesConfig;
import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.model.Copy;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.service.BorrowService;
import com.rudy.bibliotheque.mbook.service.CopyService;
import com.rudy.bibliotheque.mbook.service.UserInfoService;
import com.rudy.bibliotheque.mbook.util.Constant;
import com.rudy.bibliotheque.mbook.web.exception.CRUDIssueException;
import com.rudy.bibliotheque.mbook.web.exception.InvalidIdException;
import com.rudy.bibliotheque.mbook.web.exception.NotFoundException;
import com.rudy.bibliotheque.mbook.web.exception.ProhibitedActionException;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(Constant.LOANS_PATH)
public class LoanController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ApplicationPropertiesConfig appProperties;
    private BorrowService borrowService;
    private CopyService copyService;
    private UserInfoService userInfoService;
    private KeycloakRestTemplate keycloakRestTemplate;

    @Autowired
    public LoanController(BorrowService borrowService, CopyService copyService, UserInfoService userInfoService, ApplicationPropertiesConfig appProperties, KeycloakRestTemplate keycloakRestTemplate) {
        this.borrowService = borrowService;
        this.copyService = copyService;
        this.appProperties = appProperties;
        this.keycloakRestTemplate = keycloakRestTemplate;
        this.userInfoService = userInfoService;
    }

    /**
     * Get all the loans from the database
     * @return List of loans that contains useful informations about the related book and user
     */
    @PreAuthorize("hasRole('"+ Constant.STAFF_ROLE_NAME +"')")
    @GetMapping
    public List<Borrow> getAllLoans(@ModelAttribute("loanSearch") LoanSearch loanSearch){
        return borrowService.getLoansBySearch(loanSearch);
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PostMapping
    public ResponseEntity<Borrow> saveALoan(@RequestBody LoanCreateDTO loanCreateDTO) {
        log.info("Start method saveALoan");
        if (loanCreateDTO.getUserId() == null) {
            throw new InvalidIdException("User id has not been provided");
        }

        //link the copy
        if (loanCreateDTO.getBookId() == null && loanCreateDTO.getCode() == null) {
            throw new InvalidIdException("At least a book id or a copy code need to be provided");
        }
        Copy linkedCopy;
        if (loanCreateDTO.getCode() != null) {
            linkedCopy = copyService.getCopyById(loanCreateDTO.getCode());
            if (linkedCopy == null) {
                throw new NotFoundException("No copy available for book with id " + loanCreateDTO.getBookId());
            }
            if (linkedCopy.isBorrowed()) {
                throw new ProhibitedActionException("Copy with id " + loanCreateDTO.getCode() + " is already borrowed");
            }
        } else {
            linkedCopy = copyService.getAnAvailableCopyByBookId(loanCreateDTO.getBookId());
            if (linkedCopy == null) {
                throw new NotFoundException("No copy available for book with id " + loanCreateDTO.getBookId());
            }
        }

        Borrow borrow = new Borrow();
        borrow.setCopy(linkedCopy);
        log.debug("Copy linked to the loan");

        //Link the userInfos
        UserInfo linkedUserInfo = userInfoService.getUserInfoById(loanCreateDTO.getUserId());
        if (linkedUserInfo == null) {
            ResponseEntity<UserInfo> response = keycloakRestTemplate.getForEntity("http://localhost:8080/auth/admin/realms/bibliotheque/users/" + loanCreateDTO.getUserId(), UserInfo.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                if(response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new NotFoundException("Can't find user with id " + loanCreateDTO.getUserId());
                }
                throw new CRUDIssueException("Can't get userInfos from server");
            }
        }
        borrow.setUserInfo(linkedUserInfo);
        log.debug("User linked to the loan");

        log.debug("Update mandatory fields");

        loanToPendingLogic(borrow);

        Borrow newBorrow = borrowService.saveLoan(borrow);
        if (newBorrow == null) throw new CRUDIssueException("Can't' create loan");

        log.info("Method ended");
        return new ResponseEntity<>(newBorrow, HttpStatus.CREATED);
    }

    /**
     * Get all the loans from the database
     * @return List of loans that contains useful informations about the related book and user
     */
    @PreAuthorize("hasRole('"+ Constant.STAFF_ROLE_NAME +"')")
    @PutMapping(Constant.SLASH_ID_PATH + Constant.VALIDATE_PATH)
    public ResponseEntity<Borrow> validateALoan(@PathVariable Long id) {
        Borrow borrow = borrowService.getLoanById(id);
        if (borrow == null) {
            throw new NotFoundException("Can't find loan with id " + id);
        }
        if (borrow.getLoanStartDate() != null) {
            throw new ProhibitedActionException("This loan is already validated");
        }

        loanToOngoingLogic(borrow, appProperties.getLoanTimeInDays());

        Borrow newBorrow = borrowService.saveLoan(borrow);

        if (newBorrow == null) throw new CRUDIssueException("Can't' update loan");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get all the loans from the database
     * @return List of loans that contains useful informations about the related book and user
     */
    @PreAuthorize("hasRole('"+ Constant.STAFF_ROLE_NAME +"')")
    @PutMapping(Constant.SLASH_ID_PATH + Constant.RETURNED_PATH)
    public ResponseEntity<Borrow> saveALoanReturn(@PathVariable Long id, @RequestBody Borrow borrowAdditionalInfos) {
        Borrow borrow = borrowService.getLoanById(id);
        if (borrow == null) {
            throw new NotFoundException("Can't find loan with id " + id);
        }
        if (borrow.getLoanStartDate() == null) {
            throw new ProhibitedActionException("This loan is in pending state");
        }
        if (borrow.getReturnedOn() != null) {
            throw new ProhibitedActionException("This loan is already returned");
        }

        if (borrowAdditionalInfos.getStateAfterBorrow() != null) {
            borrow.setStateAfterBorrow(borrowAdditionalInfos.getStateAfterBorrow());
        } else {
            borrow.setStateAfterBorrow(borrow.getStateBeforeBorrow());
        }

        loanToReturnedLogic(borrow);

        Borrow newBorrow = borrowService.saveLoan(borrow);

        if (newBorrow == null) throw new CRUDIssueException("Can't' update loan");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static void loanToPendingLogic(Borrow borrow) {
        borrow.getCopy().setBorrowed(true);
        borrow.getCopy().getBook().setAvailableCopyNumber(borrow.getCopy().getBook().getAvailableCopyNumber() - 1);

        Date today = new Date();
        borrow.setStateBeforeBorrow(borrow.getCopy().getCurrentState());
        borrow.setLoanRequestDate(today);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 2);
        borrow.setDeadlineToRetrieve(calendar.getTime());
    }

    public static void loanToOngoingLogic(Borrow borrow , Integer loanTimeInDays) {
        Date today = new Date();
        borrow.setLoanStartDate(today);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, loanTimeInDays);
        borrow.setLoanEndDate(calendar.getTime());
    }
    public static void loanToReturnedLogic(Borrow borrow) {
        borrow.getCopy().setBorrowed(false);
        borrow.getCopy().getBook().setAvailableCopyNumber(borrow.getCopy().getBook().getAvailableCopyNumber()+1);

        Date today = new Date();
        borrow.setReturnedOn(today);
    }

}

package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.config.ApplicationPropertiesConfig;
import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.model.*;
import com.rudy.bibliotheque.mbook.repository.BorrowRepository;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.util.Constant;
import com.rudy.bibliotheque.mbook.web.controller.UserController;
import com.rudy.bibliotheque.mbook.web.controller.util.ControllerUtil;
import com.rudy.bibliotheque.mbook.web.exception.*;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BorrowService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ReservationService reservationService;

    private CopyService copyService;
    private UserInfoService userInfoService;
    private ApplicationPropertiesConfig appProperties;
    private KeycloakRestTemplate keycloakRestTemplate;
    private BorrowRepository borrowRepository;
    private EmailService emailService;

    @Autowired
    public BorrowService(CopyService copyService, UserInfoService userInfoService, ApplicationPropertiesConfig appProperties, KeycloakRestTemplate keycloakRestTemplate, BorrowRepository borrowRepository, EmailService emailService) {
        this.copyService = copyService;
        this.appProperties = appProperties;
        this.keycloakRestTemplate = keycloakRestTemplate;
        this.userInfoService = userInfoService;
        this.borrowRepository = borrowRepository;
        this.emailService = emailService;
    }


    public List<Borrow> getAllLoans() {
        return borrowRepository.findAll();
    }

    public List<Borrow> getLoansBySearch(LoanSearch loanSearch) {
        return borrowRepository.findAllBySearch(loanSearch);
    }

    public Borrow getLoanById(Long id) {
        return borrowRepository.findById(id).orElse(null);
    }

    public Borrow saveLoan(Borrow borrow) {
        return borrowRepository.save(borrow);
    }

    public void deleteLoanById(Long id) {
        borrowRepository.deleteById(id);
    }

    @Transactional
    public Borrow saveANewLoan(LoanCreateDTO loanCreateDTO) {
        log.info("Start method saveANewLoan");
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
                throw new NotFoundException("Can't find copy " + loanCreateDTO.getBookId());
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
            if (!loanCreateDTO.getUserId().equals(ControllerUtil.getUserIdFromToken())) {
                ResponseEntity<UserInfo> response = keycloakRestTemplate.getForEntity("http://localhost:8080/auth/admin/realms/bibliotheque/users/" + loanCreateDTO.getUserId(), UserInfo.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new NotFoundException("Can't find user with id " + loanCreateDTO.getUserId());
                    }
                    throw new CRUDIssueException("Can't get userInfos from server");
                }
            } else {
                //Create the userInfo in database
                linkedUserInfo = UserController.getUserInfoFromToken();
                linkedUserInfo = userInfoService.saveUserInfo(linkedUserInfo);
                if (linkedUserInfo == null) {
                    throw new CRUDIssueException("Can't create user_info entity");
                }
            }
        }
        borrow.setUserInfo(linkedUserInfo);
        log.debug("User linked to the loan");

        log.debug("Update mandatory fields");

        loanToPendingLogic(borrow);

        Borrow newBorrow = saveLoan(borrow);
        if (newBorrow == null) throw new CRUDIssueException("Can't create loan");

        try {
            emailService.sendLoanInPendingEmail(newBorrow);
        } catch (MessagingException e) {
            throw new EmailSendException("Can't send Email");
        }

        return newBorrow;
    }

    public void validateALoan(Long id) {
        Borrow borrow = getLoanById(id);
        if (borrow == null) {
            throw new NotFoundException("Can't find loan with id " + id);
        }
        if (borrow.getLoanStartDate() != null) {
            throw new ProhibitedActionException("This loan is already validated");
        }

        loanToOngoingLogic(borrow, appProperties.getLoanTimeInDays());

        Borrow newBorrow = saveLoan(borrow);
        if (newBorrow == null) throw new CRUDIssueException("Can't' update loan");
    }

    @Transactional
    public void saveALoanReturn(Long id, Borrow borrowAdditionalInfos) {
        Borrow borrow = getLoanById(id);
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

        Borrow newBorrow = saveLoan(borrow);
        if (newBorrow == null) throw new CRUDIssueException("Can't update loan");

        reservationService.createLoanFromBookReservations(newBorrow.getCopy().getBook());
    }

    @Transactional
    public void cancelLoan(Borrow borrow) {
        if (borrow.getLoanStartDate() != null) {
            throw new ProhibitedActionException("Can't cancel an ongoing loan");
        }
        borrow.getCopy().setBorrowed(false);
        borrow.getCopy().getBook().setAvailableCopyNumber(borrow.getCopy().getBook().getAvailableCopyNumber() + 1);
        deleteLoanById(borrow.getId());
        if (getLoanById(borrow.getId()) != null) throw new CRUDIssueException("Can't delete the book with id " + borrow.getId());
        reservationService.createLoanFromBookReservations(borrow.getCopy().getBook());
    }

    private void loanToPendingLogic(Borrow borrow) {
        borrow.getCopy().setBorrowed(true);
        borrow.getCopy().getBook().setAvailableCopyNumber(borrow.getCopy().getBook().getAvailableCopyNumber() - 1);

        Date today = new Date();
        borrow.setStateBeforeBorrow(borrow.getCopy().getCurrentState());
        borrow.setLoanRequestDate(today);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        int timeGivenToRetrieveInDays = 2;
        int addedDays = 0;
        while (addedDays < timeGivenToRetrieveInDays) {
            calendar.add(Calendar.DATE, 1);
            if (calendar.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY && calendar.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY) {
                addedDays++;
            }
        }
        borrow.setDeadlineToRetrieve(calendar.getTime());
    }

    private void loanToOngoingLogic(Borrow borrow, Integer loanTimeInDays) {
        Date today = new Date();
        borrow.setLoanStartDate(today);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, loanTimeInDays);
        while (calendar.get(Calendar.DAY_OF_WEEK) < Calendar.MONDAY || calendar.get(Calendar.DAY_OF_WEEK) > Calendar.FRIDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        borrow.setLoanEndDate(calendar.getTime());
    }

    private void loanToReturnedLogic(Borrow borrow) {
        borrow.getCopy().setBorrowed(false);
        borrow.getCopy().getBook().setAvailableCopyNumber(borrow.getCopy().getBook().getAvailableCopyNumber() + 1);

        Date today = new Date();
        borrow.setReturnedOn(today);
    }

    public boolean isUserBorrowingBook(String userId, Long bookId) {
        //Check if the user is currently borrowing this book
        LoanSearch loanSearch = new LoanSearch();
        loanSearch.setUserId(userId);
        Set<String> status = new HashSet<>();
        status.add(Constant.STATUS_PENDING);
        status.add(Constant.STATUS_ONGOING);
        status.add(Constant.STATUS_LATE);
        loanSearch.setStatus(status);
        List<Borrow> ongoingBorrowsOfUser = getLoansBySearch(loanSearch);
        if (ongoingBorrowsOfUser != null && !ongoingBorrowsOfUser.isEmpty()) {
            List<Long> ongoingBorrowedBookId = ongoingBorrowsOfUser.stream().map((borrow) -> borrow.getCopy().getBook().getId()).collect(Collectors.toList());
            return ongoingBorrowedBookId.contains(bookId);
        }
        return false;
    }

}

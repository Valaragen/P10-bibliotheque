package com.rudy.bibliotheque.webui.controller;

import com.rudy.bibliotheque.webui.dto.BorrowDTO;
import com.rudy.bibliotheque.webui.dto.LoanCreateDTO;
import com.rudy.bibliotheque.webui.dto.search.LoanSearchDTO;
import com.rudy.bibliotheque.webui.proxies.BookApiProxy;
import com.rudy.bibliotheque.webui.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping(Constant.LOANS_PATH)
public class LoanController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private BookApiProxy bookApiProxy;

    @Autowired
    public LoanController(BookApiProxy bookApiProxy){
        this.bookApiProxy = bookApiProxy;
    }

    @PreAuthorize("hasRole('"+ Constant.STAFF_ROLE_NAME +"')")
    @GetMapping
    public String getLoansPage(@ModelAttribute("loanSearch") LoanSearchDTO loanSearchDTO, Model model) {
        loadLoansInModel(loanSearchDTO, model);
        return Constant.LOANS_LIST_PAGE;
    }

    @PreAuthorize("hasRole('"+ Constant.USER_ROLE_NAME +"')")
    @GetMapping(Constant.MY_LOANS_PATH)
    public String getUserLoans(@ModelAttribute("loanSearch") LoanSearchDTO loanSearchDTO, Model model) {
        //Reset the status as the user can't define one on this page
        loanSearchDTO.setStatus(new HashSet<>());
        loadLoansInModel(loanSearchDTO, model);
        return Constant.LOANS_USER_PAGE;
    }

    @PreAuthorize("hasRole('"+ Constant.USER_ROLE_NAME +"')")
    @PostMapping(Constant.MY_LOANS_PATH)
    public String submitLoanForCurrentUser(@ModelAttribute("borrow") LoanCreateDTO loanCreateDTO) {
        ResponseEntity<BorrowDTO> newBorrow = bookApiProxy.createLoanForCurrentUser(loanCreateDTO);

        if (newBorrow.getStatusCode() != HttpStatus.CREATED) {
            //TODO add logic
            log.error("Can't create loan");
        }

        return Constant.REDIRECT + Constant.LOANS_PATH + Constant.MY_LOANS_PATH;
    }

    @PreAuthorize("hasRole('"+ Constant.USER_ROLE_NAME +"')")
    @PostMapping(Constant.SLASH_ID_PATH + Constant.EXTEND_PATH)
    public String extendMyBorrow(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        ResponseEntity<BorrowDTO> newBorrowDTO = bookApiProxy.extendMyLoan(id);

        if (newBorrowDTO.getStatusCode() != HttpStatus.NO_CONTENT) {
            //TODO add logic notification
            log.error("Can't extend borrow duration");
        }

        return Constant.REDIRECT + httpServletRequest.getHeader("referer");
    }

    private void loadLoansInModel(LoanSearchDTO loanSearchDTO, Model model) {
        List<BorrowDTO> loans = bookApiProxy.getLoansOfCurrentUser(loanSearchDTO);
        List<BorrowDTO> pendingLoans = new ArrayList<>();
        List<BorrowDTO> ongoingLoans = new ArrayList<>();
        List<BorrowDTO> lateLoans = new ArrayList<>();
        List<BorrowDTO> finishedLoans = new ArrayList<>();
        for (BorrowDTO loan: loans) {
            if (loan.getLoanStartDate() == null) {
                pendingLoans.add(loan);
            } else {
                if (loan.getReturnedOn() == null) {
                    ongoingLoans.add(loan);
                    Date today = new Date();
                    if (loan.getLoanEndDate().before(today)) {
                        lateLoans.add(loan);
                    }
                } else {
                    finishedLoans.add(loan);
                }
            }
        }

        model.addAttribute("pendingLoans", pendingLoans);
        model.addAttribute("ongoingLoans", ongoingLoans);
        model.addAttribute("lateLoans", lateLoans);
        model.addAttribute("finishedLoans", finishedLoans);
    }
}

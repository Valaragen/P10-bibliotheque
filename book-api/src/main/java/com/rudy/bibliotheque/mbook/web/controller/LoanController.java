package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.service.BorrowService;
import com.rudy.bibliotheque.mbook.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constant.LOANS_PATH)
public class LoanController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private BorrowService borrowService;

    @Autowired
    public LoanController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * Get all the loans from the database
     *
     * @return List of loans that contains useful informations about the related book and user
     */
    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @GetMapping
    public List<Borrow> getAllLoans(@ModelAttribute("loanSearch") LoanSearch loanSearch) {
        return borrowService.getLoansBySearch(loanSearch);
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PostMapping
    public ResponseEntity<Borrow> saveALoan(@RequestBody LoanCreateDTO loanCreateDTO) {
        Borrow newBorrow = borrowService.saveANewLoan(loanCreateDTO);

        log.info("Method ended");
        return new ResponseEntity<>(newBorrow, HttpStatus.CREATED);
    }

    /**
     * Get all the loans from the database
     *
     * @return List of loans that contains useful informations about the related book and user
     */
    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PutMapping(Constant.SLASH_ID_PATH + Constant.VALIDATE_PATH)
    public ResponseEntity<Borrow> validateALoan(@PathVariable Long id) {
        borrowService.validateALoan(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get all the loans from the database
     *
     * @return List of loans that contains useful informations about the related book and user
     */
    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PutMapping(Constant.SLASH_ID_PATH + Constant.RETURNED_PATH)
    public ResponseEntity<Borrow> saveALoanReturn(@PathVariable Long id, @RequestBody Borrow borrowAdditionalInfos) {
        borrowService.saveALoanReturn(id, borrowAdditionalInfos);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PutMapping(Constant.SLASH_ID_PATH + Constant.CANCEL_PATH)
    public ResponseEntity<Borrow> cancelLoan(@PathVariable Long id) {
        Borrow borrow = borrowService.getLoanById(id);
        borrowService.cancelLoan(borrow);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

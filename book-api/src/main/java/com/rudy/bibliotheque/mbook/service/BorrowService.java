package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.repository.BorrowRepository;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BorrowService {

    private BorrowRepository borrowRepository;

    @Autowired
    public BorrowService(BorrowRepository borrowRepository) {
        this.borrowRepository = borrowRepository;
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

}

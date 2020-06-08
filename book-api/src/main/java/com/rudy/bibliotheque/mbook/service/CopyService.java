package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.model.Copy;
import com.rudy.bibliotheque.mbook.repository.CopyRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class CopyService {

    private CopyRepository copyRepository;

    public CopyService(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    public List<Copy> getAllCopies() {
        return copyRepository.findAll();
    }

    public Copy getCopyById(String code) {
        return copyRepository.findById(code).orElse(null);
    }

    public Copy saveCopy(Copy copy) {
        return copyRepository.save(copy);
    }

    public String generateCode() {
        Timestamp now = new Timestamp(System.nanoTime());
        Timestamp now2 = new Timestamp(System.currentTimeMillis());
        return Long.toHexString(now.getTime() + now2.getTime());
    }

    public Copy getAnAvailableCopyByBookId(Long id) {
        return copyRepository.findFirstByBookIdAndBorrowedIsFalse(id).orElse(null);
    }
}

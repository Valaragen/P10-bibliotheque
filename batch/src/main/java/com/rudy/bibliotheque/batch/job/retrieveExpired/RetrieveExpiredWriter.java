package com.rudy.bibliotheque.batch.job.retrieveExpired;

import com.rudy.bibliotheque.batch.dto.BorrowDTO;
import com.rudy.bibliotheque.batch.proxy.BookApiProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Slf4j
public class RetrieveExpiredWriter implements ItemWriter<BorrowDTO> {

    @Autowired
    private BookApiProxy bookApiProxy;

    @Override
    public void write(List<? extends BorrowDTO> expiredBorrowList) {
        expiredBorrowList.forEach((borrow) -> {
            ResponseEntity<BorrowDTO> borrowDTOResponseEntity = bookApiProxy.cancelLoan(borrow.getId());
            if (borrowDTOResponseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                log.info("Borrow with id " + borrow.getId() + " has been cancelled");
            } else {
                log.info("Can't cancel the borrow with id " + borrow.getId());
            }
        });
    }
}
package com.rudy.bibliotheque.batch.job.retrieveExpired;

import com.rudy.bibliotheque.batch.dto.BorrowDTO;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;

/**
 * Class used to process objects
 */
public class RetrieveExpiredProcessor implements ItemProcessor<BorrowDTO, BorrowDTO> {

    @Override
    public BorrowDTO process(BorrowDTO borrowDTO) {
        if (borrowDTO.getDeadlineToRetrieve().before(new Date())) {
            return borrowDTO;
        }
        return null;
    }

}

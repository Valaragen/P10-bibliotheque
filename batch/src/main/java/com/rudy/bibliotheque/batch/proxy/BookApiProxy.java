package com.rudy.bibliotheque.batch.proxy;

import com.rudy.bibliotheque.batch.dto.BorrowDTO;
import com.rudy.bibliotheque.batch.dto.search.LoanSearchDTO;
import com.rudy.bibliotheque.batch.util.Constant;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "zuul-server")
@RibbonClient(name = "book-api")
@RequestMapping(Constant.BOOK_API_PATH)
public interface BookApiProxy {

    @GetMapping(Constant.LOANS_PATH)
    List<BorrowDTO> getAllLoans(@SpringQueryMap LoanSearchDTO loanSearchDTO);

    @PutMapping(Constant.LOANS_PATH + Constant.SLASH_ID_PATH + Constant.CANCEL_PATH)
    ResponseEntity<BorrowDTO> cancelLoan(@PathVariable("id") Long id);

}

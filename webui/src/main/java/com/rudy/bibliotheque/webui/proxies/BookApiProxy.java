package com.rudy.bibliotheque.webui.proxies;

import com.rudy.bibliotheque.webui.dto.*;
import com.rudy.bibliotheque.webui.dto.search.BookSearchDTO;
import com.rudy.bibliotheque.webui.dto.search.LoanSearchDTO;
import com.rudy.bibliotheque.webui.dto.search.ReservationSearchDTO;
import com.rudy.bibliotheque.webui.util.Constant;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "zuul-server")
@RibbonClient(name = "book-api")
@RequestMapping(Constant.BOOK_API_PATH)
public interface BookApiProxy {

    @GetMapping(Constant.BOOKS_PATH)
    List<BookDTO> getAllBooks(@SpringQueryMap BookSearchDTO bookSearchDTO);

    @GetMapping(Constant.LOANS_PATH)
    List<BorrowDTO> getAllLoans(@SpringQueryMap LoanSearchDTO loanSearchDTO);

    @GetMapping(Constant.USERS_PATH + Constant.CURRENT_PATH + Constant.LOANS_PATH)
    List<BorrowDTO> getLoansOfCurrentUser(@SpringQueryMap LoanSearchDTO loanSearchDTO);

    @PostMapping(Constant.USERS_PATH + Constant.CURRENT_PATH + Constant.LOANS_PATH)
    ResponseEntity<BorrowDTO> createLoanForCurrentUser(@RequestBody LoanCreateDTO loanCreateDTO);

    @PostMapping(Constant.BOOKS_PATH)
    ResponseEntity<BookDTO> saveBookInDatabase(@RequestBody BookDTO bookDTO);

    @GetMapping(Constant.BOOKS_PATH + Constant.SLASH_ID_PATH)
    BookDTO getBookById(@PathVariable("id") Long id);

    @PutMapping(Constant.BOOKS_PATH + Constant.SLASH_ID_PATH)
    ResponseEntity<BookDTO> updateBook(@PathVariable("id") Long id, @RequestBody BookDTO bookDTO);

    @DeleteMapping(Constant.BOOKS_PATH + Constant.SLASH_ID_PATH)
    ResponseEntity<BookDTO> deleteBook(@PathVariable("id") Long id);

    @PutMapping(Constant.USERS_PATH + Constant.CURRENT_PATH + Constant.LOANS_PATH + Constant.SLASH_ID_PATH + Constant.EXTEND_PATH)
    ResponseEntity<BorrowDTO> extendMyLoan(@PathVariable Long id);

    @GetMapping(Constant.USERS_PATH + Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH)
    List<ReservationDTO> getReservationsOfCurrentUser(@SpringQueryMap ReservationSearchDTO reservationSearchDTO);

    @PostMapping(Constant.USERS_PATH + Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH)
    ResponseEntity<ReservationDTO> createReservationForCurrentUser(@RequestBody ReservationCreateDTO reservationCreateDTO);

    @PutMapping(Constant.USERS_PATH + Constant.CURRENT_PATH + Constant.RESERVATIONS_PATH + Constant.SLASH_ID_PATH + Constant.CANCEL_PATH)
    ResponseEntity<ReservationDTO> cancelMyReservation(@PathVariable("id") Long id);

}

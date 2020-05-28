package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.config.ApplicationPropertiesConfig;
import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.search.BookSearch;
import com.rudy.bibliotheque.mbook.service.BookService;
import com.rudy.bibliotheque.mbook.util.Constant;
import com.rudy.bibliotheque.mbook.util.NullAwareBeanUtilsBean;
import com.rudy.bibliotheque.mbook.web.exception.CRUDIssueException;
import com.rudy.bibliotheque.mbook.web.exception.NotFoundException;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for the api
 * It contains all the API endpoints
 */
@RestController
@RequestMapping(Constant.BOOKS_PATH)
public class BookController {

    private BookService bookService;
    private ApplicationPropertiesConfig applicationPropertiesConfig;

    @Autowired
    public BookController(BookService bookService, ApplicationPropertiesConfig applicationPropertiesConfig) {
        this.bookService = bookService;
        this.applicationPropertiesConfig = applicationPropertiesConfig;
    }

    /**
     * Get all the books from the database
     * @return List of books from the database
     */
    @GetMapping
    public List<Book> getAllBooks(BookSearch bookSearch) {
        return bookService.getBooksBySearch(bookSearch);
    }

    /**
     * Save a new book in the database
     * @return the new saved book
     */
    @PostMapping
    public ResponseEntity<Book> saveBookInDatabase(@RequestBody Book book) {
        book.setAvailableCopyNumber(0);
        book.setCopyNumber(0);
        Book newBook = bookService.saveBook(book);
        if (newBook == null) throw new CRUDIssueException("Can't create the book");
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }

    /**
     * Get the book with the id given in the path
     * @param id the id of the book
     * @return the book with the given id
     */
    @GetMapping(Constant.SLASH_ID_PATH)
    public Book getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        if(book == null) throw new NotFoundException();
        return book;
    }

    @PutMapping(Constant.SLASH_ID_PATH)
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) throws InvocationTargetException, IllegalAccessException {
        Book currentBook = bookService.getBookById(id);
        BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
        notNull.copyProperties(currentBook, book);

        Book newBook = bookService.saveBook(currentBook);
        if (newBook == null) throw new CRUDIssueException("Can't update the book");
        return new ResponseEntity<>(newBook, HttpStatus.OK);
    }

    @DeleteMapping(Constant.SLASH_ID_PATH)
    public ResponseEntity<Book> deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        if (bookService.getBookById(id) != null) throw new CRUDIssueException("Can't delete the book");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

package com.rudy.bibliotheque.webui.controller;

import com.rudy.bibliotheque.webui.dto.BookDTO;
import com.rudy.bibliotheque.webui.dto.search.BookSearchDTO;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Objects;

@Controller
@RequestMapping(Constant.BOOKS_PATH)
public class BookController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private BookApiProxy bookApiProxy;

    @Autowired
    public BookController(BookApiProxy bookApiProxy){
        this.bookApiProxy = bookApiProxy;
    }

    @GetMapping
    public String getBooksPage(@ModelAttribute("bookSearch") BookSearchDTO bookSearchDTO, Model model) {
        if(!model.containsAttribute("bookSearch")) {
            model.addAttribute("bookSearch", bookSearchDTO);
        }
        model.addAttribute("books", bookApiProxy.getAllBooks(bookSearchDTO));
        return Constant.BOOKS_LIST_PAGE;
    }

    @GetMapping(Constant.SLASH_ID_PATH)
    public String getBookDetailsPageById(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookApiProxy.getBookById(id));
        return Constant.BOOK_DETAILS_PAGE;
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @GetMapping(Constant.ADD_PATH)
    public String getAddBookForm(Model model) {
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", new BookDTO());
        }
        return Constant.BOOK_ADD_PAGE;
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PostMapping
    public String submitBookAdd(@Valid @ModelAttribute("book") BookDTO bookDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.book", bindingResult);
            redirectAttributes.addFlashAttribute("book", bookDTO);
            return Constant.REDIRECT + Constant.BOOKS_PATH + Constant.ADD_PATH;
        }
        ResponseEntity<BookDTO> newBookDTO = bookApiProxy.saveBookInDatabase(bookDTO);

        if (newBookDTO.getStatusCode() != HttpStatus.CREATED) {
            //TODO add logic
            log.error("Cant create book");
        }

        return Constant.REDIRECT + Constant.BOOKS_PATH + Constant.SLASH + Objects.requireNonNull(newBookDTO.getBody()).getId();
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @GetMapping(Constant.SLASH_ID_PATH + Constant.MODIFY_PATH)
    public String getBookModifyForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookApiProxy.getBookById(id));
        return Constant.BOOK_MODIFY_PAGE;
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PostMapping(Constant.SLASH_ID_PATH)
    public String submitBookModify(@PathVariable Long id, @Valid @ModelAttribute("book") BookDTO bookDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.book", bindingResult);
            redirectAttributes.addFlashAttribute("book", bookDTO);
            return Constant.REDIRECT + Constant.BOOKS_PATH + id + Constant.MODIFY_PATH;
        }

        ResponseEntity<BookDTO> newBookDTO = bookApiProxy.updateBook(id, bookDTO);

        if (newBookDTO.getStatusCode() != HttpStatus.OK) {
            //TODO add logic
            log.error("Update has failed");
        }

        return Constant.REDIRECT + Constant.BOOKS_PATH + Constant.SLASH + id;
    }

    @PreAuthorize("hasRole('" + Constant.STAFF_ROLE_NAME + "')")
    @PostMapping(Constant.SLASH_ID_PATH + Constant.DELETE_PATH)
    public String submitBookDelete(@PathVariable Long id) {

        ResponseEntity<BookDTO> newBookDTO = bookApiProxy.deleteBook(id);

        if (newBookDTO.getStatusCode() != HttpStatus.NO_CONTENT) {
            //TODO add logic message on layout
            log.error("Delete has failed");
        }

        return Constant.REDIRECT + Constant.BOOKS_PATH;
    }
}

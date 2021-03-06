package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.repository.BookRepository;
import com.rudy.bibliotheque.mbook.search.BookSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Find all Book Entity in the database
     * @return All Book entity found in the database as List
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getBooksBySearch(BookSearch bookSearch) {
        return bookRepository.findAllBySearch(bookSearch);
    }

    /**
     * Find a Book by id in the database
     * @param id id of the book to find
     * @return Book found in the database
     */
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public Book saveBook(Book book){
        return bookRepository.save(book);
    }

    public void deleteBook(Book book){
        bookRepository.delete(book);
    }

    public void deleteBookById(Long id){
        bookRepository.deleteById(id);
    }

}

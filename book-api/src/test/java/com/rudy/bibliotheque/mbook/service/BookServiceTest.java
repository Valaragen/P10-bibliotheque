package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.helper.ModelObjectBuilderHelper;
import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.repository.BookRepository;
import com.rudy.bibliotheque.mbook.search.BookSearch;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

public class BookServiceTest {

    private BookRepository bookRepository = Mockito.mock(BookRepository.class);

    private BookService objectToTest = new BookService(bookRepository);

    private List<Book> booksInDb;

    @BeforeEach
    void initEach() {
        booksInDb = new ArrayList<>();
        booksInDb.add(ModelObjectBuilderHelper.getSampleBook1());
        booksInDb.add(ModelObjectBuilderHelper.getSampleBook2());
        booksInDb.add(ModelObjectBuilderHelper.getSampleBook3());
    }

    @Test
    void getAllBooksTest() {
        List<Book> expectedResult = booksInDb;
        Mockito.when(bookRepository.findAll()).thenReturn(expectedResult);

        List<Book> result = objectToTest.getAllBooks();

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBooksBySearchTest() {
        List<Book> expectedResult = Collections.singletonList(booksInDb.get(0));
        BookSearch bookSearch = new BookSearch();
        bookSearch.setName("Black and White");
        Mockito.when(bookRepository.findAllBySearch(bookSearch)).thenReturn(expectedResult);

        List<Book> result = objectToTest.getBooksBySearch(bookSearch);

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBookById_idFound_returnBook() {
        Book expectedResult = booksInDb.get(0);
        Long bookId = 1L;
        Mockito.when(bookRepository.findById(bookId)).thenReturn(Optional.ofNullable(expectedResult));

        Book result = objectToTest.getBookById(bookId);

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBookById_idNotFound_returnNull() {
        Book expectedResult = null;
        Long bookId = 1L;
        Mockito.when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Book result = objectToTest.getBookById(bookId);

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void saveBookTest() {
        Book bookToSave = new Book();

        objectToTest.saveBook(bookToSave);

        Mockito.verify(bookRepository).save(bookToSave);
    }

    @Test
    void deleteBookTest() {
        Book bookToDelete = new Book();

        objectToTest.deleteBook(bookToDelete);

        Mockito.verify(bookRepository).delete(bookToDelete);
    }

    @Test
    void deleteBookByIdTest() {
        Long idOfBookToDelete = 1L;

        objectToTest.deleteBookById(idOfBookToDelete);

        Mockito.verify(bookRepository).deleteById(idOfBookToDelete);
    }


}

package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.config.ApplicationPropertiesConfig;
import com.rudy.bibliotheque.mbook.helper.ModelObjectBuilderHelper;
import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.repository.BorrowRepository;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BorrowServiceTest {

    private CopyService copyService = Mockito.mock(CopyService.class);
    private UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
    private ApplicationPropertiesConfig appProperties = Mockito.mock(ApplicationPropertiesConfig.class);
    private KeycloakRestTemplate keycloakRestTemplate = Mockito.mock(KeycloakRestTemplate.class);
    private BorrowRepository borrowRepository = Mockito.mock(BorrowRepository.class);
    private EmailService emailService = Mockito.mock(EmailService.class);

    private BorrowService objectToTest = new BorrowService(copyService, userInfoService, appProperties, keycloakRestTemplate, borrowRepository, emailService);

    private List<Borrow> borrowsInDb;

    @BeforeEach
    void initEach() {
        borrowsInDb = new ArrayList<>();
        borrowsInDb.add(ModelObjectBuilderHelper.getSampleOngoingBorrow1());
        borrowsInDb.add(ModelObjectBuilderHelper.getSamplePendingBorrow1());
    }

    @Test
    void getAllBooksTest() {
        List<Borrow> expectedResult = borrowsInDb;
        Mockito.when(borrowRepository.findAll()).thenReturn(expectedResult);

        List<Borrow> result = objectToTest.getAllLoans();

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBooksBySearchTest() {
        List<Borrow> expectedResult = Collections.singletonList(borrowsInDb.get(0));
        LoanSearch loanSearch = new LoanSearch();
        loanSearch.setUserId("user1id");
        Mockito.when(borrowRepository.findAllBySearch(loanSearch)).thenReturn(expectedResult);

        List<Borrow> result = objectToTest.getLoansBySearch(loanSearch);

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBookById_idFound_returnBook() {
        Borrow expectedResult = borrowsInDb.get(0);
        Long loanId = 1L;
        Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.ofNullable(expectedResult));

        Borrow result = objectToTest.getLoanById(loanId);

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBookById_idNotFound_returnNull() {
        Borrow expectedResult = null;
        Long loanId = 1L;
        Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.empty());

        Borrow result = objectToTest.getLoanById(loanId);

        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void saveBookTest() {
        Borrow borrowToSave = new Borrow();

        objectToTest.saveLoan(borrowToSave);

        Mockito.verify(borrowRepository).save(borrowToSave);
    }

    @Test
    void deleteBookByIdTest() {
        Long idOfBorrowToDelete = 1L;

        objectToTest.deleteLoanById(idOfBorrowToDelete);

        Mockito.verify(borrowRepository).deleteById(idOfBorrowToDelete);
    }

}

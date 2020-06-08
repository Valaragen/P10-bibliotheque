package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.config.ApplicationPropertiesConfig;
import com.rudy.bibliotheque.mbook.dto.LoanCreateDTO;
import com.rudy.bibliotheque.mbook.helper.ModelObjectBuilderHelper;
import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.model.Copy;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.repository.BorrowRepository;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.web.exception.InvalidIdException;
import com.rudy.bibliotheque.mbook.web.exception.NotFoundException;
import com.rudy.bibliotheque.mbook.web.exception.ProhibitedActionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

public class BorrowServiceTest {

    @Mock
    private ReservationService reservationService = Mockito.mock(ReservationService.class);

    private CopyService copyService = Mockito.mock(CopyService.class);
    private UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
    private ApplicationPropertiesConfig appProperties = Mockito.mock(ApplicationPropertiesConfig.class);
    private KeycloakRestTemplate keycloakRestTemplate = Mockito.mock(KeycloakRestTemplate.class);
    private BorrowRepository borrowRepository = Mockito.mock(BorrowRepository.class);
    private EmailService emailService = Mockito.mock(EmailService.class);

    @InjectMocks
    private BorrowService objectToTest = new BorrowService(copyService, userInfoService, appProperties, keycloakRestTemplate, borrowRepository, emailService);

    private List<Borrow> borrowsInDb;

    @BeforeEach
    void initEach() {
        borrowsInDb = new ArrayList<>();
        borrowsInDb.add(ModelObjectBuilderHelper.getSampleOngoingBorrow1());
        borrowsInDb.add(ModelObjectBuilderHelper.getSamplePendingBorrow1());
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void cleanEach() {

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
        loanSearch.setUserId("userid");
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

    @Nested
    class saveANewLoanTest {
        @Test
        void saveANewLoan_loanCreateDTOUserIdIsNull_throwInvalidIdException() {
            LoanCreateDTO loanCreateDTO = new LoanCreateDTO();
            loanCreateDTO.setBookId(2L);

            Assertions.assertThatThrownBy(() -> {
                objectToTest.saveANewLoan(loanCreateDTO);
            }).isInstanceOf(InvalidIdException.class);
        }

        @Test
        void saveANewLoan_loanCreateDTOCopyCodeAndBookIdIsNull_throwInvalidIdException() {
            LoanCreateDTO loanCreateDTO = new LoanCreateDTO();
            loanCreateDTO.setUserId("userId");

            Assertions.assertThatThrownBy(() -> {
                objectToTest.saveANewLoan(loanCreateDTO);
            }).isInstanceOf(InvalidIdException.class);
        }

        @Test
        void saveANewLoan_loanCreateDTOCopyCodeIsNotNull_shouldGetLinkedCopyByHisCode() {
            LoanCreateDTO loanCreateDTO = new LoanCreateDTO();
            loanCreateDTO.setUserId("userId");
            loanCreateDTO.setBookId(1L);
            loanCreateDTO.setCode("code");
            Mockito.when(copyService.getCopyById(loanCreateDTO.getCode())).thenReturn(ModelObjectBuilderHelper.getSampleCopy2ofBook2());
            Mockito.when(userInfoService.getUserInfoById(loanCreateDTO.getUserId())).thenReturn(ModelObjectBuilderHelper.getSampleUserInfo1());
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenReturn(new Borrow());

            objectToTest.saveANewLoan(loanCreateDTO);

            Mockito.verify(copyService).getCopyById(loanCreateDTO.getCode());
        }

        @Test
        void saveANewLoan_loanCreateDTOCopyCodeIsNullAndBookIdIsDefined_shouldFindAnAvailableCopyByBookId() {
            LoanCreateDTO loanCreateDTO = new LoanCreateDTO();
            loanCreateDTO.setUserId("userId");
            loanCreateDTO.setBookId(1L);
            Mockito.when(copyService.getAnAvailableCopyByBookId(loanCreateDTO.getBookId())).thenReturn(ModelObjectBuilderHelper.getSampleCopy2ofBook2());
            Mockito.when(userInfoService.getUserInfoById(loanCreateDTO.getUserId())).thenReturn(ModelObjectBuilderHelper.getSampleUserInfo1());
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenReturn(new Borrow());

            objectToTest.saveANewLoan(loanCreateDTO);

            Mockito.verify(copyService).getAnAvailableCopyByBookId(loanCreateDTO.getBookId());
        }

        @Test
        void saveANewLoan_whenCorrectLoanCreateDTO_shouldUpdateBorrowToPendingAndChangeValueInCopyAccordingly() {
            //ARRANGE
            LoanCreateDTO loanCreateDTO = new LoanCreateDTO();
            loanCreateDTO.setUserId("userId");
            loanCreateDTO.setBookId(1L);
            loanCreateDTO.setCode("code");

            Copy linkedCopy = ModelObjectBuilderHelper.getSampleCopy2ofBook2();
            linkedCopy.setBorrowed(false);

            int availableCopiesOfBook = 2;
            linkedCopy.getBook().setAvailableCopyNumber(availableCopiesOfBook);

            Mockito.when(copyService.getCopyById(loanCreateDTO.getCode())).thenReturn(linkedCopy);
            Mockito.when(userInfoService.getUserInfoById(loanCreateDTO.getUserId())).thenReturn(ModelObjectBuilderHelper.getSampleUserInfo1());
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenAnswer(i -> i.getArguments()[0]);

            //ACT
            Borrow result = objectToTest.saveANewLoan(loanCreateDTO);

            //ASSERT
            Assertions.assertThat(result.getLoanRequestDate()).isNotNull();
            Assertions.assertThat(result.getDeadlineToRetrieve()).isNotNull();
            Assertions.assertThat(result.getCopy().isBorrowed()).isTrue();
            Assertions.assertThat(result.getCopy().getBook().getAvailableCopyNumber()).isEqualTo(availableCopiesOfBook - 1);
        }
    }

    @Nested
    class validateALoanTest {
        @Test
        void validateALoan_noLoanFoundForGivenId_throwNotFoundException() {
            Long loanId = 1L;
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> objectToTest.validateALoan(loanId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void validateALoan_loanIsAlreadyValidated_throwProhibitedActionException() {
            Long loanId = 1L;
            Borrow alreadyValidatedBorrow = new Borrow();
            alreadyValidatedBorrow.setLoanStartDate(new Date());
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(alreadyValidatedBorrow));

            Assertions.assertThatThrownBy(() -> objectToTest.validateALoan(loanId))
                    .isInstanceOf(ProhibitedActionException.class);
        }

        @Test
        void validateALoan_whenMethodCalled_shouldUpdateBorrowToOngoingAndUpdateFieldsAccordingly() {
            Long loanId = 1L;
            Borrow foundBorrow = new Borrow();
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(foundBorrow));
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenAnswer(i -> i.getArguments()[0]);

            objectToTest.validateALoan(loanId);

            Assertions.assertThat(foundBorrow.getLoanStartDate()).isNotNull();
            Assertions.assertThat(foundBorrow.getLoanEndDate()).isNotNull();
        }
    }

    @Nested
    class saveALoanReturnTest {
        @Test
        void saveALoanReturn_noLoanFoundForGivenId_throwNotFoundException() {
            Long loanId = 1L;
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> objectToTest.saveALoanReturn(loanId, null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void saveALoanReturn_loanIsInPendingState_throwProhibitedActionException() {
            Long loanId = 1L;
            Borrow pendingBorrow = new Borrow();
            pendingBorrow.setLoanStartDate(null);
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(pendingBorrow));

            Assertions.assertThatThrownBy(() -> objectToTest.saveALoanReturn(loanId, null))
                    .isInstanceOf(ProhibitedActionException.class);
        }

        @Test
        void saveALoanReturn_loanIsAlreadyReturned_throwProhibitedActionException() {
            Long loanId = 1L;
            Borrow alreadyReturnedBorrow = new Borrow();
            alreadyReturnedBorrow.setReturnedOn(new Date());
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(alreadyReturnedBorrow));

            Assertions.assertThatThrownBy(() -> objectToTest.saveALoanReturn(loanId, null))
                    .isInstanceOf(ProhibitedActionException.class);
        }

        @Test
        void saveALoanReturn_additionalInfoIsGiven_addInfosToBorrow() {
            Long loanId = 1L;
            String givenStateAfterBorrow = "abimé";
            Borrow borrow = ModelObjectBuilderHelper.getSampleOngoingBorrow1();
            Borrow borrowAdditionalInfo = new Borrow();
            borrow.setLoanStartDate(new Date());
            borrowAdditionalInfo.setStateAfterBorrow(givenStateAfterBorrow);
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(borrow));
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenAnswer(i -> i.getArguments()[0]);
            Mockito.doNothing().when(reservationService).createLoanFromBookReservations(borrow.getCopy().getBook());

            objectToTest.saveALoanReturn(loanId, borrowAdditionalInfo);

            Assertions.assertThat(borrow.getStateAfterBorrow()).isEqualTo(givenStateAfterBorrow);
        }

        @Test
        void saveALoanReturn_noAdditionalInfoIsGiven_setStateAfterBorrowFromStateBeforeBorrow() {
            Long loanId = 1L;
            Borrow borrow = ModelObjectBuilderHelper.getSampleOngoingBorrow1();
            borrow.setLoanStartDate(new Date());
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(borrow));
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenAnswer(i -> i.getArguments()[0]);
            Mockito.doNothing().when(reservationService).createLoanFromBookReservations(borrow.getCopy().getBook());

            objectToTest.saveALoanReturn(loanId, null);

            Assertions.assertThat(borrow.getStateAfterBorrow()).isEqualTo(borrow.getStateBeforeBorrow());
        }

        @Test
        void saveALoanReturn_whenMethodCall_shouldCreateLoanFromBookReservations() {
            Long loanId = 1L;
            Borrow borrow = ModelObjectBuilderHelper.getSampleOngoingBorrow1();
            borrow.setLoanStartDate(new Date());
            Mockito.when(borrowRepository.findById(loanId)).thenReturn(Optional.of(borrow));
            Mockito.when(borrowRepository.save(Mockito.any(Borrow.class))).thenAnswer(i -> i.getArguments()[0]);
            Mockito.doNothing().when(reservationService).createLoanFromBookReservations(borrow.getCopy().getBook());

            objectToTest.saveALoanReturn(loanId, null);

            Mockito.verify(reservationService).createLoanFromBookReservations(borrow.getCopy().getBook());
        }
    }

    @Nested
    class cancelLoanTest {
        @Test
        void cancelLoanReturn_givenOngoingLoan_throwProhibitedActionException() {
            Borrow borrow = new Borrow();
            borrow.setLoanStartDate(new Date());

            Assertions.assertThatThrownBy(() -> objectToTest.cancelLoan(borrow)).isInstanceOf(ProhibitedActionException.class);
        }

        @Test
        void cancelLoanReturn_whenMethodCall_throwShouldDeleteInDb() {
            Borrow borrow = ModelObjectBuilderHelper.getSamplePendingBorrow1();
            Mockito.doNothing().when(borrowRepository).deleteById(borrow.getId());

            objectToTest.cancelLoan(borrow);

            Mockito.verify(borrowRepository).deleteById(borrow.getId());
        }
    }

    @Nested
    class isUserBorrowingBookTest {
        @Test
        void isUserBorrowingBookTest_userBorrowTheGivenBook_returnTrue() {
            UserInfo userInfo = ModelObjectBuilderHelper.getSampleUserInfo1();
            Borrow ongoingBorrow = ModelObjectBuilderHelper.getSampleOngoingBorrow1();
            ongoingBorrow.setUserInfo(userInfo);
            List<Borrow> userBorrow = new ArrayList<>();
            userBorrow.add(ongoingBorrow);
            Mockito.when(borrowRepository.findAllBySearch(Mockito.any(LoanSearch.class))).thenReturn(userBorrow);

            boolean result = objectToTest.isUserBorrowingBook(ongoingBorrow.getUserInfo().getId(), ongoingBorrow.getCopy().getBook().getId());

            Assertions.assertThat(result).isTrue();
        }

        @Test
        void isUserBorrowingBookTest_userDoNotBorrowTheGivenBook_returnFalse() {
            Long notBorrowedBookId = 1000L;
            UserInfo userInfo = ModelObjectBuilderHelper.getSampleUserInfo1();
            Borrow ongoingBorrow = ModelObjectBuilderHelper.getSampleOngoingBorrow1();
            ongoingBorrow.setUserInfo(userInfo);
            List<Borrow> userBorrow = new ArrayList<>();
            userBorrow.add(ongoingBorrow);
            Mockito.when(borrowRepository.findAllBySearch(Mockito.any(LoanSearch.class))).thenReturn(userBorrow);

            boolean result = objectToTest.isUserBorrowingBook(ongoingBorrow.getUserInfo().getId(), notBorrowedBookId);

            Assertions.assertThat(result).isFalse();
        }

        @Test
        void isUserBorrowingBookTest_userDoNotBorrowAnyBooks_returnFalse() {
            Long bookId = 1L;
            String userId = "user";
            Mockito.when(borrowRepository.findAllBySearch(Mockito.any(LoanSearch.class))).thenReturn(new ArrayList<>());

            boolean result = objectToTest.isUserBorrowingBook(userId, bookId);

            Assertions.assertThat(result).isFalse();
        }
    }

}

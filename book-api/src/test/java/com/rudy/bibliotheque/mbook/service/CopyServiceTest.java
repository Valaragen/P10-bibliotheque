package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.helper.ModelObjectBuilderHelper;
import com.rudy.bibliotheque.mbook.model.Copy;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.repository.CopyRepository;
import com.rudy.bibliotheque.mbook.repository.UserInfoRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CopyServiceTest {
    private CopyRepository copyRepository = Mockito.mock(CopyRepository.class);

    private CopyService objectToTest = new CopyService(copyRepository);

    @Test
    void getAllCopiesTest() {
        List<Copy> allCopies = new ArrayList<>();
        allCopies.add(ModelObjectBuilderHelper.getSampleCopy1ofBook1());
        allCopies.add(ModelObjectBuilderHelper.getSampleCopy1ofBook2());
        Mockito.when(copyRepository.findAll()).thenReturn(allCopies);

        List<Copy> result = objectToTest.getAllCopies();

        Assertions.assertThat(result).isEqualTo(allCopies);
    }

    @Test
    void getCopyByIdTest() {
        Copy expectedCopy = ModelObjectBuilderHelper.getSampleCopy1ofBook1();
        String copyId = expectedCopy.getCode();
        Mockito.when(copyRepository.findById(copyId)).thenReturn(Optional.of(expectedCopy));

        Copy result = objectToTest.getCopyById(copyId);

        Assertions.assertThat(result).isEqualTo(expectedCopy);
    }

    @Test
    void saveCopyTest() {
        Copy copyToSave = new Copy();

        objectToTest.saveCopy(copyToSave);

        Mockito.verify(copyRepository).save(copyToSave);
    }

    @Test
    void generateCodeTest() {
        String result = objectToTest.generateCode();

        Assertions.assertThat(result).hasSize(12);
    }

    @Test
    void getAnAvailableCopyByBookIdTest() {
        Copy expectedCopy = ModelObjectBuilderHelper.getSampleCopy1ofBook1();
        Long bookId = expectedCopy.getBook().getId();
        Mockito.when(copyRepository.findFirstByBookIdAndBorrowedIsFalse(bookId)).thenReturn(Optional.of(expectedCopy));

        Copy result = objectToTest.getAnAvailableCopyByBookId(bookId);

        Assertions.assertThat(result).isEqualTo(expectedCopy);
    }
}

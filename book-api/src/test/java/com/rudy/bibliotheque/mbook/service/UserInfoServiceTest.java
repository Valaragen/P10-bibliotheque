package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.helper.ModelObjectBuilderHelper;
import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.repository.UserInfoRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class UserInfoServiceTest {
    private UserInfoRepository userInfoRepository = Mockito.mock(UserInfoRepository.class);

    private UserInfoService objectToTest = new UserInfoService(userInfoRepository);

    @Test
    void getUserInfoByIdTest() {
        UserInfo expectedUserInfo = ModelObjectBuilderHelper.getSampleUserInfo1();
        String userInfoUUID = expectedUserInfo.getId();
        Mockito.when(userInfoRepository.findById(userInfoUUID)).thenReturn(Optional.of(expectedUserInfo));

        UserInfo result = objectToTest.getUserInfoById(userInfoUUID);

        Assertions.assertThat(result).isEqualTo(expectedUserInfo);
    }

    @Test
    void saveUserInfoTest() {
        UserInfo userInfoToSave = new UserInfo();

        objectToTest.saveUserInfo(userInfoToSave);

        Mockito.verify(userInfoRepository).save(userInfoToSave);
    }
}

package com.studyolle.settings;

import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach()
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("프로필 수정 폼")
    @Test
    @WithAccount("wook")
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME));

    }

    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    @WithAccount("wook")
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + accountRepository.findByNickname("wook").getNickname()))
                .andExpect(flash().attributeExists("message"));

        Account wook = accountRepository.findByNickname("wook");
        assertEquals(bio, wook.getBio());
    }

    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    @WithAccount("wook")
    void updateProfile_error() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());
    }

    @DisplayName("패스워드 수정 폼")
    @Test
    @WithAccount("wook")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME));
    }

    @DisplayName("패스워드 수정 정상")
    @Test
    @WithAccount("wook")
    void updatePassword() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "q1w2e3r4t5")
                .param("newPasswordConfirm", "q1w2e3r4t5")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account wook = accountRepository.findByNickname("wook");

        assertTrue(passwordEncoder.matches("q1w2e3r4t5", wook.getPassword()));
    }

    @DisplayName("패스워드 수정 오류")
    @Test
    @WithAccount("wook")
    void updatePassword_error() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "q1w2e3r4t5")
                        .param("newPasswordConfirm", "3341234566")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));


        Account wook = accountRepository.findByNickname("wook");

        assertFalse(passwordEncoder.matches("q1w2e3r4t5", wook.getPassword()));
    }

    @DisplayName("알림 폼")
    @Test
    @WithAccount("wook")
    void notificationsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_NOTIFICATION_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(view().name(SettingsController.SETTINGS_NOTIFICATION_VIEW_NAME));
    }

    @DisplayName("알림 상태 변경")
    @Test
    @WithAccount("wook")
    void updateNotifications() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_NOTIFICATION_URL)
                .param("studyCreatedByEmail", "true")
                .param("studyCreatedByWeb", "false")
                .param("studyEnrollmentResultByEmail", "false")
                .param("studyEnrollmentResultByWeb", "true")
                .param("studyUpdatedByWeb", "true")
                .param("studyUpdatedByEmail", "false").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_NOTIFICATION_URL))
                .andExpect(flash().attributeExists("message"));

        Account wook = accountRepository.findByNickname("wook");
        System.out.println("wook.isStudyCreatedByEmail() = " + wook.isStudyCreatedByEmail());
        System.out.println("wook.isStudyCreatedByWeb() = " + wook.isStudyCreatedByWeb());
        System.out.println("wook.isStudyEnrollmentResultByEmail() = " + wook.isStudyEnrollmentResultByEmail());
        System.out.println("wook.isStudyEnrollmentResultByWeb() = " + wook.isStudyEnrollmentResultByWeb());
        System.out.println("wook.isStudyUpdatedByEmail() = " + wook.isStudyUpdatedByEmail());
        System.out.println("wook.isStudyUpdatedByWeb() = " + wook.isStudyUpdatedByWeb());

    }

}
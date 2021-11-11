package com.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import jdk.jshell.spi.ExecutionControlProvider;
import lombok.ToString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ToString
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;
    
    @Autowired
    ZoneRepository zoneRepository;

    @AfterEach()
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("태그 수정 폼")
    @Test
    @WithAccount("wook")
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAG_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAG_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @DisplayName("태그 추가")
    @Test
    @WithAccount("wook")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAG_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Optional<Tag> newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account wook = accountRepository.findByNickname("wook");

        assertTrue(wook.getTags().contains(newTag.get()));
    }

    @DisplayName("태그 삭제")
    @Test
    @WithAccount("wook")
    void removeTag() throws Exception {
        Account wook = accountRepository.findByNickname("wook");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(wook, newTag);

        assertTrue(wook.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAG_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(wook.getTags().contains(newTag));
    }

    @DisplayName("활동지역 폼")
    @Test
    @WithAccount("wook")
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));

        String zoneName = "Goyang(고양시)/Gyeonggi";
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(zoneName);

        Optional<Zone> byCityAndLocalNameOfCity = zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCity(), zoneForm.getLocalNameOfCity());
        assertNotNull(byCityAndLocalNameOfCity.get());

        assertTrue(zoneForm.getCity().equals("Goyang"));
        assertTrue(zoneForm.getLocalNameOfCity().equals("고양시"));
    }

    @DisplayName("활동지역 추가")
    @Test
    @WithAccount("wook")
    void addZones() throws Exception {
        String zoneName = "Goyang(고양시)/Gyeonggi";
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(zoneName);

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account wook = accountRepository.findByNickname("wook");
        Optional<Zone> byCityAndLocalNameOfCity = zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCity(), zoneForm.getLocalNameOfCity());

        byCityAndLocalNameOfCity.orElseThrow();

        assertTrue(wook.getZones().contains(byCityAndLocalNameOfCity.get()));
    }

    @DisplayName("활동지역 삭제")
    @Test
    @WithAccount("wook")
    void removeZones() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName("Goyang(고양시)/gyeonggi");

        Optional<Zone> zone = zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCity(), zoneForm.getLocalNameOfCity());

        Account wook = accountRepository.findByNickname("wook");
        accountService.addZones(wook, zone.get());

        Optional<Zone> byCityAndLocalNameOfCity = zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCity(), zoneForm.getLocalNameOfCity());

        byCityAndLocalNameOfCity.orElseThrow();

        assertTrue(wook.getZones().contains(byCityAndLocalNameOfCity.get()));

        accountService.removeZones(wook, zone.get());

        assertFalse(wook.getZones().contains(byCityAndLocalNameOfCity.get()));
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
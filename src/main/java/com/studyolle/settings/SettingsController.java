package com.studyolle.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.settings.form.*;
import com.studyolle.settings.validator.NicknameFormValidator;
import com.studyolle.settings.validator.PasswordFormValidator;
import com.studyolle.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";
    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSWORD_URL = "/settings/password";
    static final String SETTINGS_NOTIFICATION_VIEW_NAME = "settings/notifications";
    static final String SETTINGS_NOTIFICATION_URL = "/settings/notifications";
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";
    static final String SETTINGS_ACCOUNT_URL = "/settings/account";
    static final String SETTINGS_TAG_VIEW_NAME = "settings/tags";
    static final String SETTINGS_TAG_URL = "/settings/tags";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameFormValidator nicknameFormValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }

    @GetMapping(SETTINGS_PROFILE_URL)
    public String updateProfileForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));

        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile
            , Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/profile/" + account.getNickname();
    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }
    
    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors
            , Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }
        
        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        
        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    @GetMapping(SETTINGS_NOTIFICATION_URL)
    public String notificationForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));

        return SETTINGS_NOTIFICATION_VIEW_NAME;
    }
    
    @PostMapping(SETTINGS_NOTIFICATION_URL)
    public String updateNotification(@CurrentUser Account account, @Valid @ModelAttribute Notifications notifications
            ,Errors errors, Model model, RedirectAttributes redirectAttributes ) {
        
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATION_VIEW_NAME;
        }
        
        accountService.updateNotifications(account, notifications);
        redirectAttributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        
        return "redirect:" + SETTINGS_NOTIFICATION_URL;
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String accountForm(@CurrentUser Account account, Model model) {
        NicknameForm nicknameForm = new NicknameForm();
        nicknameForm.setNickname(account.getNickname());

        model.addAttribute(account);
        model.addAttribute(nicknameForm);

        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateNickname(@CurrentUser Account account, @Valid NicknameForm nicknameForm
            , Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm);
        attributes.addFlashAttribute("message", "닉네임 변경이 완료되었습니다.");

        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }

    @GetMapping(SETTINGS_TAG_URL)
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS_TAG_VIEW_NAME;
    }
    
    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title).orElseGet(() -> tagRepository.save(Tag.builder()
                .title(tagForm.getTagTitle())
                .build()));

        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        Optional<Tag> byTitle = tagRepository.findByTitle(tagForm.getTagTitle());
        byTitle.ifPresent(tag -> {
            accountService.removeTag(account, tag);
        });

        return ResponseEntity.ok().build();
    }
}

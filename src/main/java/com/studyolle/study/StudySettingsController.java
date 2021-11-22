package com.studyolle.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.study.form.StudyDescriptionForm;
import com.studyolle.study.validator.StudyDescriptionFormValidator;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final StudyDescriptionFormValidator studyDescriptionFormValidator;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;

    @InitBinder("studyDescriptionForm")
    public void studyDescriptionFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyDescriptionFormValidator);
    }

    @GetMapping("/description")
    public String descriptionForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyWithManagers(path);

        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));

        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, @PathVariable String path
            , @Valid StudyDescriptionForm studyDescriptionForm, Errors errors, Model model
            , RedirectAttributes attributes) {

        Study study = studyService.getStudyWithManagersToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정하였습니다.");

        return "redirect:/study/" + getPath(path) + "/settings/description";
    }

    @GetMapping("/banner")
    public String bannerForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyWithManagers(path);

        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String updateBannerImage(@CurrentUser Account account, @PathVariable String path, String image
            , RedirectAttributes attributes) {
        Study study = studyService.getStudyWithManagersToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/{bannerUse}")
    public String bannerUseSetting(@CurrentUser Account account
            , @PathVariable String path, @PathVariable String bannerUse, Model model, RedirectAttributes attributes) {

        Study study = studyService.getStudyWithManagersToUpdate(account, path);
        studyService.updateBannerUse(study, bannerUse);

        model.addAttribute(account);
        model.addAttribute(study);

        attributes.addFlashAttribute("message", "배너 사용상태가 변경되었습니다.");

        return "study/settings/banner";
    }

    @GetMapping("/tags")
    public String tagsForm(@CurrentUser Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study study = studyService.getStudyWithManagersAndTags(path);

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("tags", study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    public ResponseEntity addStudyTag(@CurrentUser Account account, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyWithManagersAndTagsToUpdate(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle()).orElseGet(() -> tagRepository.save(Tag.builder()
                .title(tagForm.getTagTitle())
                .build()));

        studyService.addStudyTags(study, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public ResponseEntity removeStudyTag(@CurrentUser Account account, @PathVariable String path
            , @RequestBody TagForm tagForm) {

        Study study = studyService.getStudyWithManagersAndTagsToUpdate(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle()).orElseThrow();

        studyService.removeStudyTags(study, tag);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String zonesForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyWithManagersAndZones(path);

        List<String> allZones = zoneRepository.findAll()
                .stream()
                .map(m -> m.getCity() + "(" + m.getLocalNameOfCity() + ")/" + m.getProvince()).collect(Collectors.toList());

        List<Object> studyZones = study.getZones()
                .stream()
                .map(m -> m.getCity() + "(" + m.getLocalNameOfCity() + ")/" + m.getProvince()).collect(Collectors.toList());

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        model.addAttribute("zones", studyZones);

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    public ResponseEntity addStudyZones(@CurrentUser Account account, @PathVariable String path
            , @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyWithManagersAndZonesToUpdate(account, path);
        Zone zone = zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCity(), zoneForm.getLocalNameOfCity()).orElseThrow();

        studyService.addStudyZones(study, zone);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    public ResponseEntity removeStudyZones(@CurrentUser Account account, @PathVariable String path
            , @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyWithManagersAndZonesToUpdate(account, path);
        Zone zone = zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCity(), zoneForm.getLocalNameOfCity()).orElseThrow();

        studyService.removeStudyZones(study, zone);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studyForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyWithManagers(path);

        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String studyPublish(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithManagersToUpdate(account, path);
        studyService.publishStudy(study);

        model.addAttribute(study);
        model.addAttribute(account);

        attributes.addFlashAttribute("message", "스터디가 공개되었습니다.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, String newPath
            , Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithManagersToUpdate(account, path);

        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "사용할 수 없는 스터디 경로입니다");
            return "study/settings/study";
        }

        studyService.updatePath(study, newPath);
        attributes.addFlashAttribute("message", "스터디 경로가 변경되었습니다.");
        return "redirect:/study/" + getPath(newPath) + "/settings/study";
    }

    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, String newTitle
            , Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithManagersToUpdate(account, path);

        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "사용할 수 없는 스터디 이름입니다.");
            return "/study/settings/study";
        }

        studyService.updateTitle(study, newTitle);
        attributes.addFlashAttribute("message", "스터디 이름이 변경되었습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }


}

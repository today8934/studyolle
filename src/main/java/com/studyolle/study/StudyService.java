package com.studyolle.study;

import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.studyolle.study.form.StudyForm.VALID_PATH_PATTERN;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);

        if (!account.isManagerOf(study)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }

        return study;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
    }

    public void updateBannerUse(Study study, String bannerUse) {
        study.setUseBanner(bannerUse.equals("enable"));
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void addStudyTags(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void removeStudyTags(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addStudyZones(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeStudyZones(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public boolean isValidPath(String path) {
        if (!path.matches(VALID_PATH_PATTERN)) {
            return false;
        }

        return !studyRepository.existsByPath(path);
    }

    public void updatePath(Study study, String path) {
        study.setPath(path);
    }

    public void publishStudy(Study study) {
        study.setPublished(true);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() < 50;
    }

    public void updateTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }
}

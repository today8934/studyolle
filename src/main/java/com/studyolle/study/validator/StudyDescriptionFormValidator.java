package com.studyolle.study.validator;

import com.studyolle.study.form.StudyDescriptionForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class StudyDescriptionFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return StudyDescriptionForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyDescriptionForm studyDescriptionForm = (StudyDescriptionForm) target;

        if (studyDescriptionForm.getShortDescription().length() > 100 ) {
            errors.rejectValue("shortDescription", "wrong.shortDescription"
                    , "100자를 초과하였습니다.");
        }
    }
}

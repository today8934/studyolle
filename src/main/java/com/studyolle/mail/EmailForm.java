package com.studyolle.mail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailForm {

    private String to;
    private String subject;
    private String message;
}

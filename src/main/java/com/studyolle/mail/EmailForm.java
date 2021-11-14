package com.studyolle.mail;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class EmailForm {

    private String to;
    private String subject;
    private String message;
}

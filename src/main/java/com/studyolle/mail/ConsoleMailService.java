package com.studyolle.mail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleMailService implements EmailService{

    @Override
    public void send(EmailForm emailForm) {
        log.info(emailForm.getMessage());
    }
}

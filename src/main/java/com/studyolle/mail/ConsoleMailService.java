package com.studyolle.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("local")
@Component
public class ConsoleMailService implements EmailService{

    @Override
    public void send(EmailForm emailForm) {
        log.info("sent email: {}", emailForm.getMessage());
    }
}

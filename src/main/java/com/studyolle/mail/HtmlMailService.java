package com.studyolle.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class HtmlMailService implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(EmailForm emailForm) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailForm.getTo());
            mimeMessageHelper.setSubject(emailForm.getSubject());
            mimeMessageHelper.setText(emailForm.getMessage(), false);
        } catch (MessagingException e) {
            log.info("failed sent email", e);
        }

    }
}

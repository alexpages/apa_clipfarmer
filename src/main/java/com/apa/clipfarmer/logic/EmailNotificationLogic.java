package com.apa.clipfarmer.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service that notifies a user through email notification.
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationLogic {

    private final JavaMailSender mailSender;
    private static final String RECIPIENT_EMAIL = "alexpagesandreu@gmail.com";

    public void sendEmail(String subject, String messageBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(RECIPIENT_EMAIL);
            helper.setSubject(subject);
            helper.setText(messageBody, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", RECIPIENT_EMAIL);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", RECIPIENT_EMAIL, e);
        }
    }
}

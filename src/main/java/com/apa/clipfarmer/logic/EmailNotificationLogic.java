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
    private static final String PRE_SUBJECT = "Clip Farmer Notification Service: ";

    public void sendEmail(String subject, String messageBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Set recipient and subject
            helper.setTo(RECIPIENT_EMAIL);
            helper.setSubject(PRE_SUBJECT + subject);

            // Format the message body with HTML
            String formattedMessageBody = "<html><body>" +
                    "<h2 style='color: #2C3E50;'>Clip Farmer Notification</h2>" +
                    "<p style='font-size: 16px;'>Hello,</p>" +
                    "<p style='font-size: 16px;'>Here is your notification:</p>" +
                    "<blockquote style='background-color: #f4f4f4; padding: 10px; border-left: 5px solid #2980B9;'>" +
                    "<p style='font-style: italic;'>" + messageBody + "</p>" +
                    "</blockquote>" +
                    "<hr style='border: 1px solid #BDC3C7;'>" +
                    "<footer style='font-size: 12px; color: #7F8C8D;'>" +
                    "<p>Best regards,<br>The Clip Farmer Team</p>" +
                    "</footer>" +
                    "</body></html>";

            // Set email body
            helper.setText(formattedMessageBody, true);

            // Send the email
            mailSender.send(message);
            log.info("Email sent successfully to {}", RECIPIENT_EMAIL);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", RECIPIENT_EMAIL, e);
        }
    }
}

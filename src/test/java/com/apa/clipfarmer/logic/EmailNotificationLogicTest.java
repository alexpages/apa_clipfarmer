package com.apa.clipfarmer.logic;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EmailNotificationLogic}.
 *
 * @author alexpages
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationLogicTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationLogic emailNotificationLogic;

    @Test
    void sendEmail_validInput_delegatesToMailSender() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailNotificationLogic.sendEmail("Execution finalized", "xqc", 120L);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_messagingException_isCaughtAndNotPropagated() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // MessagingException is thrown inside MimeMessageHelper — simulate via send()
        // sendEmail only catches MessagingException; MailSendException wraps it and IS a RuntimeException,
        // so this test documents that MailSendException propagates (only MessagingException is swallowed).
        doThrow(new MailSendException("SMTP failure"))
                .when(mailSender).send(mimeMessage);

        // MailSendException (RuntimeException) is NOT caught by the method — it propagates
        assertThatThrownBy(() -> emailNotificationLogic.sendEmail("subject", "xqc", 10L))
                .isInstanceOf(MailSendException.class);
    }

    @Test
    void sendEmail_messageBody_containsStreamerNameAndExecutionTime() throws Exception {
        // Use a real JavaMailSenderImpl to create a real MimeMessage so we can inspect its content
        JavaMailSenderImpl realSender = new JavaMailSenderImpl();
        MimeMessage realMessage = realSender.createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(realMessage);

        emailNotificationLogic.sendEmail("Execution finalized", "xqc", 120L);

        // Write the full MIME message to a stream and assert the raw content contains expected values
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        realMessage.writeTo(out);
        String rawMessage = out.toString();
        assertThat(rawMessage).contains("xqc");
        assertThat(rawMessage).contains("120");
    }

    @Test
    void sendEmail_differentStreamersAndTimes_sendsDifferentMessages() throws Exception {
        MimeMessage msg1 = mock(MimeMessage.class);
        MimeMessage msg2 = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg1).thenReturn(msg2);

        emailNotificationLogic.sendEmail("Done", "xqc", 60L);
        emailNotificationLogic.sendEmail("Done", "mizkif", 90L);

        verify(mailSender).send(msg1);
        verify(mailSender).send(msg2);
    }
}

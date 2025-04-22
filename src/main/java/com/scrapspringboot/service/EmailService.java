package com.scrapspringboot.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.verification.url}")
    private String verificationBaseUrl;


    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject("Verifica tu cuenta de IndexerTFG");

            String verificationUrl = verificationBaseUrl + token;

            message.setText("Hola " + username + ",\n\n"
                    + "Gracias por registrarte. Por favor, haz clic en el siguiente enlace para activar tu cuenta:\n"
                    + verificationUrl + "\n\n"
                    + "Si no te registraste, por favor ignora este correo.\n\n"
                    + "Saludos,\nEl equipo de IndexerTFG");

            mailSender.send(message);
            log.info("Verification email sent successfully to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());

        }
    }
}
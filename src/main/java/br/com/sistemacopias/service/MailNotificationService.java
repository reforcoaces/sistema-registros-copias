package br.com.sistemacopias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class MailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String defaultTo;

    public MailNotificationService(
            org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.username:}") String fromAddress,
            @Value("${app.notification.to:reforco.aces@gmail.com}") String defaultTo) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress != null && !fromAddress.isBlank() ? fromAddress : "noreply@localhost";
        this.defaultTo = defaultTo;
    }

    public boolean isMailConfigured() {
        return mailSender != null;
    }

    public void sendSimple(String subject, String htmlBody) {
        if (!isMailConfigured()) {
            log.debug("E-mail nao configurado (spring.mail.host). Envio ignorado: {}", subject);
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(defaultTo);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("E-mail enviado: {} -> {}", subject, defaultTo);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail: {}", subject, e);
        }
    }

    public void sendWithAttachment(String subject, String htmlBody, Path attachmentPath, String attachmentName) {
        if (!isMailConfigured()) {
            log.debug("E-mail nao configurado. Backup mensal ignorado.");
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(defaultTo);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addAttachment(attachmentName, new FileSystemResource(attachmentPath.toFile()));
            mailSender.send(message);
            log.info("E-mail com anexo enviado: {} -> {}", subject, defaultTo);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail com anexo: {}", subject, e);
        }
    }
}

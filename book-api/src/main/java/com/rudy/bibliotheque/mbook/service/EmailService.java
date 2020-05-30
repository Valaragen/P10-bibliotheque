package com.rudy.bibliotheque.mbook.service;

import com.rudy.bibliotheque.mbook.model.Borrow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${config.batch.notifications.email}")
    private String email;

    @Async
    public void sendLoanInPendingEmail(Borrow borrow) throws MessagingException {
        // Prepare the evaluation context
        final Context ctx = new Context(Locale.FRANCE);
        ctx.setVariable("bookName", borrow.getCopy().getBook().getName());
        ctx.setVariable("username", borrow.getUserInfo().getUsername());
        ctx.setVariable("deadlineToRetrieve", borrow.getDeadlineToRetrieve());

        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true , "UTF-8");

        helper.setFrom(sender);
        helper.setTo(borrow.getUserInfo().getEmail());
        helper.setCc(sender);

        // Create the TEXT subject using Thymeleaf
        final String content = this.templateEngine.process("loan-pending-email-subject.txt", ctx);
        helper.setSubject(content);

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process("loan-pending-email-body.html", ctx);
        helper.setText(htmlContent, true); // true = isHtml

        log.info("Preparing message for: " + borrow.getUserInfo().getEmail());

        mailSender.send(message);
    }
}

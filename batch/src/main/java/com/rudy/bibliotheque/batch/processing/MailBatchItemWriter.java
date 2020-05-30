package com.rudy.bibliotheque.batch.processing;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;
import java.util.List;

public class MailBatchItemWriter implements ItemWriter<MimeMessage> {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void write(List<? extends MimeMessage> messages) throws Exception {
        messages.forEach((message)->mailSender.send(message));
    }

}
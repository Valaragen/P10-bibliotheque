package com.rudy.bibliotheque.batch.web.controller;

import com.rudy.bibliotheque.batch.util.Constant;
import com.rudy.bibliotheque.batch.web.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Constant.USER_ROLE_NAME)
public class UserController {

    private MailService mailService;

    public UserController(MailService mailService) {
        this.mailService = mailService;
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @GetMapping(Constant.CURRENT_PATH + Constant.LOANS_PATH + Constant.PENDING_MAIL_PATH)
    public void sendLoanPendingMail() {
        
    }
}

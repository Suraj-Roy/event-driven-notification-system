package com.email.emailservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailSender {

    public void send(String to, String subject, String body) {
        log.info("Sending email: to={}, subject={}", to, subject);

    }
}

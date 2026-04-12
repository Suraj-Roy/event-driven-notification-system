package com.push.pushservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushSender {

    public void send(String userId, String title, String body) {
        log.info("Sending push: userId={}, title={}", userId, title);
    }
}
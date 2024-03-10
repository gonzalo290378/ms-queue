package com.bench.msqueue.controllers;

import com.bench.msqueue.dto.ECheckDTO;
import com.bench.msqueue.service.impl.QueueServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api/v1/queue")
public class QueueController {

    @Autowired
    private Environment environment;

    @Autowired
    private QueueServiceImpl queueServiceImpl;

    //CONFIG-SERVER
    @Value("${configuration.text}")
    private String text;

    @PostMapping("/deferred-payment")
    @PreAuthorize("hasRole('ROLE_USER')")
    public void deferredPayment(@RequestBody ECheckDTO eCheckDTO) {
        log.info("Calling deferredPayment with {}", eCheckDTO);
        queueServiceImpl.producer(eCheckDTO);
        //this.jobEcheck();
    }

    @GetMapping("/jobEcheck")
    @PreAuthorize("hasRole('ROLE_USER')")
    public void jobEcheck() {
        log.info("Calling jobEcheck with {}");
        queueServiceImpl.jobEcheck();
    }


        @GetMapping("/get-config")
    public ResponseEntity<?> getConfig(@Value("${server.port}") String port) {
        log.info("getConfig {}" + " port: " + port);
        Map<String, String> json = new HashMap<>();
        json.put("text", text);
        json.put("port", port);

        if (environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("dev")) {
            json.put("env", environment.getActiveProfiles()[0]);
        }
        return new ResponseEntity<Map<String, String>>(json, HttpStatus.OK);
    }

    @GetMapping("/authorized")
    public Map<String, String> authorized(@RequestParam String code) {
        log.info("Calling authorized with {}", code);
        return Collections.singletonMap("code", code);
    }
}

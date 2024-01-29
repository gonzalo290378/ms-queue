package com.bench.msqueue.service;

import com.bench.msqueue.dto.ECheckDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import model.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;

public interface QueueService {

    public void producer(ECheckDTO eCheckDTO) throws JsonProcessingException;

    public void consumer(@Payload ECheckDTO eCheckDTO);

    public void jobEcheck();

    public Optional<Account> getAccount(Long accountNumber);

    public ResponseEntity<ECheckDTO> save(ECheckDTO eCheckDTO);

}


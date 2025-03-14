package com.bench.msqueue.service.impl;

import com.bench.msqueue.dto.ECheckDTO;
import com.bench.msqueue.enums.PaymentState;
import com.bench.msqueue.model.Account;
import com.bench.msqueue.service.QueueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service("serviceRestTemplate")
@Slf4j
public class QueueServiceImpl implements QueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    public void producer(ECheckDTO eCheckDTO) {
        try {
            String orderJson = objectMapper.writeValueAsString(eCheckDTO);
            Message message = MessageBuilder
                    .withBody(orderJson.getBytes())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.convertAndSend("echeq.exchange", "echeq.process", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "Queue-1")
    public void consumer(@Payload ECheckDTO eCheckDTO) {
        log.info("Consumer: {}", eCheckDTO);
        if (!eCheckDTO.getPaymentDate().isEqual(now())) {
            eCheckDTO.setState(PaymentState.PROCESSED_BY_QUEUE);
            save(eCheckDTO);
        }
    }


    @Transactional(readOnly = true)
    public void jobEcheck() {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ECheckDTO[]> eCheckDTO = restTemplate
                .exchange("http://localhost:8090/ms-payments/api/v1/payments/findProcessedByQueue", HttpMethod.GET,
                        entity, new ParameterizedTypeReference<>() {
                        });
        for (ECheckDTO eCheckTransfer : eCheckDTO.getBody()) {
            Optional<Account> accountSender = getAccount(eCheckTransfer.getAccountNumberSender());
            Optional<Account> accountReceiver = getAccount(eCheckTransfer.getAccountNumberReceiver());
            transferProcess(eCheckTransfer, accountSender, accountReceiver);
        }
    }

    private void transferProcess(ECheckDTO echeckTransfer, Optional<Account> accountSender, Optional<Account> accountReceiver) {
        if (accountSender.get().getBalance() - echeckTransfer.getAmount() >= 0) {
            accountSender.get().setBalance(accountSender.get().getBalance() - echeckTransfer.getAmount());
            accountReceiver.get().setBalance(accountReceiver.get().getBalance() + echeckTransfer.getAmount());
            save(accountSender.get());
            save(accountReceiver.get());
            echeckTransfer.setState(PaymentState.APPROVED);
        } else {
            echeckTransfer.setState(PaymentState.REJECTED);
        }
        save(echeckTransfer);
    }

    private ResponseEntity<Account> save(Account account) {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        HttpEntity<Account> entity = new HttpEntity<>(account, headers);
        HashMap<String, Long> uriPathVariable = new HashMap<>();
        Long id = account.getAccountNumber();
        uriPathVariable.put("id", id);
        return restTemplate.exchange("http://localhost:8090/ms-accounts/api/v1/accounts/{id}", HttpMethod.PUT, entity, Account.class, uriPathVariable);
    }

    public Optional<Account> getAccount(Long id) {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        HttpEntity<Account> entity = new HttpEntity<>(headers);
        HashMap<String, Long> uriPathVariable = new HashMap<>();
        uriPathVariable.put("id", id);
        return Optional.ofNullable(restTemplate.
                exchange("http://localhost:8090/ms-accounts/api/v1/accounts/{id}",
                        HttpMethod.GET, entity, Account.class, uriPathVariable).getBody());
    }

    public ResponseEntity<ECheckDTO> save(ECheckDTO eCheckDTO) {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        HashMap<String, Long> uriPathVariable = new HashMap<>();
        HttpEntity<ECheckDTO> entity = new HttpEntity<>(eCheckDTO, headers);
        uriPathVariable.put("id", eCheckDTO.getId());
        return restTemplate.exchange("http://localhost:8090/ms-payments/api/v1/payments/{id}", HttpMethod.PUT, entity, ECheckDTO.class, uriPathVariable);
    }

    private LocalDate now() {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        return localDateTime.toLocalDate();
    }
}
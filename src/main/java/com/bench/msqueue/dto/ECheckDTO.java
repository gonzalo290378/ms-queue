package com.bench.msqueue.dto;

import com.bench.msqueue.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ECheckDTO implements Serializable {

    private Long id;

    private PaymentMethod paymentMethod;

    private TypeCurrency type;

    private PaymentState state;

    private Double amount;

    private LocalDate issueDate;

    private LocalDate paymentDate;

    private Long accountNumberSender;

    private Long accountNumberReceiver;

    private Long edays;

}
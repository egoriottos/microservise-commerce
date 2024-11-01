package org.example.orderservice.payment;

import org.example.orderservice.customer.CustomerResponse;
import org.example.orderservice.order.PaymentMethod;
import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}

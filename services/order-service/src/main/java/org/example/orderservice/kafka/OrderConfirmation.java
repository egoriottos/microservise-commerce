package org.example.orderservice.kafka;

import org.example.orderservice.customer.CustomerResponse;
import org.example.orderservice.order.PaymentMethod;
import org.example.orderservice.product.PurchaseResponse;
import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}

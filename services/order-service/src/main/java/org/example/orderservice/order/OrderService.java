package org.example.orderservice.order;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.customer.CustomerClient;
import org.example.orderservice.exception.BusinessException;
import org.example.orderservice.kafka.OrderConfirmation;
import org.example.orderservice.kafka.OrderProducer;
import org.example.orderservice.orderline.OrderLineRequest;
import org.example.orderservice.orderline.OrderLineService;
import org.example.orderservice.payment.PaymentClient;
import org.example.orderservice.payment.PaymentRequest;
import org.example.orderservice.product.ProductClient;
import org.example.orderservice.product.PurchaseRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper mapper;
    private final OrderRepository repository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;

    public Integer createOrder(@Valid OrderRequest request) {
        //check the customer --> OpenFeign
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with provided ID"));
        //purchase the products --> product-microservice (RestTemplate)
        var purchasedProducts = productClient.purchaseProducts(request.products());
        // persis order
        var order = this.repository.save(mapper.toOrder(request));
        //persist order lines
        for (PurchaseRequest purchaseRequest : request.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }
        //start payment process
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);
        // send the order confirmation --> notification-microservice
        orderProducer.sendOrderConfirmation(new OrderConfirmation(
                request.reference(),
                request.amount(),
                request.paymentMethod(),
                customer,
                purchasedProducts
        ));
        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return repository.findAll().stream().map(mapper::fromOrder).toList();
    }

    public OrderResponse findById(Integer orderId) {
        return repository.findById(orderId)
                .map(mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException("No order found with provided ID:: " + orderId));
    }
}

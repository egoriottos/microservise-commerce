package org.example.paymentservice.payment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public record Customer(
       String id,
       @NotNull(message = "Required firstname")
       String firstname,
       @NotNull(message = "Required lastname")
       String lastname,
       @NotNull(message = "Required email")
       @Email(message = "Email is not correctly formatted")
       String email
) {
}

package com.coderbank.customer_service.dto.response;

import com.coderbank.customer_service.model.Customer;

public record CustomerResponseDTO(
        String id,
        String name,
        String cpf,
        String email,
        String address
) {
}

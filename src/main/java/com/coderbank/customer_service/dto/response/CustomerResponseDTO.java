package com.coderbank.customer_service.dto.response;

import com.coderbank.customer_service.model.Customer;

public record CustomerResponseDTO(
        String id,
        String name,
        String cpf,
        String email,
        String address
) {


    public String setId(String string) {
        return string;
    }

    public String setName(String name) {
        return name;
    }

    public String setCpf(String cpf) {
        return cpf;
    }

    public String setEmail(String email) {
        return email;
    }

    public String setAddress(String address) {
        return address;
    }
}

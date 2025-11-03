package com.coderbank.customer_service.factory;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.model.Customer;

// Fábrica para criar instâncias de Customer a partir de CustomerRequestDTO
public class CustomerFactory {
    // Cria um novo Customer com base nos dados fornecidos em CustomerRequestDTO
    public static Customer createFromRequest(CustomerRequestDTO customerRequest){
        Customer customer = new Customer();
        customer.setName(customerRequest.name());
        customer.setCpf(customerRequest.cpf());
        customer.setEmail(customerRequest.email());
        customer.setAddress(customerRequest.address());
        return customer;
    }
}

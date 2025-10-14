package com.coderbank.customer_service.factory;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.model.Customer;


public class CustomerFactory {
    public static Customer createFromRequest(CustomerRequestDTO customerRequest){
        Customer customer = new Customer();
        customer.setName(customerRequest.getName());
        customer.setCpf(customerRequest.getCpf());
        customer.setEmail(customerRequest.getEmail());
        customer.setAddress(customerRequest.getAddress());
        return customer;
    }
}

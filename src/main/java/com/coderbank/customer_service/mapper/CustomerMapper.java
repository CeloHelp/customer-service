package com.coderbank.customer_service.mapper;

import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


public class CustomerMapper {
    public static CustomerResponseDTO toResponse(Customer customer ){

        return new CustomerResponseDTO(
                customer.getId().toString(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getAddress()
        );

    }
    public static List<CustomerResponseDTO> toResponseList(List<Customer> customers ){
        return customers.stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());
    }
}

package com.coderbank.customer_service.mapper;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


public class CustomerMapper {
    // Converte Customer para CustomerResponseDTO
    public static CustomerResponseDTO toResponse(Customer customer ){
        // Mapeia os campos do Customer para o CustomerResponseDTO

        return new CustomerResponseDTO(
                customer.getId().toString(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getAddress()
        );

    }
    // Converte uma lista de Customer para uma lista de CustomerResponseDTO
    public static List<CustomerResponseDTO> toResponseList(List<Customer> customers ){
        return customers.stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Atualiza os campos de um Customer com os dados de um CustomerRequestDTO
    public static void updateEntity(Customer entity, CustomerRequestDTO customerRequestDTO){
        entity.setName(customerRequestDTO.name());
        entity.setEmail(customerRequestDTO.email());
        entity.setAddress(customerRequestDTO.address());



    }



}

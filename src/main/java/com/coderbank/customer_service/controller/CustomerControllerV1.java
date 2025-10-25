package com.coderbank.customer_service.controller;


import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.mapper.CustomerMapper;
import com.coderbank.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerControllerV1 {

    private final CustomerService customerService;


    public CustomerControllerV1(CustomerService customerService) {
        this.customerService = customerService;

    }


    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {

        CustomerResponseDTO createdCustomer = customerService.createCustomer(customerRequestDTO);

        // Retorna 201 Created com o local do novo recurso no cabeçalho Location
        URI location = URI.create(String.format("/api/v1/customers/%s", createdCustomer.id()));


        return ResponseEntity.status(201).body(createdCustomer);


    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable UUID id, @Valid @RequestBody CustomerRequestDTO customerRequestDTO) {

        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, customerRequestDTO);

        // Retorna 201 Created com o local do novo recurso no cabeçalho Location
        URI location = URI.create(String.format("/api/v1/customers/%s", updatedCustomer.id()));


        return ResponseEntity.status(201).body(updatedCustomer);


    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> getAllCustomers = customerService.getAllCustomers();

        // Retorna 201 Created com o local do novo recurso no cabeçalho Location
        URI location = URI.create("/api/v1/customers");

        return ResponseEntity.status(200).body(getAllCustomers);

    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {
        CustomerResponseDTO customerResponseDTO = customerService.getCustomerById(id);

        URI location = URI.create(String.format("/api/v1/customers/%s", customerResponseDTO.id()));

        return ResponseEntity.status(200).body(customerResponseDTO);

    }
}

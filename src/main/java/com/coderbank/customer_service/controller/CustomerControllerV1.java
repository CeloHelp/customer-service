package com.coderbank.customer_service.controller;


import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Operações de clientes (CRUD)")
public class CustomerControllerV1 {

    private final CustomerService customerService;


    public CustomerControllerV1(CustomerService customerService) {
        this.customerService = customerService;

    }


    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente e retorna seus dados")
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {

        log.info("Recebendo requisição para criar cliente com CPF: {}", customerRequestDTO.cpf());

        CustomerResponseDTO createdCustomer = customerService.createCustomer(customerRequestDTO);

        log.info("Cliente criado com sucesso: {}", createdCustomer);

        // Retorna 201 Created com o local do novo recurso no cabeçalho Location
        URI location = URI.create(String.format("/api/v1/customers/%s", createdCustomer.id()));


        return ResponseEntity.status(201).body(createdCustomer);


    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente pelo ID")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable("id") UUID id, @Valid @RequestBody CustomerRequestDTO customerRequestDTO) {

        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, customerRequestDTO);


        URI location = URI.create(String.format("/api/v1/customers/%s", updatedCustomer.id()));


        return ResponseEntity.status(201).body(updatedCustomer);


    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes. Retorna 204 se vazio")
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> getAllCustomers = customerService.getAllCustomers();

        if(getAllCustomers.isEmpty()){
            return ResponseEntity.status(204).build();
        }




        URI location = URI.create(String.format("/api/v1/customers/"));

        return ResponseEntity.status(200).body(getAllCustomers);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna dados do cliente pelo ID")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable("id") UUID id) {
        CustomerResponseDTO customerResponseDTO = customerService.getCustomerById(id);


        URI location = URI.create(String.format("/api/v1/customers/%s", customerResponseDTO.id()));

        return ResponseEntity.status(200).body(customerResponseDTO);

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Exclui cliente pelo ID e retorna os dados excluídos")
    public ResponseEntity<CustomerResponseDTO> deleteCustomer(@PathVariable("id") UUID id) {
        CustomerResponseDTO customerResponseDTO = customerService.getCustomerById(id);

        customerService.deleteCustomer(id);


        URI location = URI.create(String.format("/api/v1/customers/%s", customerResponseDTO.id()));

        return ResponseEntity.status(200).body(customerResponseDTO);

    }
}

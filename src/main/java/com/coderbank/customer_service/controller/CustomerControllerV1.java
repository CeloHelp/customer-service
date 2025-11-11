package com.coderbank.customer_service.controller;


import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.mapper.CustomerMapper;
import com.coderbank.customer_service.service.CustomerService;
import com.coderbank.customer_service.utils.LogSanitizer;
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
        String safeCpf = LogSanitizer.maskCpf(customerRequestDTO.cpf());
        String safeEmail = LogSanitizer.maskEmail(customerRequestDTO.email());

        log.info("Recebendo requisição para criar cliente com CPF: {} e Email:{}", safeCpf, safeEmail);

        CustomerResponseDTO createdCustomer = customerService.createCustomer(customerRequestDTO);

        log.info("Cliente criado com sucesso (HTTP 201): {}", createdCustomer.id());

        // Retorna 201 Created com o local do novo recurso no cabeçalho Location
        URI location = URI.create(String.format("/api/v1/customers/%s", createdCustomer.id()));


        return ResponseEntity.created(location).body(CustomerMapper.toResponse(createdCustomer));


    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente pelo ID")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable("id") UUID id, @Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        String safeCpf = LogSanitizer.maskCpf(customerRequestDTO.cpf());
        String safeEmail = LogSanitizer.maskEmail(customerRequestDTO.email());

        log.info("Recebendo requisição para atualizar cliente com ID: {} , CPF: {} e Email: {}", id, safeCpf, safeEmail);
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, customerRequestDTO);


        log.info("Cliente atualizado com sucesso(HTTP 200): {}", updatedCustomer.id());
        return ResponseEntity.status(201).body(updatedCustomer);


    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes. Retorna 204 se vazio")
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {

        log.info("Recebendo requisição para listar todos os clientes");

        List<CustomerResponseDTO> getAllCustomers = customerService.getAllCustomers();



        if(getAllCustomers.isEmpty()){
            log.debug("Nenhum cliente encontrado na base de dados");
            return ResponseEntity.status(204).build();
        }


        log.info("Clientes listado com sucesso (HTTP 200): {}", getAllCustomers.toString());

        return ResponseEntity.status(200).body(getAllCustomers);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna dados do cliente pelo ID")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable("id") UUID id) {

        log.info("Recebendo requisição para buscar cliente com ID: {}", id);
        CustomerResponseDTO customerResponseDTO = customerService.getCustomerById(id);


        log.info("Cliente encontrado com sucesso (HTTP 200): {}", customerResponseDTO.id());
        return ResponseEntity.status(200).body(customerResponseDTO);

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Exclui cliente pelo ID e retorna os dados excluídos")
    public ResponseEntity<CustomerResponseDTO> deleteCustomer(@PathVariable("id") UUID id) {

        log.info("Recebendo requisição para excluir cliente com ID: {}", id);
        CustomerResponseDTO customerResponseDTO = customerService.getCustomerById(id);

        customerService.deleteCustomer(id);


        log.info("Cliente excluído com sucesso (HTTP 200): {}", customerResponseDTO.id());
        return ResponseEntity.status(200).body(customerResponseDTO);

    }
}

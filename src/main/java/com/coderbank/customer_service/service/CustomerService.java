package com.coderbank.customer_service.service;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.exceptions.custom.CustomerNotFoundException;
import com.coderbank.customer_service.exceptions.custom.DuplicateCpfException;
import com.coderbank.customer_service.factory.CustomerFactory;
import com.coderbank.customer_service.mapper.CustomerMapper;
import com.coderbank.customer_service.model.Customer;
import com.coderbank.customer_service.repository.CustomerRepository;
import com.coderbank.customer_service.utils.LogSanitizer;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Construtor para injeção de dependência

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Outros métodos de serviço (criar, atualizar, deletar, buscar clientes, etc.

    @Transactional
    public CustomerResponseDTO createCustomer(final CustomerRequestDTO customerRequestDTO) {

        long t0 = System.currentTimeMillis();

        String safeCpf = LogSanitizer.maskCpf(customerRequestDTO.cpf());
        String safeEmail = LogSanitizer.maskEmail(customerRequestDTO.email());

       // Lógica para criar um novo cliente
        log.debug("Iniciando o Processo de Criação de Cliente com CPF: {} e Email: {}", safeCpf, safeEmail);

        Customer customer = CustomerFactory.createFromRequest(customerRequestDTO);

        Optional <Customer> existingCustomer = customerRepository.findByCpf(customerRequestDTO.cpf());
        existingCustomer.ifPresent(c -> {
            log.warn("Tentativa de criação de cliente com CPF duplicado: {}", LogSanitizer.maskCpf(customerRequestDTO.cpf()));
            throw new DuplicateCpfException("CPF duplicado: " + LogSanitizer.maskCpf(customerRequestDTO.cpf()));
        });

        customerRepository.save(customer);

        log.info("Cliente salvo com sucesso no banco: {}({} ms)", customer.getId(), System.currentTimeMillis() - t0);
        return CustomerMapper.toResponse(customer);

    }


    @Transactional  // Anotação para gerenciar transações. Caso algo dê errado a transação será revertida automáticamente. //
    public CustomerResponseDTO updateCustomer(UUID id, CustomerRequestDTO customerRequestDTO) {

        log.debug("Iniciando o processo de atualização do cliente com ID: {}", id);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cliente não encontrado com o ID: {}", id);
                    return new CustomerNotFoundException("Cliente não encontrado com o ID: " + id);
                });

        CustomerMapper.updateEntity(existingCustomer, customerRequestDTO);

        Customer saved = customerRepository.save(existingCustomer);

        log.info("Cliente atualizado e salvo no banco de dados: {}", saved.getId());
        return CustomerMapper.toResponse(saved);

    }



    public List<CustomerResponseDTO> getAllCustomers(){
        // Lógica para buscar todos os clientes

        log.debug("Iniciando o processo de consulta de clientes");

        List<Customer> customers = customerRepository.findAll();

        log.debug("Clientes consultados com sucesso: {}", customers.size());
        return CustomerMapper.toResponseList(customers);


    }



    public CustomerResponseDTO getCustomerById(UUID id) {
        // Lógica para buscar um cliente pelo ID

        log.debug("Iniciando o processo de consulta do cliente com ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->{

                    log.warn("Cliente não encontrado com o ID: {}", id);
                            return new CustomerNotFoundException("Cliente não encontrado com o ID: " + id);
                        }
                );

        return CustomerMapper.toResponse(customer);
    }


    @Transactional
    public void deleteCustomer(UUID id) {
        // Lógica para deletar um cliente pelo ID

        log.debug("Iniciando o processo de exclusão do cliente com ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cliente não encontrado para exclusão. ID: {}", id);
                    return new CustomerNotFoundException("Cliente não encontrado com o ID: " + id);
                });


        log.info("Cliente encontrado para exclusão: {}", customer.getId());
        customerRepository.delete(customer);
    }


}




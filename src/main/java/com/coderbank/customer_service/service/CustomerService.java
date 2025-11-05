package com.coderbank.customer_service.service;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.exceptions.custom.CustomerNotFoundException;
import com.coderbank.customer_service.exceptions.custom.DuplicateCpfException;
import com.coderbank.customer_service.factory.CustomerFactory;
import com.coderbank.customer_service.mapper.CustomerMapper;
import com.coderbank.customer_service.model.Customer;
import com.coderbank.customer_service.repository.CustomerRepository;
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

    private CustomerRepository customerRepository;

    // Construtor para injeção de dependência
    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Outros métodos de serviço (criar, atualizar, deletar, buscar clientes, etc.

    @Transactional
    public CustomerResponseDTO createCustomer(final CustomerRequestDTO customerRequestDTO) {

        String maskedCpf = customerRequestDTO.cpf().substring(0, 3) + "*****" + customerRequestDTO.cpf().substring(8);
       // Lógica para criar um novo cliente
        log.debug("Iniciando o Processo de Criação de Cliente com CPF: {}", maskedCpf);

        Customer customer = CustomerFactory.createFromRequest(customerRequestDTO);

        Optional <Customer> existingCustomer = customerRepository.findByCpf(customerRequestDTO.cpf());
        existingCustomer.ifPresent(c -> {
            log.warn("Tentativa de criação de cliente com CPF duplicado: {}", customer.getId());
            throw new DuplicateCpfException("CPF duplicado: " + customer.getId());
        });

        customerRepository.save(customer);

        log.info("Cliente salvo com sucesso no banco: {}", customer.getId());
        return CustomerMapper.toResponse(customer);



    }


    @Transactional  // Anotação para gerenciar transações. Caso algo dê errado a transação será revertida automáticamente. //
    public CustomerResponseDTO updateCustomer(UUID id, CustomerRequestDTO customerRequestDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Cliente não encontrado com o ID: " + id));

        CustomerMapper.updateEntity(existingCustomer, customerRequestDTO);

        Customer saved = customerRepository.save(existingCustomer);
        return CustomerMapper.toResponse(saved);
    }


    public List<CustomerResponseDTO> getAllCustomers(){
        // Lógica para buscar todos os clientes

        List<Customer> customers = customerRepository.findAll();
        return CustomerMapper.toResponseList(customers);





    }


    public CustomerResponseDTO getCustomerById(UUID id) {
        // Lógica para buscar um cliente pelo ID

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Cliente não encontrado com o ID: " + id));

        return CustomerMapper.toResponse(customer);
    }

    @Transactional
    public void deleteCustomer(UUID id) {
        // Lógica para deletar um cliente pelo ID

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Cliente não encontrado com o ID: " + id));

        customerRepository.delete(customer);
    }


}




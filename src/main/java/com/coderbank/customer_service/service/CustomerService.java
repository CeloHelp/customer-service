package com.coderbank.customer_service.service;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.exceptions.custom.CustomerNotFoundException;
import com.coderbank.customer_service.exceptions.custom.DuplicateCpfException;
import com.coderbank.customer_service.factory.CustomerFactory;
import com.coderbank.customer_service.mapper.CustomerMapper;
import com.coderbank.customer_service.model.Customer;
import com.coderbank.customer_service.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
       // Lógica para criar um novo cliente

        Customer customer = CustomerFactory.createFromRequest(customerRequestDTO);

        Optional <Customer> existingCustomer = customerRepository.findByCpf(customer.getCpf());
        existingCustomer.ifPresent(c -> {
            throw new DuplicateCpfException("CPF duplicado: " + customer.getCpf());
        });

        customerRepository.save(customer);

        return CustomerMapper.toResponse(customer);



    }


    @Transactional  // Anotação para gerenciar transações. Caso algo dê errado a transação será revertida automáticamente. //
    public CustomerResponseDTO updateCustomer(UUID id, CustomerRequestDTO customerRequestDTO) {
        // Lógica para atualizar um cliente

       Optional<Customer> existingCustomer = Optional.ofNullable(customerRepository.findById(id).orElseThrow(()
               -> new CustomerNotFoundException("Cliente não encontrado com o ID: " + id)));


       if(customerRepository.existsById(id)){
           customerRepository.save(CustomerFactory.createFromRequest(customerRequestDTO));
       }

        Customer customerToUpdate = existingCustomer.get();
        CustomerMapper.updateEntity(customerToUpdate, customerRequestDTO);
        customerRepository.save(customerToUpdate);
        return CustomerMapper.toResponse(customerToUpdate);



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




package com.coderbank.customer_service.service;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.exceptions.custom.CustomerNotFoundException;
import com.coderbank.customer_service.exceptions.custom.DuplicateCpfException;
import com.coderbank.customer_service.model.Customer;
import com.coderbank.customer_service.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_success_setsIdAndReturnsResponse() {
        CustomerRequestDTO req = new CustomerRequestDTO("João", "12345678901", "joao@example.com", "Rua X, 1");

        when(customerRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CustomerResponseDTO resp = customerService.createCustomer(req);

        assertNotNull(resp);
        assertNotNull(resp.id());
        assertEquals("João", resp.name());
        assertEquals("12345678901", resp.cpf());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateCpf_throwsDuplicateCpfException() {
        CustomerRequestDTO req = new CustomerRequestDTO("João", "12345678901", "joao@example.com", "Rua X, 1");
        Customer existing = new Customer("Existing", "12345678901", "e@example.com", "Somewhere");
        existing.setId(UUID.randomUUID());

        when(customerRepository.findByCpf(anyString())).thenReturn(Optional.of(existing));

        assertThrows(DuplicateCpfException.class, () -> customerService.createCustomer(req));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getAllCustomers_mapsList() {
        Customer c1 = new Customer("A", "11122233344", "a@example.com", "X");
        c1.setId(UUID.randomUUID());
        Customer c2 = new Customer("B", "55566677788", "b@example.com", "Y");
        c2.setId(UUID.randomUUID());

        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        var dtos = customerService.getAllCustomers();

        assertEquals(2, dtos.size());
        assertEquals(c1.getName(), dtos.get(0).name());
    }

    @Test
    void getCustomerById_found_returnsResponse() {
        UUID id = UUID.randomUUID();
        Customer c = new Customer("Z", "99988877766", "z@example.com", "Z street");
        c.setId(id);

        when(customerRepository.findById(id)).thenReturn(Optional.of(c));

        var resp = customerService.getCustomerById(id);

        assertEquals(id.toString(), resp.id());
        assertEquals("Z", resp.name());
    }

    @Test
    void getCustomerById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(id));
    }

    @Test
    void updateCustomer_existing_updatesAndReturns() {
        UUID id = UUID.randomUUID();
        Customer existing = new Customer("Old", "11122233344", "old@example.com", "Old addr");
        existing.setId(id);

        CustomerRequestDTO req = new CustomerRequestDTO("New", "11122233344", "new@example.com", "New addr");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resp = customerService.updateCustomer(id, req);

        assertEquals("New", resp.name());
        assertEquals("new@example.com", resp.email());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_savesSameEntityInstance() {
        UUID id = UUID.randomUUID();
        Customer existing = new Customer("Old", "11122233344", "old@example.com", "Old addr");
        existing.setId(id);

        CustomerRequestDTO req = new CustomerRequestDTO("New", "11122233344", "new@example.com", "New addr");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerService.updateCustomer(id, req);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer savedEntity = captor.getValue();

        assertSame(existing, savedEntity);
        assertEquals(id, savedEntity.getId());
        assertEquals("New", savedEntity.getName());
        assertEquals("new@example.com", savedEntity.getEmail());
        assertEquals("New addr", savedEntity.getAddress());
    }

    @Test
    void updateCustomer_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        CustomerRequestDTO req = new CustomerRequestDTO("New", "11122233344", "new@example.com", "New addr");

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(id, req));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(customerRepository).findById(id);
    }

    @Test
    void deleteCustomer_existing_deletes() {
        UUID id = UUID.randomUUID();
        Customer c = new Customer("Del", "33322211100", "d@example.com", "D addr");
        c.setId(id);

        when(customerRepository.findById(id)).thenReturn(Optional.of(c));

        customerService.deleteCustomer(id);

        verify(customerRepository).delete(c);
    }

}


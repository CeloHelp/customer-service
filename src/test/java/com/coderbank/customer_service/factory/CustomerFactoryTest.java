package com.coderbank.customer_service.factory;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.factory.CustomerFactory;
import com.coderbank.customer_service.model.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerFactoryTest {

    @Test
    void createFromRequest_mapsAllFields() {
        CustomerRequestDTO req = new CustomerRequestDTO("João Silva", "12345678901", "joao@example.com", "Rua A, 123");

        Customer customer = CustomerFactory.createFromRequest(req);

        assertNotNull(customer);
        assertEquals("João Silva", customer.getName());
        assertEquals("12345678901", customer.getCpf());
        assertEquals("joao@example.com", customer.getEmail());
        assertEquals("Rua A, 123", customer.getAddress());
    }
}


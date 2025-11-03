package com.coderbank.customer_service.mapper;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.mapper.CustomerMapper;
import com.coderbank.customer_service.model.Customer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer("Maria", "98765432100", "maria@example.com", "Av B, 456");
        customer.setId(id);

        var dto = CustomerMapper.toResponse(customer);

        assertNotNull(dto);
        assertEquals(id.toString(), dto.id());
        assertEquals("Maria", dto.name());
        assertEquals("98765432100", dto.cpf());
        assertEquals("maria@example.com", dto.email());
        assertEquals("Av B, 456", dto.address());
    }

    @Test
    void toResponseList_mapsList() {
        Customer c1 = new Customer("A", "11122233344", "a@example.com", "X");
        c1.setId(UUID.randomUUID());
        Customer c2 = new Customer("B", "55566677788", "b@example.com", "Y");
        c2.setId(UUID.randomUUID());

        List<CustomerResponseDTO> dtos = CustomerMapper.toResponseList(List.of(c1, c2));

        assertEquals(2, dtos.size());
        assertEquals(c1.getName(), dtos.get(0).name());
        assertEquals(c2.getCpf(), dtos.get(1).cpf());
    }

    @Test
    void updateEntity_updatesMutableFieldsOnly() {
        Customer entity = new Customer("Old", "00011122233", "old@example.com", "Old address");
        entity.setId(UUID.randomUUID());

        CustomerRequestDTO req = new CustomerRequestDTO("New Name", "00011122233", "new@example.com", "New address");

        CustomerMapper.updateEntity(entity, req);

        assertEquals("New Name", entity.getName());
        assertEquals("00011122233", entity.getCpf()); // cpf must remain same since updateEntity doesn't change cpf
        assertEquals("new@example.com", entity.getEmail());
        assertEquals("New address", entity.getAddress());
    }
}


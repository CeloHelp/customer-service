package com.coderbank.customer_service.controller;

import com.coderbank.customer_service.dto.request.CustomerRequestDTO;
import com.coderbank.customer_service.dto.response.CustomerResponseDTO;
import com.coderbank.customer_service.exceptions.custom.CustomerNotFoundException;
import com.coderbank.customer_service.exceptions.custom.DuplicateCpfException;
import com.coderbank.customer_service.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CustomerControllerV1.class)
class CustomerControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("POST /api/v1/customers - success (201)")
    void createCustomer_success() throws Exception {
        CustomerRequestDTO req = new CustomerRequestDTO("João", "52998224725", "joao@example.com", "Rua X, 1");
        CustomerResponseDTO resp = new CustomerResponseDTO(UUID.randomUUID().toString(), "João", "52998224725", "joao@example.com", "Rua X, 1");

        when(customerService.createCustomer(any(CustomerRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.cpf").value("52998224725"));
    }

    @Test
    @DisplayName("POST /api/v1/customers - duplicate cpf -> 400")
    void createCustomer_duplicateCpf_returnsBadRequest() throws Exception {
        CustomerRequestDTO req = new CustomerRequestDTO("João", "52998224725", "joao@example.com", "Rua X, 1");

        when(customerService.createCustomer(any(CustomerRequestDTO.class))).thenThrow(new DuplicateCpfException("CPF duplicado"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CPF duplicado"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} - success (201)")
    void updateCustomer_success() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerRequestDTO req = new CustomerRequestDTO("New Name", "52998224725", "new@example.com", "New addr");
        CustomerResponseDTO resp = new CustomerResponseDTO(id.toString(), "New Name", "52998224725", "new@example.com", "New addr");

        when(customerService.updateCustomer(Mockito.eq(id), any(CustomerRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} - not found -> 404")
    void updateCustomer_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerRequestDTO req = new CustomerRequestDTO("New Name", "52998224725", "new@example.com", "New addr");

        when(customerService.updateCustomer(Mockito.eq(id), any(CustomerRequestDTO.class))).thenThrow(new CustomerNotFoundException("Cliente não encontrado"));

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente não encontrado"));
    }

    @Test
    @DisplayName("GET /api/v1/customers - non empty -> 200")
    void getAllCustomers_nonEmpty() throws Exception {
        CustomerResponseDTO c1 = new CustomerResponseDTO(UUID.randomUUID().toString(), "A", "11122233344", "a@example.com", "X");
        CustomerResponseDTO c2 = new CustomerResponseDTO(UUID.randomUUID().toString(), "B", "55566677788", "b@example.com", "Y");

        when(customerService.getAllCustomers()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/customers - empty -> 204")
    void getAllCustomers_empty() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - success -> 200")
    void getCustomerById_success() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponseDTO resp = new CustomerResponseDTO(id.toString(), "Z", "99988877766", "z@example.com", "Z street");

        when(customerService.getCustomerById(id)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Z"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - not found -> 404")
    void getCustomerById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new CustomerNotFoundException("Cliente não encontrado"));

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente não encontrado"));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id} - success -> 200")
    void deleteCustomer_success() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponseDTO resp = new CustomerResponseDTO(id.toString(), "Del", "33322211100", "d@example.com", "D addr");

        when(customerService.getCustomerById(id)).thenReturn(resp);
        doNothing().when(customerService).deleteCustomer(id);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id} - not found -> 404")
    void deleteCustomer_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new CustomerNotFoundException("Cliente não encontrado"));

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente não encontrado"));
    }

}

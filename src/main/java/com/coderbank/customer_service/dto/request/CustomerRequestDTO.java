package com.coderbank.customer_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record CustomerRequestDTO(

        @NotBlank(message = "O nome deve ser preenchido")
        String name,

        @NotBlank(message = "O CPF deve ser preenchido")
        @CPF(message = " O CPF informado é inválido")
        String cpf,

        String email,

        String address
) {
        public String getName() {
                return name;
        }
        public String getCpf() {
                return cpf;
        }
        public String getEmail() {
                return email;

        }
        public String getAddress() {
                return address;
        }

}

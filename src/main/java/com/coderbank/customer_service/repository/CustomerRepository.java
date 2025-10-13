package com.coderbank.customer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository {
    Optional findByCpf(String cpf);
    Optional findByEmail(String email);

}

package com.coderbank.customer_service.repository;

import com.coderbank.customer_service.model.Customer;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional findByCpf(String cpf);
    Optional findByEmail(String email);

    List<Customer> id(UUID id);

    Optional<Customer> findByid(UUID id);

    Optional<Customer> findById(SingularAttribute<AbstractPersistable, Serializable> id);


}

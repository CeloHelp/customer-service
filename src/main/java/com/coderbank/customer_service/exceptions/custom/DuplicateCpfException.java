package com.coderbank.customer_service.exceptions.custom;

public class DuplicateCpfException extends RuntimeException {
    public DuplicateCpfException(String message) {
        super("CPF duplicado: " + message);
    }
}

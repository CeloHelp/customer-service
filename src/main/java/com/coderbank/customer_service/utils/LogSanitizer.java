package com.coderbank.customer_service.utils;

import java.util.Objects;
import org.slf4j.Logger;

public class LogSanitizer {
    private LogSanitizer() {

    }

    public static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "Invalid CPF";
        }
        return cpf.substring(0, 3) + "*****" + cpf.substring(8);
    }

    public static String maskEmail(String email){
        if(email == null || !email.contains("@")) return "EMAIL_INVALIDO";
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        return localPart.charAt(0) + "*****@" + domainPart;


    }

    public static void safeInfo(Logger log, String message, Object... params){
        if (log.isInfoEnabled()){
            log.info("[SAFE] " + message, params);
        }
    }
}

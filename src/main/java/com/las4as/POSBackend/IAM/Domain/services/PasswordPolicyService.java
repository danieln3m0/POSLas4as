package com.las4as.POSBackend.IAM.Domain.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    
    public PasswordValidationResult validatePassword(String password) {
        List<String> violations = new ArrayList<>();
        
        if (password == null || password.isEmpty()) {
            violations.add("La contraseña es requerida");
            return new PasswordValidationResult(false, violations);
        }
        
        if (password.length() < MIN_LENGTH) {
            violations.add("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        
        if (password.length() > MAX_LENGTH) {
            violations.add("La contraseña no puede tener más de " + MAX_LENGTH + " caracteres");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            violations.add("La contraseña debe contener al menos una letra mayúscula");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            violations.add("La contraseña debe contener al menos una letra minúscula");
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            violations.add("La contraseña debe contener al menos un número");
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            violations.add("La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{}|;':\"\\,.<>/?)");
        }
        
        // Verificar patrones comunes débiles
        if (containsCommonWeakPatterns(password)) {
            violations.add("La contraseña contiene patrones comunes que la hacen vulnerable");
        }
        
        return new PasswordValidationResult(violations.isEmpty(), violations);
    }
    
    private boolean containsCommonWeakPatterns(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Patrones comunes débiles
        String[] weakPatterns = {
            "123456", "password", "admin", "qwerty", "abc123", 
            "letmein", "welcome", "monkey", "dragon", "master",
            "pos", "sistema", "usuario", "clave"
        };
        
        for (String pattern : weakPatterns) {
            if (lowerPassword.contains(pattern)) {
                return true;
            }
        }
        
        // Verificar secuencias
        if (containsSequence(password)) {
            return true;
        }
        
        // Verificar repeticiones
        return containsRepeatedCharacters(password);
    }
    
    private boolean containsSequence(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char current = password.charAt(i);
            char next1 = password.charAt(i + 1);
            char next2 = password.charAt(i + 2);
            
            if (next1 == current + 1 && next2 == current + 2) {
                return true; // Secuencia ascendente como 123, abc
            }
            if (next1 == current - 1 && next2 == current - 2) {
                return true; // Secuencia descendente como 321, cba
            }
        }
        return false;
    }
    
    private boolean containsRepeatedCharacters(String password) {
        int maxRepeats = 3;
        int currentRepeats = 1;
        
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                currentRepeats++;
                if (currentRepeats >= maxRepeats) {
                    return true;
                }
            } else {
                currentRepeats = 1;
            }
        }
        return false;
    }
    
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Longitud
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        
        // Complejidad
        if (UPPERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (DIGIT_PATTERN.matcher(password).matches()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score += 20;
        
        // Penalizar patrones débiles
        if (containsCommonWeakPatterns(password)) {
            score -= 30;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> violations;
        
        public PasswordValidationResult(boolean valid, List<String> violations) {
            this.valid = valid;
            this.violations = violations;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getViolations() {
            return violations;
        }
    }
}

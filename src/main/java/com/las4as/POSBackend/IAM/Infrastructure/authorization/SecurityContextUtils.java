package com.las4as.POSBackend.IAM.Infrastructure.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para obtener información del usuario actual autenticado
 */
@Component
public class SecurityContextUtils {
    
    /**
     * Obtiene el ID del usuario actual desde el contexto de seguridad
     * @return ID del usuario actual o null si no está autenticado
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        // Si el principal es una instancia de UserPrincipal personalizado
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        
        // Si el principal es una instancia de User de Spring Security
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            // En este caso, el username debería ser el ID del usuario
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene el nombre de usuario actual desde el contexto de seguridad
     * @return nombre de usuario actual o null si no está autenticado
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getName();
    }
    
    /**
     * Obtiene los roles del usuario actual
     * @return array de roles o array vacío si no está autenticado
     */
    public static String[] getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return new String[0];
        }
        
        return authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5)) // Remueve el prefijo "ROLE_"
            .toArray(String[]::new);
    }
    
    /**
     * Verifica si el usuario actual tiene un rol específico
     * @param role el rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public static boolean currentUserHasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getName());
    }
}

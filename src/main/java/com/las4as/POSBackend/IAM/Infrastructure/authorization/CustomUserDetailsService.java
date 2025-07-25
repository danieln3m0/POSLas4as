package com.las4as.POSBackend.IAM.Infrastructure.authorization;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio personalizado para cargar detalles del usuario para autenticación
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameValue(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));
        
        return UserPrincipal.create(user);
    }
    
    /**
     * Carga un usuario por su ID (útil para tokens JWT)
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id));
        
        return UserPrincipal.create(user);
    }
    
    /**
     * Obtiene el User entity por username
     */
    @Transactional
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameValue(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));
    }
    
    /**
     * Obtiene el User entity por ID
     */
    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id));
    }
}

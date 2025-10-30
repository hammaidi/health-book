package com.healthbook.service;

import com.healthbook.entity.Role;
import com.healthbook.entity.User;
import com.healthbook.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
    }

    public User createUser(User user) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        // Sauvegarder en base de données
        return userRepository.save(user);
    }

    // Méthode pour créer un utilisateur de test (optionnel)
    public void createTestUsers() {
        // Créer un admin de test (optionnel)
        if (!userRepository.existsByUsername("admin@healthbook.com")) {
            User admin = new User();
            admin.setUsername("admin@healthbook.com");
            admin.setPassword("$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ012345"); // mot de passe hashé
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
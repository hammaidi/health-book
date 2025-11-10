package com.healthbook.service;

import com.healthbook.entity.Role;
import com.healthbook.entity.User;
import com.healthbook.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //  CONFIGURATION EXTERNE
    @Value("${app.admin.email:admin@healthbook.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.auto-create:true}")
    private boolean autoCreateAdmin;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================
    // CRÉATION AUTO DE L'ADMIN AVEC CONFIG EXTERNE
    // ========================
    @PostConstruct
    public void createDefaultAdmin() {
        if (!autoCreateAdmin) {
            System.out.println("⚙️  Création auto admin désactivée");
            return;
        }

        try {
            if (!userRepository.existsByUsername(adminEmail)) {
                User admin = new User();
                admin.setUsername(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                
                userRepository.save(admin);
                System.out.println("🎉 ===================================");
                System.out.println("✅ COMPTE ADMIN CRÉÉ AVEC SUCCÈS !");
                System.out.println("📧 Email: " + adminEmail);
                System.out.println("🔑 Mot de passe: " + adminPassword);
                System.out.println("👑 Rôle: ADMINISTRATEUR");
                System.out.println("🎉 ===================================");
            } else {
                System.out.println("ℹ️  Compte admin existe déjà: " + adminEmail);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur création admin: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
    }
    
 
    public boolean userExists(String email) {
        return userRepository.findByUsername(email).isPresent();
    }
    
    
    
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        return userRepository.save(user);
    }
}
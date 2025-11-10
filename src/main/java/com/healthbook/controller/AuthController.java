package com.healthbook.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.healthbook.entity.Medecin;
import com.healthbook.entity.Patient;
import com.healthbook.entity.Role;
import com.healthbook.entity.User;
import com.healthbook.service.MedecinService;
import com.healthbook.service.PatientService;
import com.healthbook.service.UserService;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    private final PatientService patientService;
    private final MedecinService medecinService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PatientService patientService, 
                         MedecinService medecinService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.patientService = patientService;
        this.medecinService = medecinService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String role,
                         @RequestParam String nom,
                         @RequestParam String prenom,
                         @RequestParam(required = false) String telephone,
                         @RequestParam(required = false) String dateNaissance,
                         @RequestParam(required = false) String specialite,
                         Model model) {
        
        try {
            // ============================================
            // 🔒 VALIDATION MÉTIER RENFORCÉE
            // ============================================
            
            // 1. Validation des champs obligatoires
            if (email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty() ||
                nom == null || nom.trim().isEmpty() || 
                prenom == null || prenom.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
                
                model.addAttribute("error", "Tous les champs obligatoires doivent être remplis");
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // 2. Validation format email
            if (!isValidEmail(email)) {
                model.addAttribute("error", "Format d'email invalide");
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // 3. 🔒 VALIDATION ROBUSTE MOT DE PASSE
            String passwordError = validatePassword(password);
            if (passwordError != null) {
                model.addAttribute("error", passwordError);
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // 4. Validation noms
            if (!isValidName(nom) || !isValidName(prenom)) {
                model.addAttribute("error", "Le nom et prénom ne doivent contenir que des lettres, espaces et tirets (2-100 caractères)");
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // 5. Validation rôle
            Role userRole;
            try {
                userRole = Role.valueOf(role.toUpperCase());
                if (!Arrays.asList(Role.PATIENT, Role.MEDECIN).contains(userRole)) {
                    model.addAttribute("error", "Rôle invalide");
                    model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                    return "auth/register";
                }
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Rôle invalide");
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // 6. Validation téléphone
            if (telephone != null && !telephone.trim().isEmpty() && !isValidTelephone(telephone)) {
                model.addAttribute("error", "Format de téléphone invalide (10-20 chiffres)");
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // 7. Validation date de naissance
            LocalDate parsedDateNaissance = null;
            if (dateNaissance != null && !dateNaissance.trim().isEmpty()) {
                try {
                    parsedDateNaissance = LocalDate.parse(dateNaissance);
                    if (parsedDateNaissance.isAfter(LocalDate.now().minusYears(1))) {
                        model.addAttribute("error", "La date de naissance doit être dans le passé");
                        model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                        return "auth/register";
                    }
                } catch (DateTimeParseException e) {
                    model.addAttribute("error", "Format de date invalide (YYYY-MM-JJ)");
                    model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                    return "auth/register";
                }
            }

            // 8. Vérification email unique
            if (userService.userExists(email)) {
                model.addAttribute("error", "Cet email est déjà utilisé");
                model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
                return "auth/register";
            }

            // ============================================
            // ✅ CRÉATION DU COMPTE
            // ============================================
            
            logger.info("Création compte: {} {} ({})", prenom, nom, userRole);

            Patient patient = null;
            Medecin medecin = null;
            
            if (userRole == Role.PATIENT) {
                patient = new Patient();
                patient.setNom(nom.trim());
                patient.setPrenom(prenom.trim());
                patient.setEmail(email.trim().toLowerCase());
                patient.setTelephone(telephone != null ? telephone.trim() : null);
                if (parsedDateNaissance != null) {
                    patient.setDateNaissance(parsedDateNaissance);
                }
                patient = patientService.createPatient(patient);
            } 
            else if (userRole == Role.MEDECIN) {
                medecin = new Medecin();
                medecin.setNom(nom.trim());
                medecin.setPrenom(prenom.trim());
                medecin.setEmail(email.trim().toLowerCase());
                medecin.setTelephone(telephone != null ? telephone.trim() : null);
                medecin.setSpecialite(specialite != null && !specialite.trim().isEmpty() ? specialite.trim() : "Généraliste");
                medecin = medecinService.createMedecin(medecin);
            }
            
            User user = new User();
            user.setUsername(email.trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(userRole);
            
            if (patient != null) user.setPatient(patient);
            if (medecin != null) user.setMedecin(medecin);
            
            userService.createUser(user);
            
            model.addAttribute("success", "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
            return "auth/register";
            
        } catch (Exception e) {
            logger.error("Erreur inscription: {}", e.getMessage());
            model.addAttribute("error", "Une erreur technique est survenue. Veuillez réessayer.");
            model.addAttribute("roles", Arrays.asList("PATIENT", "MEDECIN"));
            return "auth/register";
        }
    }

    // ============================================
    // 🔒 MÉTHODES DE VALIDATION ROBUSTES
    // ============================================

    private boolean isValidEmail(String email) {
        return email != null && 
               email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") && 
               email.length() <= 150;
    }

    /**
     * 🔒 VALIDATION ROBUSTE MOT DE PASSE
     * - 8 caractères minimum
     * - Au moins 3 des 4 conditions suivantes :
     *   - Majuscule
     *   - Minuscule  
     *   - Chiffre
     *   - Caractère spécial
     */
    private String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }
        
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*"); 
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        int conditions = 0;
        if (hasUpper) conditions++;
        if (hasLower) conditions++;
        if (hasDigit) conditions++;
        if (hasSpecial) conditions++;
        
        if (conditions < 3) {
            return "Le mot de passe doit contenir au moins 3 des 4 éléments suivants :\n" +
                   "- Une lettre majuscule\n" +
                   "- Une lettre minuscule\n" + 
                   "- Un chiffre\n" +
                   "- Un caractère spécial (!@#$%^&* etc.)";
        }
        
        return null; // ✅ Mot de passe valide
    }

    private boolean isValidName(String name) {
        return name != null && 
               name.matches("^[a-zA-ZÀ-ÿ\\s-]{2,100}$") && 
               name.trim().length() >= 2;
    }

    private boolean isValidTelephone(String telephone) {
        return telephone.matches("^[0-9+\\s()-]{10,20}$");
    }
}
package com.healthbook.controller;

import com.healthbook.entity.*;
import com.healthbook.service.UserService;
import com.healthbook.service.PatientService;
import com.healthbook.service.MedecinService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

@Controller
public class AuthController {

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
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
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
            System.out.println("=== DÉBUT INSCRIPTION ===");
            System.out.println("Email: " + email + ", Rôle: " + role + ", Nom: " + nom + ", Prénom: " + prenom);
            
            // Créer Patient ou Medecin selon le rôle
            Patient patient = null;
            Medecin medecin = null;
            
            if (role.equals("PATIENT")) {
                System.out.println("Création Patient...");
                patient = new Patient();
                patient.setNom(nom);
                patient.setPrenom(prenom);
                patient.setEmail(email);
                patient.setTelephone(telephone);
                if (dateNaissance != null && !dateNaissance.isEmpty()) {
                    patient.setDateNaissance(LocalDate.parse(dateNaissance));
                }
                patient = patientService.createPatient(patient);
                System.out.println("Patient créé ID: " + patient.getId());
            } 
            else if (role.equals("MEDECIN")) {
                System.out.println("Création Medecin...");
                medecin = new Medecin();
                medecin.setNom(nom);
                medecin.setPrenom(prenom);
                medecin.setEmail(email);
                medecin.setTelephone(telephone);
                medecin.setSpecialite(specialite != null ? specialite : "Généraliste");
                medecin = medecinService.createMedecin(medecin);
                System.out.println("Medecin créé ID: " + medecin.getId());
            }
            
            // Créer le User
            User user = new User();
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(Role.valueOf(role));
            
            // Lier le User au Patient/Medecin
            if (patient != null) {
                user.setPatient(patient);
            }
            if (medecin != null) {
                user.setMedecin(medecin);
            }
            
            // Sauvegarder le User
            User savedUser = userService.createUser(user);
            System.out.println("User créé ID: " + savedUser.getId());
            
            model.addAttribute("success", "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            return "auth/register";
            
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur : " + e.getMessage());
            return "auth/register";
        }
    }
}
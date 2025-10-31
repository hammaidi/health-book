package com.healthbook.controller;

import com.healthbook.entity.*;
import com.healthbook.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class DashboardController {

    private final RendezVousService rendezVousService;
    private final UserService userService;
    private final PatientService patientService;
    private final MedecinService medecinService;

    public DashboardController(RendezVousService rendezVousService, 
                             UserService userService,
                             PatientService patientService,
                             MedecinService medecinService) {
        this.rendezVousService = rendezVousService;
        this.userService = userService;
        this.patientService = patientService;
        this.medecinService = medecinService;
    }

    // ========================
    // DASHBOARD PRINCIPAL - GET /dashboard
    // ========================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletResponse response) {
        // 🔥 DÉSACTIVER le cache pour forcer le rafraîchissement
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = (User) userService.loadUserByUsername(username);

        // 🔥 FORCER le rechargement des RDV à CHAQUE appel
        var rendezVous = rendezVousService.getRendezVousByUser(user);
        model.addAttribute("rendezvous", rendezVous);
        model.addAttribute("user", user);

        // Rediriger vers le template approprié
        switch (user.getRole()) {
            case PATIENT:
                return "dashboard/patient";
            case MEDECIN:
                return "dashboard/medecin";
            case ADMIN:
                return "dashboard/admin";
            default:
                return "redirect:/";
        }
    }

    // ========================
    // FORMULAIRE ÉDITION PROFIL - GET /dashboard/edit
    // ========================
    @GetMapping("/dashboard/edit")
    public String editProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = (User) userService.loadUserByUsername(username);
        
        model.addAttribute("user", user);
        
        // Pré-remplir avec les données existantes
        if (user.getRole() == Role.PATIENT && user.getPatient() != null) {
            model.addAttribute("person", user.getPatient());
        } else if (user.getRole() == Role.MEDECIN && user.getMedecin() != null) {
            model.addAttribute("person", user.getMedecin());
        }
        
        return "dashboard/edit-profile";
    }

    // ========================
    // TRAITEMENT ÉDITION PROFIL - POST /dashboard/edit
    // ========================
    @PostMapping("/dashboard/edit")
    public String updateProfile(@RequestParam String nom,
                              @RequestParam String prenom,
                              @RequestParam(required = false) String telephone,
                              Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);
            
            // Mettre à jour selon le rôle
            if (user.getRole() == Role.PATIENT && user.getPatient() != null) {
                Patient patient = user.getPatient();
                patient.setNom(nom);
                patient.setPrenom(prenom);
                patient.setTelephone(telephone);
                patientService.updatePatient(patient.getId(), patient);
            } else if (user.getRole() == Role.MEDECIN && user.getMedecin() != null) {
                Medecin medecin = user.getMedecin();
                medecin.setNom(nom);
                medecin.setPrenom(prenom);
                medecin.setTelephone(telephone);
                medecinService.updateMedecin(medecin.getId(), medecin);
            }
            
            return "redirect:/dashboard?success=Profil+mis+à+jour";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "dashboard/edit-profile";
        }
    }
}
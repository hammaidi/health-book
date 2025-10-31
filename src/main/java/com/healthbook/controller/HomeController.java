package com.healthbook.controller;

import com.healthbook.entity.User;
import com.healthbook.entity.Role;
import com.healthbook.service.PatientService;
import com.healthbook.service.MedecinService;
import com.healthbook.service.RendezVousService;
import com.healthbook.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PatientService patientService;
    private final MedecinService medecinService;
    private final RendezVousService rendezVousService;
    private final UserService userService;

    public HomeController(PatientService patientService,
                         MedecinService medecinService,
                         RendezVousService rendezVousService,
                         UserService userService) {
        this.patientService = patientService;
        this.medecinService = medecinService;
        this.rendezVousService = rendezVousService;
        this.userService = userService;
    }

    // ========================
    // PAGE D'ACCUEIL - http://localhost:8081/
    // ========================
    @GetMapping("/")
    public String home(Model model) {
        // Récupérer l'utilisateur connecté (si connecté)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && 
                                 !authentication.getName().equals("anonymousUser");
        
        if (isAuthenticated) {
            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);
            model.addAttribute("currentUser", user);
        }
        
        // Statistiques globales (visibles par tous)
        long totalPatients = patientService.countPatients();
        long totalMedecins = medecinService.getAllMedecins().size();
        
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalMedecins", totalMedecins);
        model.addAttribute("prochainsRDV", rendezVousService.getProchainsRendezVous());
        
        return "home";
    }
}
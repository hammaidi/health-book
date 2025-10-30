package com.healthbook.controller;

import com.healthbook.entity.User;
import com.healthbook.service.RendezVousService;
import com.healthbook.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final RendezVousService rendezVousService;
    private final UserService userService;

    public DashboardController(RendezVousService rendezVousService, UserService userService) {
        this.rendezVousService = rendezVousService;
        this.userService = userService;
    }

    // ========================
    // DASHBOARD PRINCIPAL - GET /dashboard
    // ========================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = (User) userService.loadUserByUsername(username);

        // RDV selon le rôle
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
}
package com.healthbook.controller;

import com.healthbook.entity.Medecin;
import com.healthbook.entity.Patient;
import com.healthbook.entity.Role;
import com.healthbook.entity.RendezVous;
import com.healthbook.entity.User;
import com.healthbook.service.MedecinService;
import com.healthbook.service.PatientService;
import com.healthbook.service.RendezVousService;
import com.healthbook.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/rendezvous")
public class RendezVousController {

    private final RendezVousService rendezVousService;
    private final PatientService patientService;
    private final MedecinService medecinService;
    private final UserService userService;

    public RendezVousController(RendezVousService rendezVousService,
                              PatientService patientService,
                              MedecinService medecinService,
                              UserService userService) {
        this.rendezVousService = rendezVousService;
        this.patientService = patientService;
        this.medecinService = medecinService;
        this.userService = userService;
    }

    // ========================
    // MÉTHODE UTILITAIRE - Récupérer l'utilisateur connecté
    // ========================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return (User) userService.loadUserByUsername(username);
    }

    // ========================
    // FORMULAIRE RDV - GET /rendezvous/new
    // ========================
    @GetMapping("/new")
    public String showRendezVousForm(Model model) {
        User currentUser = getCurrentUser();
        
        // 🔒 SÉCURITÉ : Filtrer les patients selon le rôle
        if (currentUser.getRole() == Role.ADMIN) {
            // Admin : voir tous les patients et médecins
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("medecins", medecinService.getAllMedecins());
        } else if (currentUser.getRole() == Role.PATIENT) {
            // Patient : ne peut prendre RDV que pour lui-même
            model.addAttribute("patients", List.of(currentUser.getPatient()));
            model.addAttribute("medecins", medecinService.getAllMedecins());
        } else if (currentUser.getRole() == Role.MEDECIN) {
            // Médecin : ne peut pas prendre de RDV
            return "redirect:/dashboard?error=Accès+refusé";
        }
        
        return "rendezvous/form";
    }

    // ========================
    // PRISE DE RDV - POST /rendezvous/new
    // ========================
    @PostMapping("/new")
    public String prendreRendezVous(@RequestParam Long patientId,
                                  @RequestParam Long medecinId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeure,
                                  @RequestParam String motif,
                                  Model model) {
        User currentUser = getCurrentUser();
        
        // 🔒 SÉCURITÉ : Vérifier les permissions
        if (currentUser.getRole() == Role.PATIENT && 
            !currentUser.getPatient().getId().equals(patientId)) {
            return "redirect:/dashboard?error=Accès+refusé";
        }
        
        try {
            var rdv = rendezVousService.prendreRendezVous(patientId, medecinId, dateHeure, motif);
            return "redirect:/rendezvous?success=RDV+pris+avec+succès";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            
            // Recharger les listes selon le rôle
            if (currentUser.getRole() == Role.ADMIN) {
                model.addAttribute("patients", patientService.getAllPatients());
                model.addAttribute("medecins", medecinService.getAllMedecins());
            } else if (currentUser.getRole() == Role.PATIENT) {
                model.addAttribute("patients", List.of(currentUser.getPatient()));
                model.addAttribute("medecins", medecinService.getAllMedecins());
            }
            
            return "rendezvous/form";
        }
    }

    // ========================
    // LISTE RDV - GET /rendezvous
    // ========================
    @GetMapping
    public String listRendezVous(Model model) {
        User currentUser = getCurrentUser();
        
        // 🔒 SÉCURITÉ : Utiliser la méthode sécurisée
        var rendezvous = rendezVousService.getRendezVousByUser(currentUser);
        model.addAttribute("rendezvous", rendezvous);
        
        return "rendezvous/list";
    }

    // ========================
    // CONFIRMER UN RDV - POST /rendezvous/{id}/confirm
    // ========================
    @PostMapping("/{id}/confirm")
    public String confirmRendezVous(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            RendezVous rdv = rendezVousService.findById(id)
                    .orElseThrow(() -> new RuntimeException("RDV non trouvé"));
            
            // 🔒 SÉCURITÉ : Seul le médecin concerné peut confirmer
            if (currentUser.getRole() == Role.MEDECIN && 
                currentUser.getMedecin() != null &&
                currentUser.getMedecin().getId().equals(rdv.getMedecin().getId())) {
                
                rendezVousService.confirmerRendezVous(id);
                return "redirect:/dashboard?success=RDV+confirmé";
            }
            
            return "redirect:/dashboard?error=Accès+refusé";
        } catch (Exception e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    // ========================
    // ANNULER UN RDV - POST /rendezvous/{id}/cancel  
    // ========================
    @PostMapping("/{id}/cancel")
    public String cancelRendezVous(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            RendezVous rdv = rendezVousService.findById(id)
                    .orElseThrow(() -> new RuntimeException("RDV non trouvé"));
            
            // 🔒 SÉCURITÉ : Patient peut annuler ses RDV, médecin peut annuler ses consultations
            boolean canCancel = false;
            
            if (currentUser.getRole() == Role.PATIENT && 
                currentUser.getPatient() != null &&
                currentUser.getPatient().getId().equals(rdv.getPatient().getId())) {
                canCancel = true;
            } else if (currentUser.getRole() == Role.MEDECIN && 
                       currentUser.getMedecin() != null &&
                       currentUser.getMedecin().getId().equals(rdv.getMedecin().getId())) {
                canCancel = true;
            } else if (currentUser.getRole() == Role.ADMIN) {
                canCancel = true;
            }
            
            if (canCancel) {
                rendezVousService.annulerRendezVous(id);
                return "redirect:/dashboard?success=RDV+annulé";
            }
            
            return "redirect:/dashboard?error=Accès+refusé";
        } catch (Exception e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }
}
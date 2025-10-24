package com.healthbook.controller;

import com.healthbook.service.MedecinService;
import com.healthbook.service.PatientService;
import com.healthbook.service.RendezVousService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/rendezvous")
public class RendezVousController {

    private final RendezVousService rendezVousService;
    private final PatientService patientService;
    private final MedecinService medecinService;

    // ✅ INJECTION MULTIPLE - Tous les services nécessaires
    public RendezVousController(RendezVousService rendezVousService,
                              PatientService patientService,
                              MedecinService medecinService) {
        this.rendezVousService = rendezVousService;
        this.patientService = patientService;
        this.medecinService = medecinService;
    }

    // ========================
    // FORMULAIRE RDV - GET /rendezvous/new
    // ========================
    @GetMapping("/new")
    public String showRendezVousForm(Model model) {
        //  Prépare les listes pour les dropdowns
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("medecins", medecinService.getAllMedecins());
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
        try {
            // Le Service gère TOUTE la logique complexe
            // - Vérifie existence patient/médecin
            // - Vérifie disponibilité créneau  
            // - Vérifie date dans futur
            // - Crée le RDV
            var rdv = rendezVousService.prendreRendezVous(patientId, medecinId, dateHeure, motif);
            return "redirect:/rendezvous?success=RDV+pris+avec+succès";
        } catch (Exception e) {
            // En cas d'erreur, réaffiche le formulaire avec message
            model.addAttribute("error", e.getMessage());
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("medecins", medecinService.getAllMedecins());
            return "rendezvous/form";
        }
    }

    // ========================
    // LISTE RDV - GET /rendezvous
    // ========================
    @GetMapping
    public String listRendezVous(Model model) {
        model.addAttribute("rendezvous", rendezVousService.getProchainsRendezVous());
        return "rendezvous/list";
    }
}
package com.healthbook.controller;

import com.healthbook.service.PatientService;
import com.healthbook.service.MedecinService;
import com.healthbook.service.RendezVousService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PatientService patientService;
    private final MedecinService medecinService;
    private final RendezVousService rendezVousService;

    // ✅ INJECTION des Services (BONNE PRATIQUE)
    public HomeController(PatientService patientService,
                         MedecinService medecinService,
                         RendezVousService rendezVousService) {
        this.patientService = patientService;
        this.medecinService = medecinService;
        this.rendezVousService = rendezVousService;
    }

    // ========================
    // PAGE D'ACCUEIL - http://localhost:8081/
    // ========================
    @GetMapping("/")
    public String home(Model model) {
        // ✅ Le Controller utilise les Services, PAS les Repositories
        long totalPatients = patientService.countPatients();
        long totalMedecins = medecinService.getAllMedecins().size();
        
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalMedecins", totalMedecins);
        model.addAttribute("prochainsRDV", rendezVousService.getProchainsRendezVous());
        
        return "home"; // → Renvoie le template home.html
    }
}
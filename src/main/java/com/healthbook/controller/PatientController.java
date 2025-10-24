package com.healthbook.controller;

import com.healthbook.entity.Patient;
import com.healthbook.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patients") // → Toutes les URLs commencent par /patients
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ========================
    // LISTE PATIENTS - GET /patients
    // ========================
    @GetMapping
    public String listPatients(Model model) {
        // ✅ Appel du SERVICE, pas du Repository
        List<Patient> patients = patientService.getAllPatients();
        model.addAttribute("patients", patients);
        return "patients/list"; // → templates/patients/list.html
    }

    // ========================
    // FORMULAIRE AJOUT - GET /patients/new
    // ========================
    @GetMapping("/new")
    public String showAddForm(Model model) {
        // Prépare un objet vide pour le formulaire
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }

    // ========================
    // TRAITEMENT AJOUT - POST /patients/new
    // ========================
    @PostMapping("/new")
    public String addPatient(@ModelAttribute Patient patient, Model model) {
        try {
            // Le Service gère la logique (vérification email, etc.)
            patientService.createPatient(patient);
            return "redirect:/patients?success=Patient+ajouté";
        } catch (Exception e) {
            // Gestion d'erreur propre
            model.addAttribute("error", e.getMessage());
            return "patients/form";
        }
    }

    // ========================
    // DÉTAILS PATIENT - GET /patients/{id}
    // ========================
    @GetMapping("/{id}")
    public String viewPatient(@PathVariable Long id, Model model) {
        // Optional pour gérer le "cas où pas trouvé"
        Optional<Patient> patient = patientService.getPatientById(id);
        if (patient.isPresent()) {
            model.addAttribute("patient", patient.get());
            return "patients/details";
        } else {
            return "redirect:/patients?error=Patient+non+trouvé";
        }
    }

    // ========================
    // SUPPRESSION - GET /patients/{id}/delete
    // ========================
    @GetMapping("/{id}/delete")
    public String deletePatient(@PathVariable Long id) {
        try {
            // Le Service vérifie si le patient existe avant suppression
            patientService.deletePatient(id);
            return "redirect:/patients?success=Patient+supprimé";
        } catch (Exception e) {
            return "redirect:/patients?error=Erreur+suppression";
        }
    }
}


package com.healthbook.controller;

import com.healthbook.entity.Patient;
import com.healthbook.entity.User;
import com.healthbook.service.PatientService;
import com.healthbook.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;
    private final UserService userService;

    public PatientController(PatientService patientService, UserService userService) {
        this.patientService = patientService;
        this.userService = userService;
    }

    // Récupérer l'utilisateur connecté
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return (User) userService.loadUserByUsername(username);
    }

    // ========================
    // LISTE PATIENTS - GET /patients
    // ========================
    @GetMapping
    public String listPatients(Model model) {
        User currentUser = getCurrentUser();
        List<Patient> patients;
        
        // 🔒 SÉCURITÉ : Filtrer selon le rôle
        if (currentUser.getRole().name().equals("ADMIN")) {
            patients = patientService.getAllPatients(); // Admin voit tout
        } else if (currentUser.getRole().name().equals("PATIENT")) {
            // Patient ne voit que lui-même
            patients = List.of(currentUser.getPatient());
        } else {
            // Médecin ou autres : liste vide ou accès refusé
            return "redirect:/dashboard?error=Accès+refusé";
        }
        
        model.addAttribute("patients", patients);
        return "patients/list";
    }

    // ========================
    // DÉTAILS PATIENT - GET /patients/{id}
    // ========================
    @GetMapping("/{id}")
    public String viewPatient(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        
        // 🔒 SÉCURITÉ : Vérifier les permissions
        if (currentUser.getRole().name().equals("PATIENT") && 
            !currentUser.getPatient().getId().equals(id)) {
            return "redirect:/dashboard?error=Accès+refusé";
        }
        
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
        User currentUser = getCurrentUser();
        
        // 🔒 SÉCURITÉ : Seul ADMIN peut supprimer
        if (!currentUser.getRole().name().equals("ADMIN")) {
            return "redirect:/dashboard?error=Accès+refusé";
        }
        
        try {
            patientService.deletePatient(id);
            return "redirect:/patients?success=Patient+supprimé";
        } catch (Exception e) {
            return "redirect:/patients?error=Erreur+suppression";
        }
    }

    // Les autres méthodes restent inchangées...
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }

    @PostMapping("/new")
    public String addPatient(@ModelAttribute Patient patient, Model model) {
        try {
            patientService.createPatient(patient);
            return "redirect:/patients?success=Patient+ajouté";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "patients/form";
        }
    }
}
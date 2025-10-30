package com.healthbook.controller;

import com.healthbook.entity.Medecin;
import com.healthbook.service.MedecinService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/medecins")
public class MedecinController {

    private final MedecinService medecinService;

    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    // ========================
    // LISTE MÉDECINS - GET /medecins
    // ========================
    @GetMapping
    public String listMedecins(Model model) {
        //  Récupère données + liste des spécialités
        model.addAttribute("medecins", medecinService.getAllMedecins());
        model.addAttribute("specialites", medecinService.getAllSpecialites());
        return "medecins/list";
    }

    // ========================
    // FORMULAIRE AJOUT - GET /medecins/new
    // ========================
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("medecin", new Medecin());
        model.addAttribute("specialites", medecinService.getAllSpecialites());
        return "medecins/form";
    }

    // ========================
    // RECHERCHE - GET /medecins/search
    // ========================
    @GetMapping("/search")
    public String searchMedecins(@RequestParam(required = false) String specialite,
                               @RequestParam(required = false) String nom,
                               Model model) {
        //  Recherche avec filtres optionnels
        List<Medecin> medecins = medecinService.searchMedecins(specialite, nom);
        
        model.addAttribute("medecins", medecins);
        model.addAttribute("specialites", medecinService.getAllSpecialites());
        model.addAttribute("selectedSpecialite", specialite);
        model.addAttribute("searchNom", nom);
        
        return "medecins/list";
    }
}
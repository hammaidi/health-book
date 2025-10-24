package com.healthbook.service;

import com.healthbook.entity.Medecin;
import com.healthbook.repository.MedecinRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedecinService {

    private final MedecinRepository medecinRepository;

    public MedecinService(MedecinRepository medecinRepository) {
        this.medecinRepository = medecinRepository;
    }

    // ========================
    // CRÉER UN MÉDECIN
    // ========================
    public Medecin createMedecin(Medecin medecin) {
        if (medecinRepository.findByEmail(medecin.getEmail()).isPresent()) {
            throw new RuntimeException("Un médecin avec cet email existe déjà");
        }
        return medecinRepository.save(medecin);
    }

    // ========================
    // RÉCUPÉRER TOUS LES MÉDECINS
    // ========================
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    // ========================
    // RÉCUPÉRER MÉDECIN PAR ID
    // ========================
    public Optional<Medecin> getMedecinById(Long id) {
        return medecinRepository.findById(id);
    }

    // ========================
    // TROUVER PAR SPÉCIALITÉ
    // ========================
    public List<Medecin> getMedecinsBySpecialite(String specialite) {
        return medecinRepository.findBySpecialite(specialite);
    }

    // ========================
    // LISTER TOUTES LES SPÉCIALITÉS
    // ========================
    public List<String> getAllSpecialites() {
        return medecinRepository.findAllSpecialites();
    }

    // ========================
    // RECHERCHER DES MÉDECINS
    // ========================
    public List<Medecin> searchMedecins(String specialite, String nom) {
        if (specialite != null && !specialite.isEmpty()) {
            return medecinRepository.findBySpecialiteAndNomContainingIgnoreCase(specialite, nom);
        } else {
            return medecinRepository.findByNomContainingIgnoreCase(nom);
        }
    }

    // ========================
    // METTRE À JOUR UN MÉDECIN
    // ========================
    public Medecin updateMedecin(Long id, Medecin medecinDetails) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + id));

        if (medecinDetails.getEmail() != null && !medecinDetails.getEmail().equals(medecin.getEmail())) {
            Optional<Medecin> existingMedecin = medecinRepository.findByEmail(medecinDetails.getEmail());
            if (existingMedecin.isPresent() && !existingMedecin.get().getId().equals(id)) {
                throw new RuntimeException("Cet email est déjà utilisé par un autre médecin");
            }
        }

        medecin.setNom(medecinDetails.getNom());
        medecin.setPrenom(medecinDetails.getPrenom());
        medecin.setSpecialite(medecinDetails.getSpecialite());
        medecin.setEmail(medecinDetails.getEmail());
        medecin.setTelephone(medecinDetails.getTelephone());

        return medecinRepository.save(medecin);
    }

    // ========================
    // SUPPRIMER UN MÉDECIN
    // ========================
    public void deleteMedecin(Long id) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id: " + id));
        medecinRepository.delete(medecin);
    }

    public List<Medecin> findAll() {
        return medecinRepository.findAll();
    }
}
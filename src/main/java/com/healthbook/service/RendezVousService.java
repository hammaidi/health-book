package com.healthbook.service;

import com.healthbook.entity.Medecin;
import com.healthbook.entity.Patient;
import com.healthbook.entity.RendezVous;
import com.healthbook.entity.User;
import com.healthbook.entity.Role;
import com.healthbook.repository.RendezVousRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final PatientService patientService;
    private final MedecinService medecinService;

    public RendezVousService(RendezVousRepository rendezVousRepository, 
                           PatientService patientService, 
                           MedecinService medecinService) {
        this.rendezVousRepository = rendezVousRepository;
        this.patientService = patientService;
        this.medecinService = medecinService;
    }

    // ========================
    // PRENDRE UN RENDEZ-VOUS
    // ========================
    public RendezVous prendreRendezVous(Long patientId, Long medecinId, LocalDateTime dateHeure, String motif) {
        // Vérifier que le patient existe
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        // Vérifier que le médecin existe
        Medecin medecin = medecinService.getMedecinById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        // Vérifier la disponibilité du créneau
        if (!isCreneauDisponible(medecin, dateHeure)) {
            throw new RuntimeException("Ce créneau n'est pas disponible");
        }

        // Vérifier que la date n'est pas dans le passé
        if (dateHeure.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Impossible de prendre un RDV dans le passé");
        }

        // Créer le rendez-vous
        RendezVous rendezVous = new RendezVous();
        rendezVous.setPatient(patient);
        rendezVous.setMedecin(medecin);
        rendezVous.setDateHeure(dateHeure);
        rendezVous.setMotif(motif);
        rendezVous.setStatut(RendezVous.StatutRDV.EN_ATTENTE);

        return rendezVousRepository.save(rendezVous);
    }

    // ========================
    // VÉRIFIER DISPONIBILITÉ CRÉNEAU
    // ========================
    public boolean isCreneauDisponible(Medecin medecin, LocalDateTime dateHeure) {
        long nbRendezVous = rendezVousRepository.countRendezVousByMedecinAndDateHeure(medecin, dateHeure);
        return nbRendezVous == 0;
    }

    // ========================
    // CONFIRMER UN RDV
    // ========================
    @Transactional
    public RendezVous confirmerRendezVous(Long rdvId) {
        RendezVous rendezVous = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

        rendezVous.setStatut(RendezVous.StatutRDV.CONFIRME);
        RendezVous rdvSauvegarde = rendezVousRepository.save(rendezVous);
        
        // 🔥 FORCER le flush pour s'assurer que c'est sauvegardé
        rendezVousRepository.flush();
        
        return rdvSauvegarde;
    }

    // ========================
    // ANNULER UN RDV
    // ========================
    @Transactional
    public RendezVous annulerRendezVous(Long rdvId) {
        RendezVous rendezVous = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

        rendezVous.setStatut(RendezVous.StatutRDV.ANNULE);
        RendezVous rdvSauvegarde = rendezVousRepository.save(rendezVous);
        
        // 🔥 FORCER le flush pour s'assurer que c'est sauvegardé
        rendezVousRepository.flush();
        
        return rdvSauvegarde;
    }

    // ========================
    // RDV PAR UTILISATEUR (AMÉLIORÉE)
    // ========================
    @Transactional(readOnly = true)
    public List<RendezVous> getRendezVousByUser(User user) {
        // 🔥 TOUJOURS recharger depuis la base de données
        if (user.getRole() == Role.PATIENT && user.getPatient() != null) {
            return rendezVousRepository.findByPatient(user.getPatient());
        } else if (user.getRole() == Role.MEDECIN && user.getMedecin() != null) {
            return rendezVousRepository.findByMedecin(user.getMedecin());
        } else if (user.getRole() == Role.ADMIN) {
            return rendezVousRepository.findAll();
        }
        return Collections.emptyList();
    }

    // ========================
    // PROCHAINS RDV (pour dashboard)
    // ========================
    public List<RendezVous> getProchainsRendezVous() {
        return rendezVousRepository.findProchainsRendezVous(LocalDateTime.now());
    }

    // ========================
    // TOUS LES RDV
    // ========================
    public List<RendezVous> findAll() {
        return rendezVousRepository.findAll();
    }

    // ========================
    // RDV PAR ID
    // ========================
    public Optional<RendezVous> findById(Long id) {
        return rendezVousRepository.findById(id);
    }

    // ========================
    // SUPPRIMER RDV
    // ========================
    public void deleteRendezVous(Long id) {
        rendezVousRepository.deleteById(id);
    }

    // ========================
    // RÉCUPÉRER RDV D'UN PATIENT
    // ========================
    public List<RendezVous> getRendezVousByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        
        return rendezVousRepository.findByPatient(patient);
    }

    // ========================
    // RÉCUPÉRER RDV D'UN MÉDECIN
    // ========================
    public List<RendezVous> getRendezVousByMedecin(Long medecinId) {
        Medecin medecin = medecinService.getMedecinById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        
        return rendezVousRepository.findByMedecin(medecin);
    }
}
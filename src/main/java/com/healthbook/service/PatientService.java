package com.healthbook.service;

import com.healthbook.entity.Patient;
import com.healthbook.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // ========================
    // CRÉER UN PATIENT
    // ========================
    public Patient createPatient(Patient patient) {
        // Vérifier si l'email existe déjà
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new RuntimeException("Un patient avec cet email existe déjà");
        }
        
        return patientRepository.save(patient);
    }

    // ========================
    // RÉCUPÉRER TOUS LES PATIENTS
    // ========================
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // ========================
    // RÉCUPÉRER PATIENT PAR ID
    // ========================
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    // ========================
    // METTRE À JOUR UN PATIENT
    // ========================
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé avec l'id: " + id));

        // Vérifier si le nouvel email n'est pas déjà utilisé par un autre patient
        if (patientDetails.getEmail() != null && !patientDetails.getEmail().equals(patient.getEmail())) {
            Optional<Patient> existingPatient = patientRepository.findByEmail(patientDetails.getEmail());
            if (existingPatient.isPresent() && !existingPatient.get().getId().equals(id)) {
                throw new RuntimeException("Cet email est déjà utilisé par un autre patient");
            }
        }

        // Mise à jour des champs
        patient.setNom(patientDetails.getNom());
        patient.setPrenom(patientDetails.getPrenom());
        patient.setEmail(patientDetails.getEmail());
        patient.setTelephone(patientDetails.getTelephone());
        patient.setDateNaissance(patientDetails.getDateNaissance());

        return patientRepository.save(patient);
    }

    // ========================
    // SUPPRIMER UN PATIENT
    // ========================
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé avec l'id: " + id));
        
        patientRepository.delete(patient);
    }

    // ========================
    // RECHERCHER DES PATIENTS
    // ========================
    public List<Patient> searchPatients(String recherche) {
        return patientRepository.searchPatients(recherche);
    }

    // ========================
    // COMPTER LE NOMBRE DE PATIENTS
    // ========================
    public long countPatients() {
        return patientRepository.count();
    }

    // ========================
    // TROUVER PAR EMAIL
    // ========================
    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    // ========================
    // MÉTHODES SIMPLIFIÉES POUR LE CONTROLLER
    // ========================
    
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public void deletePatientById(Long id) {
        patientRepository.deleteById(id);
    }
}
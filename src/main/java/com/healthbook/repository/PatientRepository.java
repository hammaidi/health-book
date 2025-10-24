package com.healthbook.repository;

import com.healthbook.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Trouver un patient par email (unique)
    Optional<Patient> findByEmail(String email);
    
    // Trouver les patients par nom (recherche)
    List<Patient> findByNomContainingIgnoreCase(String nom);
    
    // Trouver les patients par prénom (recherche)
    List<Patient> findByPrenomContainingIgnoreCase(String prenom);
    
    // Compter le nombre total de patients
    long count();
    
    // Recherche combinée nom + prénom
    @Query("SELECT p FROM Patient p WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(p.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Patient> searchPatients(@Param("recherche") String recherche);
}
package com.healthbook.repository;

import com.healthbook.entity.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    // Trouver un médecin par email (unique)
    Optional<Medecin> findByEmail(String email);
    
    // Trouver les médecins par spécialité
    List<Medecin> findBySpecialite(String specialite);
    
    // Trouver les médecins par nom (recherche)
    List<Medecin> findByNomContainingIgnoreCase(String nom);
    
    // Liste de toutes les spécialités disponibles
    @Query("SELECT DISTINCT m.specialite FROM Medecin m ORDER BY m.specialite")
    List<String> findAllSpecialites();
    
    // Recherche de médecins par spécialité et nom
    List<Medecin> findBySpecialiteAndNomContainingIgnoreCase(String specialite, String nom);
    
    // Compter le nombre de médecins par spécialité
    @Query("SELECT COUNT(m) FROM Medecin m WHERE m.specialite = :specialite")
    long countBySpecialite(@Param("specialite") String specialite);
}
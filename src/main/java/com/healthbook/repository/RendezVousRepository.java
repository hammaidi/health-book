package com.healthbook.repository;

import com.healthbook.entity.Medecin;
import com.healthbook.entity.Patient;
import com.healthbook.entity.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // Trouver les RDV d'un patient
    List<RendezVous> findByPatient(Patient patient);
    
    // Trouver les RDV d'un médecin
    List<RendezVous> findByMedecin(Medecin medecin);
    
    // Trouver les RDV par statut
    List<RendezVous> findByStatut(RendezVous.StatutRDV statut);
    
    // Trouver les RDV dans une plage de dates
    List<RendezVous> findByDateHeureBetween(LocalDateTime start, LocalDateTime end);
    
    // RDV d'un médecin à une date spécifique
    @Query("SELECT rv FROM RendezVous rv WHERE rv.medecin = :medecin AND DATE(rv.dateHeure) = DATE(:date)")
    List<RendezVous> findRendezVousByMedecinAndDate(
            @Param("medecin") Medecin medecin, 
            @Param("date") LocalDateTime date);
    
    // Vérifier si un créneau est disponible pour un médecin
    @Query("SELECT COUNT(rv) FROM RendezVous rv WHERE rv.medecin = :medecin AND rv.dateHeure = :dateHeure AND rv.statut != 'ANNULE'")
    long countRendezVousByMedecinAndDateHeure(
            @Param("medecin") Medecin medecin, 
            @Param("dateHeure") LocalDateTime dateHeure);
    
    // Prochains RDV (non annulés)
    @Query("SELECT rv FROM RendezVous rv WHERE rv.dateHeure >= :now AND rv.statut != 'ANNULE' ORDER BY rv.dateHeure ASC")
    List<RendezVous> findProchainsRendezVous(@Param("now") LocalDateTime now);
}
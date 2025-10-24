package com.healthbook.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "duree")
    private Integer duree = 30; // 30 minutes par défaut

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutRDV statut = StatutRDV.EN_ATTENTE;

    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    // === ENUM ===
    public enum StatutRDV {
        EN_ATTENTE, CONFIRME, ANNULE, TERMINE
    }

    // === CONSTRUCTEURS ===
    public RendezVous() {}

    public RendezVous(Patient patient, Medecin medecin, LocalDateTime dateHeure, String motif) {
        this.patient = patient;
        this.medecin = medecin;
        this.dateHeure = dateHeure;
        this.motif = motif;
    }

    // === GETTERS & SETTERS ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Medecin getMedecin() { return medecin; }
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }

    public StatutRDV getStatut() { return statut; }
    public void setStatut(StatutRDV statut) { this.statut = statut; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    // === toString() ===
    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", patient=" + patient.getNom() + " " + patient.getPrenom() +
                ", medecin=" + medecin.getNom() + " " + medecin.getPrenom() +
                ", dateHeure=" + dateHeure +
                ", statut=" + statut +
                '}';
    }
}
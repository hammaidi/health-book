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

    // === CONSTRUCTEURS & GETTERS/SETTERS ===
    // (à compléter)
}
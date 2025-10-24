package com.healthbook.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "medecin")
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "specialite", nullable = false, length = 100)
    private String specialite;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "tarif_consultation", precision = 8, scale = 2)
    private BigDecimal tarifConsultation;

    // === CONSTRUCTEURS ===
    public Medecin() {}

    public Medecin(String nom, String prenom, String specialite, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.email = email;
    }

    // === GETTERS & SETTERS ===
    // (à ajouter comme pour Patient)
}
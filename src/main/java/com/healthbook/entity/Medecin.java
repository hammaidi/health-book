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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public BigDecimal getTarifConsultation() { return tarifConsultation; }
    public void setTarifConsultation(BigDecimal tarifConsultation) { this.tarifConsultation = tarifConsultation; }

    // === toString() ===
    @Override
    public String toString() {
        return "Medecin{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", specialite='" + specialite + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
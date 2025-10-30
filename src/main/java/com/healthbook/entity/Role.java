package com.healthbook.entity;

public enum Role {
    PATIENT,    // Peut voir ses RDV seulement
    MEDECIN,    // Voir ses consultations + patients  
    ADMIN       // Accès complet
}
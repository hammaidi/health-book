package com.healthbook.service;

import com.healthbook.entity.*;
import com.healthbook.repository.RendezVousRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendezVousServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private MedecinService medecinService;

    @InjectMocks
    private RendezVousService rendezVousService;

    @Test
    void prendreRendezVous_Success() {
        // Given
        Long patientId = 1L;
        Long medecinId = 1L;
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        String motif = "Consultation générale";

        Patient patient = new Patient();
        patient.setId(patientId);
        
        Medecin medecin = new Medecin();
        medecin.setId(medecinId);
        
        RendezVous rdv = new RendezVous();
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setDateHeure(dateTime);
        rdv.setMotif(motif);
        rdv.setStatut(RendezVous.StatutRDV.EN_ATTENTE);

        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));
        when(medecinService.getMedecinById(medecinId)).thenReturn(Optional.of(medecin));
        when(rendezVousRepository.countRendezVousByMedecinAndDateHeure(medecin, dateTime)).thenReturn(0L);
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rdv);

        // When
        RendezVous result = rendezVousService.prendreRendezVous(patientId, medecinId, dateTime, motif);

        // Then
        assertNotNull(result);
        assertEquals(RendezVous.StatutRDV.EN_ATTENTE, result.getStatut());
        assertEquals(patient, result.getPatient());
        assertEquals(medecin, result.getMedecin());
        verify(rendezVousRepository, times(1)).save(any(RendezVous.class));
    }

    @Test
    void prendreRendezVous_CreneauIndisponible_ThrowsException() {
        // Given
        Long patientId = 1L;
        Long medecinId = 1L;
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);

        Patient patient = new Patient();
        Medecin medecin = new Medecin();

        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));
        when(medecinService.getMedecinById(medecinId)).thenReturn(Optional.of(medecin));
        when(rendezVousRepository.countRendezVousByMedecinAndDateHeure(medecin, dateTime)).thenReturn(1L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> rendezVousService.prendreRendezVous(patientId, medecinId, dateTime, "motif"));
        
        assertEquals("Ce créneau n'est pas disponible", exception.getMessage());
        verify(rendezVousRepository, never()).save(any(RendezVous.class));
    }

    @Test
    void prendreRendezVous_DateDansLePasse_ThrowsException() {
        // Given
        Long patientId = 1L;
        Long medecinId = 1L;
        LocalDateTime dateTime = LocalDateTime.now().minusDays(1); // Date passée

        Patient patient = new Patient();
        Medecin medecin = new Medecin();

        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));
        when(medecinService.getMedecinById(medecinId)).thenReturn(Optional.of(medecin));
        when(rendezVousRepository.countRendezVousByMedecinAndDateHeure(medecin, dateTime)).thenReturn(0L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> rendezVousService.prendreRendezVous(patientId, medecinId, dateTime, "motif"));
        
        assertEquals("Impossible de prendre un RDV dans le passé", exception.getMessage());
        verify(rendezVousRepository, never()).save(any(RendezVous.class));
    }

    @Test
    void confirmerRendezVous_Success() {
        // Given
        Long rdvId = 1L;
        RendezVous rdv = new RendezVous();
        rdv.setId(rdvId);
        rdv.setStatut(RendezVous.StatutRDV.EN_ATTENTE);

        when(rendezVousRepository.findById(rdvId)).thenReturn(Optional.of(rdv));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rdv);

        // When
        RendezVous result = rendezVousService.confirmerRendezVous(rdvId);

        // Then
        assertEquals(RendezVous.StatutRDV.CONFIRME, result.getStatut());
        verify(rendezVousRepository, times(1)).save(rdv);
        verify(rendezVousRepository, times(1)).flush();
    }

    @Test
    void annulerRendezVous_Success() {
        // Given
        Long rdvId = 1L;
        RendezVous rdv = new RendezVous();
        rdv.setId(rdvId);
        rdv.setStatut(RendezVous.StatutRDV.EN_ATTENTE);

        when(rendezVousRepository.findById(rdvId)).thenReturn(Optional.of(rdv));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rdv);

        // When
        RendezVous result = rendezVousService.annulerRendezVous(rdvId);

        // Then
        assertEquals(RendezVous.StatutRDV.ANNULE, result.getStatut());
        verify(rendezVousRepository, times(1)).save(rdv);
        verify(rendezVousRepository, times(1)).flush();
    }

    @Test
    void getRendezVousByUser_Patient() {
        // Given
        User user = new User();
        user.setRole(Role.PATIENT);
        
        Patient patient = new Patient();
        patient.setId(1L);
        user.setPatient(patient);
        
        List<RendezVous> rdvs = Arrays.asList(new RendezVous(), new RendezVous());
        when(rendezVousRepository.findByPatient(patient)).thenReturn(rdvs);

        // When
        List<RendezVous> result = rendezVousService.getRendezVousByUser(user);

        // Then
        assertEquals(2, result.size());
        verify(rendezVousRepository, times(1)).findByPatient(patient);
    }

    @Test
    void getRendezVousByUser_Admin() {
        // Given
        User user = new User();
        user.setRole(Role.ADMIN);
        
        List<RendezVous> rdvs = Arrays.asList(new RendezVous(), new RendezVous(), new RendezVous());
        when(rendezVousRepository.findAll()).thenReturn(rdvs);

        // When
        List<RendezVous> result = rendezVousService.getRendezVousByUser(user);

        // Then
        assertEquals(3, result.size());
        verify(rendezVousRepository, times(1)).findAll();
    }
}
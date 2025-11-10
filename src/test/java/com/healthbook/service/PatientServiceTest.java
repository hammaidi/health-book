package com.healthbook.service;

import com.healthbook.entity.Patient;
import com.healthbook.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void createPatient_Success() {
        // Given
        Patient patient = new Patient();
        patient.setNom("Dupont");
        patient.setPrenom("Jean");
        patient.setEmail("jean.dupont@email.com");
        patient.setTelephone("0123456789");
        patient.setDateNaissance(LocalDate.of(1985, 5, 15));
        
        when(patientRepository.findByEmail("jean.dupont@email.com")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // When
        Patient result = patientService.createPatient(patient);

        // Then
        assertNotNull(result);
        assertEquals("Dupont", result.getNom());
        assertEquals("jean.dupont@email.com", result.getEmail());
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void createPatient_EmailAlreadyExists_ThrowsException() {
        // Given
        Patient patient = new Patient();
        patient.setEmail("existant@email.com");
        
        when(patientRepository.findByEmail("existant@email.com"))
            .thenReturn(Optional.of(new Patient()));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> patientService.createPatient(patient));
        
        assertEquals("Un patient avec cet email existe déjà", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getPatientById_Found() {
        // Given
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setNom("Martin");
        
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When
        Optional<Patient> result = patientService.getPatientById(patientId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Martin", result.get().getNom());
    }

    @Test
    void getPatientById_NotFound() {
        // Given
        Long patientId = 999L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When
        Optional<Patient> result = patientService.getPatientById(patientId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void updatePatient_Success() {
        // Given
        Long patientId = 1L;
        Patient existingPatient = new Patient();
        existingPatient.setId(patientId);
        existingPatient.setNom("Dupont");
        existingPatient.setPrenom("Jean");
        existingPatient.setEmail("ancien@email.com");
        
        Patient updatedPatient = new Patient();
        updatedPatient.setNom("Dupont-Modifié");
        updatedPatient.setPrenom("Jean-Modifié");
        updatedPatient.setEmail("nouveau@email.com");
        updatedPatient.setTelephone("0987654321");
        
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.findByEmail("nouveau@email.com")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

        // When
        Patient result = patientService.updatePatient(patientId, updatedPatient);

        // Then
        assertNotNull(result);
        assertEquals("Dupont-Modifié", result.getNom());
        assertEquals("nouveau@email.com", result.getEmail());
        verify(patientRepository, times(1)).save(existingPatient);
    }

    @Test
    void updatePatient_EmailAlreadyUsedByOtherPatient_ThrowsException() {
        // Given
        Long patientId = 1L;
        Patient existingPatient = new Patient();
        existingPatient.setId(patientId);
        existingPatient.setEmail("ancien@email.com");
        
        Patient otherPatient = new Patient();
        otherPatient.setId(2L); // Autre ID
        otherPatient.setEmail("deja.utilise@email.com");
        
        Patient updatedPatient = new Patient();
        updatedPatient.setEmail("deja.utilise@email.com");
        
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.findByEmail("deja.utilise@email.com"))
            .thenReturn(Optional.of(otherPatient));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> patientService.updatePatient(patientId, updatedPatient));
        
        assertEquals("Cet email est déjà utilisé par un autre patient", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getAllPatients_ReturnsList() {
        // Given
        Patient patient1 = new Patient();
        patient1.setNom("Patient1");
        Patient patient2 = new Patient();
        patient2.setNom("Patient2");
        
        List<Patient> patients = Arrays.asList(patient1, patient2);
        when(patientRepository.findAll()).thenReturn(patients);

        // When
        List<Patient> result = patientService.getAllPatients();

        // Then
        assertEquals(2, result.size());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void deletePatient_Success() {
        // Given
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).delete(patient);

        // When
        patientService.deletePatient(patientId);

        // Then
        verify(patientRepository, times(1)).delete(patient);
    }

    @Test
    void deletePatient_NotFound_ThrowsException() {
        // Given
        Long patientId = 999L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> patientService.deletePatient(patientId));
        
        assertEquals("Patient non trouvé avec l'id: 999", exception.getMessage());
        verify(patientRepository, never()).delete(any(Patient.class));
    }

    @Test
    void countPatients_ReturnsCount() {
        // Given
        when(patientRepository.count()).thenReturn(5L);

        // When
        long result = patientService.countPatients();

        // Then
        assertEquals(5L, result);
        verify(patientRepository, times(1)).count();
    }
}
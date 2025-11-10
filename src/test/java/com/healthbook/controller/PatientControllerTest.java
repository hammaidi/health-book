package com.healthbook.controller;

import com.healthbook.entity.Patient;
import com.healthbook.entity.Role;
import com.healthbook.entity.User;
import com.healthbook.service.PatientService;
import com.healthbook.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPatients_AdminUser_ReturnsAllPatients() throws Exception {
        // Given
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setNom("Dupont");
        
        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setNom("Martin");
        
        List<Patient> patients = Arrays.asList(patient1, patient2);
        
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        
        when(userService.loadUserByUsername(any())).thenReturn(adminUser);
        when(patientService.getAllPatients()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attribute("patients", patients));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void listPatients_PatientUser_ReturnsOnlyOwnProfile() throws Exception {
        // Given
        Patient currentPatient = new Patient();
        currentPatient.setId(1L);
        currentPatient.setNom("Dupont");
        
        User patientUser = new User();
        patientUser.setRole(Role.PATIENT);
        patientUser.setPatient(currentPatient);
        
        when(userService.loadUserByUsername(any())).thenReturn(patientUser);

        // When & Then
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attribute("patients", List.of(currentPatient)));
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void listPatients_MedecinUser_RedirectsToDashboard() throws Exception {
        // Given
        User medecinUser = new User();
        medecinUser.setRole(Role.MEDECIN);
        
        when(userService.loadUserByUsername(any())).thenReturn(medecinUser);

        // When & Then
        mockMvc.perform(get("/patients"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=Accès+refusé"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void viewPatient_PatientAccessingOwnProfile_Success() throws Exception {
        // Given
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setNom("Dupont");
        
        User currentUser = new User();
        currentUser.setRole(Role.PATIENT);
        currentUser.setPatient(patient); // Même patient
        
        when(userService.loadUserByUsername(any())).thenReturn(currentUser);
        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));

        // When & Then
        mockMvc.perform(get("/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/details"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attribute("patient", patient));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void viewPatient_PatientAccessingOtherProfile_RedirectsWithError() throws Exception {
        // Given
        Long otherPatientId = 2L; // Autre patient
        Patient currentPatient = new Patient();
        currentPatient.setId(1L); // ID différent
        
        User currentUser = new User();
        currentUser.setRole(Role.PATIENT);
        currentUser.setPatient(currentPatient);
        
        when(userService.loadUserByUsername(any())).thenReturn(currentUser);

        // When & Then
        mockMvc.perform(get("/patients/{id}", otherPatientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=Accès+refusé"));
        
        verify(patientService, never()).getPatientById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePatient_AdminUser_Success() throws Exception {
        // Given
        Long patientId = 1L;
        
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        
        when(userService.loadUserByUsername(any())).thenReturn(adminUser);
        doNothing().when(patientService).deletePatient(patientId);

        // When & Then
        mockMvc.perform(get("/patients/{id}/delete", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients?success=Patient+supprimé"));
        
        verify(patientService, times(1)).deletePatient(patientId);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deletePatient_NonAdminUser_RedirectsWithError() throws Exception {
        // Given
        Long patientId = 1L;
        
        User patientUser = new User();
        patientUser.setRole(Role.PATIENT);
        
        when(userService.loadUserByUsername(any())).thenReturn(patientUser);

        // When & Then
        mockMvc.perform(get("/patients/{id}/delete", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=Accès+refusé"));
        
        verify(patientService, never()).deletePatient(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPatient_ValidPatient_RedirectsWithSuccess() throws Exception {
        // Given
        Patient patient = new Patient();
        patient.setNom("Dupont");
        patient.setPrenom("Jean");
        patient.setEmail("jean.dupont@email.com");
        
        when(patientService.createPatient(any(Patient.class))).thenReturn(patient);

        // When & Then
        mockMvc.perform(post("/patients/new")
                .param("nom", "Dupont")
                .param("prenom", "Jean")
                .param("email", "jean.dupont@email.com")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients?success=Patient+ajouté"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPatient_InvalidPatient_ReturnsFormWithError() throws Exception {
        // Given
        when(patientService.createPatient(any(Patient.class)))
                .thenThrow(new RuntimeException("Email déjà utilisé"));

        // When & Then
        mockMvc.perform(post("/patients/new")
                .param("nom", "Toto")
                .param("prenom", "Jean")
                .param("email", "existant@email.com")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeExists("error"));
    }
}
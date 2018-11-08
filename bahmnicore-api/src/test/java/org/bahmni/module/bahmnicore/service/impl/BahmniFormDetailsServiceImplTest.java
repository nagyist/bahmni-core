package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.contract.form.data.FormDetails;
import org.bahmni.module.bahmnicore.contract.form.helper.FormType;
import org.bahmni.module.bahmnicore.contract.form.helper.ObsUtil;
import org.bahmni.module.bahmnicore.contract.form.mapper.FormDetailsMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({FormType.class, ObsUtil.class, FormDetailsMapper.class})
@RunWith(PowerMockRunner.class)
public class BahmniFormDetailsServiceImplTest {

    private VisitService visitService = mock(VisitService.class);
    private PatientService patientService = mock(PatientService.class);
    private EncounterService encounterService = mock(EncounterService.class);
    private ObsService obsService = mock(ObsService.class);

    private Patient patient = mock(Patient.class);
    private Person person = mock(Person.class);
    private Visit visit = mock(Visit.class);
    private Encounter encounter = mock(Encounter.class);

    private Obs height = mock(Obs.class);
    private Obs weight = mock(Obs.class);
    private List<Obs> obs = Arrays.asList(height, weight);


    private BahmniFormDetailsServiceImpl bahmniFormDetailsService;
    private String patientUuid = "patient-uuid";

    @Before
    public void setUp() {
        bahmniFormDetailsService = new BahmniFormDetailsServiceImpl(patientService, visitService,
                encounterService, obsService);

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        when(visitService.getVisitsByPatient(patient)).thenReturn(singletonList(visit));
        List<Encounter> encounters = singletonList(encounter);
        when(encounterService.getEncounters(any(EncounterSearchCriteria.class))).thenReturn(encounters);
        when(patient.getPerson()).thenReturn(person);
        when(obsService.getObservations(anyListOf(Person.class), anyListOf(Encounter.class), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(Boolean.class))).thenReturn(obs);
    }

    @Test
    public void shouldReturnEmptyCollectionOfFormDetailsIfPatientDoesNotFound() {

        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(null);

        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsService.getFormDetails("patient uuid", "v1");

        assertEquals(0, formDetailsCollection.size());

    }

    @Test
    public void shouldReturnFormDetailsForGivenPatientUuidAndFormTypeIsV2() {

        FormDetails formDetails = mock(FormDetails.class);

        FormDetails anotherFormDetails = mock(FormDetails.class);

        FormType formType = mock(FormType.class);
        Whitebox.setInternalState(FormType.class, "FORM_BUILDER_FORMS", formType);
        when(formType.get()).thenReturn("v2");

        mockStatic(ObsUtil.class);
        when(ObsUtil.filterFormBuilderObs(obs)).thenReturn(obs);

        mockStatic(FormDetailsMapper.class);
        List<FormDetails> expectedFormDetails = Arrays.asList(formDetails, anotherFormDetails);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(expectedFormDetails);
        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", "v2");

        assertEquals(2, formBuilderFormDetails.size());
        containsInAnyOrder(expectedFormDetails, formBuilderFormDetails.toArray());

        verifyCommonMockCalls();
        verify(formType, times(1)).get();

        verifyStatic(VerificationModeFactory.times(1));
        ObsUtil.filterFormBuilderObs(obs);

        verifyStatic(VerificationModeFactory.times(1));
        FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class));
    }

    @Test
    public void shouldReturnFormDetailsOfTypeV2ForGivenPatientUuidAndNoFormTypeIsProvided() {

        FormDetails formDetails = mock(FormDetails.class);

        FormDetails anotherFormDetails = mock(FormDetails.class);

        FormType formType = mock(FormType.class);
        Whitebox.setInternalState(FormType.class, "FORM_BUILDER_FORMS", formType);
        when(formType.get()).thenReturn("v2");

        mockStatic(ObsUtil.class);
        when(ObsUtil.filterFormBuilderObs(obs)).thenReturn(obs);

        mockStatic(FormDetailsMapper.class);
        List<FormDetails> expectedFormDetails = Arrays.asList(formDetails, anotherFormDetails);
        when(FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class)))
                .thenReturn(expectedFormDetails);
        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", null);

        assertEquals(2, formBuilderFormDetails.size());
        containsInAnyOrder(expectedFormDetails, formBuilderFormDetails.toArray());

        verifyCommonMockCalls();
        verify(formType, times(1)).get();

        verifyStatic(VerificationModeFactory.times(1));
        ObsUtil.filterFormBuilderObs(obs);

        verifyStatic(VerificationModeFactory.times(1));
        FormDetailsMapper.createFormDetails(anyListOf(Obs.class), any(FormType.class));
    }

    @Test
    public void shouldReturnEmptyCollectionOfFormDetailsIfFormTypeIsAvailableButNotV2() {

        FormType formType = mock(FormType.class);
        Whitebox.setInternalState(FormType.class, "FORM_BUILDER_FORMS", formType);
        when(formType.get()).thenReturn("v2");

        Collection<FormDetails> formBuilderFormDetails = bahmniFormDetailsService.getFormDetails("patient-uuid", "v1");

        assertEquals(0, formBuilderFormDetails.size());
        verify(formType, times(1)).get();
    }

    private void verifyCommonMockCalls() {
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(visitService, times(1)).getVisitsByPatient(patient);
        verify(encounterService, times(1)).getEncounters(any(EncounterSearchCriteria.class));
        verify(patient, times(1)).getPerson();
        verify(obsService, times(1)).getObservations(anyListOf(Person.class),
                anyListOf(Encounter.class), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Boolean.class));
    }
}
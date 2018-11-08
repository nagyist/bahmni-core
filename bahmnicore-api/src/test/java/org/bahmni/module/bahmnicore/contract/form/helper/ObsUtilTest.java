package org.bahmni.module.bahmnicore.contract.form.helper;

import org.junit.Test;
import org.openmrs.Obs;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObsUtilTest {

    @Test
    public void shouldReturnEmptyObsWhenPassedInObsListIsNull() {
        List<Obs> obs = ObsUtil.filterFormBuilderObs(null);
        assertEquals(0, obs.size());
    }

    @Test
    public void shouldReturnEmptyObsWhenPassedInObsListIsEmpty() {
        List<Obs> obs = ObsUtil.filterFormBuilderObs(new ArrayList<>());
        assertEquals(0, obs.size());
    }

    @Test
    public void shouldReturnEmptyObsWhenPassedInObsDontHaveFormFieldPath() {
        Obs observation = mock(Obs.class);
        List<Obs> obs = ObsUtil.filterFormBuilderObs(singletonList(observation));
        assertEquals(0, obs.size());
    }

    @Test
    public void shouldReturnObsWhichHaveFormFieldPath() {
        Obs observation = mock(Obs.class);
        Obs anotherObservation = mock(Obs.class);
        when(observation.getFormFieldPath()).thenReturn("FormName.1/1-0");

        List<Obs> obs = ObsUtil.filterFormBuilderObs(asList(observation, anotherObservation));
        assertEquals(1, obs.size());
        assertEquals(observation, obs.get(0));
    }
}
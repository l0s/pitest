package org.pitest.junit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullConfigurationTest {

    NullConfiguration underTest = new NullConfiguration();

    @Test
    public void findsNoTests() {
        assertThat(underTest.testUnitFinder().findTestUnits(this.getClass())).isEmpty();
    }

    @Test
    public void findsNoSuites() {
        assertThat(underTest.testSuiteFinder().apply(this.getClass())).isEmpty();
    }

    @Test
    public void passesVerification() {
        assertThat(underTest.verifyEnvironment()).isEmpty();
    }
}
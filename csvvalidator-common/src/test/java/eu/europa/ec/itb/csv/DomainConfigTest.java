package eu.europa.ec.itb.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DomainConfigTest {

    @Test
    void testNewLabelConfig() {
        var config = new DomainConfig();
        assertNotNull(config);
    }

}

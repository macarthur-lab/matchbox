package org.broadinstitute.macarthurlab.matchbox.config;

import org.mockito.Mockito;
import org.monarchinitiative.exomiser.core.genome.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ExomiserConfig {

    /**
     * This is a stub bean - we're not using this aspect of the Exomiser so this will act as a placeholder for the
     * context to load.
     *
     * @return a placeholder GenomeAnalysisService
     */
    @Bean
    public GenomeAnalysisService genomeAnalysisService() {
        return new GenomeAnalysisServiceImpl(GenomeAssembly.HG19,
                Mockito.mock(GenomeAnalysisService.class),
                Mockito.mock(VariantDataService.class),
                Mockito.mock(VariantFactory.class));
    }

}

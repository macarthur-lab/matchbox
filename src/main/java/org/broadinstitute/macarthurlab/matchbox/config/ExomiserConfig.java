package org.broadinstitute.macarthurlab.matchbox.config;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ExomiserConfig {

    @Bean
    public JannovarData jannovarData() {
        //return an empty transcript list - we're not using it and it adds time to start-up.
        return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
    }
}

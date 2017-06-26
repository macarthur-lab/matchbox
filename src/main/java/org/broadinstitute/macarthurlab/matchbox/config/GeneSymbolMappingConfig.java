/**
 * To configure interaction with MongoDB
 */
package org.broadinstitute.macarthurlab.matchbox.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Stream;


@Configuration
public class GeneSymbolMappingConfig {

    private static final Logger logger = LoggerFactory.getLogger(GeneSymbolMappingConfig.class);

    @Autowired
    private Environment environment;

    @Bean
    public Map<String,String> geneSymbolToEnsemblId() {
        String geneSymbolToEnsemblIdName = environment.getProperty("matchbox.gene-symbol-to-id-mappings");
        Path geneSymbolToEnsemblIdFile = Paths.get(geneSymbolToEnsemblIdName);
        logger.info("Loading Gene symbol to Id mappings from {}", geneSymbolToEnsemblIdFile);
        Map<String,String> geneSymbolToEnsemblId = new HashMap<>();
        try (Stream<String> lines = Files.lines(geneSymbolToEnsemblIdFile)) {
            lines.forEach(line -> {
                        /**
                         * Each row is expected to look like,
                         * HGNC:5  A1BG    ENSG00000121410
                         */
                        StringTokenizer st = new StringTokenizer(line);
                        if (st.countTokens() == 3) {
                            st.nextToken();
                            String geneSymbol = st.nextToken();
                            String ensemblId = st.nextToken();
                            geneSymbolToEnsemblId.put(geneSymbol, ensemblId);
                        }
                    }
            );
        } catch (Exception e){
            logger.error("Error reading gene symbol to emsembl id map:"+e.toString() + " : " + e.getMessage());
            //This is bad. Really bad. Don't start the server without these.
            throw new RuntimeException(e);
        }
        return geneSymbolToEnsemblId;
    }

    //Should be deprecated and unnecessary.
//	@Value("${keyTrustStore}")
//	private String keyTrustStore;
//
//	@Bean
//	public void trustStore() {
//		//set the trust store java path (required for Communication class as well
//		System.setProperty("javax.net.ssl.trustStore", keyTrustStore);
//	}

}

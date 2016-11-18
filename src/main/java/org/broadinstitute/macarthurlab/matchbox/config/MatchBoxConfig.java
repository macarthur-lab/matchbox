package org.broadinstitute.macarthurlab.matchbox.config;

import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;
import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerNode;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.newInputStream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class MatchBoxConfig {

    private static final Logger logger = LoggerFactory.getLogger(MatchBoxConfig.class);
    public static final String APPLICATION_VND_GA4GH_MATCHMAKER_V1_0_JSON = "application/vnd.ga4gh.matchmaker.v1.0+json";

    @Autowired
    Environment environment;


    @Bean
    public List<Node> matchMakerNodes() {
        String nodesPath = environment.getProperty("matchbox.nodes");
        List<Node> nodes = new ArrayList<>();
        nodes.add(testRefServerNode());
        nodes.add(monarchServerNode());
        logger.info("Linked with nodes:");
        nodes.forEach(node -> logger.info("{}: {}", node.getName(), node.getUrl()));
        return nodes;
    }

    @Bean
    public List<AuthorizedToken> matchMakerNodeTokens() {
        List<AuthorizedToken> authorizedTokens = new ArrayList<>();
        authorizedTokens.add(defaultAuthorizedToken());
        return authorizedTokens;
    }

    private AuthorizedToken defaultAuthorizedToken() {
        return new AuthorizedToken("Default Access Token", "854a439d278df4283bf5498ab020336cdc416a7d", "Broad Institute JCMG", "harindra@broadinstitute.org");
    }

    private BufferedReader readPath(Path path) {
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            Reader reader = new InputStreamReader(newInputStream(path), decoder);
            return new BufferedReader(reader);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to find file: " + path, ex);
        }
    }

    @Bean
    MatchmakerNode testRefServerNode() {
        logger.info("Creating test server node");
        return new MatchmakerNode("Test Reference Server",
                "854a439d278df4283bf5498ab020336cdc416a7d",
                "http://localhost:8090/match",
                APPLICATION_VND_GA4GH_MATCHMAKER_V1_0_JSON,
                APPLICATION_VND_GA4GH_MATCHMAKER_V1_0_JSON,
                "en-US",
                true);
    }

    @Bean
    MatchmakerNode monarchServerNode() {
        logger.info("Creating Monarch server node");
        return new MatchmakerNode("Monarch MME Server",
                "none",
                "https://mme.monarchinitiative.org/match",
                APPLICATION_VND_GA4GH_MATCHMAKER_V1_0_JSON,
                APPLICATION_VND_GA4GH_MATCHMAKER_V1_0_JSON,
                "en-US",
                false);
    }

}

package org.broadinstitute.macarthurlab.matchbox.config;

import org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerNode;
import org.broadinstitute.macarthurlab.matchbox.entities.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class MatchMakerNodeConfig {

    private static final Logger logger = LoggerFactory.getLogger(MatchMakerNodeConfig.class);

    @Autowired
    private Environment environment;

    @Bean
    public List<Node> matchMakerNodes() {
        String nodeFile = environment.getProperty("matchbox.connected-nodes");
        logger.info("Reading MME nodes from {}", nodeFile);

        StringBuilder nodeJson = new StringBuilder();
        try (BufferedReader r = Files.newBufferedReader(Paths.get(nodeFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                nodeJson.append(line);
            }
        } catch (Exception e) {
            logger.error("error reading node config file:" + nodeFile + " : " + e);
            //This is bad. Really bad. Don't start the server without these either.
            throw new RuntimeException(e);
        }

        List<Node> matchMakerNodes = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(nodeJson.toString());
        } catch (ParseException e) {
            logger.error("{}", e);
            throw new RuntimeException(e);
        }
        JSONArray nodes = (JSONArray) jsonObject.get("nodes");

        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = (JSONObject) nodes.get(i);
            matchMakerNodes.add(new MatchmakerNode((String) node.get("name"),
                    (String) node.get("token"),
                    (String) node.get("url"),
                    (String) node.get("acceptHeader"),
                    (String) node.get("contentTypeHeader"),
                    (String) node.get("contentLanguage"),
                    (boolean) node.get("selfSignedCertificate")));
        }

        logger.info("Connected Nodes:");
        matchMakerNodes.forEach(node -> logger.info("{}", node));

        return matchMakerNodes;
    }

}

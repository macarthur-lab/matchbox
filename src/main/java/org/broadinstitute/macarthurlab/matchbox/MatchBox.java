package org.broadinstitute.macarthurlab.matchbox;

import java.io.IOException;

import org.broadinstitute.macarthurlab.matchbox.config.MatchMakerNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * Entry point to application.
 *
 */
@SpringBootApplication
@ImportResource("file:config/config.xml")
public class MatchBox {
	private static final Logger logger = LoggerFactory.getLogger(MatchMakerNodeConfig.class);

    public static void main( String[] args ) throws IOException {
    	logger.info( "Starting matchbox server.." );
        SpringApplication.run(MatchBox.class, args);
    }
}



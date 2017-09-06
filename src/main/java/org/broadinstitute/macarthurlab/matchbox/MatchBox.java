package org.broadinstitute.macarthurlab.matchbox;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * Entry point to application. Application starts here.
 *
 */
@SpringBootApplication
@ImportResource("file:config/config.xml")
public class MatchBox {
    public static void main( String[] args ) throws IOException {
        System.out.println( "Starting matchbox server.." );
        SpringApplication.run(MatchBox.class, args);
    }
}



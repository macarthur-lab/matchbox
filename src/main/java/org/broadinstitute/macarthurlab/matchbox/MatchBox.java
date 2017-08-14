package org.broadinstitute.macarthurlab.matchbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ImportResource;


/**
 * Entry point to application. Application starts here.
 *
 */
@SpringBootApplication
@ImportResource("file:config/config.xml")
public class MatchBox {
    public static void main( String[] args ) {
        System.out.println( "Starting matchbox server.." );
        SpringApplication.run(MatchBox.class, args);
    }
}



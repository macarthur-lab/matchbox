package org.broadinstitute.macarthurlab.matchbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * Entry point to application. Application starts here.
 *
 */
@SpringBootApplication
@ImportResource("file:resources/config.xml")
public class MatchmakerExchange 
{
    public static void main( String[] args )
    {
        System.out.println( "Starting server.." );
        SpringApplication.run(MatchmakerExchange.class, args);
    }
}



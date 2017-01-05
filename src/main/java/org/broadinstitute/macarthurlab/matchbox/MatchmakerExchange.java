package org.broadinstitute.macarthurlab.matchbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Entry point to application. Application starts here.
 *
 */

@SpringBootApplication
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class MatchmakerExchange 
{
    public static void main( String[] args )
    {
        System.out.println( "Starting matchbox server.." );
        SpringApplication.run(MatchmakerExchange.class, args);
    }
}



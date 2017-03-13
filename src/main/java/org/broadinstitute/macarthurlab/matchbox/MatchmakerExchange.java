package org.broadinstitute.macarthurlab.matchbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;


/**
 * Entry point to application. Application starts here.
 *
 */

@SpringBootApplication
@Configuration
@ComponentScan
@EnableAutoConfiguration
@PropertySource("file:resources/application.properties")
public class MatchmakerExchange 
{
    public static void main( String[] args )
    {
        System.out.println( "Starting matchbox server.." );
        SpringApplication.run(MatchmakerExchange.class, args);
    }
}



package org.broadinstitute.macarthurlab.matchbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Entry point to application. Application starts here.
 *
 */

@SpringBootApplication
public class MatchBox
{
    public static void main(String[] args) {
        System.out.println("Starting Matchbox server..");
        SpringApplication.run(MatchBox.class, args);
    }
}



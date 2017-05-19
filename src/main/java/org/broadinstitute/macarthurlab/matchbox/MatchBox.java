package org.broadinstitute.macarthurlab.matchbox;

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

//    @Autowired
//    static Environment environment;

    public static void main( String[] args ) {
        System.out.println( "Starting matchbox server.." );
        //set the trust store java path (required for Communication class as well
//        System.setProperty("javax.net.ssl.trustStore", environment.getProperty("keyTrustStore"));
        SpringApplication.run(MatchBox.class, args);
    }
}



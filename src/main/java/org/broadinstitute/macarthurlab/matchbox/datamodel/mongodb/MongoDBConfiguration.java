/**
 * To configure interaction with MongoDB
 */
package org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@EnableMongoRepositories
@Configuration
public class MongoDBConfiguration extends AbstractMongoConfiguration {
	
 
    @Override
    /**
     * This get's set as the database. Collection name is set in 
     * entity itself
     */
    protected String getDatabaseName() {
        return "mme_primary";
    }
 
    @Override
	@Bean
    /**
     * Hostname of database is set here. Assumed to be localhost for now. Please update as needed.
     * TODO: take out password from here
     */
    public Mongo mongo() throws Exception {
    	MongoClient mongoClient = new MongoClient();
    	DB db = mongoClient.getDB("http://localhost/mme_primary");
    	boolean auth = db.authenticate("matchmaker", "m@tchmaykrusr".toCharArray());
        //return new MongoClient("127.0.0.1", 27017);
    	return mongoClient;
    }

    @Override
    protected String getMappingBasePackage() {
      return "org.broadinstitute.macarthurlab.matchbox";
    }

}

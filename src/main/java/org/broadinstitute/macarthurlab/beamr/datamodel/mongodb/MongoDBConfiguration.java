/**
 * To configure interaction with MongoDB
 */
package org.broadinstitute.macarthurlab.beamr.datamodel.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

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
        return "beamr";
    }
 
    @Override
    /**
     * Hostname of database is set here. Assumed to be localhost for now. Please update as needed.
     */
    public Mongo mongo() throws Exception {
        return new MongoClient("127.0.0.1", 27017);
    }
    
    @Override
    protected String getMappingBasePackage() {
      return "org.broadinstitute.macarthurlab.beamr";
    }
    
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }
}

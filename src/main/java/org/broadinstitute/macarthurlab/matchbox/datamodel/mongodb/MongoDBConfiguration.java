/**
 * To configure interaction with MongoDB
 */
package org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;


@Component
@EnableMongoRepositories
@PropertySource("file:resources/application.properties")
public class MongoDBConfiguration extends AbstractMongoConfiguration{

	
	@Value("${mongoDatabaseHostName}")
	private String databaseHostName;
	
	@Value("${mongoDatabaseUserName}")
	private String username;
	
	@Value("${mongoDatabasePassword}")
	private String password;
	
	@Value("${mongoDatabaseName}")
	private String databaseName;
	
	@Value("${mongoDatabaseMappingBasePackage}")
	private String mappingBasePackage;
	
	@Value("${keyTrustStore}")
	private String keyTrustStore;
	
 
    @Override
    /**
     * This get's set as the database. Collection name is set in 
     * entity itself
     */
    protected String getDatabaseName() {
        return this.databaseName;
    }
 
    @Override
	@Bean
    /**
     * Hostname of database is set here. Assumed to be localhost for now. Please update as needed.
     * TODO: take out password from here
     */
    public Mongo mongo() throws Exception {	
    	//set the trust store java path (required for Communication class as well
    	System.setProperty("javax.net.ssl.trustStore",this.getKeyTrustStore());
    	
    	MongoClient mongoClient = new MongoClient(this.getDatabaseHostName(), 27017);
    	DB db = mongoClient.getDB(this.getDatabaseName());
    	boolean auth = db.authenticate(this.getUsername(), this.getPassword().toCharArray());
    	return mongoClient;
    }

    @Override
    protected String getMappingBasePackage() {
      return this.mappingBasePackage;
    }

	/**
	 * @return the databaseHostName
	 */
	public String getDatabaseHostName() {
		return databaseHostName;
	}

	/**
	 * @param databaseHostName the databaseHostName to set
	 */
	public void setDatabaseHostName(String databaseHostName) {
		this.databaseHostName = databaseHostName;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param databaseName the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @param mappingBasePackage the mappingBasePackage to set
	 */
	public void setMappingBasePackage(String mappingBasePackage) {
		this.mappingBasePackage = mappingBasePackage;
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * @return the keyTrustStore
	*/
	public String getKeyTrustStore() {
		return this.keyTrustStore;
	}

	/**
	 * @param keyTrustStore the keyTrustStore to set
	*/
	public void setKeyTrustStore(String keyTrustStore) {
		this.keyTrustStore = keyTrustStore;
	}

}

/**
 * A flexible Data store for matchbox
 */
package org.broadinstitute.macarthurlab.matchbox.datamodel;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.match.GenotypeSimilarityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * @author harindra
 *
 */
public class DataModelImpl implements DataModel{
	
	@Value("${datasource.type}")
	private String datasourceType;
	
	private static final String MONGODB="mongodb";
	private static final String POSTGRESQL="postgres";
	
    private static final Logger logger = LoggerFactory.getLogger(GenotypeSimilarityServiceImpl.class);
    
    /**
     * A connection to MongoDB for queries
     */
    @Autowired
    private PatientMongoRepository patientMongoRepository;
    
    @Autowired
    private MongoOperations operator;
    
    private Object databaseConnection;
	
	
	/**
	 * Constructor
	 * @throws RuntimeException when data source is not defined in app properties at startup 
	 */
	public DataModelImpl() throws Exception {
		if (this.getDatasourceType().equals(MONGODB)){
			this.databaseConnection = this.getOperator();
		}
		if (this.getDatasourceType().equals(POSTGRESQL)){
			
		}
		throw new Exception("data source not defined");
	}
	
	
	/**
	 * @return the datasourceType
	 */
	public String getDatasourceType() {
		return datasourceType;
	}
	
	/**
	 * @param datasourceType the datasourceType to set
	 */
	public void setDatasourceType(String datasourceType) {
		this.datasourceType = datasourceType;
	}


	/**
	 * @return the patientMongoRepository
	 */
	public PatientMongoRepository getPatientMongoRepository() {
		return patientMongoRepository;
	}


	/**
	 * @param patientMongoRepository the patientMongoRepository to set
	 */
	public void setPatientMongoRepository(PatientMongoRepository patientMongoRepository) {
		this.patientMongoRepository = patientMongoRepository;
	}


	/**
	 * @return the operator
	 */
	public MongoOperations getOperator() {
		return operator;
	}


	/**
	 * @param operator the operator to set
	 */
	public void setOperator(MongoOperations operator) {
		this.operator = operator;
	}


	/**
	 * @return the databaseConnection
	 */
	public Object getDatabaseConnection() {
		return databaseConnection;
	}


	/**
	 * @param databaseConnection the databaseConnection to set
	 */
	public void setDatabaseConnection(Object databaseConnection) {
		this.databaseConnection = databaseConnection;
	}
	
	
	
	
	
	
	//depending on app prop use one of the interfaces, might need to not use autowiring anymore
}

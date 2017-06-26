/**
 * To represent a public facing metric end point
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;

import org.broadinstitute.macarthurlab.matchbox.entities.Metric;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PublicMetric;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author harindra
 *
 */
@Service(value="publicMetricServiceImpl")
public class PublicMetricServiceImpl extends BaseMetric implements MetricService {

	@Override
	/**
	 * Returns a String representing a JSON
	 * TODO should be an obj reprsenting the JSON
	 */
	public Metric getMetrics() {
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> allPatients = operator.find(q,Patient.class);
		
		return new PublicMetric(
								this.getNumberOfSubmitters(allPatients), 
								this.countGenesInSystem(allPatients).size(), 
								this.countPhenotypesInSystem(allPatients).size(),
								this.getNumberOfCasesWithDiagnosis(), 
								this.getNumOfPatientsInSystem(allPatients), 
								this.getPercentageOfGenesThatMatch(allPatients),
								this.getMeanNumberOfGenesPerCase(allPatients), 
								this.getMeanNumberOfVariantsPerCase(allPatients), 
								this.getMeanNumberOfPhenotypesPerCase(allPatients),
								this.getNumOfIncomingMatchRequests(), 
								this.getNumOfMatches());
	}

}



















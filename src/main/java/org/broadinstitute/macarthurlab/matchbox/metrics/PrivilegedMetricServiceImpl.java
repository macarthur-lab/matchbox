/**
 * Generate metrics
 */
package org.broadinstitute.macarthurlab.matchbox.metrics;


import org.broadinstitute.macarthurlab.matchbox.entities.Metric;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PrivilegedMetric;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author harindra
 *
 */
@Service(value="privilegedMetricServiceImpl")
public class PrivilegedMetricServiceImpl extends BaseMetric implements MetricService{

	/**
	 * Returns a String representing a JSON
	 * TODO should be an obj reprsenting the JSON
	 */
	public PrivilegedMetric getMetricsTEMP() {
		return new PrivilegedMetric();
	}


	@Override
	/**
	 * Returns a seiries of metric as JSON
	 */
	public Metric getMetrics() {	
		StringBuilder query = new StringBuilder("{}");
		BasicQuery q = new BasicQuery(query.toString());
		List<Patient> allPatients = operator.find(q,Patient.class);

		return new PrivilegedMetric(
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
								this.getNumOfMatches(),
								this.countGenesInSystem(allPatients),
								this.countPhenotypesInSystem(allPatients));
	}
}



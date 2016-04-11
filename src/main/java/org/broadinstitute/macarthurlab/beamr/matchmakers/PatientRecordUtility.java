/**
 * A suite of methods to help process patient records from other matchmakers
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadinstitute.macarthurlab.beamr.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.beamr.entities.Patient;
import org.broadinstitute.macarthurlab.beamr.entities.PhenotypeFeature;
import org.broadinstitute.macarthurlab.beamr.entities.Variant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author harindra
 *
 */
public class PatientRecordUtility {
	
	/**
	 * Default constructor does nothing
	 */
	public PatientRecordUtility(){}
	
	/**
	 * Parses a patient information set encoded in a JSON string and returns a Patient
	 * object that encloses that information
	 * @param patientString	A JSON string of data about the patient (structure expected)
	 * @return	A Patient obect that encloses the JSON data
	 */
	public Patient parsePatientInformation(String patientJsonString){
		JSONParser parser = new JSONParser();
		try{
			JSONObject jsonObject = (JSONObject) parser.parse(patientJsonString);
			JSONObject patient = (JSONObject)jsonObject.get("patient");
			
			String id = (String)patient.get("id"); 
			String label = (String)patient.get("label"); 
			String species = (String)patient.get("species");
			String sex = (String)patient.get("sex");
			String ageOfOnset = (String)patient.get("ageOfOnset");
			String inheritanceMode = (String)patient.get("inheritanceMode");
			
			Map<String,String> contactDets = new HashMap<String,String>();
			JSONObject  contact = (JSONObject)patient.get("contact");
			contactDets.put("name", (String)contact.get("name"));
			contactDets.put("institution", (String)contact.get("institution"));
			contactDets.put("href", (String)contact.get("href"));
			
			List<Map<String,String>> disorderDets = new ArrayList<Map<String,String>>();
			JSONArray  disorders = (JSONArray)patient.get("disorders");
			for (int i=0; i<disorders.size(); i++){
				Map <String,String> disorderDet = new HashMap<String,String>();
				JSONObject disorder = (JSONObject)disorders.get(i);
				disorderDet.put("id", (String)disorder.get("id"));
				disorderDets.add(disorderDet);
			}
			
			List<PhenotypeFeature> featuresDets = new ArrayList<PhenotypeFeature>();
			JSONArray  features = (JSONArray)patient.get("features");
			for (int i=0; i<features.size(); i++){
				JSONObject feature = (JSONObject)features.get(i);
				featuresDets.add(
						new PhenotypeFeature(
								(String)feature.get("id"),
								(String)feature.get("observed"),
								(String)feature.get("ageOfOnset")
								));
			}
			
			List<GenomicFeature> genomicFeaturesDets = new ArrayList<GenomicFeature>();
			JSONArray  genomicFeatures = (JSONArray)patient.get("genomicFeatures");
			for (int i=0; i<genomicFeatures.size(); i++){
				JSONObject genomicFeature = (JSONObject)genomicFeatures.get(i);
				
				Map<String,String> geneDets = new HashMap<String,String>();
				JSONObject geneGenomicFeature  = (JSONObject)genomicFeature.get("gene");
				geneDets.put("id",(String)geneGenomicFeature.get("id"));
				
			
				JSONObject variantGenomicFeature  = (JSONObject)genomicFeature.get("variant");
				Variant variantDets = new Variant(
						(String)variantGenomicFeature.get("assembly"),
						(String)variantGenomicFeature.get("referenceName"),
						(Long)variantGenomicFeature.get("start"),
						(Long)variantGenomicFeature.get("end"),
						(String)variantGenomicFeature.get("referenceBases"),
						(String)variantGenomicFeature.get("alternateBases")
						);

				Long zygosity = (Long)genomicFeature.get("zygosity");
				
				
				Map<String,String> typeDets = new HashMap<String,String>();
				JSONObject typeGenomicFeature  = (JSONObject)genomicFeature.get("type");
				typeDets.put("id", (String)typeGenomicFeature.get("id"));
				typeDets.put("label", (String)typeGenomicFeature.get("label"));
				
				genomicFeaturesDets.add(
						new GenomicFeature(
								geneDets,
								variantDets,
								zygosity,
								typeDets
								));
			}
			
			return new Patient(
					id, 
					label, 
					contactDets, 
					species,
					sex,
					ageOfOnset, 
					inheritanceMode, 
					disorderDets, 
					featuresDets,
					genomicFeaturesDets);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return new Patient();
	}
}

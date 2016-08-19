/**
 * A suite of methods to help process patient records from other matchmakers
 */
package org.broadinstitute.macarthurlab.matchbox.matchmakers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.PatientMongoRepository;
import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author harindra
 * Does a high level check if the basic requirements are satisfied
 *
 */
public class PatientRecordUtility {
	@Autowired
	private PatientMongoRepository patientMongoRepository;
	
	/**
	 * Default constructor does nothing
	 */
	public PatientRecordUtility(){}
	
	
	public boolean areAllRequiredFieldsPresent(String patientJsonString){
		boolean verdict=true;
		JSONParser parser = new JSONParser();
		try{
			JSONObject jsonObject = (JSONObject) parser.parse(patientJsonString);
			JSONObject patient = (JSONObject)jsonObject.get("patient");	

		//Id
		if (patient.containsKey("id")){
			String id = (String)patient.get("id");
		}
		else{
			System.out.println("Error-areAllRequiredFieldsPresent: id missing, failing requirements!");
			verdict=false;
		}
		//Contact
		if (patient.containsKey("contact")){
			Map<String,String> contactDets = new HashMap<String,String>();
			JSONObject  contact = (JSONObject)patient.get("contact");
			contactDets.put("name", (String)contact.get("name"));
			contactDets.put("institution", (String)contact.get("institution"));
			contactDets.put("href", (String)contact.get("href"));
			}
		else{
			System.out.println("Error-areAllRequiredFieldsPresent: Some or all contact information missing, failing requirements!");
			verdict=false;
		}
		
		//Either features or genomicFeature HAVE TO BE PRESENT
		if (!patient.containsKey("features")  && !patient.containsKey("genomicFeatures") ){
			System.out.println("Error-areAllRequiredFieldsPresent: features and genomicFeature both missing, failing requirements!");
			verdict=false;
		}
		}
		catch(Exception e){
			System.out.println("ERROR: a required value is missing or wrong value in place, error parsing and absorbing patient data (areAllRequiredFieldsPresent): " + e.toString());
			System.out.println("Entered patient: "+ patientJsonString);
			verdict=false;
		}
		return verdict;
	}
	
	
	
	/**
	 * Parses a patient information set encoded in a JSON string and returns a Patient
	 * object that encloses that information
	 * @param patientString	A JSON string of data about the patient (structure expected)
	 * @return	A Patient object that encloses the JSON data
	 */
	public Patient parsePatientInformation(String patientJsonString){
		JSONParser parser = new JSONParser();
		try{
			JSONObject jsonObject = (JSONObject) parser.parse(patientJsonString);
			JSONObject patient = (JSONObject)jsonObject.get("patient");
			
			//REQUIRED
			String id = (String)patient.get("id");
			
			//REQUIRED
			Map<String,String> contactDets = new HashMap<String,String>();
			JSONObject  contact = (JSONObject)patient.get("contact");
			contactDets.put("name", (String)contact.get("name"));
			contactDets.put("institution", (String)contact.get("institution"));
			contactDets.put("href", (String)contact.get("href"));
			
			//if genomicFeatures is not supplied, this is REQUIRED
			List<PhenotypeFeature> featuresDets = new ArrayList<PhenotypeFeature>();
			if (patient.containsKey("features")){
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
			}
			
			//if features is not supplied, this is REQUIRED
			List<GenomicFeature> genomicFeaturesDets = new ArrayList<GenomicFeature>();
			if (patient.containsKey("genomicFeatures")){
				JSONArray  genomicFeatures = (JSONArray)patient.get("genomicFeatures");
				
				for (int i=0; i<genomicFeatures.size(); i++){
					JSONObject genomicFeature = (JSONObject)genomicFeatures.get(i);
					
					//REQUIRED (a map with a single key "id")
					JSONObject geneGenomicFeature  = (JSONObject)genomicFeature.get("gene");
					Map<String,String> geneDets = new HashMap<String,String>();
					geneDets.put("id",(String)geneGenomicFeature.get("id"));
				
					//OPTIONAL
					Variant variant = new Variant("", 
							  "", 
							  -1L, 
							  -1L, 
							  "",
							  "");
					if (genomicFeature.containsKey("variant")){
						JSONObject variantGenomicFeature  = (JSONObject)genomicFeature.get("variant"); 
						//REQUIRED
						String assembly=(String)variantGenomicFeature.get("assembly");
						//REQUIRED
						String referenceName=(String)variantGenomicFeature.get("referenceName");
						//REQUIRED
						Long start = (Long)variantGenomicFeature.get("start");
						//OPTIONAL
						Long end=-1L;
						if (variantGenomicFeature.containsKey("end")){
							end = (Long)variantGenomicFeature.get("end");
						}
						//OPTIONAL
						String referenceBases="";
						if(variantGenomicFeature.containsKey("referenceBases")){
							referenceBases = (String)variantGenomicFeature.get("referenceBases");
						}
						//OPTIONAL
						String alternateBases ="";
						if (variantGenomicFeature.containsKey("alternateBases")){
							alternateBases = (String)variantGenomicFeature.get("alternateBases");
						}
						
						variant = new Variant(assembly, 
													  referenceName, 
													  start, 
													  end, 
													  referenceBases,
													  alternateBases);
					}
					
					//OPTIONAL
					Long zygosity = -1L;
					if (genomicFeature.containsKey("zygosity")){
						zygosity = (Long)genomicFeature.get("zygosity");
					}
				
					//OPTIONAL
					Map<String,String> typeDets = new HashMap<String,String>();
					typeDets.put("id", "");
					typeDets.put("label", "");
					if (genomicFeature.containsKey("type")){
						JSONObject typeGenomicFeature  = (JSONObject)genomicFeature.get("type");
						//REQUIRED
						typeDets.put("id", (String)typeGenomicFeature.get("id"));
						//OPTIONAL
						String label="";
						if (typeGenomicFeature.containsKey("label")){
							label=(String)typeGenomicFeature.get("label");
							typeDets.put("label", (String)typeGenomicFeature.get("label"));
						}
					}
					
					genomicFeaturesDets.add(new GenomicFeature(geneDets, 
															   variant, 
															   zygosity,
															   typeDets)
												);
				}
			}
			
			//OPTIONAL
			String label="";
			if (patient.containsKey("label")){
				label = (String)patient.get("label"); 
			}
			
			//OPTIONAL
			String species="";
			if (patient.containsKey("species")){
				species = (String)patient.get("species");
			}
			
			//OPTIONAL
			String sex="";
			if (patient.containsKey("sex")){
				sex = (String)patient.get("sex");
			}
			//OPTIONAL
			String ageOfOnset="";
			if (patient.containsKey("ageOfOnset")){
				ageOfOnset = (String)patient.get("ageOfOnset");
			}
			
			//OPTIONAL
			String inheritanceMode="";
			if (patient.containsKey("ageOfOnset")){
				inheritanceMode = (String)patient.get("inheritanceMode");
			}
			
			//OPTIONAL
			List<Map<String,String>> disorderDets = new ArrayList<Map<String,String>>();
			if (patient.containsKey("disorders")){
			JSONArray  disorders = (JSONArray)patient.get("disorders");
			for (int i=0; i<disorders.size(); i++){
				Map <String,String> disorderDet = new HashMap<String,String>();
				JSONObject disorder = (JSONObject)disorders.get(i);
				disorderDet.put("id", (String)disorder.get("id"));
				disorderDets.add(disorderDet);
			}
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
	
	
	/**
	 * Parsed json payload from a delete call. The payload looks like {"id":"idToDelete"}
	 * @param jsonString	json string
	 * @return parsed values as a map
	 */
	public Map<String,String> parsePatientIdFromDeleteCall(String jsonString){
		JSONParser parser = new JSONParser();
		Map<String,String> parsed = new HashMap<String,String> ();
		try{
			JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
			String id = (String)jsonObject.get("id");
			parsed.put("id", id);
		}
		catch(Exception e){
				System.out.println("ERROR: parsing id from JSON DELETE call :"+e);
			}
		return parsed;
	}
	
	
	/**
	 * Checks if this patient is already in the MME system locally (no way to check in other nodes
	 * as of July 2016)
	 * @param patient	A Patient structure
	 * @return	True if exists
	 */
	public boolean isPatientInMme(Patient patient){
		
		return true;
	}


	/**
	 * @return the patientMongoRepository
	 */
	public PatientMongoRepository getPatientMongoRepository() {
		return patientMongoRepository;
	}

	
}

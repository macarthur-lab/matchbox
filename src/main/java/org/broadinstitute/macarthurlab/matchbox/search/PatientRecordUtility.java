/**
 * A suite of methods to help process patient records from other matchmakers
 */
package org.broadinstitute.macarthurlab.matchbox.search;


import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author harindra
 * Does a high level check if the basic requirements are satisfied
 *
 */
@Component
public class PatientRecordUtility {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
			logger.error("Error-areAllRequiredFieldsPresent: id missing, failing requirements!: "+patientJsonString);
			verdict=false;
		}
		//Contact
		if (patient.containsKey("contact")){
			Map<String,String> contactDets = new HashMap<>();
			JSONObject  contact = (JSONObject)patient.get("contact");
			contactDets.put("name", (String)contact.get("name"));
			contactDets.put("institution", (String)contact.get("institution"));
			contactDets.put("href", (String)contact.get("href"));
			}
		else{
			logger.warn("areAllRequiredFieldsPresent: Some or all contact information missing, failing requirements!: "+patientJsonString);
			verdict=false;
		}
		
		//Either features or genomicFeature HAVE TO BE PRESENT
		if (!patient.containsKey("features")  && !patient.containsKey("genomicFeatures") ){
			logger.warn("areAllRequiredFieldsPresent: features and genomicFeature both missing, failing requirements!: "+patientJsonString);
			verdict=false;
		}
		}
		catch(Exception e){
			logger.warn("required value is missing or wrong value in place, error parsing and absorbing patient data (areAllRequiredFieldsPresent): " + e.toString() + " : " + patientJsonString);
			verdict=false;
		}
		return verdict;
	}
	
	
	
	/**
	 * Parses a patient information set encoded in a JSON string and returns a Patient
	 * object that encloses that information
	 * @param patientJsonString	A JSON string of data about the patient (structure expected)
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
			Map<String,String> contactDets = new HashMap<>();
			JSONObject  contact = (JSONObject)patient.get("contact");
			contactDets.put("name", (String)contact.get("name"));
			contactDets.put("institution", (String)contact.get("institution"));
			contactDets.put("href", (String)contact.get("href"));
			
			//if genomicFeatures is not supplied, this is REQUIRED
			List<PhenotypeFeature> featuresDets = new ArrayList<>();
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
			List<GenomicFeature> genomicFeaturesDets = new ArrayList<>();
			if (patient.containsKey("genomicFeatures")){
				JSONArray  genomicFeatures = (JSONArray)patient.get("genomicFeatures");
				
				for (int i=0; i<genomicFeatures.size(); i++){
					JSONObject genomicFeature = (JSONObject)genomicFeatures.get(i);
					
					//REQUIRED (a map with a single key "id")
					JSONObject geneGenomicFeature  = (JSONObject)genomicFeature.get("gene");
					Map<String,String> geneDets = new HashMap<>();
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
			List<Map<String,String>> disorderDets = new ArrayList<>();
			if (patient.containsKey("disorders")){
			JSONArray  disorders = (JSONArray)patient.get("disorders");
			for (int i=0; i<disorders.size(); i++){
				Map <String,String> disorderDet = new HashMap<>();
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
				logger.error("ERROR: parsing id from JSON DELETE call :"+e.getMessage());
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
	 * The standard information release disclaimer
	 * @return a disclaimer string
	 */
	public String getDisclaimer(){
		StringBuilder disclaimer = new StringBuilder();
		disclaimer.append("The data in Matchmaker Exchange is provided for research use only. ");
		disclaimer.append("Broad Institute provides the data in Matchmaker Exchange 'as is'. Broad Institute makes no representations or warranties of any kind concerning the data, express or implied, including without limitation, warranties of merchantability, fitness for a particular purpose, noninfringement, or the absence of latent or other defects, whether or not discoverable. Broad will not be liable to the user or any third parties claiming through user, for any loss or damage suffered through the use of Matchmaker Exchange. In no event shall Broad Institute or its respective directors, officers, employees, affiliated investigators and affiliates be liable for indirect, special, incidental or consequential damages or injury to property and lost profits, regardless of whether the foregoing have been advised, shall have other reason to know, or in fact shall know of the possibility of the foregoing. ");
		disclaimer.append("Prior to using Broad Institute data in a publication, the user will contact the owner of the matching dataset to assess the integrity of the match. If the match is validated, the user will offer appropriate recognition of the data owner's contribution, in accordance with academic standards and custom. Proper acknowledgment shall be made for the contributions of a party to such results being published or otherwise disclosed, which may include co-authorship. ");
		disclaimer.append("If Broad Institute contributes to the results being published, the authors must acknowledge Broad Institute using the following wording: 'This study makes use of data shared through the Broad Institute matchbox repository. Funding for the Broad Institute was provided in part by National Institutes of Health grant UM1 HG008900 to Daniel MacArthur and Heidi Rehm.' ");
		disclaimer.append("User will not attempt to use the data or Matchmaker Exchange to establish the individual identities of any of the subjects from whom the data were obtained. This applies to matches made within Broad Institute or with any other database included in the Matchmaker Exchange. ");
		return disclaimer.toString();
	}
	
}

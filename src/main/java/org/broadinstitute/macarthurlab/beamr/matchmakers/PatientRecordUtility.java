/**
 * A suite of methods to help process patient records from other matchmakers
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

import java.io.IOException;
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
			verdict=false;
		}
		
		
		//Either features or genomicFeature HAVE TO BE PRESENT
		if (!patient.containsKey("features")  && !patient.containsKey("genomicFeatures") ){
			verdict=false;
		}

		
		}
		catch(Exception e){
			System.out.println("ERROR:parsing input query patient data: " + e.toString());
			verdict=false;
		}

		return verdict;
	}
	
	/**
	 * Parses a patient information set encoded in a JSON string and returns a Patient
	 * object that encloses that information
	 * @param patientString	A JSON string of data about the patient (structure expected)
	 * @return	A Patient obect that encloses the JSON data
	 */
	public Patient parsePatientInformation(String patientJsonString){
		System.out.println(patientJsonString);
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
					GenomicFeature parsedGenomicFeature= new GenomicFeature();
					JSONObject genomicFeature = (JSONObject)genomicFeatures.get(i);
					Map<String,String> geneDets = new HashMap<String,String>();
					
					//REQUIRED (a map with a single key "id")
					JSONObject geneGenomicFeature  = (JSONObject)genomicFeature.get("gene");
					geneDets.put("id",(String)geneGenomicFeature.get("id"));
					parsedGenomicFeature.setGene(geneDets);
				
					//OPTIONAL
					if (patient.containsKey("variant")){
						JSONObject variantGenomicFeature  = (JSONObject)genomicFeature.get("variant");
						Variant variantDets = new Variant();
					
						//REQUIRED
						String assembly=(String)variantGenomicFeature.get("assembly");
						variantDets.setAssembly(assembly);
						//REQUIRED
						String referenceName=(String)variantGenomicFeature.get("referenceName");
						variantDets.setReferenceName(referenceName);
						//REQUIRED
						Long start = (Long)variantGenomicFeature.get("start");
						variantDets.setStart(start);
						//OPTIONAL
						if (variantGenomicFeature.containsKey("end")){
							Long end = (Long)variantGenomicFeature.get("end");
							variantDets.setEnd(end);
						}
						//OPTIONAL
						if(variantGenomicFeature.containsKey("referenceBases")){
							String referenceBases = (String)variantGenomicFeature.get("referenceBases");
							variantDets.setReferenceBases(referenceBases);
						}
						//OPTIONAL
						if (variantGenomicFeature.containsKey("alternateBases")){
							String alternateBases = (String)variantGenomicFeature.get("alternateBases");
							variantDets.setAlternateBases(alternateBases);
						}
						parsedGenomicFeature.setVariant(variantDets);
					}
					
					//OPTIONAL
					if (genomicFeature.containsKey("zygosity")){
						Long zygosity = (Long)genomicFeature.get("zygosity");
						parsedGenomicFeature.setZygosity(zygosity);
					}
				
					//OPTIONAL
					if (genomicFeature.containsKey("type")){
						Map<String,String> typeDets = new HashMap<String,String>();
						JSONObject typeGenomicFeature  = (JSONObject)genomicFeature.get("type");
						//REQUIRED
						typeDets.put("id", (String)typeGenomicFeature.get("id"));
						//OPTIONAL
						if (typeGenomicFeature.containsKey("label")){
							typeDets.put("label", (String)typeGenomicFeature.get("label"));
						}
						parsedGenomicFeature.setType(typeGenomicFeature);
					}
					
					genomicFeaturesDets.add(parsedGenomicFeature);
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
}

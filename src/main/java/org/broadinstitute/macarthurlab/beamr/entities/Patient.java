package org.broadinstitute.macarthurlab.beamr.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="patient")
public class Patient {
	
	@Id
	private final String id;
	private final String label;
	/**
	 * This contains details on who submitted the patient in,
	 * "name", "institution", "href"
	 */
	private final Map<String,String> contact;
	/**
	 * This contains a NCBI taxon identifier
	 */
	private final String species;
	/**
	 * Allowed values:
	 * "FEMALE"|"MALE"|"OTHER"|"MIXED_SAMPLE"|"NOT_APPLICABLE"
	 */
	private final String sex;
	/**
	 * This should contain HPO code
	 */
	private final String ageOfOnset;
	/**
	 * This should contain HPO code
	 */
	private final String inheritanceMode;
	/**
	 * This contains a list of Maps. each Map is a disorder 
	 * Map looks like,
	 *  "id" : "MIM:######"|"Orphanet:#####"|…
	 */
	private final List<Map<String,String>> disorders;
	/**
	 * Features is a list of Feature objects,
	 */
	private final List<PhenotypeFeature> features;
	/**
	 * Represents genomic features and are a list of GenomicFeature objects
	 */
	private final List<GenomicFeature> genomicFeatures;
	
	
	/**
	 * Default constructor builds empty object
	 */
	public Patient() {
		this.id = "";
		this.label = "";
		this.contact = new HashMap<String, String>();
		this.species = "";
		this.sex = "";
		this.ageOfOnset = "";
		this.inheritanceMode = "";
		this.disorders = new ArrayList<Map<String,String>>();
		this.features = new ArrayList<PhenotypeFeature>();
		this.genomicFeatures = new ArrayList<GenomicFeature>();
	}
	
	
	
	/**
	 * @param id	Id of patient
	 * @param label	label for patient
	 * @param contact	primary contact regarding patient
	 * @param species	species of patient
	 * @param sex	sex of patient
	 * @param ageOfOnset	age of onset of disease
	 * @param inheritanceMode	inheritance mode
	 * @param disorders	disorders affecting patient
	 * @param features	features
	 * @param genomicFeatures	genomic features
	 */
	public Patient(String id, String label, Map<String, String> contact, String species, String sex,
			String ageOfOnset, String inheritanceMode, List<Map<String, String>> disorders, List<PhenotypeFeature> features,
			List<GenomicFeature> genomicFeatures) {
		this.id = id;
		this.label = label;
		this.contact = contact;
		this.species = species;
		this.sex = sex;
		this.ageOfOnset = ageOfOnset;
		this.inheritanceMode = inheritanceMode;
		this.disorders = disorders;
		this.features = features;
		this.genomicFeatures = genomicFeatures;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return the contact
	 */
	public Map<String, String> getContact() {
		return contact;
	}
	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}
	/**
	 * @return the sex
	 */
	public String getSex() {
		return sex;
	}
	/**
	 * @return the ageOfOnset
	 */
	public String getAgeOfOnset() {
		return ageOfOnset;
	}
	/**
	 * @return the inheritanceMode
	 */
	public String getInheritanceMode() {
		return inheritanceMode;
	}
	/**
	 * @return the disorders
	 */
	public List<Map<String, String>> getDisorders() {
		return disorders;
	}
	/**
	 * @return the features
	 */
	public List<PhenotypeFeature> getFeatures() {
		return features;
	}
	/**
	 * @return the genomicFeatures
	 */
	public List<GenomicFeature> getGenomicFeatures() {
		return genomicFeatures;
	}



	/* 
	 * To String method
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Patient [id=" + id + ", label=" + label + ", contact=" + contact + ", species=" + species + ", sex="
				+ sex + ", ageOfOnset=" + ageOfOnset + ", inheritanceMode=" + inheritanceMode + ", disorders="
				+ disorders + ", features=" + features + ", genomicFeatures=" + genomicFeatures + "]";
	}




	
	
}

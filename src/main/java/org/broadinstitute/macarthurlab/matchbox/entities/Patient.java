package org.broadinstitute.macarthurlab.matchbox.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection="patient")
@Entity
@Table(name = "patient")
public class Patient{
	
	/**
	 * Should this ID be the ID of the patient (which enforces uniqueness)
	 * of allow mongo to generate a object ID (side effect: can insert same patient
	 * many times)
	 * REQUIRED
	 */
	@Id
	private final String id;
	
	/**
	 * OPTIONAL
	 */
	private final String label;
	
	/**
	 * This contains details on who submitted the patient in,
	 * "name", "institution", "href"
	 * REQUIRED
	 */
	@ElementCollection
	private final Map<String,String> contact;
	
	/**
	 * This contains a NCBI taxon identifier
	 * OPTIONAL
	 */
	private final String species;
	
	/**
	 * Allowed values:
	 * "FEMALE"|"MALE"|"OTHER"|"MIXED_SAMPLE"|"NOT_APPLICABLE"
	 * OPTIONAL
	 */
	private final String sex;
	
	/**
	 * This should contain HPO code
	 * OPTIONAL
	 */
	private final String ageOfOnset;
	
	/**
	 * This should contain HPO code
	 * OPTIONAL
	 */
	private final String inheritanceMode;
	
	/**
	 * This contains a list of Maps. each Map is a disorder 
	 * Map looks like,
	 *  "id" : "MIM:######"|"Orphanet:#####"|…
	 *  OPTIONAL
	 */
	@ElementCollection
	private final List<Disorder> disorders;
	
	/**
	 * Features is a list of Feature objects,
	 * *******************************
	 * IF genomicFeatures NOT give:  *
	 * 	REQUIRED  					 *
	 * ELSE:	 					 *
	 *  OPTIONAL  					 *
	 * *******************************
	 */
	@ElementCollection(targetClass=PhenotypeFeature.class)
	private final List<PhenotypeFeature> features;
	
	/**
	 * Represents genomic features and are a list of GenomicFeature objects
	 * *******************************
	 * IF features NOT give:		 *
	 * 	REQUIRED  --------			 *
	 * ELSE:      --------			 *
	 *  OPTIONAL  --------			 *
	 * *******************************
	 */
	@ElementCollection(targetClass=GenomicFeature.class)
	private final List<GenomicFeature> genomicFeatures;
	
	
	/**
	 * Default constructor builds empty object
	 */
	public Patient() {
		this.id = "";
		this.label = "";
		this.contact = new HashMap<>();
		this.species = "";
		this.sex = "";
		this.ageOfOnset = "";
		this.inheritanceMode = "";
		this.disorders = new ArrayList<Disorder>();
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
	public Patient(String id, 
					String label, 
					Map<String, String> contact, 
					String species, 
					String sex,
					String ageOfOnset, 
					String inheritanceMode, 
					List<Disorder> disorders, 
					List<PhenotypeFeature> features,
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
	public List<Disorder> getDisorders() {
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

	
	
	
	/**
	 * Returns a JSON representation and keeps out empty fields
	 * @return A JSON string
	 */
	public String getEmptyFieldsRemovedJson(){
		StringBuilder asJson=new StringBuilder();
		asJson.append("{");
		
		//REQUIRED
		asJson.append("\"id\":");
		asJson.append("\"" + this.getId() + "\"");
		//OPTIONAL
		if (this.getLabel() !=null && !this.getLabel().equals("")){
			asJson.append(",");
			asJson.append("\"label\":");
			asJson.append("\"" + this.getLabel() + "\"");
		}
		//REQUIRED
		asJson.append(",");
		asJson.append("\"contact\":{");
		int i=0;
		for (String k:this.getContact().keySet()){
			if (this.getContact().get(k) != null && !this.getContact().get(k).equals("")){
				asJson.append("\"" +  k + "\":");
				asJson.append("\"" + this.getContact().get(k) + "\"");
				if (i<this.getContact().size()-1){
					asJson.append(",");
				}
			}
			i+=1;
		}
		asJson.append("}");
		//OPTIONAL
		if (this.getSpecies() != ""){
			asJson.append(",");
			asJson.append("\"species\":");
			asJson.append("\"" + this.getSpecies() + "\"");
		}
		//OPTIONAL
		if (this.getSex() != ""){
			asJson.append(",");
			asJson.append("\"sex\":");
			asJson.append("\"" + this.getSex() + "\"");
		}
		//OPTIONAL
		if (this.getAgeOfOnset() != ""){
			asJson.append(",");
			asJson.append("\"ageOfOnset\":");
			asJson.append("\"" + this.getAgeOfOnset() + "\"");
		}
		//OPTIONAL
		if (this.getInheritanceMode() != ""){
			asJson.append(",");
			asJson.append("\"inheritanceMode\":");
			asJson.append("\"" + this.getInheritanceMode() + "\"");
		}
		//OPTIONAL
		if (this.getDisorders().size()>0){
			asJson.append(",");
			asJson.append("\"disorders\":[");
			int j=0;
			for (Disorder disorder:this.getDisorders()){
				asJson.append("{");
				asJson.append("disorderId:");
				asJson.append(disorder.getDisorderId());
				asJson.append(",");
				asJson.append("label:");
				asJson.append(disorder.getLabel());
				asJson.append("}");
				if (j<this.getDisorders().size()-1){
					asJson.append(",");
				}
				j+=1;
			}
			asJson.append("]");
		}

		//possibly OPTIONAL
		if (this.getFeatures().size()>0){
			asJson.append(",");
			asJson.append("\"features\":[");
			int k=0;
			for (PhenotypeFeature phenotypeFeature:this.getFeatures()){
				asJson.append(phenotypeFeature.getEmptyFieldsRemovedJson());
				if (k<this.getFeatures().size()-1){
					asJson.append(",");
				}
				k+=1;
			}
			asJson.append("]");
		}

		//possibly OPTIONAL
		if (this.getGenomicFeatures().size()>0){
			asJson.append(",");
			asJson.append("\"genomicFeatures\":[");
			int p=0;
			for (GenomicFeature genomicFeature:this.getGenomicFeatures()){
				asJson.append(genomicFeature.getEmptyFieldsRemovedJson());
				if (p<this.getGenomicFeatures().size()-1){
					asJson.append(",");
				}
				p+=1;
			}
			asJson.append("]");
		}
		
		//close the patient data structure
		asJson.append("}");
		return asJson.toString();
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ageOfOnset == null) ? 0 : ageOfOnset.hashCode());
		result = prime * result + ((contact == null) ? 0 : contact.hashCode());
		result = prime * result + ((disorders == null) ? 0 : disorders.hashCode());
		result = prime * result + ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((genomicFeatures == null) ? 0 : genomicFeatures.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((inheritanceMode == null) ? 0 : inheritanceMode.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((sex == null) ? 0 : sex.hashCode());
		result = prime * result + ((species == null) ? 0 : species.hashCode());
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		if (ageOfOnset == null) {
			if (other.ageOfOnset != null)
				return false;
		} else if (!ageOfOnset.equals(other.ageOfOnset))
			return false;
		if (contact == null) {
			if (other.contact != null)
				return false;
		} else if (!contact.equals(other.contact))
			return false;
		if (disorders == null) {
			if (other.disorders != null)
				return false;
		} else if (!disorders.equals(other.disorders))
			return false;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (genomicFeatures == null) {
			if (other.genomicFeatures != null)
				return false;
		} else if (!genomicFeatures.equals(other.genomicFeatures))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (inheritanceMode == null) {
			if (other.inheritanceMode != null)
				return false;
		} else if (!inheritanceMode.equals(other.inheritanceMode))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (sex == null) {
			if (other.sex != null)
				return false;
		} else if (!sex.equals(other.sex))
			return false;
		if (species == null) {
			if (other.species != null)
				return false;
		} else if (!species.equals(other.species))
			return false;
		return true;
	}
	
	
	
}
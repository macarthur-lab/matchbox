package org.broadinstitute.macarthurlab.beamr.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * @author harindra
 *
 */
public class GenomicFeature {
	/**
	 * Description of the gene that looks like,
	 * gene" : {
          "id" : <gene symbol>|<ensembl gene ID>|<entrez gene ID>
        }
	 */
	private final Map<String,String> gene;
	/**
	 * A description of a variant that looks like,
	 * "variant" : {
          "assembly" : "NCBI36"|"GRCh37.p13"|"GRCh38.p1"|…,
          "referenceName" : "1"|"2"|…|"X"|"Y",
          "start" : <number>,
          "end" : <number>,
          "referenceBases" : "A"|"ACG"|…,
          "alternateBases" : "A"|"ACG"|…
        },
	 */
	private final Map<String,String> variant;
	private final int zygosity;
	/**
	 * A type value that looks like,
	 *  "type" : {
          "id" : <SO code>,
          "label" : "STOPGAIN"
        }
	 */
	private final Map<String,String> type;
	/**
	 * @return the gene
	 */
	
	
	public Map<String, String> getGene() {
		return gene;
	}
	
	
	/**
	 * Default constructor creates empty object
	 */
	public GenomicFeature() {
		this.gene = new HashMap<String,String>();
		this.variant = new HashMap<String,String>();
		this.zygosity = 0;
		this.type = new HashMap<String,String>();;
	}
	
	
	/**
	 * @param gene	gene name
	 * @param variant	variant details
	 * @param zygosity	zygosity
	 * @param type	type
	 */
	public GenomicFeature(Map<String, String> gene, Map<String, String> variant, int zygosity,
			Map<String, String> type) {
		this.gene = gene;
		this.variant = variant;
		this.zygosity = zygosity;
		this.type = type;
	}
	
	
	/**
	 * @return the variant
	 */
	public Map<String, String> getVariant() {
		return variant;
	}
	/**
	 * @return the zygosity
	 */
	public int getZygosity() {
		return zygosity;
	}
	/**
	 * @return the type
	 */
	public Map<String, String> getType() {
		return type;
	}
	

}

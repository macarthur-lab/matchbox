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
	private Map<String,String> gene;
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
	private Variant variant;
	
	private Long zygosity;
	/**
	 * A type value that looks like,
	 *  "type" : {
          "id" : <SO code>,
          "label" : "STOPGAIN"
        }
	 */
	private Map<String,String> type;
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
		this.variant = new Variant();
		this.zygosity = new Long(0);
		this.type = new HashMap<String, String>();
	}
	
	
	/**
	 * @param gene	gene name
	 * @param variant	variant details
	 * @param zygosity	zygosity
	 * @param type	type
	 */
	public GenomicFeature(Map<String, String> gene, Variant variant, Long zygosity,
			Map<String, String> type) {
		this.gene = gene;
		this.variant = variant;
		this.zygosity = zygosity;
		this.type = type;
	}
	
	
	/**
	 * @return the variant
	 */
	public Variant getVariant() {
		return this.variant;
	}
	/**
	 * @return the zygosity
	 */
	public Long getZygosity() {
		return zygosity;
	}
	/**
	 * @return the type
	 */
	public Map<String, String> getType() {
		return type;
	}


	/* 
	 * To String method(non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GenomicFeature [gene=" + gene + ", variant=" + variant + ", zygosity=" + zygosity + ", type=" + type
				+ "]";
	}


	/**
	 * @param gene the gene to set
	 */
	public void setGene(Map<String, String> gene) {
		this.gene = gene;
	}


	/**
	 * @param variant the variant to set
	 */
	public void setVariant(Variant variant) {
		this.variant = variant;
	}


	/**
	 * @param zygosity the zygosity to set
	 */
	public void setZygosity(Long zygosity) {
		this.zygosity = zygosity;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(Map<String, String> type) {
		this.type = type;
	}
	
	
	
	

}

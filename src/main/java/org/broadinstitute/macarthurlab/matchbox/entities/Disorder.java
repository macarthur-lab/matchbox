/**
 * 
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

import javax.persistence.Embeddable;

/**
 * @author harindra
 *
 */
@Embeddable
public class Disorder {
	
	private final String disorderId;
	private final String label;

	
	/**
	 * Default constructor
	 */
	public Disorder(){
		this.disorderId="";
		this.label="";
	}
	
	/**
	 * constructor
	 */
	public Disorder(String disorderId, String label){
		this.disorderId=disorderId;
		this.label=disorderId;
	}

	/**
	 * @return the disorderId
	 */
	public String getDisorderId() {
		return disorderId;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
}

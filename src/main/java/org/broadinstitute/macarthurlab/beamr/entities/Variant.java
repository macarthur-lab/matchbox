/**
 * Represents a Variant record
 */
package org.broadinstitute.macarthurlab.beamr.entities;

/**
 * @author harindra
 *
 */
public class Variant {
	private String assembly;
	private String referenceName;
	private Long start;
	private Long end;
	private String referenceBases;
	private String alternateBases;
	
	
	/**
	 * Default constructor creates empty object
	 */
	public Variant(){
		this.assembly=null;
		this.referenceName=null;
		this.start=null;
		this.end=null;
		this.referenceBases=null;
		this.alternateBases=null;
	}
	
	
	/**
	 * @param assembly	assembly record
	 * @param referenceName	reference name used
	 * @param start	start pos
	 * @param end	end pos
	 * @param referenceBases	reference bases
	 * @param alternateBases	alternate bases
	 */
	public Variant(String assembly, 
				   String referenceName, 
				   Long start, 
				   Long end, 
				   String referenceBases,
				   String alternateBases) {
		super();
		this.assembly = assembly;
		this.referenceName = referenceName;
		this.start = start;
		this.end = end;
		this.referenceBases = referenceBases;
		this.alternateBases = alternateBases;
	}
	/**
	 * @return the assembly
	 */
	public String getAssembly() {
		return assembly;
	}
	/**
	 * @return the referenceName
	 */
	public String getReferenceName() {
		return referenceName;
	}
	/**
	 * @return the start
	 */
	public Long getStart() {
		return start;
	}
	/**
	 * @return the end
	 */
	public Long getEnd() {
		return end;
	}
	/**
	 * @return the referenceBases
	 */
	public String getReferenceBases() {
		return referenceBases;
	}
	/**
	 * @return the alternateBases
	 */
	public String getAlternateBases() {
		return alternateBases;
	}


	/* To string method
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Variant [assembly=" + assembly + ", referenceName=" + referenceName + ", start=" + start + ", end="
				+ end + ", referenceBases=" + referenceBases + ", alternateBases=" + alternateBases + "]";
	}


	/**
	 * @param assembly the assembly to set
	 */
	public void setAssembly(String assembly) {
		this.assembly = assembly;
	}


	/**
	 * @param referenceName the referenceName to set
	 */
	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}


	/**
	 * @param start the start to set
	 */
	public void setStart(Long start) {
		this.start = start;
	}


	/**
	 * @param end the end to set
	 */
	public void setEnd(Long end) {
		this.end = end;
	}


	/**
	 * @param referenceBases the referenceBases to set
	 */
	public void setReferenceBases(String referenceBases) {
		this.referenceBases = referenceBases;
	}


	/**
	 * @param alternateBases the alternateBases to set
	 */
	public void setAlternateBases(String alternateBases) {
		this.alternateBases = alternateBases;
	}
	
	

}

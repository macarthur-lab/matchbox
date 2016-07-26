/**
 * Represents a Variant record
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

/**
 * @author harindra
 *
 */
public class Variant {
	private final String assembly;
	private final String referenceName;
	private final Long start;
	private final Long end;
	private final String referenceBases;
	private final String alternateBases;
	

	
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

}

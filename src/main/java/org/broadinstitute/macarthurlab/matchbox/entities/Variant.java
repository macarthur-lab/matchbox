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
	private final boolean shareVariantLevelData;
	

	
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
				   String alternateBases,
				   boolean shareVariantLevelData) {
		this.assembly = assembly;
		this.referenceName = referenceName;
		this.start = start;
		this.end = end;
		this.referenceBases = referenceBases;
		this.alternateBases = alternateBases;
		this.shareVariantLevelData = shareVariantLevelData;
	}
	
	/**
	 * Deafult constructor for mostly testing
	 */
	public Variant(){
		this.assembly="";
		this.referenceName="";
		this.start=0L;
		this.end=0L;
		this.referenceBases="";
		this.alternateBases="";
		this.shareVariantLevelData=true;
	}
	
	/**
	 * Returns true if ALL fields are unpopulated
	 */
	public boolean isUnPopulated(){
        return this.assembly.equals("") &&
                this.referenceName.equals("") &&
                this.start == -1 &&
                this.end == -1 &&
                this.referenceBases.equals("") &&
                this.alternateBases.equals("");
    }
	
	
	/**
	 * Returns true if at least one field is unpopulated
	 */
	public boolean isPartiallyPopulated(){
        return this.assembly.equals("") ||
                this.referenceName.equals("") ||
                this.start == -1 ||
                this.end == -1 ||
                this.referenceBases.equals("") ||
                this.alternateBases.equals("");
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
	
	/**
	 * @return the shareVariantLevelData
	 */
	public boolean shareVariantLevelData() {
		return shareVariantLevelData;
	}

	/* To string method
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Variant [assembly=" + assembly + ", referenceName=" + referenceName + ", start=" + start + ", end="
				+ end + ", referenceBases=" + referenceBases + ", alternateBases=" + alternateBases + ", shareVariantLevelData=" + shareVariantLevelData + "]";
	}
	
	
	/**
	 * Returns a JSON representation and keeps out empty fields
	 * @return A JSON string
	 */
	public String getEmptyFieldsRemovedJson(){
		StringBuilder asJson=new StringBuilder();
		asJson.append("{");
		if (!getAssembly().equals("")){
			asJson.append("\"assembly\":");
			asJson.append("\"" + this.getAssembly() + "\"");
		}
		if (!getReferenceName().equals("")){
			asJson.append(",");
			asJson.append("\"referenceName\":");
			asJson.append("\"" + this.getReferenceName() + "\"");
		}
		if (getStart() != 0L && getStart() != -1){
			asJson.append(",");
			asJson.append("\"start\":");
			asJson.append(this.getStart());	
		}
		if (getEnd() != 0L && getEnd() != -1){
			asJson.append(",");
			asJson.append("\"end\":");
			asJson.append(this.getEnd());	
		}
		if (!getReferenceBases().equals("")){
			asJson.append(",");
			asJson.append("\"referenceBases\":");
			asJson.append("\"" + this.getReferenceBases() + "\"");
		}
		if (!getAlternateBases().equals("")){
			asJson.append(",");
			asJson.append("\"alternateBases\":");
			asJson.append("\"" + this.getAlternateBases() + "\"");
		}
		asJson.append("}");
		return asJson.toString();
	}

}

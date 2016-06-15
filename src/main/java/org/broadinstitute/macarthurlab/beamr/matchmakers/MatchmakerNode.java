/**
 * Represents a single matchmaker node. Nodes are setup through spring.xml file
 * via dependency injection
 */
package org.broadinstitute.macarthurlab.beamr.matchmakers;

/**
 * @author harindra
 *
 */
public class MatchmakerNode implements Node{
	private final String name;
	private final String token;
	private final String url;
	private final String acceptHeader;
	private final String contentTypeHeader;
	private final String contentLanguage;
	private final boolean selfSignedCertificate;

	/**
	 * Constructor
	 */
	public MatchmakerNode(String name, 
						  String token, 
						  String url,
						  String acceptHeader,
						  String contentTypeHeader, 
						  String contentLanguage,
						  boolean selfSignedCertificate){
		this.name=name;
		this.token=token;
		this.url=url;
		this.acceptHeader = acceptHeader;
		this.contentTypeHeader = contentTypeHeader;
		this.contentLanguage = contentLanguage;
		this.selfSignedCertificate = selfSignedCertificate;
	}
	

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getToken() {
		return this.token;
	}

	@Override
	public String getUrl() {
		return this.url;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MatchmakerNode [name=" + name + ", token=" + token + ", url=" + url + ", acceptHeader=" + acceptHeader
				+ ", contentHeader=" + contentTypeHeader + ", contentLanguage=" + contentLanguage + "]";
	}


	/**
	 * @return the acceptHeader
	 */
	public String getAcceptHeader() {
		return acceptHeader;
	}


	/**
	 * @return the contentHeader
	 */
	public String getContentTypeHeader() {
		return contentTypeHeader;
	}


	/**
	 * @return the contentLanguage
	 */
	public String getContentLanguage() {
		return contentLanguage;
	}


	/**
	 * @return the selfSignedCertificate
	 */
	public boolean isSelfSignedCertificate() {
		return selfSignedCertificate;
	}
	
	



	
	
	

}

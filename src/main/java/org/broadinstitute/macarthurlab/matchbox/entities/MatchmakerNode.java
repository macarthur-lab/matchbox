/**
 * Represents a single matchmaker node. Nodes are setup through spring.xml file
 * via dependency injection
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

/**
 * @author harindra
 *
 */
public class MatchmakerNode implements Node{
	private String name;
	private String token;
	private String url;
	private String acceptHeader;
	private String contentTypeHeader;
	private String contentLanguage;
	private boolean selfSignedCertificate;

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


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}


	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}


	/**
	 * @param acceptHeader the acceptHeader to set
	 */
	public void setAcceptHeader(String acceptHeader) {
		this.acceptHeader = acceptHeader;
	}


	/**
	 * @param contentTypeHeader the contentTypeHeader to set
	 */
	public void setContentTypeHeader(String contentTypeHeader) {
		this.contentTypeHeader = contentTypeHeader;
	}


	/**
	 * @param contentLanguage the contentLanguage to set
	 */
	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}


	/**
	 * @param selfSignedCertificate the selfSignedCertificate to set
	 */
	public void setSelfSignedCertificate(boolean selfSignedCertificate) {
		this.selfSignedCertificate = selfSignedCertificate;
	}
	
	



	
	
	

}

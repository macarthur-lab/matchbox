/**
 * Represents an authorized token and persisted in database
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

import org.springframework.data.annotation.Id;

/**
 * Represents an athorized token
 *
 */
public class AuthorizedToken {
	@Id
	private String id;
	/**
	 * Authorization token
	 */
	private final String token;
	/**
	 * The center that the token is for
	 */
	private final String centerName;
	/**
	 * The primary contact email of the center
	 */
	private final String primaryContactEmail;
	
	
	/**
	 * Default no-argument constructor creates empty object
	 */
	public AuthorizedToken() {
		this.id = "";
		this.token = "";
		this.centerName = "";
		this.primaryContactEmail = "";
	}
	
	
	/**
	 * @param id	id in database
	 * @param token	token
	 * @param centerName	name of the center the token is for
	 * @param primaryContactEmail	contact email address for this center
	 */
	public AuthorizedToken(String id, String token, String centerName, String primaryContactEmail) {
		this.id = id;
		this.token = token;
		this.centerName = centerName;
		this.primaryContactEmail = primaryContactEmail;
	}
	
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @return the centerName
	 */
	public String getCenterName() {
		return centerName;
	}
	/**
	 * @return the primaryContactEmail
	 */
	public String getPrimaryContactEmail() {
		return primaryContactEmail;
	}
	
	
	
}

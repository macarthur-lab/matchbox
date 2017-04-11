/**
 * Represents all authorized centers
 */
package org.broadinstitute.macarthurlab.matchbox.authentication;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;
import org.springframework.stereotype.Service;

/**
 * @author harindra
 *
 */
public class AccessAuthorizedNode {
	private List<AuthorizedToken> accessAuthorizedNodes;

	/**
	 * Sets up tokens
	 */
	public AccessAuthorizedNode(List<AuthorizedToken> authorisedNodes) {
		this.accessAuthorizedNodes = authorisedNodes;
	}
	
	/**
	 * Default constructor for spring
	 */
	public AccessAuthorizedNode() {}

	/**
	 * @return the accessAuthorizedNodes
	 */
	public List<AuthorizedToken> getAccessAuthorizedNodes() {
		return accessAuthorizedNodes;
	}

	/**
	 * @param accessAuthorizedNodes the accessAuthorizedNodes to set
	 */
	public void setAccessAuthorizedNodes(List<AuthorizedToken> accessAuthorizedNodes) {
		this.accessAuthorizedNodes = accessAuthorizedNodes;
	}	
	
	

}

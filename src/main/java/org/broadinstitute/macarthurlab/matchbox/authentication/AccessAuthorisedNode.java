/**
 * Represents all authorized centers
 */
package org.broadinstitute.macarthurlab.matchbox.authentication;

import java.util.List;

import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;

/**
 * @author harindra
 *
 */
public class AccessAuthorisedNode {
	private final List<AuthorizedToken> accessAuthorizedNodes;

	/**
	 * Sets up tokens
	 */
	public AccessAuthorisedNode(List<AuthorizedToken> authorisedNodes) {
		this.accessAuthorizedNodes = authorisedNodes;
	}

	/**
	 * @return the accessAuthorizedNodes
	 */
	public List<AuthorizedToken> getAccessAuthorizedNodes() {
		return accessAuthorizedNodes;
	}	

}

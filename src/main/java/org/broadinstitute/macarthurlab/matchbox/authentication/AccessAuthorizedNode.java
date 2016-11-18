/**
 * Represents all authorized centers
 */
package org.broadinstitute.macarthurlab.matchbox.authentication;

import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author harindra
 *
 */
@Component
public class AccessAuthorizedNode {

	private List<AuthorizedToken> accessAuthorizedNodes;

	/**
	 * Sets up tokens
	 */
	@Autowired
	public AccessAuthorizedNode(List<AuthorizedToken> authorisedNodes) {
		this.accessAuthorizedNodes = authorisedNodes;
	}

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

/**
 * Represents a matchmaker node
 */
package org.broadinstitute.macarthurlab.matchbox.entities;

/**
 * @author harindra
 *
 */
public interface Node {
	public String getName();
	public String getToken();
	public String getUrl();
	public String getContentTypeHeader();
	public String getContentLanguage();
	public String getAcceptHeader();
	public boolean isSelfSignedCertificate();
}

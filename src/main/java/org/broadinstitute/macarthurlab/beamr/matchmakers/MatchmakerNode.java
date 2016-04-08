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

	/**
	 * Constructor
	 */
	public MatchmakerNode(String name, String token, String url){
		this.name=name;
		this.token=token;
		this.url=url;
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

}

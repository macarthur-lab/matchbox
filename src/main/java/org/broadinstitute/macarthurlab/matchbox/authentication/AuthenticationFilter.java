/**
 * 	Authentication module
 * 
 * #TODO
 * 1. Add filter to allow ONLY privileged users access to /individual/*
 * branches
 */
package org.broadinstitute.macarthurlab.matchbox.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthenticationFilter implements Filter{
	private static final String X_AUTH_TOKEN_HEADER="X-Auth-Token";
	private static final String ACCEPT_HEADER="Accept";
	private final AccessAuthorizedNode accessAuthorizedNode;
	private final List<String> authorizedTokens;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Map<String,String> tokenToMMECenterMapping;
	

		
	/**
	 * Initializes class
	 */
	public AuthenticationFilter(){
    	String configFile = "file:" + System.getProperty("user.dir") + "/resources/config.xml";
    	ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
    	this.accessAuthorizedNode = context.getBean("accessAuthorizedNode", AccessAuthorizedNode.class);
	
    	Map<String,String> tokenToMMECenterMapping = new HashMap<String,String>();
    	List<String> authorizedTokes=new ArrayList<String>();
    	for(AuthorizedToken authorizeNode  : this.getAccessAuthorizedNode().getAccessAuthorizedNodes()){
    		authorizedTokes.add(authorizeNode.getToken());
    		tokenToMMECenterMapping.put(authorizeNode.getToken(),authorizeNode.getCenterName());
    	}
    	this.authorizedTokens = authorizedTokes;
    	this.tokenToMMECenterMapping = tokenToMMECenterMapping;
	}
	

	@Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
    	
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            
            //----extract and validate headers from request----
            
            //Unauthorized
            Map<String,String> validationResult=validateXAuthToken(request.getHeader(AuthenticationFilter.getxAuthTokenHeader()));
            if (validationResult.get("validated").equals("no")){
            	this.getLogger().warn("authentication failed for: "+req.getServerName());
            	response.sendError(401,"authentication failed");
            }
            //if authorized append the name of the center the match request came in from
            if (validationResult.get("validated").equals("yes")){
            	request.setAttribute("originMatchmakerNodeName",validationResult.get("originMatchmakerNodeName"));
            }
            
            
            //unsupported API version
            if (!validateAcceptHeader(request.getHeader(AuthenticationFilter.getAcceptHeader()))){
            	this.getLogger().warn("Accept header validation failed for: "+req.getServerName());
            	response.sendError(406,"unsupported API version, supported versions=[1.0]");
            }
            chain.doFilter(request, response);
    }

    
    /**
     * Validate this accept header
     * @param acceptHeader	An accept header from a request
     * @return	true if validated, false otherwise
     */
    private boolean validateAcceptHeader(String acceptHeader){
    	if (acceptHeader.equals("application/vnd.ga4gh.matchmaker.v1.0+json")){
    		return true;
    	}
    	return false;
    }
   
    
    
    /**
     * Validate this accept header
     * @param xAuthToken	A X-Auth-Token header from a request
     * @return	true if validated, false otherwise
     */
    private Map<String,String> validateXAuthToken(String xAuthToken){
    	Map<String, String> validationResult=new HashMap<String, String>();
    	if (this.getAuthorizedTokens().contains(xAuthToken)){
    		String mmeNodeName = this.getTokenToMMECenterMapping().get(xAuthToken); //how to pass this to controller??
    		validationResult.put("validated", "yes");
    		validationResult.put("originMatchmakerNodeName", mmeNodeName);
    	}
    	else{
    		validationResult.put("validated", "no");
    	}
    	return validationResult;
    }
    
    
    /**
	 * @return the xAuthTokenHeader
	 */
	public static String getxAuthTokenHeader() {
		return X_AUTH_TOKEN_HEADER;
	}
	
	/**
	 * @return the acceptHeader
	 */
	public static String getAcceptHeader() {
		return ACCEPT_HEADER;
	}

	@Override
	/**
	 * Part of implementation contract, but not needed 
	 */
    public void init(FilterConfig arg0) throws ServletException {
    }


	/**
	 * @return the accessAuthorizedNode
	 */
	public AccessAuthorizedNode getAccessAuthorizedNode() {
		return accessAuthorizedNode;
	}


	/**
	 * @return the authorizedTokens
	 */
	public List<String> getAuthorizedTokens() {
		return authorizedTokens;
	}


	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}


	/**
	 * @return the tokenToMMECenterMapping
	 */
	public Map<String, String> getTokenToMMECenterMapping() {
		return tokenToMMECenterMapping;
	}
	
	
	
	

}
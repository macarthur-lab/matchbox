/**
 * 	Authentication module
 * 
 * #TODO
 * 1. Add filter to allow ONLY privileged users access to /individual/*
 * branches
 */
package org.broadinstitute.macarthurlab.matchbox.authentication;

import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthenticationFilter implements Filter{
	private static final String X_AUTH_TOKEN_HEADER="X-Auth-Token";
	private static final String ACCEPT_HEADER="Accept";
	private final AccessAuthorizedNode accessAuthorizedNode;
	private final List<String> authorizedTokens;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Initializes class
	 */
	public AuthenticationFilter(AccessAuthorizedNode accessAuthorizedNode){
		this.accessAuthorizedNode = accessAuthorizedNode;
		//TODO: This is a very roundabout way of loading a list of token strings
    	List<String> authorizedTokens = new ArrayList<>();
    	for(AuthorizedToken authorizeNode  : accessAuthorizedNode.getAccessAuthorizedNodes()){
    		authorizedTokens.add(authorizeNode.getToken());
    	}
    	this.authorizedTokens = authorizedTokens;
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
            if (!validateXAuthToken(request.getHeader(AuthenticationFilter.getxAuthTokenHeader()))){
            	logger.warn("authentication failed for: "+req.getServerName());
            	response.sendError(401,"authentication failed");
            }
            //unsupported API version
            if (!validateAcceptHeader(request.getHeader(AuthenticationFilter.getAcceptHeader()))){
            	logger.warn("Accept header validation failed for: "+req.getServerName());
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
    private boolean validateXAuthToken(String xAuthToken){
    	if (authorizedTokens.contains(xAuthToken)){
    		return true;
    	}
    	return false;
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

}
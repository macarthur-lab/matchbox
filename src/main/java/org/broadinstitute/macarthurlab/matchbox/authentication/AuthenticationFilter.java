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
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration;
import org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken;
import org.broadinstitute.macarthurlab.matchbox.matchmakers.MatchmakerSearch;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter implements Filter{
	private static final String X_AUTH_TOKEN_HEADER="X-Auth-Token";
	private static final String ACCEPT_HEADER="Accept";
	private final AccessAuthorizedNode accessAuthorizedNode;
	private final List<String> authorizedTokens;


		
	/**
	 * Initializes class
	 */
	public AuthenticationFilter(){
    	String configFile = "file:" + System.getProperty("user.dir") + "/resources/config.xml";
    	ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
    	this.accessAuthorizedNode = context.getBean("accessAuthorizedNode", AccessAuthorizedNode.class);
	
    	List<String> authorizedTokes=new ArrayList<String>();
    	for(AuthorizedToken authorizeNode  : this.getAccessAuthorizedNode().getAccessAuthorizedNodes()){
    		authorizedTokes.add(authorizeNode.getToken());
    	}
    	this.authorizedTokens = authorizedTokes;
	}
	

	@Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
    	
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            
            //----extract and validate headers from request----
            
            //Unauthorized
            if (!validateXAuthToken(request.getHeader(AuthenticationFilter.getxAuthTokenHeader()))){
            	response.sendError(401,"authentication failed");
            }
            //unsupported API version
            if (!validateAcceptHeader(request.getHeader(AuthenticationFilter.getAcceptHeader()))){
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
    	if (this.getAuthorizedTokens().contains(xAuthToken)){
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
	
	
	
	

}
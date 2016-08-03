/**
 * Adjusting for certificate issues
 * - This was needed for GeneMatcher node certificate issues
 */
package org.broadinstitute.macarthurlab.matchbox.network;


import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

/**
 * A {@link X509TrustManager} and {@link HostnameVerifier} which trust everything.
 * 
 * @author    Torleif Berger
 * @license   http://creativecommons.org/licenses/by/3.0/
 * @see       http://www.geekality.net/?p=2408
 */
public final class CertificateAdjustment implements X509TrustManager, HostnameVerifier
{
    public X509Certificate[] getAcceptedIssuers() {return null;}
    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
    public boolean verify(String hostname, SSLSession session) {return true;}
    
    /**
     * Installs a new {@link TrustAllCertificates} as trust manager and hostname verifier. 
     */
    public static void install()
    {
        try
        {
        	CertificateAdjustment trustAll = new CertificateAdjustment();
            
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, 
                    new TrustManager[]{trustAll}, 
                    new java.security.SecureRandom());          
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(trustAll);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Failed setting up all thrusting certificate manager.", e);
        }
        catch (KeyManagementException e)
        {
            throw new RuntimeException("Failed setting up all thrusting certificate manager.", e);
        }
    }
    
    
    
    
    
    
    
    
    //
    private static final TrustingHostnameVerifier 
    TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();
 private static SSLSocketFactory factory;

 /** Call this with any HttpURLConnection, and it will 
  modify the trust settings if it is an HTTPS connection. */
 public static void relaxHostChecking(HttpsURLConnection conn) 
     throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

    if (conn instanceof HttpsURLConnection) {
       HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
       SSLSocketFactory factory = prepFactory(httpsConnection);
       httpsConnection.setSSLSocketFactory(factory);
       httpsConnection.setHostnameVerifier(TRUSTING_HOSTNAME_VERIFIER);
    }
 }

 static synchronized SSLSocketFactory 
          prepFactory(HttpsURLConnection httpsConnection) 
          throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    if (factory == null) {
       SSLContext ctx = SSLContext.getInstance("TLS");
       ctx.init(null, new TrustManager[]{ new AlwaysTrustManager() }, null);
       factory = ctx.getSocketFactory();
    }
    return factory;
 }
 
 private static final class TrustingHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
       return true;
    }
 }

 private static class AlwaysTrustManager implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
    public X509Certificate[] getAcceptedIssuers() { return null; }      
 }
    //
    
    
    
}
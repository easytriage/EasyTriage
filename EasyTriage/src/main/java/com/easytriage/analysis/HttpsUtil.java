package com.easytriage.analysis;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUtil {
	
	public static SSLSocketFactory getHttpsSocket() {
		SSLSocketFactory ssf = null;
		try {
			TrustManager[] tm = {new TrustManagerAndVerifier ()}; 
			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(null, tm, new java.security.SecureRandom()); 
			ssf = sslContext.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ssf;
	}
	
    private static class TrustManagerAndVerifier implements  X509TrustManager{
		
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[]{};
		}
		
		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
				String authType) throws java.security.cert.CertificateException {
		}
		
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
				String authType) throws java.security.cert.CertificateException {
		}

	};

}

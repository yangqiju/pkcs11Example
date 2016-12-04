package com.joyveb.pkcs11example.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

public class HttpClientExample {

	private static final String[] TLS_Protocols = { "TLSv1.2" };
	private static long keepAliveTime = 60 * 1000;
	
	
	public static void main(String[] args) throws KeyStoreException,
			NoSuchProviderException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		HttpClientBuilder builder = HttpClientBuilder.create();
		String pkcs11config = "library = /usr/local/lib/libeTPkcs11.dylib\n"
				+ "name = safenetSC\n "
				+ "slot = 0";
		Provider provider = new sun.security.pkcs11.SunPKCS11(
				new ByteArrayInputStream(pkcs11config.getBytes()));
		Security.addProvider(provider);//添加支持
		
		KeyStore pkcss11KS = KeyStore.getInstance("PKCS11","SunPKCS11-safenetSC");
		pkcss11KS.load(null, "".toCharArray());//TODO ukey pin
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(pkcss11KS, null);
		
		SSLContext defaultSSLContexts = SSLContexts.createDefault();
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{new AllTrustManager()};
		defaultSSLContexts.init(kmf.getKeyManagers(), trustAllCerts, null);
		HostnameVerifier verifier = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		

		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
				defaultSSLContexts, TLS_Protocols, null, verifier);
		builder.setSSLSocketFactory(sslConnectionFactory);

		builder.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(org.apache.http.HttpResponse arg0,
					HttpContext arg1) {
				return keepAliveTime;
			}
		});
		CloseableHttpClient  httpclient = builder.build();
		
		String url = "https://127.0.0.1:6699/";
		HttpGet httpGet = new HttpGet(url);
		
		//TODO 需要设置 userToken属性，不然每次请求时都需要重新建立连接
		// userToken 是证书中的名称
		//如果使用nginx keepalive，nginx默认 100次请求会断开连接，需要设置keepalive_requests 参数。
		HttpContext context = new HttpCoreContext();
		context.setAttribute(HttpClientContext.USER_TOKEN, "CN=yangqiju.joyveb.com");
		
		try (CloseableHttpResponse response = httpclient.execute(httpGet,context)) {
			String responseMsg = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println("response:"+responseMsg);
		}
		
		httpclient.close();
	}
	
}

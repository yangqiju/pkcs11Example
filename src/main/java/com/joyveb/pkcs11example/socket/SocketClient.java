package com.joyveb.pkcs11example.socket;

import java.io.ByteArrayInputStream;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import com.joyveb.pkcs11example.httpclient.AllTrustManager;

/**
 * socket pkcs11 的client demo
 * server 是一样的配置，没有展示
 * @author yangqiju
 *
 */
public class SocketClient {

	public static void main(String[] args) throws Exception {
		System.setProperty("javax.net.ssl.keyStoreType", "pkcs11");//使用pkcs11接口
		System.setProperty("javax.net.ssl.keyStore", "NONE"); //必须使用NONE
		System.setProperty("javax.net.ssl.keyStorePassword", "");//TODO Ukey pin
		System.setProperty("javax.net.ssl.keyStoreProvider","SunPKCS11-safenetSC");
		System.setProperty("javax.net.debug", "ssl");//debug
		
		String pkcs11config = "library = /usr/local/lib/libeTPkcs11.dylib\n"
				+ "name = safenetSC\n "
				+ "slot = 0";
		Provider p = new sun.security.pkcs11.SunPKCS11(
				new ByteArrayInputStream(pkcs11config.getBytes()));
		
		Security.addProvider(p);//向虚拟机添加算法提供者
		
		SSLContext ctx = SSLContext.getInstance("TLSv1.2");
		TrustManager[] trustManagers = new TrustManager[] { new AllTrustManager() };
		ctx.init(null, trustManagers, null);
		
		SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket("localhost", 10036);
		System.out.println(socket.getClass());
		try {
			socket.startHandshake();//执行握手操作
			System.out.println("success..");
		} catch (Exception e) {
			System.out.println("error...");
			e.printStackTrace();
		}
	
	}
}

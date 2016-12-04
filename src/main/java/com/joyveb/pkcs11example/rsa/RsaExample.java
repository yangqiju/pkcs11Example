package com.joyveb.pkcs11example.rsa;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

import javax.crypto.Cipher;

public class RsaExample {
	
	private static final String ukeyPassword = "";//TODO ukey pin码
	private static final String privateKeyId = "";//TODO 私钥的id号
	
	public static void main(String[] args) throws Exception {
		String pkcs11config = "library = /usr/local/lib/libeTPkcs11.dylib\n"
				+ "name = safenetSC\n "
				+ "slot = 0";
		Provider testProvider = new sun.security.pkcs11.SunPKCS11(
				new ByteArrayInputStream(pkcs11config.getBytes()));
		Security.addProvider(testProvider);//添加支持
		KeyStore testKeystore = KeyStore.getInstance("PKCS11","SunPKCS11-safenetSC");
		
		testKeystore.load(null, ukeyPassword.toCharArray());
		KeyFactory keyFactory = KeyFactory.getInstance("RSA",
				testKeystore.getProvider());
		String id = privateKeyId;//id
		PrivateKey privateKey = (PrivateKey) testKeystore.getKey(id, null);
		PublicKey publicKey = testKeystore.getCertificate(id).getPublicKey();
		
		//加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		byte[] doFinal = cipher.doFinal("test".getBytes());
		
		//解密
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		byte[] plainBytes = cipher.doFinal(doFinal);
		
		//签名
		Signature signatureEngine = Signature.getInstance("SHA1WithRSA",
				testKeystore.getProvider());
		signatureEngine.initSign(privateKey);
		signatureEngine.update("sign data".getBytes());
		byte[] signature = signatureEngine.sign();
		
		//验证
		Signature signatureEngine2 = Signature.getInstance("SHA1WithRSA");
		signatureEngine2.initVerify(publicKey);
		signatureEngine2.update("sign data".getBytes());
		boolean verify = signatureEngine2.verify(signature);
		
	}
}

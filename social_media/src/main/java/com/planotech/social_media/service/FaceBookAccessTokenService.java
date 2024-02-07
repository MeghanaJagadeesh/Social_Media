package com.planotech.social_media.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.types.Page;

@Service
public class FaceBookAccessTokenService {

	@Value("${spring.social.facebook.appId}")
	private String appId;

	@Value("${spring.social.facebook.appSecret}")
	private String appSecret;

	@Value("${spring.social.redirect_uri}")
	private String redirect_uri;

	@Value("${spring.socia.facebookPageId}")
	private String facebookPageId;

	private String userAccessToken = null;

	public String generateUserAccessToken(String code) {
		FacebookClient client = new DefaultFacebookClient(Version.LATEST);
		FacebookClient.AccessToken accessToken = client.obtainUserAccessToken(appId, appSecret, redirect_uri, code);
		userAccessToken = accessToken.getAccessToken();
		String pageAccessToken = getPageAccessToken(userAccessToken, facebookPageId);
		String appSecretProof = generateAppSecretProof(appSecret, userAccessToken);
		String pageAppSecretProof = generateAppSecretProof(appSecret, pageAccessToken);

		System.out.println("UseraccessToken  :  " + accessToken.getAccessToken());
		System.out.println("userAppsecretproof   " + appSecretProof);	
		System.out.println("PageAppsecretproof   " + pageAppSecretProof);
		return userAccessToken;
		
	}

	public String getPageAccessToken(String userAccessToken, String pageId) {
		FacebookClient client = new DefaultFacebookClient(userAccessToken, Version.LATEST);
		Connection<Page> pages = client.fetchConnection("me/accounts", Page.class);

		for (List<Page> pageList : pages) {
			for (Page page : pageList) {
				if (page.getId().equals(pageId)) {
					System.out.println("Page Access :   " + page.getAccessToken());
					return page.getAccessToken();
				}
			}
		}
		return null;
	}

	public String generateAppSecretProof(String facebookAppSecret, String accessToken) {
		Mac sha256Hmac;
		try {
			sha256Hmac = Mac.getInstance("HmacSHA256");

			SecretKeySpec secretKey = new SecretKeySpec(facebookAppSecret.getBytes(), "HmacSHA256");
			sha256Hmac.init(secretKey);

			byte[] hmacBytes = sha256Hmac.doFinal(accessToken.getBytes());
			StringBuilder hexStringBuilder = new StringBuilder();
			for (byte b : hmacBytes) {
				hexStringBuilder.append(String.format("%02x", b));
			}
			return hexStringBuilder.toString();
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}
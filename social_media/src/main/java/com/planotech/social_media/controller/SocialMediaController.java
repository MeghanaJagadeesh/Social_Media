package com.planotech.social_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.planotech.social_media.service.FaceBookAccessTokenService;
import com.planotech.social_media.service.FaceBookService;
import com.planotech.social_media.service.SocialMediaService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SocialMediaController {

	@Autowired
	SocialMediaService service;

	@GetMapping("/")
	public String loadLogin() {
		return "Login";
	}
	
	@Value("${spring.social.facebook.appId}")
	private String appId;

	@Value("${spring.social.facebook.appSecret}")
	private String appSecret;

	@Value("${spring.social.redirect_uri}")
	private String redirect_uri;

	@Value("${spring.socia.facebookPageId}")
	private String facebookPageId;
	
	@Autowired
	FaceBookAccessTokenService faceBookAccessTokenService;
	
	@Autowired
	FaceBookService faceBookService;

//	@PostMapping("/social/login")
//	public String login(@RequestParam String username, @RequestParam String password, ModelMap map,
//			HttpSession session) {
//		if (username.equals(adminUsername) && password.equals(adminPassword)) {
//			session.setAttribute("admin", "admin");
//			map.put("pass", "Login Success");
//			return "index";
//		} else {
//			map.put("fail", "Invalid Username or Password");
//			return "Login";
//		}
//	}
	
	@PostMapping("/post-message")
	@ResponseBody
	public String postMessage(@RequestParam String code,@RequestParam String message) {
		String userAccessToken = faceBookAccessTokenService.generateUserAccessToken(code);
		String pageAccessToken = faceBookAccessTokenService.getPageAccessToken(userAccessToken, facebookPageId);
		if (faceBookService.postToPage(facebookPageId, pageAccessToken, message)) {
			return "Sucess";
		} else {
			return "Reject";
		}
	}
	
	@GetMapping("/app-secretProof")
	@ResponseBody
	public String getAppSecretProof(@RequestParam String accessToken) {
		return faceBookAccessTokenService.generateAppSecretProof(appSecret,accessToken);
	}
	
	@PostMapping("/post-ImageOrVideo")
	@ResponseBody
	public String postPage(@RequestParam String code,@RequestParam String message, MultipartFile file) {
		System.out.println("Controller ********************************************"+message);
		String userAccessToken = faceBookAccessTokenService.generateUserAccessToken(code);
		String pageAccessToken = faceBookAccessTokenService.getPageAccessToken(userAccessToken, facebookPageId);
		if (faceBookService.postMediaToPage(facebookPageId, pageAccessToken, message,file)) {
			return "Sucess";
		} else {
			return "Reject";
		}
	}

}

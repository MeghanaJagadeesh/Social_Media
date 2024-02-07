package com.planotech.social_media.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.planotech.social_media.dao.SocialMediaDao;
import com.planotech.social_media.dto.SocialMediaPost;

@Service
public class SocialMediaService {

	@Autowired
	SocialMediaPost mediaPost;

	@Autowired
	SocialMediaDao dao;

	@Autowired
	FaceBookService faceBookService;

	private static int imageid;

//	public String postOnSocialMedia(SocialMediaPost post, MultipartFile imageFile, ModelMap map, HttpSession session)
//			throws IOException {
//
//		if (session.getAttribute("admin") != null) {
//			byte[] pictureBytes = imageFile.getBytes();
//			String imageName = imageFile.getOriginalFilename();
//			String imageType = imageFile.getContentType();
//			if (faceBookService.post(post, pictureBytes, imageName, imageType, session)) {
//
//				String folderName = "src/main/resources/static/Posts";
//				File postFolder = new File(folderName);
//				if (!postFolder.exists()) {
//					postFolder.mkdirs();
//				}
//				BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
//
//				imageid++; // for file name
//				File saveImage = new File(postFolder, "mediaImg" + imageid + "post.png");
//
//				ImageIO.write(bufferedImage, "png", saveImage);
//				post.setImagePath(saveImage.getAbsolutePath());
//				post.setDateTime(LocalDateTime.now());
//				dao.save(post);
//				map.put("pass", "Post Uploded and saved in database Successfully");
//				return "index.html";
//			} else {
//				map.put("pass", "Failed to post");
//				return "index.html";
//			}
//		} else {
//			map.put("fail", "Session Expiry, Please Login");
//			return "Login";
//		}
//	}

}

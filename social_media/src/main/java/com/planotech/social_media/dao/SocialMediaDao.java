package com.planotech.social_media.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.planotech.social_media.dto.SocialMediaPost;
import com.planotech.social_media.repository.SocialMediaRepository;

@Repository
public class SocialMediaDao {

	@Autowired
	SocialMediaRepository repository;

	public void save(SocialMediaPost post) {
		repository.save(post);
	}

}

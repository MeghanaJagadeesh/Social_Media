package com.planotech.social_media.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planotech.social_media.dto.SocialMediaPost;

public interface SocialMediaRepository extends JpaRepository<SocialMediaPost, Integer> {

}

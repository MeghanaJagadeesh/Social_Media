package com.planotech.social_media.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;

@Service
public class FaceBookService {

	@Value("${spring.socia.facebookPageId}")
	private String facebookPageId;

	public boolean postToPage(String pageId, String pageAccessToken, String message) {
		FacebookClient client = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
		try {
			FacebookType response = client.publish(pageId + "/feed", FacebookType.class,
					Parameter.with("message", message));
			System.out.println("Post ID: " + response.getId());
			return true;
		} catch (FacebookException e) {
			System.out.println("Error posting to page: " + e.getMessage());
			return false;
		}
	}

	public boolean postMediaToPage(String facebookPageId, String pageAccessToken, String message,
			MultipartFile mediaFile) {
		System.out.println("mediaFile Content Type: " + mediaFile.getContentType());
		System.out.println("mediaFile Size: " + mediaFile.getSize());
		FacebookClient client = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
		try {
			FacebookType response;
			if (determineFileType(mediaFile)) {
				System.out.println("video **********************************");
//				File vidFile = convertMultipartFileToFile(mediaFile);
				byte[] videoByte = mediaFile.getBytes();
				int videosize = videoByte.length;
				String uploadSessionId = createVideoUploadSession(client, facebookPageId, videosize, pageAccessToken);
				System.out.println("uploadSessionId : " + uploadSessionId);
				long chunkSize = 5 * 1024 * 1024; // 5MB chunk size
				long startOffset = 0;

				while (startOffset < videosize) {
					byte[] videoChunk = getChunk(videoByte, startOffset, chunkSize);
					String end_offset = uploadVideoChunk(client, facebookPageId, uploadSessionId, startOffset,
							videoChunk, pageAccessToken, message);
					startOffset += Long.parseLong(end_offset);
				}
				if (finishVideoUploadSession(client, uploadSessionId, pageAccessToken, message))
					return true;
				else
					return false;

			} else {
				response = client.publish(facebookPageId + "/photos", FacebookType.class,
						BinaryAttachment.with("source", mediaFile.getBytes()), Parameter.with("message", message));
				System.out.println("Post ID: " + response.getId());
				return true;
			}

		} catch (FacebookException e) {
			System.out.println("Error posting media to page: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			System.out.println("Unsupported file type: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean finishVideoUploadSession(FacebookClient client, String uploadSessionId, String pageAccessToken,
			String message) {
		FacebookType response = client.publish(uploadSessionId, FacebookType.class,
				Parameter.with("access_token", pageAccessToken), Parameter.with("upload_phase", "finish"),
				Parameter.with("upload_session_id", uploadSessionId), Parameter.with("description", message));
		System.out.println("response " + response);
		System.out.println("Post ID : " + response.getId());
		return response.getId() != null;
	}

	private byte[] getChunk(byte[] videoData, long startOffset, long chunkSize) {
		System.out.println("Chunk **************************************");
		int remainingBytes = (int) Math.min(chunkSize, videoData.length - startOffset);
		byte[] chunk = new byte[remainingBytes];
		System.arraycopy(videoData, (int) startOffset, chunk, 0, remainingBytes);
		return chunk;
	}

	// post video
	public String createVideoUploadSession(FacebookClient client, String pageId, long fileSize,
			String pageAccessToken) {
		System.out.println("create vid session **********************8");
		JsonObject response = client.publish(pageId + "/videos", JsonObject.class,
				Parameter.with("upload_phase", "start"), Parameter.with("file_size", fileSize));
		System.out.println("upload session id  " + response.get("upload_session_id"));
		return response.get("upload_session_id").toString();
	}

	public String uploadVideoChunk(FacebookClient client, String facebookPageId, String uploadSessionId,
			long startOffset, byte[] vidFile, String pageAccessToken, String message) {
		System.out.println("uploadVideoChunk method ********************");
//		JsonObject response = client.publish(uploadSessionId, JsonObject.class, BinaryAttachment.with("video_file_chunk", vidFile),
//				Parameter.with("start_offset", startOffset), Parameter.with("file_size", vidFile.length),
//				Parameter.with("upload_phase", "transfer"));
//		
		BatchRequest batchRequest = new BatchRequest.BatchRequestBuilder("/videos").parameters(
				Parameter.with("access_token", pageAccessToken), Parameter.with("start_offset", startOffset),
				Parameter.with("upload_session_id", uploadSessionId), Parameter.with("video_file_chunk", vidFile))
				.build();
		System.out.println("uploadVideoChunk *******************************");
		List<BatchResponse> batchResponses = client.executeBatch(batchRequest);

		for (BatchResponse response : batchResponses) {
			System.out.println("Inside For loop ***********************");
			System.out.println("batchResponses   " + response);
			try {
				String endOffset = response.getBody().valueOf("end_offset");
				if (endOffset != null && !endOffset.isEmpty()) {
					// Handle the endOffset as needed
					System.out.println("End Offset: " + endOffset);
					startOffset += Long.parseLong(endOffset);
				}
			} catch (NumberFormatException e) {
				// Handle the case where "end_offset" cannot be parsed as a Long
				System.out.println("Error parsing end_offset: " + e.getMessage());
			}
		}
		return batchResponses.get(batchResponses.size() - 1).getBody().valueOf("end_offset");
	}

//	public boolean postMediaToPage(String facebookPageId, String pageAccessToken, String message,
//			MultipartFile mediaFile) {
//		System.out.println("mediaFile Content Type: " + mediaFile.getContentType());
//		System.out.println("mediaFile Size: " + mediaFile.getSize());
//		FacebookClient client = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
//		try {
//			FacebookType response;
//			if (determineFileType(mediaFile)) {
//				long fileSize = mediaFile.getSize();
//				System.out.println("fileSize : "+fileSize);
//				response = client.publish(facebookPageId + "/videos", FacebookType.class,
//						BinaryAttachment.with("source", mediaFile.getBytes()),
//						Parameter.with("title", mediaFile.getName()), Parameter.with("description", message),
//						Parameter.with("upload_phase", "start"), Parameter.with("file_size", fileSize));
//				System.out.println("Post ID: " + response.getId());
//				return true;
//			} else {
//				response = client.publish(facebookPageId + "/photos", FacebookType.class,
//						BinaryAttachment.with("source", mediaFile.getBytes()), Parameter.with("message", message));
//				System.out.println("Post ID: " + response.getId());
//				return true;
//			}
//
//		} catch (FacebookException e) {
//			System.out.println("Error posting media to page: " + e.getMessage());
//			e.printStackTrace();
//			return false;
//		} catch (IllegalArgumentException e) {
//			System.out.println("Unsupported file type: " + e.getMessage());
//			e.printStackTrace();
//			return false;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return false;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	public boolean determineFileType(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		System.out.println("FileType ********************************************");
		System.out.println("FileType: " + fileExtension);
		if (fileExtension.equals("mp4") || fileExtension.equals("mov") || fileExtension.equals("mp3")) {
			return true;
		} else if (fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png")) {
			return false;
		} else {
			throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
		}
	}

}
//
//public File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
//	File file = new File(multipartFile.getOriginalFilename());
//	FileOutputStream fileOutputStream = new FileOutputStream(file);
//	fileOutputStream.write(multipartFile.getBytes());
//	fileOutputStream.close();
//	return file;
//}

//// save file
//String folderName = "src/main/resources/static/files";
//File file = new File(folderName);
//if (!file.exists()) {
//	file.mkdirs();
//}
//imgId++;
//String fileName = mediaFile.getOriginalFilename();
//String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
//
////String fileName1 = fileName + "."+fileExtension;
//File saveFile = new File(file, fileName);
//ImageIO.write(ImageIO.read(inputStream), fileExtension, saveFile);
//String filePath = saveFile.getPath();
//
//System.out.println("filePath: " + filePath);
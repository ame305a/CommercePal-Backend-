package com.commerce.pal.backend.service.amazon;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.module.database.ImageService;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class UploadService {
    @Autowired
    private AmazonS3 amazonS3;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.images.url}")
    private String baseUrl;

    private final ImageService imageService;

    @Autowired
    public UploadService(ImageService imageService) {
        this.imageService = imageService;
    }

    public JSONObject uploadFile(MultipartFile multipartFile, String platform, String id, String type) {
        JSONObject responseMap = new JSONObject();

        try {

            final File file = convertMultiPartFileToFile(multipartFile);
            String fileName = type + "_" + String.valueOf(System.currentTimeMillis()) + "_" + getRandomNumber() + "." + getFileExtension(file.getName());
            String bucketLocation = bucketName + "/" + platform + "/Images";
            String imageUrl = baseUrl + "/" + platform + "/" + "Images" + "/" + fileName;
            uploadFileToS3Bucket(bucketLocation, file, fileName);

            // Update the Tables
            JSONObject uploadJson = new JSONObject();
            uploadJson.put("Id", id);
            uploadJson.put("Type", type);
            uploadJson.put("Platform", platform);
            uploadJson.put("ImageUrl", imageUrl);

            JSONObject upRes = imageService.updateImage(uploadJson);

            if (upRes.getString("Status").equals("00")) {
                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "success")
                        .put("imageUrl", imageUrl)
                        .put("statusMessage", "Request Successful");
            } else {
                responseMap.put("statusCode", ResponseCodes.REQUEST_FAILED)
                        .put("statusDescription", upRes.getString("Narration"))
                        .put("statusMessage", upRes.getString("Narration"));
            }
            log.log(Level.INFO, "File upload is completed.");
            file.delete();    // To remove the file locally created in the project folder.
        } catch (final AmazonServiceException ex) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "Error =" + ex.getMessage())
                    .put("statusMessage", "Error=" + ex.getMessage());
            log.log(Level.WARNING, "Error= {} while uploading file.", ex.getMessage());
        }
        return responseMap;
    }

    public String uploadFileAlone(MultipartFile multipartFile, String platform, String type) {
        String responseMap = new String();
        try {

            final File file = convertMultiPartFileToFile(multipartFile);
            String fileName = type + "_" + String.valueOf(System.currentTimeMillis()) + "_" + getRandomNumber() + "." + getFileExtension(file.getName());
            String bucketLocation = bucketName + "/" + platform + "/" + type;
            String imageUrl = baseUrl + "/" + platform + "/" + type + "/" + fileName;
            uploadFileToS3Bucket(bucketLocation, file, fileName);
            responseMap = imageUrl;
            log.log(Level.INFO, "File upload is completed.");
            file.delete();    // To remove the file locally created in the project folder.
        } catch (final AmazonServiceException ex) {
            log.log(Level.WARNING, "Error= {} while uploading file.", ex.getMessage());
        }
        return responseMap;
    }


    private File convertMultiPartFileToFile(MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (final IOException ex) {
            log.log(Level.SEVERE, "Error converting the multi-part file to file= ", ex.getMessage());
        }
        return file;
    }

    private void uploadFileToS3Bucket(String bucketName, final File file, String uniqueFileName) {
        log.log(Level.INFO, "Uploading file with name= " + uniqueFileName);
        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, uniqueFileName, file);
        amazonS3.putObject(putObjectRequest);
    }

    public String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public String getRandomNumber() {
        Integer min = 101;
        Integer max = 999;
        Integer b = (int) (Math.random() * (max - min + 1) + min);
        return b.toString();
    }
}

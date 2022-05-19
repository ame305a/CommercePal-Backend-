package com.commerce.pal.backend.controller;


import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.service.amazon.AWSS3Service;
import com.commerce.pal.backend.service.amazon.UploadService;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;


@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping(value = "/prime/api/v1/upload")
public class AWSS3Ctrl {

    @Autowired
    private AWSS3Service service;

    @Autowired
    private UploadService uploadService;

    @PostMapping(value = "/images")
    public ResponseEntity<String> uploadFile(@RequestPart(value = "file") final MultipartFile multipartFile,
                                             @RequestPart(value = "platform") String platform,
                                             @RequestPart(value = "type") String type) {
        service.uploadFile(multipartFile);
        final String response = "[" + multipartFile.getOriginalFilename() + "] uploaded successfully.";
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/image", method = RequestMethod.POST)
    public ResponseEntity<String> uploadImage(@RequestPart(value = "file") MultipartFile file,
                                              @RequestPart(value = "platform") String platform,
                                              @RequestPart(value = "type") String type,
                                              @RequestPart(value = "id") String id
    ) {
        log.log(Level.INFO, "File Name :" + file.getName());
        JSONObject response = uploadService.uploadFile(file, platform, id, type);
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @PostMapping("/multi-upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                              @RequestPart(value = "platform") String platform,
                                              @RequestPart(value = "type") String type,
                                              @RequestPart(value = "id") String id) {
        AtomicReference<JSONObject> response = new AtomicReference<>(new JSONObject());
        try {
            List<String> fileNames = new ArrayList<>();

            Arrays.asList(files).stream().forEach(file -> {
                response.set(uploadService.uploadFile(file, platform, id, type));
            });

            return new ResponseEntity<>(response.get().toString(), HttpStatus.OK);
        } catch (Exception e) {
            log.log(Level.WARNING, "MULTIPLE IMAGE UPLOAD ERROR : " + e.getMessage());
            response.get().put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response.get().toString());
        }
    }

    @RequestMapping(value = "/upload-pick-up", method = RequestMethod.POST)
    public ResponseEntity<String> uploadPickUp(@RequestPart(value = "file") MultipartFile file,
                                              @RequestPart(value = "id") String id,
                                              @RequestPart(value = "orderItemId") String orderItemId
    ) {
        log.log(Level.INFO, "File Name :" + file.getName());
        JSONObject response = uploadService.uploadPickUpPhoto(file, id, orderItemId);
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
}

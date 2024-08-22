package com.commerce.pal.backend.controller.user.auth;

import com.commerce.pal.backend.dto.auth.OAuth2FollowUpReqDto;
import com.commerce.pal.backend.dto.auth.OAuth2ReqDto;
import com.commerce.pal.backend.module.socialLogin.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/oauth2"})
@RequiredArgsConstructor
public class SocialLoginController {

    private final OAuth2Service OAuth2Service;

    @PostMapping
    public ResponseEntity<String> processAuthLogin(@RequestBody @Valid OAuth2ReqDto OAuth2ReqDto) {
        JSONObject response = OAuth2Service.processAuthLogin(OAuth2ReqDto);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping(value = "/follow-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAuthFollowUpReq(@RequestBody @Valid OAuth2FollowUpReqDto followUpReqDto) {
        JSONObject response = OAuth2Service.processAuthFollowUpReq(followUpReqDto);
        return ResponseEntity.ok(response.toString());
    }

}


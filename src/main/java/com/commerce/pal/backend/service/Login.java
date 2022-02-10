/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.commerce.pal.backend.service;


import com.commerce.pal.backend.common.Encryption;
import com.commerce.pal.backend.common.JwtTokenUtil;
import com.commerce.pal.backend.common.ResponseCodes;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;

/**
 * @author Settim
 */
@Log
@Component
public class Login {

    public JSONObject pickAndProcess(JSONObject request) {

        JSONObject response = new JSONObject();
        try {


            request.put("validation", "pass");

            String toHash = request.getString("password").trim() + request.getString("email").trim() + "1000";
            String passwordHash = Encryption.hashSHA512(toHash);
            request.put("passwordHash", passwordHash);

            /*
            if (db.loginUser(request)) {
                JwtTokenUtil util = new JwtTokenUtil();
                String userToken = util.generateToken(request.getString("email"));
                response.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "success")
                        .put("userToken", userToken)
                        .put("statusMessage", "login successful");
            } else {
                response.put("statusCode", ResponseCodes.SYSTEM_LOGIN_NOT_SUCCESSFUL)
                        .put("statusDescription", "login failed invalid details")
                        .put("statusMessage", "login failed invalid details");
            }

             */

        } catch (NoSuchAlgorithmException e) {
            response.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return response;
    }
}

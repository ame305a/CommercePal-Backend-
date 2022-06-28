package com.commerce.pal.backend.integ.notification.push;

import com.commerce.pal.backend.utils.HttpProcessor;
import lombok.extern.java.Log;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Level;

@Log
@Service
public class OneSignal {
    @Value("${commerce.pal.notification.one.signal.endpoint}")
    private String PUSH_END_POINT;

    private final HttpProcessor httpProcessor;

    @Autowired
    public OneSignal(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
    }

    public void pickAndProcess(String userId, String header, String message, JSONObject data) {
        try {
            JSONObject pushBdy = new JSONObject();
            pushBdy.put("UserId", userId);
            pushBdy.put("Heading", header);
            pushBdy.put("Message", message);
            pushBdy.put("data", data);

            RequestBuilder builder = new RequestBuilder("POST");
            builder.addHeader("Content-Type", "application/json; charset=UTF-8")
                    .setBody(pushBdy.toString())
                    .setUrl(PUSH_END_POINT)
                    .build();
            JSONObject resp = httpProcessor.jsonRequestProcessor(builder);
            log.log(Level.INFO, "CommercePal Res : " + resp.toString());
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }
    }
}

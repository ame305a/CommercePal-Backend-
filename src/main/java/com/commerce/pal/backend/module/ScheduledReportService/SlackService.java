package com.commerce.pal.backend.module.ScheduledReportService;

import com.commerce.pal.backend.utils.HttpProcessor;
import lombok.extern.java.Log;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log
public class SlackService {

    @Value("${commerce.pal.slack.report}")
    private String SLACK_WEBHOOK_URL;

    private final HttpProcessor httpProcessor;

    public SlackService(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
    }

    public void sendProductReport(String reportType, String dateString, JSONObject payload) {
        String reportMessage = generateProductReportMessage(reportType, dateString, payload);

        RequestBuilder builder = new RequestBuilder("POST");
        builder.addHeader("Content-Type", "application/json; charset=UTF-8")
                .setBody(reportMessage)
                .setUrl(SLACK_WEBHOOK_URL)
                .build();
        httpProcessor.jsonRequestProcessor(builder);
    }

    // Construct the Slack message for the product report
    private String generateProductReportMessage(String reportType, String dateString, JSONObject payload) {
        return String.format(
                "{\n" +
                        "  \"blocks\": [\n" +
                        "    {\n" +
                        "      \"type\": \"divider\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"type\": \"section\",\n" +
                        "      \"text\": {\n" +
                        "        \"type\": \"mrkdwn\",\n" +
                        "        \"text\": \"*%s Product Report: %s*\"\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"type\": \"section\",\n" +
                        "      \"text\": {\n" +
                        "        \"type\": \"mrkdwn\",\n" +
                        "        \"text\": \"*Summary:*\\n• Products created by warehouse: %d\\n• Products created by merchants: %d\\n• Total approved products: %d\\n• Current Total Pending Products: %d\"\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"type\": \"divider\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}",
                reportType, dateString, payload.getInt("warehouseProductCount"), payload.getInt("merchantProductCount"), payload.getInt("approvedProductCount"), payload.getInt("pendingProductCount")
        );
    }

}

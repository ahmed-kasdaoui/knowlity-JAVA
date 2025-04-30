package com.esprit.knowlity.Utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FacebookPoster {
    // TODO: Replace with your actual Page Access Token and Page ID
    private static final String PAGE_ACCESS_TOKEN = "EAAq1jUXunxQBOxr3qXCxWKLKVarrhK90Je7GnHrKGY4QF2jghFYTgAzJZAuYFDRISY2rkMJduXxVWNeUZCNnWBw88VScGzySVY4GrlrJODmACZCFfMimoxNS7uHZBLPtZApUJhUMckALChavZBe8NWT8HizJU9yXjPmelMf3mFjsoanmQHTrgAzzGFWXCHKnuZB";
    private static final String PAGE_ID = "535397399664579";

    /**
     * Posts a message to the configured Facebook Page using the Graph API.
     * @param message The message to post.
     * @return true if successful, false otherwise.
     */
    public static boolean postToPage(String message) {
        try {
            String urlStr = String.format(
                "https://graph.facebook.com/%s/feed?access_token=%s",
                PAGE_ID, PAGE_ACCESS_TOKEN
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String postData = "message=" + java.net.URLEncoder.encode(message, "UTF-8");
            byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postDataBytes);
            }
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("Error posting to Facebook: " + e.getMessage());
        }
        return false;
    }
}

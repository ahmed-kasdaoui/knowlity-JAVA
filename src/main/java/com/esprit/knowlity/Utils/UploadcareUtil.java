package com.esprit.knowlity.Utils;

import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

public class UploadcareUtil {
    private static final OkHttpClient client = new OkHttpClient();
    // TODO: Move these to config or env
    private static final String UPLOADCARE_PUBLIC_KEY = "8bd1b406d937e33dd670";
    private static final String UPLOADCARE_UPLOAD_URL = "https://upload.uploadcare.com/base/";

    public static String uploadToUploadcare(File file, String certId) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("UPLOADCARE_PUB_KEY", UPLOADCARE_PUBLIC_KEY)
                .addFormDataPart("UPLOADCARE_STORE", "auto")
                .addFormDataPart("file", certId + ".pdf",
                        RequestBody.create(file, MediaType.parse("application/pdf")))
                .build();

        Request request = new Request.Builder()
                .url(UPLOADCARE_UPLOAD_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed: " + response.message());
            }
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            String fileId = json.getString("file");
            return "https://ucarecdn.com/" + fileId + "/";
        }
    }
}

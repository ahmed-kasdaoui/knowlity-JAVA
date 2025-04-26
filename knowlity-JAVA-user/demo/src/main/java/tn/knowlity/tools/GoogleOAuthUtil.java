package tn.knowlity.tools;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GoogleOAuthUtil {
    private static final Logger LOGGER = Logger.getLogger(GoogleOAuthUtil.class.getName());
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";

    public static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream resourceStream = GoogleOAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (resourceStream == null) {
            LOGGER.severe("Client secrets file not found: " + CREDENTIALS_FILE_PATH);
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        InputStreamReader in = new InputStreamReader(resourceStream);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, in);

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        File dataStoreDir = new File(TOKENS_DIRECTORY_PATH);
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8080)
                .build();

        try {
            LOGGER.info("Starting local server on port 8080 for callback: " + REDIRECT_URI);
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            LOGGER.info("Authorization successful for user");
            return credential;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to authorize with Google: " + e.getMessage(), e);
            throw e;
        } finally {
            try {
                receiver.stop();
                LOGGER.info("Local server stopped");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error stopping local server: " + e.getMessage(), e);
            }
        }
    }

    public static Userinfo getUserInfo(Credential credential) throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("KnowLity")
                .build();
        Userinfo userInfo = oauth2.userinfo().get().execute();
        LOGGER.info("Retrieved user info: " + userInfo.getEmail());
        return userInfo;
    }

    public static String getAuthorizationUrl() throws IOException, GeneralSecurityException {
        InputStream resourceStream = GoogleOAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (resourceStream == null) {
            LOGGER.severe("Client secrets file not found: " + CREDENTIALS_FILE_PATH);
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        InputStreamReader in = new InputStreamReader(resourceStream);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, in);

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI);
        String url = authorizationUrl.build();
        LOGGER.info("Generated authorization URL: " + url);
        return url;
    }
}
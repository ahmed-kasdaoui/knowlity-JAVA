package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TranslationService {
    private static final String[] API_SERVERS = {
        "https://translate.terraprint.co/",
        "https://translate.argosopentech.com/",
        "https://libretranslate.de/"
    };
    
    private String currentServer;
    private static final String API_PATH = "translate";
    private static final String DETECT_PATH = "detect";
    private final OkHttpClient client;
    private final Gson gson;

    public TranslationService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
        findWorkingServer();
    }

    private void findWorkingServer() {
        for (String server : API_SERVERS) {
            try {
                Request request = new Request.Builder()
                    .url(server + "languages")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        currentServer = server;
                        return;
                    }
                }
            } catch (Exception ignored) {
                // Continue to next server
            }
        }
        
        // Si aucun serveur ne répond, on utilise le premier par défaut
        currentServer = API_SERVERS[0];
    }

    public String detectLanguage(String text) throws IOException {
        // Vérifier si un serveur est disponible
        if (currentServer == null) {
            findWorkingServer();
        }

        Map<String, String> requestData = new HashMap<>();
        requestData.put("q", text);

        RequestBody body = RequestBody.create(
            gson.toJson(requestData),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(currentServer + DETECT_PATH)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Si le serveur actuel échoue, essayer de trouver un autre serveur
                findWorkingServer();
                if (!currentServer.equals(request.url().toString())) {
                    return detectLanguage(text); // Réessayer avec le nouveau serveur
                }
                throw new IOException("Erreur lors de la détection de la langue: " + response);
            }

            String responseBody = response.body().string();
            JsonObject[] detections = gson.fromJson(responseBody, JsonObject[].class);
            
            if (detections.length > 0) {
                return detections[0].get("language").getAsString();
            } else {
                throw new IOException("Impossible de détecter la langue");
            }
        } catch (IOException e) {
            // Si une erreur de connexion se produit, essayer un autre serveur
            findWorkingServer();
            throw new IOException("Erreur de connexion au service de traduction. Veuillez réessayer.", e);
        }
    }

    public String translateText(String text, String sourceLang, String targetLang) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Le texte à traduire ne peut pas être vide");
        }

        // Vérifier si un serveur est disponible
        if (currentServer == null) {
            findWorkingServer();
        }

        // Si la langue source n'est pas spécifiée, on la détecte
        if (sourceLang == null || sourceLang.equals("auto")) {
            try {
                sourceLang = detectLanguage(text);
            } catch (IOException e) {
                throw new IOException("Impossible de détecter la langue source. Veuillez vérifier votre connexion internet.", e);
            }
        }

        // Préparer les données pour la requête
        Map<String, String> requestData = new HashMap<>();
        requestData.put("q", text);
        requestData.put("source", sourceLang);
        requestData.put("target", targetLang);

        // Créer la requête POST
        RequestBody body = RequestBody.create(
            gson.toJson(requestData),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(currentServer + API_PATH)
            .post(body)
            .build();

        // Exécuter la requête
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Si le serveur actuel échoue, essayer de trouver un autre serveur
                findWorkingServer();
                if (!currentServer.equals(request.url().toString())) {
                    return translateText(text, sourceLang, targetLang); // Réessayer avec le nouveau serveur
                }

                String errorBody = response.body() != null ? response.body().string() : "";
                int code = response.code();
                String errorMessage;
                
                switch (code) {
                    case 429:
                        errorMessage = "Trop de requêtes. Veuillez réessayer dans quelques instants.";
                        break;
                    case 503:
                        errorMessage = "Le service de traduction est temporairement indisponible. Veuillez réessayer plus tard.";
                        break;
                    case 413:
                        errorMessage = "Le texte à traduire est trop long.";
                        break;
                    default:
                        errorMessage = String.format("Erreur lors de la traduction (code %d): %s", code, errorBody);
                }
                
                throw new IOException(errorMessage);
            }

            // Parser la réponse
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            if (!jsonResponse.has("translatedText")) {
                throw new IOException("La réponse ne contient pas de traduction. Le service pourrait être temporairement indisponible.");
            }
            
            return jsonResponse.get("translatedText").getAsString();
        } catch (IOException e) {
            // Si une erreur de connexion se produit, essayer un autre serveur
            findWorkingServer();
            if (!currentServer.equals(request.url().toString())) {
                return translateText(text, sourceLang, targetLang); // Réessayer avec le nouveau serveur
            }
            throw new IOException("Erreur de connexion au service de traduction. Veuillez réessayer.", e);
        } catch (Exception e) {
            throw new IOException("Une erreur inattendue s'est produite lors de la traduction. Veuillez réessayer.", e);
        }
    }
}

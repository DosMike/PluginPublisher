package de.dosmike.sponge.github;

import com.itwookie.utils.Request;
import de.dosmike.sponge.limiter.XRateLimit;
import de.dosmike.sponge.pluginpublisher.Arguments;
import de.dosmike.sponge.pluginpublisher.Executable;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

public class APICommons {

    static String baseURL = "https://api.github.com";
    static String UserAgent = "Custom CD for SpongePowered projects/1.0 (by DosMike)";

    static XRateLimit bucket = new XRateLimit("X-RateLimit-Remaining", "X-RateLimit-Reset", false);

    public static HttpsURLConnection getApiConnection(String method, String endpoint, Consumer<HttpURLConnection> connectionTransformer) {
        bucket.waitForNext();
        try {
            Request request = new Request(method,baseURL+endpoint);
            request.setRequestHeader("User-Agent", UserAgent);
            request.setRequestHeader("Accept", "application/vnd.github.v3+json");
            request.setRequestHeader("Authorization", "token "+System.getenv(Arguments.gitAPIKey));
            HttpsURLConnection connection = (HttpsURLConnection)request.getRawConnection();
            connectionTransformer.accept(connection);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static HttpsURLConnection getApiConnection(String method, URL target, Consumer<HttpURLConnection> connectionTransformer) {
        bucket.waitForNext();
        try {
            Request request = new Request(method,target);
            request.setRequestHeader("User-Agent", UserAgent);
            request.setRequestHeader("Accept", "application/vnd.github.v3+json");
            request.setRequestHeader("Authorization", "token "+System.getenv(Arguments.gitAPIKey));
            HttpsURLConnection connection = (HttpsURLConnection)request.getRawConnection();
            connectionTransformer.accept(connection);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void getBucketInformation(HttpURLConnection connection) {
        bucket.updateBucket(connection);
    }

}

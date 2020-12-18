package de.dosmike.sponge.oreapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.sponge.oreapi.v2.OreDeployVersionInfo;
import de.dosmike.sponge.oreapi.v2.OreSession;
import de.dosmike.sponge.pluginpublisher.Statics;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Calls within this class initially block, but do not consider and Rate Limits. In order to prevent dos-ing the Ore
 * Servers it is encouraged to use the RateLimiter with <code>OreApi.waitFor(()-&gt;OreApi.Method)</code>.
 * If you want to perform bulk operations you should consider collecting the futures returned from
 * <code>OreApi.enqueue(()-&gt;OreApi.Method)</code> and passing your collection through
 * <code>RateLimiter.waitForAll(collection)</code>. The resulting collection will be a list, holding order
 * of your supplied collection if applicable.
 * Note: This implementation shares a lot with the Ore-Get implementation, but is not 100% compatible due to this
 * implementation not relying on the SpongeAPI being available.
 */
public class OreApiV2 implements AutoCloseable {

    private static OreSession session = new OreSession();
    private RateLimiter limiter;

    /**
     * shorthand for {@link RateLimiter#waitFor(Supplier)}.<br>
     * API calls return optionals, so does waitFor. This method automatically unboxes one optional
     */
    public <T> Optional<T> waitFor(Supplier<Optional<T>> task) {
        return limiter.waitFor(task).orElseGet(Optional::empty);
    }
    private String apiKey=null;
    public OreApiV2() {
        limiter = new RateLimiter();
        limiter.start();
    }
    public OreApiV2(String APIkey) {
        this();
        apiKey = APIkey;
    }
    @Override
    public void close() throws Exception {
        limiter.halt();
    }

    private static final SimpleDateFormat timestampParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final SimpleDateFormat timestampParser2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        timestampParser.setLenient(true);
        timestampParser2.setLenient(true);
    }
    public static long superTimeParse(String time) {
        try {
            return timestampParser.parse(time).getTime();
        } catch (Exception ignore) {
        }
        try {
            return timestampParser2.parse(time).getTime();
        } catch (Exception ignore) {
        }
        throw new RuntimeException("Could not parse time \"" + time + "\"");
    }

    private static HttpsURLConnection createConnection(String endpoint) throws IOException {
        return createConnection(endpoint, "application/json");
    }

    private static HttpsURLConnection createConnection(String endpoint, String contentType) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://ore.spongepowered.org/api/v2" + endpoint).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", Statics.USER_AGENT);
        if (contentType != null)
            connection.setRequestProperty("Content-Type", contentType);
        return connection;
    }

    private static JsonObject parseJson(HttpsURLConnection connection) throws IOException {
        return JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
    }

    public boolean authenticate() {
        if (session.isValid()) return true;
        try {
            HttpsURLConnection connection = createConnection("/authenticate");
            if (apiKey != null) connection.setRequestProperty("Authorization", "OreApi apikey="+apiKey);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                tryPrintErrorBody(connection);
                return false;
            }
            session = new OreSession(parseJson(connection));
            return session.isValid();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public JsonObject getProjectById(String projectId) {
        authenticate();
        limiter.takeRequest();
        try {
            String totalQuery = "/projects/" + URLEncoder.encode(projectId, "UTF-8");
            HttpsURLConnection connection = session.authenticate(createConnection(totalQuery));
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode() != 404) tryPrintErrorBody(connection);
                throw new RuntimeException("Could not fetch project: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
            return parseJson(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This endpoint requires the permission <tt>create_version</tt>. To use this, please provide an ApiKey
     * with appropriate permissions in the constructor.
     */
    public boolean createVersion(String pluginId, OreDeployVersionInfo info, Path file) {
        authenticate();
        limiter.takeRequest();
        try {
            String boundary = "--------------------ENTRY_SEPARATOR_" + UUID.randomUUID().toString();
            String jsonPluginInfo = info.toJson().toString();
            String headersEntry1 = "Content-Disposition: form-data; name=\"plugin-info\"\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n";
            String fileName = file.getFileName().toString(), fileNameASCII = fileName.replace('"', '_');
            String headersEntry2 = "Content-Disposition: form-data; name=\"plugin-file\"; filename=\"" + fileNameASCII + "\"; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + "\r\n" +
                    "Content-Type: " + Files.probeContentType(file) + "\r\n" +
                    "\r\n";
            String endpoint = "/projects/" + URLEncoder.encode(pluginId, "UTF-8") + "/versions";
            HttpsURLConnection connection = session.authenticate(createConnection(endpoint, "multipart/form-data; boundary="+boundary));
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write("--"+boundary+"\r\n");
            osw.write(headersEntry1);
            osw.write(jsonPluginInfo+"\r\n");
            osw.write("--"+boundary+"\r\n");
            osw.write(headersEntry2);
            osw.flush();
            Files.copy(file, connection.getOutputStream());
            osw.write("\r\n");
            osw.write("--" + boundary + "--\r\n");
            osw.flush();

            System.out.println("Upload Complete                                            ");
            if (connection.getResponseCode() != 201) {
                tryPrintErrorBody(connection);
                throw new RuntimeException("API returned with "+connection.getResponseCode()+": "+connection.getResponseMessage());
            }
            JsonObject responseObject = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            return true;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static void tryPrintErrorBody(HttpsURLConnection connection) {
        try {
            System.err.println("Error Body for response "+connection.getResponseCode()+": "+connection.getResponseMessage());
            InputStream in = connection.getErrorStream();
            if (in != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while ((line = br.readLine()) != null)
                    System.err.println(line);
                br.close();
            }
        } catch (IOException ignore) {}
    }

    public void destroySession() {
        authenticate();
        limiter.takeRequest();
        try {
            String totalQuery = "/sessions/current";
            HttpsURLConnection connection = session.authenticate(createConnection(totalQuery));
            connection.setRequestMethod("DELETE");
            session = new OreSession();
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

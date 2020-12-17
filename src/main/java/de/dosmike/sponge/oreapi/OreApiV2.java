package de.dosmike.sponge.oreapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.sponge.oreapi.v2.*;
import de.dosmike.sponge.utils.CachingCollection;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

    private static final String UserAgent = "PluginPublisher/1.1.0 (by DosMike)";

    private OreSession session;
    private RateLimiter limiter;

    public RateLimiter getRateLimiter() {
        return limiter;
    }

    /**
     * shorthand for {@link RateLimiter#waitFor(Supplier)}.<br>
     * API calls return optionals, so does waitFor. This method automatically unboxes one optional
     */
    public <T> Optional<T> waitFor(Supplier<Optional<T>> task) {
        return limiter.waitFor(task).orElseGet(Optional::empty);
    }
    /** shorthand for {@link RateLimiter#enqueue(Supplier)} */
    public <T> Future<T> enqueue(Supplier<T> task) {
        return limiter.enqueue(task);
    }
    private String apiKey=null;
    public OreApiV2() {
        session = new OreSession();
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

    private static JsonParser parser = new JsonParser();
    private static final Pattern paginationPattern = Pattern.compile("limit=[1-9][0-9]*&offset=[0-9]+");

    private static HttpsURLConnection createConnection(String endpoint) throws IOException {
        return createConnection(endpoint, "application/json");
    }

    private static HttpsURLConnection createConnection(String endpoint, String contentType) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://ore.spongepowered.org/api/v2" + endpoint).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", UserAgent);
        if (contentType != null)
            connection.setRequestProperty("Content-Type", contentType);
        return connection;
    }

    private static JsonObject parseJson(HttpsURLConnection connection) throws IOException {
        return parser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
    }
    public OreSession getSession() {
        return session;
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

    /**
     * @param pagination a string as returned by {@link OrePagination#getQueryPage}
     * @return empty if connection failed
     */
    public Optional<OreResultList<OreProject>> projectSearch(String queryString, @Nullable String pagination) {
        if (!authenticate())
            throw new IllegalStateException("Could not create API session");
        //don't want to cache a search
        limiter.takeRequest();
        try {
            String totalQuery = "/projects?q="+ URLEncoder.encode(queryString,"UTF-8");
            if (pagination != null) {
                if (!paginationPattern.matcher(pagination).matches())
                    throw new IllegalArgumentException("Invalid pagination string");
                totalQuery += "&"+pagination;
            }
            HttpsURLConnection connection = session.authenticate(createConnection(totalQuery));
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                tryPrintErrorBody(connection);
                return Optional.empty();
            }
            OreResultList<OreProject> resultList = new OreResultList<>(parseJson(connection), OreProject.class);
            for (OreProject p : resultList.getResult())
                cacheProject(p);
            return Optional.of(resultList);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * @return empty if the connection failed or no such plugin exists
     */
    public Optional<OreProject> getProject(String pluginId) {
        authenticate();
        Optional<OreProject> cache = getCachedProject(pluginId);
        if (cache.isPresent()) return cache;

        limiter.takeRequest();
        try {
            String totalQuery = "/projects/"+ URLEncoder.encode(pluginId, "UTF-8");
            HttpsURLConnection connection = session.authenticate(createConnection(totalQuery));
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                return Optional.empty();
            }
            return Optional.of(cacheProject(new OreProject(parseJson(connection))));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * @return empty if the connection failed or no such plugin exists
     */
    public Optional<OreResultList<OreVersion>> listVersions(String pluginId, @Nullable String pagination) {
        authenticate();
        //can't think of an easy way to get the ResultList back from the version cache, so I won't bother
        limiter.takeRequest();
        try {
            String totalQuery = "/projects/"+ URLEncoder.encode(pluginId, "UTF-8")+"/versions";
            if (pagination != null) {
                if (!paginationPattern.matcher(pagination).matches())
                    throw new IllegalArgumentException("Invalid pagination string");
                totalQuery += "?"+pagination;
            }
            HttpsURLConnection connection = session.authenticate(createConnection(totalQuery));
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                tryPrintErrorBody(connection);
                return Optional.empty();
            }
            OreResultList<OreVersion> resultList = new OreResultList<>(parseJson(connection), OreVersion.class);
            for (OreVersion v : resultList.getResult())
                cacheVersion(pluginId.toLowerCase(), v);
            return Optional.of(resultList);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * @return empty if the connection failed or no such plugin or version exists
     */
    public Optional<OreVersion> getVersion(String pluginId, String versionName) {
        authenticate();
        Optional<OreVersion> cache = getCachedVersion(pluginId, versionName);
        if (cache.isPresent()) return cache;
        limiter.takeRequest();
        try {
            String totalQuery = "/projects/"+ URLEncoder.encode(pluginId, "UTF-8")+"/versions/"+ URLEncoder.encode(versionName, "UTF-8");
            HttpsURLConnection connection = session.authenticate(createConnection(totalQuery));
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                return Optional.empty();
            }
            oreVersionCache.get(pluginId);
            return Optional.of(cacheVersion(pluginId.toLowerCase(), new OreVersion(parseJson(connection))));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /** This endpoint requires the permission <tt>create_version</tt>. To use this, please provide an ApiKey
     * with appropriate permissions in the constructor. */
    public Optional<OreVersion> createVersion(String pluginId, OreDeployVersionInfo info, Path file) {
        authenticate();
        limiter.takeRequest();
        try {
            String boundary="--------------------ENTRY_SEPARATOR_"+ UUID.randomUUID().toString();
            String jsonPluginInfo = info.toJson().toString();
            String headersEntry1 = "Content-Disposition: form-data; name=\"plugin-info\"\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n";
            String fileName = file.getFileName().toString(), fileNameASCII = fileName.replace('"','_');
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
            JsonParser parser = new JsonParser();
            JsonObject responseObject = parser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            return Optional.of(new OreVersion(responseObject));
        } catch (IOException e) {
//            e.printStackTrace();
            return Optional.empty();
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

    public URL getDownloadURL(OreNamespace namespace, OreVersion version) {
        authenticate();
        try {
            if (version.getReviewState().equals(OreReviewState.REVIEWED))
                return new URL("https://ore.spongepowered.org/"+
                        URLEncoder.encode(namespace.getOwner(), "UTF-8")+"/"+
                        URLEncoder.encode(namespace.getSlug(), "UTF-8")+"/versions/"+
                        URLEncoder.encode(version.getName(), "UTF-8")+"/download");

            // I'll just fetch the url here, since I prompt confirmation within PluginJob
            URL requestUrl = new URL("https://ore.spongepowered.org/"+
                    URLEncoder.encode(namespace.getOwner(), "UTF-8")+"/"+
                    URLEncoder.encode(namespace.getSlug(), "UTF-8")+"/versions/"+
                    URLEncoder.encode(version.getName(), "UTF-8")+"/confirm?api=true");
            HttpsURLConnection connection = session.authenticate((HttpsURLConnection) requestUrl.openConnection());
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "OreGet (by DosMike)/1.0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                tryPrintErrorBody(connection);
                return null;
            }
            JsonObject response = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            String string = response.get("url").getAsString();
            return new URL(string);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private CachingCollection<OreProject> oreProjectCache = new CachingCollection<>(5, TimeUnit.MINUTES);
    private OreProject cacheProject(OreProject project) {
        oreProjectCache.add(project);
        return project;
    }
    private Optional<OreProject> getCachedProject(String pluginId) {
        return oreProjectCache.stream().filter(e->e.getPluginId().equalsIgnoreCase(pluginId)).findFirst();
    }
    private Map<String, CachingCollection<OreVersion>> oreVersionCache = new HashMap<>();
    private OreVersion cacheVersion(String pluginId, OreVersion version) {
        CachingCollection<OreVersion> cache = oreVersionCache.get(pluginId.toLowerCase());
        if (cache == null) {
            cache = new CachingCollection<>(5, TimeUnit.MINUTES);
            oreVersionCache.put(pluginId.toLowerCase(), cache);
        }
        cache.add(version);
        return version;
    }
    private Optional<OreVersion> getCachedVersion(String pluginId, String versionName) {
        CachingCollection<OreVersion> collection = oreVersionCache.get(pluginId.toLowerCase());
        if (collection == null) return Optional.empty();
        else return collection.stream()
                .filter(v -> v.getName().equalsIgnoreCase(versionName))
                .findFirst();
    }

    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    public ScheduledExecutorService getThreadPool() {
        return threadPool;
    }

    public void continueSession(OreSession session) {
        if (session.isValid()) {
            this.session = session;
        } else throw new IllegalArgumentException("The provided session is invalid");
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

package de.dosmike.sponge.github;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.itwookie.utils.Request;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReleaseAPI {

    Gson gson = new Gson();

    /**
     * Creates a new release
     * @param projectSlug &lt;user name&gt;/&lt;project name&gt; as presented in the project URL
     * @param tagName the name of the tag to create. e.g. <tt>2.4</tt>
     * @param targetCommitish the commitish this tag stamms from. most likely <tt>master</tt>
     * @param fullName the full tag name. e.g. <tt>Release Build for January</tt>
     * @param description tag description. this should be markdown
     * @param isDraft use true if you want to save this release as draft
     * @param isPrerelease use true if you want to mark this release as pre-release
     */
    public JsonObject createReleaseTag(String projectSlug, String tagName, String targetCommitish, String fullName, String description, boolean isDraft, boolean isPrerelease) {
        //create JSON body:
        JsonObject body = new JsonObject();
        body.addProperty("tag_name", tagName);
        body.addProperty("target_commitish", targetCommitish);
        body.addProperty("name", fullName);
        body.addProperty("body", description);
        body.addProperty("draft", isDraft);
        body.addProperty("prerelease", isPrerelease);
        String jsonBody = body.toString();

        HttpsURLConnection apiConnection = APICommons.getApiConnection("POST", "/repos/"+projectSlug+"/releases", (con)->{
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Content-Length", String.valueOf(jsonBody.getBytes(StandardCharsets.US_ASCII).length));
            con.setDoInput(true);
            con.setDoOutput(true);
            try {
                OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(), StandardCharsets.US_ASCII);
                out.write(jsonBody);
                out.flush();
            } catch (IOException e) {}
        });

        try {
            if (apiConnection.getResponseCode() != 201) {
                System.err.println("API returned with "+apiConnection.getResponseCode()+": "+apiConnection.getResponseMessage());
                BufferedReader br = new BufferedReader(new InputStreamReader(apiConnection.getErrorStream()));
                String line; while ((line = br.readLine())!=null) System.err.println(line);
                throw new RuntimeException();
            }
            JsonReader reader = gson.newJsonReader(new InputStreamReader(apiConnection.getInputStream()));
            JsonParser parser = new JsonParser();
            return parser.parse(reader).getAsJsonObject();
        } catch (IOException e) {
            System.err.println("Could not create release tag: "+e.getMessage());
            throw new RuntimeException();
        } finally {
            APICommons.getBucketInformation(apiConnection);
        }
    }
    /**
     * Creates a new release
     * @param projectSlug &lt;user name&gt;/&lt;project name&gt; as presented in the project URL
     * @param tagName the name of the tag to create. e.g. <tt>2.4</tt>
     * @param targetCommitish the commitish this tag stamms from. most likely <tt>master</tt>
     * @param fullName the full tag name. e.g. <tt>Release Build for January</tt>
     * @param description tag description. this should be markdown
     */
    public JsonObject createReleaseTag(String projectSlug, String tagName, String targetCommitish, String fullName, String description) {
        return createReleaseTag(projectSlug, tagName, targetCommitish, fullName, description, false, false);
    }
    /**
     * Creates a new release
     * @param projectSlug &lt;user name&gt;/&lt;project name&gt; as presented in the project URL
     * @param tagName the name of the tag to create. e.g. <tt>2.4</tt>
     * @param targetCommitish the commitish this tag stamms from. most likely <tt>master</tt>
     * @param description tag description. this should be markdown
     * @param isDraft use true if you want to save this release as draft
     * @param isPrerelease use true if you want to mark this release as pre-release
     */
    public JsonObject createReleaseTag(String projectSlug, String tagName, String targetCommitish, String description, boolean isDraft, boolean isPrerelease) {
        return createReleaseTag(projectSlug, tagName, targetCommitish, "Automatic CD Release "+tagName, description, isDraft, isPrerelease);
    }
    /**
     * Creates a new release
     * @param projectSlug &lt;user name&gt;/&lt;project name&gt; as presented in the project URL
     * @param tagName the name of the tag to create. e.g. <tt>2.4</tt>
     * @param targetCommitish the commitish this tag stamms from. most likely <tt>master</tt>
     * @param description tag description. this should be markdown
     */
    public JsonObject createReleaseTag(String projectSlug, String tagName, String targetCommitish, String description) {
        return createReleaseTag(projectSlug, tagName, targetCommitish, "Automatic CD Release "+tagName, description, false, false);
    }

    public JsonObject uploadAsset(String assetURL, Path file) {
        try {
            String filename = file.getFileName().toString();
            long size = Files.size(file);
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream"; //it's binary, have fun ;D
            }

            URL target = new URL(assetURL.replace("{?name,label}", "?name="+filename));

            new Request("POST", target);
            HttpsURLConnection connection = APICommons.getApiConnection("POST", target, (c)->{});
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",contentType);
            connection.setRequestProperty("Content-Length",String.valueOf(size));
            long progress=0;
            try (InputStream fis = Files.newInputStream(file)) {
                byte[] buffer = new byte[1024];
                int r;
                while ((r = fis.read(buffer)) >= 0) {
                    connection.getOutputStream().write(buffer, 0, r);
                    progress+=r;
                    System.out.print("Uploading... " + progress + "/" + size+" bytes\r");
                }
                connection.getOutputStream().flush();
            }
            System.out.println("Upload Complete                                            ");
            if (connection.getResponseCode() != 201) {
                System.err.println("API returned with "+connection.getResponseCode()+": "+connection.getResponseMessage());
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line; while ((line = br.readLine())!=null) System.err.println(line);
                throw new RuntimeException();
            }
            APICommons.getBucketInformation(connection);

            JsonParser parser = new JsonParser();
            return parser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
        } catch (IOException e) {
            System.err.println("Unable to upload asset: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

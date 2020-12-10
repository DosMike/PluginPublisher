package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonObject;
import de.dosmike.sponge.oreapi.OreApiV2;

import javax.net.ssl.HttpsURLConnection;

public class OreSession {

    String session;
    long expires;

    public OreSession(JsonObject json) {
        this.session = json.get("session").getAsString();
        String tmp = json.get("expires").getAsString();
        try {
            expires = OreApiV2.superTimeParse(tmp);
        } catch (RuntimeException ignore) {
            System.err.println("Could not parse timestamp in blobSession");
            expires = System.currentTimeMillis()+3600_000;
        }
    }
    public OreSession() {
        session = "";
        expires = 0L;
    }
    /** in order to restore a session that might still be valid */
    public OreSession(String sessionToken, Long validUntilUnixMS) {
        session = sessionToken;
        expires = validUntilUnixMS;
    }

    public boolean isValid() {
        return System.currentTimeMillis() < expires;
    }
    /**
     * @return connection for piping
     */
    public HttpsURLConnection authenticate(HttpsURLConnection connection) {
        // Reversed from Network monitor because not documented:
        // Why not simply `OreApi SESSION`? You know it's supposed to be a session!?
        connection.setRequestProperty("Authorization", "OreApi session="+session);
        return connection;
    }

    public String toString() {
        return session+":"+expires;
    }

}

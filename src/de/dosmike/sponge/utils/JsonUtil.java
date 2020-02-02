package de.dosmike.sponge.utils;

import com.google.gson.JsonObject;

public class JsonUtil {

    public static String optString(JsonObject object, String node) {
        if (!object.has(node) || object.get(node).isJsonNull()) return null;
        return object.get(node).getAsString();
    }
    public static int optInt(JsonObject object, String node) {
        if (!object.has(node) || object.get(node).isJsonNull()) return 0;
        return object.get(node).getAsInt();
    }
    public static long optLong(JsonObject object, String node) {
        if (!object.has(node) || object.get(node).isJsonNull()) return 0L;
        return object.get(node).getAsLong();
    }

}

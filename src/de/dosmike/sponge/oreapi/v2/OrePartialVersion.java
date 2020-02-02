package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.dosmike.sponge.utils.JsonUtil;

/**
 * IMPORTANT: This is only a partial version that
 * contains the most crucial display information for recommended versions
 * to be displayed on the project page. For full version objects please
 * use the version endpoints.
 */
public class OrePartialVersion {

    String version;
    OreTag[] tags;

    public OrePartialVersion(JsonObject object) {
        version = JsonUtil.optString(object, "version");
        JsonArray array = object.getAsJsonArray("tags");
        tags = new OreTag[array.size()];
        for (int i = 0; i < array.size(); i++)
            tags[i] = new OreTag(array.get(i).getAsJsonObject());
    }

    public String getVersion() {
        return version;
    }

    public OreTag[] getTags() {
        return tags;
    }
}

package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonObject;
import de.dosmike.sponge.utils.JsonUtil;

public class OreNamespace {

    String owner;
    String slug;

    public OreNamespace(JsonObject object) {
        owner = JsonUtil.optString(object, "owner");
        slug = JsonUtil.optString(object, "slug");
    }

    public String getOwner() {
        return owner;
    }

    public String getSlug() {
        return slug;
    }
}

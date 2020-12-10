package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonObject;
import de.dosmike.sponge.utils.JsonUtil;

import java.awt.*;

public class OreTag {

    String name;
    String data;
    String displayData;
    String minecraftVersion;
    Color foreground;
    Color background;

    public OreTag(JsonObject object) {
        name = JsonUtil.optString(object, "name");
        data = JsonUtil.optString(object, "data");
        displayData = JsonUtil.optString(object, "display_data");
        minecraftVersion = JsonUtil.optString(object,"minecraft_version");
        foreground = Color.decode(object.getAsJsonObject("color").get("foreground").getAsString());
        background = Color.decode(object.getAsJsonObject("color").get("background").getAsString());
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getDisplayData() {
        return displayData;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

}

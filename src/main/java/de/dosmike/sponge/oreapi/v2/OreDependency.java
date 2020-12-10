package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonObject;
import de.dosmike.sponge.utils.JsonUtil;

public class OreDependency {

    String pluginId;
    String version;

    public OreDependency(JsonObject object) {
        pluginId = JsonUtil.optString(object, "plugin_id");
        version = JsonUtil.optString(object,"version");
    }

    public String getPluginId() {
        return pluginId;
    }

    /**
     * NOTE: might be a version range.<br>
     * Details on VersionRange <a href="https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#CJHDEHAB">https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#CJHDEHAB</a><br>
     * NOTE: Versions are not guaranteed to have any format! Thus all versions are handled as Strings.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Check if plugin id and version match
     */
    public boolean equals(OreDependency other) {
        return this.pluginId.equalsIgnoreCase(other.pluginId) &&
                this.version.equals(other.version);
    }
}

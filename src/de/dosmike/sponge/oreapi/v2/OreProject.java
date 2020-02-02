package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.dosmike.sponge.oreapi.OreApiV2;
import de.dosmike.sponge.utils.JsonUtil;

/** does not parse URLs to prevent Malformed URL Exceptions */
public class OreProject {

    long createdAt;
    String pluginId;
    String name;
    OreNamespace namespace;
    OrePartialVersion[] promotedVersions;
    int views;
    int downloads;
    int stars;
    OreCategory category;
    String description;
    long lastUpdate;
    String visibility; //always "public" for this implementation
    String urlHomepage;
    String urlIssues;
    String urlSources;
    String urlSupport;
    String urlLicense;
    String license;
    boolean forumSync;
    String urlIcon;

    public OreProject(JsonObject object) {
        try {
            createdAt = OreApiV2.superTimeParse(object.get("created_at").getAsString());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        pluginId = JsonUtil.optString(object, "plugin_id");
        name = JsonUtil.optString(object, "name");
        namespace = new OreNamespace(object.get("namespace").getAsJsonObject());
        JsonArray array = object.getAsJsonArray("promoted_versions");
        promotedVersions = new OrePartialVersion[array.size()];
        for (int i = 0; i < array.size(); i++)
            promotedVersions[i] = new OrePartialVersion(array.get(i).getAsJsonObject());
        views = JsonUtil.optInt(object.getAsJsonObject("stats"), "views");
        downloads = JsonUtil.optInt(object.getAsJsonObject("stats"), "downloads");
        stars = JsonUtil.optInt(object.getAsJsonObject("stats"), "stars");
        category = OreCategory.fromString(object.get("category").getAsString());
        description = JsonUtil.optString(object,"description");
        try {
            if (object.has("last_update") && !object.get("last_update").isJsonNull())
                lastUpdate = OreApiV2.superTimeParse(object.get("last_update").getAsString());
            else
                lastUpdate = 0L;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        visibility = JsonUtil.optString(object, "visibility");
        JsonObject settings = object.getAsJsonObject("settings");
        urlHomepage = JsonUtil.optString(settings, "homepage");
        urlIssues = JsonUtil.optString(settings, "issues");
        urlSources = JsonUtil.optString(settings, "sources");
        urlSupport = JsonUtil.optString(settings, "support");
        urlLicense = JsonUtil.optString(settings.getAsJsonObject("license"), "url");
        license = JsonUtil.optString(settings.getAsJsonObject("license"), "name");
        forumSync = settings.get("forum_sync").getAsBoolean();
        urlIcon = JsonUtil.optString(settings, "icon_url");
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getName() {
        return name;
    }

    public OreNamespace getNamespace() {
        return namespace;
    }

    /**
     * The Versions returned here are partial!
     * For full version objects, please call {@link OreApiV2#listVersions}
     */
    public OrePartialVersion[] getPromotedVersions() {
        return promotedVersions;
    }

    public int getViews() {
        return views;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getStars() {
        return stars;
    }

    public OreCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getUrlHomepage() {
        return urlHomepage;
    }

    public String getUrlIssues() {
        return urlIssues;
    }

    public String getUrlSources() {
        return urlSources;
    }

    public String getUrlSupport() {
        return urlSupport;
    }

    public String getUrlLicense() {
        return urlLicense;
    }

    public String getLicense() {
        return license;
    }

    public boolean isForumSync() {
        return forumSync;
    }

    public String getUrlIcon() {
        return urlIcon;
    }

}

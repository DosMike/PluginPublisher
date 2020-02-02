package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.dosmike.sponge.oreapi.OreApiV2;
import de.dosmike.sponge.utils.JsonUtil;

public class OreVersion {

    long createdAt;
    String name;
    OreDependency[] dependencies;
    String visibility;
    String description;
    int downloads;
    long fileSize;
    String fileMD5;
    String author;
    OreReviewState reviewState;
    OreTag[] tags;

    public OreVersion(JsonObject object) {
        try {
            createdAt = OreApiV2.superTimeParse(object.get("created_at").getAsString());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        name = JsonUtil.optString(object,"name");
        JsonArray array = object.getAsJsonArray("dependencies");
        dependencies = new OreDependency[array.size()];
        for (int i = 0; i < array.size(); i++)
            dependencies[i] = new OreDependency(array.get(i).getAsJsonObject());
        visibility = JsonUtil.optString(object,"visibility");
        description = JsonUtil.optString(object, "description");
        downloads = JsonUtil.optInt(object.getAsJsonObject("stats"), "downloads");
        fileSize = JsonUtil.optLong(object.getAsJsonObject("file_info"), "size_bytes");
        fileMD5 = JsonUtil.optString(object.getAsJsonObject("file_info"), "md5_hash");
        author = JsonUtil.optString(object, "author");
        reviewState = OreReviewState.fromString(JsonUtil.optString(object, "review_state"));
        array = object.getAsJsonArray("tags");
        tags = new OreTag[array.size()];
        for (int i = 0; i < array.size(); i++)
            tags[i] = new OreTag(array.get(i).getAsJsonObject());
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public OreDependency[] getDependencies() {
        return dependencies;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getDescription() {
        return description;
    }

    public int getDownloads() {
        return downloads;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public String getAuthor() {
        return author;
    }

    public OreReviewState getReviewState() {
        return reviewState;
    }

    public OreTag[] getTags() {
        return tags;
    }
}

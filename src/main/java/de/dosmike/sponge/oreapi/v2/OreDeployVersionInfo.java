package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonObject;

/** transmit only data-bag */
public class OreDeployVersionInfo {

    boolean createForumPost;
    String description; //markdown
    String channel = "Release";

    //Region builder
    public static class Builder {
        OreDeployVersionInfo info;
        private Builder() {
            info = new OreDeployVersionInfo();
        }
        public Builder setCreateForumPost(boolean create) {
            info.createForumPost = create;
            return Builder.this;
        }
        public Builder setDescription(String markdown) {
            info.description = markdown;
            return Builder.this;
        }
        public Builder setChannel(String channelName) {
            info.channel = channelName;
            return Builder.this;
        }
        public OreDeployVersionInfo build() {
            return info;
        }
    }
    public static Builder builder() {
        return new Builder();
    }
    //endregion

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("create_forum_post", createForumPost);
        root.addProperty("description", description);

        JsonObject jtag = new JsonObject();
        jtag.addProperty("Channel", channel);

        root.add("tags", jtag);
        return root;
    }

}

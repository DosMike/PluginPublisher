package de.dosmike.sponge.pluginpublisher.tasks;

public class OreConfiguration {

	String project = null;
	String channel = "release";
	String apiKey = null;
	Object asset = null;
	String description = null;

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Object getAsset() {
		return asset;
	}

	public void setAsset(Object asset) {
		this.asset = asset;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean usable() {
		return apiKey != null && project != null && asset != null;
	}

	void validate() {
		if (apiKey == null)
			throw new IllegalArgumentException("Missing Ore API Key");
		if (project == null)
			throw new IllegalArgumentException("Missing Ore Project Slug");
		if (asset == null)
			throw new IllegalArgumentException("Missing Ore Asset Path");
	}
}

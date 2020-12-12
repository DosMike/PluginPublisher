package de.dosmike.sponge.pluginpublisher.tasks;

public class GitConfiguration {

	String apiKey = null;
	String slug = null;
	String tag = null;
	String tagFull = null;
	String commitish = "master";
	String[] assets = new String[0];
	String description = null;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTagFull() {
		return tagFull;
	}

	public void setTagFull(String tagFull) {
		this.tagFull = tagFull;
	}

	public String getCommitish() {
		return commitish;
	}

	public void setCommitish(String commitish) {
		this.commitish = commitish;
	}

	public String[] getAssets() {
		return assets;
	}

	public void setAssets(String[] assets) {
		this.assets = assets;
	}

	public String getAssets(int index) {
		return assets[index];
	}

	public void setAssets(int index, String value) {
		assets[index] = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean usable() {
		return apiKey != null && slug != null && tag != null;
	}

	void validate() {
		if (tagFull == null && tag != null) tagFull = "Automatic Release " + tag;
		if (apiKey == null)
			throw new IllegalArgumentException("Missing Git API Key");
		if (tag == null)
			throw new IllegalArgumentException("Missing Git Commitish");
		if (slug == null)
			throw new IllegalArgumentException("Missing Git Project Slug");
	}
}

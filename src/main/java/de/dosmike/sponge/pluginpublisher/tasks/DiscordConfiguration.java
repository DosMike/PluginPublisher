package de.dosmike.sponge.pluginpublisher.tasks;

public class DiscordConfiguration {

	String webHook = null;
	String header = null;
	String description = null;

	public String getWebHook() {
		return webHook;
	}

	public void setWebHook(String webHook) {
		this.webHook = webHook;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean usable() {
		return webHook != null;
	}

	void validate() {
		if (webHook == null)
			throw new IllegalArgumentException("Missing Discord WebHook URL");
		if (!webHook.startsWith("https://discord.com/api/webhooks/"))
			throw new IllegalArgumentException("The provided URL is not a Discord WebHook URL");
	}
}

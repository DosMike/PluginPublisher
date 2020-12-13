package de.dosmike.sponge.pluginpublisher.tasks;

public class DiscordBotConfiguration {

	String apiKey = null;
	String server = null;
	String channel = null;
	String header = null;
	String description = null;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
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
		return apiKey != null && server != null && channel != null;
	}

	void validate() {
		if (apiKey == null)
			throw new IllegalArgumentException("Missing Discord API Key / WebHook URL");
		if (server == null)
			throw new IllegalArgumentException("Missing Discord Server ID");
		if (channel == null)
			throw new IllegalArgumentException("Missing Discord Channel ID");
	}
}

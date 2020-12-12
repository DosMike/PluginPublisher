package de.dosmike.sponge.pluginpublisher.gradle;

import java.nio.file.Path;

/**
 * has to contain Java Beans properties
 */
public class GradleConfiguration {

	public String gitAPIKey = null;
	public String gitSlug = null;
	public String gitTag = null;
	public String gitTagFull = null;
	public String gitCommitish = "master";
	public Path[] gitAssets = new Path[0];
	public String oreProject = null;
	public String oreChannel = "Release";
	public String oreAPIKey = null;
	public Path oreAsset = null;
	public String discordAPIKey = null;
	public String discordServer = null;
	public String discordChannel = null;
	public String discordHeader = null;
	public String description = null; // user input if no file was specified

	public String getGitAPIKey() {
		return gitAPIKey;
	}

	public void setGitAPIKey(String gitAPIKey) {
		this.gitAPIKey = gitAPIKey;
	}

	public String getGitSlug() {
		return gitSlug;
	}

	public void setGitSlug(String gitSlug) {
		this.gitSlug = gitSlug;
	}

	public String getGitTag() {
		return gitTag;
	}

	public void setGitTag(String gitTag) {
		this.gitTag = gitTag;
	}

	public String getGitTagFull() {
		return gitTagFull;
	}

	public void setGitTagFull(String gitTagFull) {
		this.gitTagFull = gitTagFull;
	}

	public String getGitCommitish() {
		return gitCommitish;
	}

	public void setGitCommitish(String gitCommitish) {
		this.gitCommitish = gitCommitish;
	}

	public Path[] getGitAssets() {
		return gitAssets;
	}

	public void setGitAssets(Path[] gitAssets) {
		this.gitAssets = gitAssets;
	}

	public Path getGitAssets(int index) {
		return gitAssets[index];
	}

	public void setGitAssets(int index, Path asset) {
		this.gitAssets[index] = asset;
	}

	public String getOreProject() {
		return oreProject;
	}

	public void setOreProject(String oreProject) {
		this.oreProject = oreProject;
	}

	public String getOreChannel() {
		return oreChannel;
	}

	public void setOreChannel(String oreChannel) {
		this.oreChannel = oreChannel;
	}

	public String getOreAPIKey() {
		return oreAPIKey;
	}

	public void setOreAPIKey(String oreAPIKey) {
		this.oreAPIKey = oreAPIKey;
	}

	public Path getOreAsset() {
		return oreAsset;
	}

	public void setOreAsset(Path oreAsset) {
		this.oreAsset = oreAsset;
	}

	public String getDiscordAPIKey() {
		return discordAPIKey;
	}

	public void setDiscordAPIKey(String discordAPIKey) {
		this.discordAPIKey = discordAPIKey;
	}

	public String getDiscordServer() {
		return discordServer;
	}

	public void setDiscordServer(String discordServer) {
		this.discordServer = discordServer;
	}

	public String getDiscordChannel() {
		return discordChannel;
	}

	public void setDiscordChannel(String discordChannel) {
		this.discordChannel = discordChannel;
	}

	public String getDiscordHeader() {
		return discordHeader;
	}

	public void setDiscordHeader(String discordHeader) {
		this.discordHeader = discordHeader;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

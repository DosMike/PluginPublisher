package de.dosmike.sponge.pluginpublisher.gradle;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class GradleConfiguration {

	public String gitAPIKey = null;
	public String gitSlug = null;
	public String gitTag = null;
	public String gitTagFull = null;
	public String gitCommitish = "master";
	public List<Path> gitAssets = new LinkedList<>();
	public String oreProject = null;
	public String oreChannel = "Release";
	public String oreAPIKey = null;
	public Path oreAsset = null;
	public String discordAPIKey = null;
	public String discordServer = null;
	public String discordChannel = null;
	public String discordHeader = null;
	public String releaseDescriptionString = null; // user input if no file was specified

}

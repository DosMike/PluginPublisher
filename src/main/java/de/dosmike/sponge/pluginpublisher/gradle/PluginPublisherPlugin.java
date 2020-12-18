package de.dosmike.sponge.pluginpublisher.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PluginPublisherPlugin implements Plugin<Project> {

	final static String TaskNameGit = "PublishToGitHub";
	final static String TaskNameOre = "PublishToOre";
	final static String TaskNameOreLookup = "FetchOreProject";
	final static String TaskNameDiscordHook = "NotifyDiscord";
	final static String TaskNameDiscordBot = "NotifyDiscordBot";
	final static String TaskNameDiscordBot2 = "TerminateDiscordBot";

	@Override
	public void apply(Project project) {
		project.getTasks().create(TaskNameGit, PublishToGitTask.class)
				.setDescription("Creates a new GitHub Release with attached assets");
		project.getTasks().create(TaskNameOre, PublishToOreTask.class)
				.setDescription("Creates a new Ore Release with attached plugin");
		project.getTasks().create(TaskNameOreLookup, FetchOreProjectTask.class)
				.setDescription("Can be used to fetch project information from Ore");
		project.getTasks().create(TaskNameDiscordHook, NotifyDiscordTask.class)
				.setDescription("Sends a Notification to a Discord Channel using a WebHook");
		project.getTasks().create(TaskNameDiscordBot, NotifyDiscordTask.class)
				.setDescription("Sends a Notification to a Discord Channel through a JDA Bot Connection");
		project.getTasks().create(TaskNameDiscordBot2, TerminateDiscordConnectionTask.class)
				.setDescription("Closes the last JDA Bot Connection (if present)");
	}

}

package de.dosmike.sponge.pluginpublisher.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PluginPublisherPlugin implements Plugin<Project> {

	final static String TaskNameGit = "PublishToGitHub";
	final static String TaskNameOre = "PublishToOre";
	final static String TaskNameDiscord = "NotifyDiscord";
	final static String TaskNameDiscord2 = "TerminateDiscord";

	@Override
	public void apply(Project project) {
		project.getTasks().create(TaskNameGit, PublishToGitTask.class);
		project.getTasks().create(TaskNameOre, PublishToOreTask.class);
		project.getTasks().create(TaskNameDiscord, NotifyDiscordTask.class);
		project.getTasks().create(TaskNameDiscord2, TerminateDiscordConnectionTask.class);
	}

}

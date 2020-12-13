package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.DiscordBotConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class NotifyDiscordBotTask extends DefaultTask {

	@Input
	public DiscordBotConfiguration configuration = new DiscordBotConfiguration();

	@TaskAction
	public void publish() throws TaskRunException {
		if (!configuration.usable())
			throw new TaskRunException("The task was not configured");
		TaskFunctors.runDiscordTask(configuration);
	}

}

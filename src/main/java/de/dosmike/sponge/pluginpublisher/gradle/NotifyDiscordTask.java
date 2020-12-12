package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.DiscordConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class NotifyDiscordTask extends DefaultTask {

	@Input
	public DiscordConfiguration configuration = new DiscordConfiguration();

	@TaskAction
	public void publish() throws TaskRunException {
		if (!configuration.usable())
			throw new TaskRunException("The task was not configured");
		TaskFunctors.runDiscordTask(configuration);
	}

}

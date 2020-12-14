package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.OreConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class PublishToOreTask extends DefaultTask {

	@Input
	public OreConfiguration configuration = new OreConfiguration();

	@TaskAction
	public void publish() throws TaskRunException {
		if (!configuration.usable())
			throw new TaskRunException("The task was not configured");
		TaskFunctors.runOreTask(configuration, string -> getProject().file(string));
	}

}

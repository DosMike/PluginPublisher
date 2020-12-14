package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.GitConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class PublishToGitTask extends DefaultTask {

	@Input
	public GitConfiguration configuration = new GitConfiguration();

	@TaskAction
	public void publish() throws TaskRunException {
		if (!configuration.usable())
			throw new TaskRunException("The task was not configured");
		TaskFunctors.runGitTask(configuration, strings -> getProject().files((Object[]) strings));
	}

}

package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class TerminateDiscordConnectionTask extends DefaultTask {

	@TaskAction
	public void publish() throws TaskRunException {
		TaskFunctors.terminateJDA();
	}

}

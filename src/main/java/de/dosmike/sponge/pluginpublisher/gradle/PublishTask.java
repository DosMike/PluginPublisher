package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.Executable;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class PublishTask extends DefaultTask {

	@TaskAction
	public void publish() {
		Executable.runFromArguments();
	}

}

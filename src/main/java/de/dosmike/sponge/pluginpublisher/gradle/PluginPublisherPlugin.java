package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.Arguments;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PluginPublisherPlugin implements Plugin<Project> {

	final static String ConfigurationName = "PluginPublisher";
	final static String TaskName = "PublishPlugin";

	@Override
	public void apply(Project project) {
		GradleConfiguration config = project.getExtensions().create(ConfigurationName, GradleConfiguration.class);
		PublishTask task = project.getTasks().create(TaskName, PublishTask.class);
		task.doFirst((t) -> Arguments.fromGradleConfiguration(config));
	}
}

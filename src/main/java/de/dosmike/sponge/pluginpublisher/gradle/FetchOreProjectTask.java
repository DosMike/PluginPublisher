package de.dosmike.sponge.pluginpublisher.gradle;

import com.google.gson.JsonObject;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class FetchOreProjectTask extends DefaultTask {

	@Input
	public Property<String> apiKey = getProject().getObjects().property(String.class);
	@Input
	public Property<String> pluginId = getProject().getObjects().property(String.class);
	@Input
	public Property<JsonObject> projectData = getProject().getObjects().property(JsonObject.class);

	@TaskAction
	public void publish() throws TaskRunException {
		projectData.set(TaskFunctors.runOreProjectLookup(apiKey.get(), pluginId.get()));
	}

}

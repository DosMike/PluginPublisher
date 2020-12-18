package de.dosmike.sponge.pluginpublisher.gradle;

import com.google.gson.JsonObject;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.TaskAction;

public class FetchOreProjectTask extends DefaultTask {

	@Input
	public Property<String> apiKey = getProject().getObjects().property(String.class);
	@Input
	public Property<String> projectId = getProject().getObjects().property(String.class);

	public Property<JsonObject> projectData = getProject().getObjects().property(JsonObject.class).value(new JsonObject());

	@TaskAction
	public void fetch() {
		try {
			projectData.set(TaskFunctors.runOreProjectLookup(apiKey.get(), projectId.get()));
		} catch (Throwable t) {
			t.printStackTrace();
			projectData.set(new JsonObject());
			throw new StopExecutionException(t.getMessage());
		}
	}

}

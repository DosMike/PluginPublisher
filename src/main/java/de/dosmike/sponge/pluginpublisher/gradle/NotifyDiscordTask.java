package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.DiscordConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class NotifyDiscordTask extends DefaultTask {

    @Input
    public Property<String> webHookUrl = getProject().getObjects().property(String.class);
    @Input
    public Property<String> messageHeader = getProject().getObjects().property(String.class);
    @Input
    public Property<String> messageBody = getProject().getObjects().property(String.class);

    @TaskAction
    public void publish() throws TaskRunException {
        DiscordConfiguration configuration = new DiscordConfiguration();
        configuration.setWebHook(webHookUrl.get());
        configuration.setHeader(messageHeader.getOrNull());
        configuration.setDescription(messageBody.get());

        if (!configuration.usable())
            throw new TaskRunException("The task was not configured");
        TaskFunctors.runDiscordTask(configuration);
    }

}

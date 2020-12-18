package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.DiscordBotConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class NotifyDiscordBotTask extends DefaultTask {

    @Input
    public Property<String> apiKey = getProject().getObjects().property(String.class);
    @Input
    public Property<String> guildId = getProject().getObjects().property(String.class);
    @Input
    public Property<String> channelId = getProject().getObjects().property(String.class);
    @Input
    public Property<String> messageHeader = getProject().getObjects().property(String.class);
    @Input
    public Property<String> messageBody = getProject().getObjects().property(String.class);

    @TaskAction
    public void publish() throws TaskRunException {
        DiscordBotConfiguration configuration = new DiscordBotConfiguration();
        configuration.setApiKey(apiKey.get());
        configuration.setServer(guildId.get());
        configuration.setChannel(channelId.get());
        configuration.setHeader(messageHeader.getOrNull());
        configuration.setChannel(messageBody.get());

        if (!configuration.usable())
            throw new TaskRunException("The task was not configured");
        TaskFunctors.runDiscordTask(configuration);
    }

}

package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.OreConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class PublishToOreTask extends DefaultTask {

    /**
     * Please make sure your token has the <tt>create_version</tt> permission
     */
    @Input
    public Property<String> apiKey = getProject().getObjects().property(String.class);
    /**
     * Optional, defaults to <tt>Release</tt>
     */
    @Input
    public Property<String> channel = getProject().getObjects().property(String.class);
    /**
     * Optional, defaults to <tt>true</tt>
     */
    @Input
    public Property<Boolean> createForumPost = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<String> messageBody = getProject().getObjects().property(String.class);
    @Input
    public Property<Object> uploadAsset = getProject().getObjects().property(Object.class);

    @TaskAction
    public void publish() throws TaskRunException {
        OreConfiguration configuration = new OreConfiguration();
        configuration.setApiKey(apiKey.get());
        configuration.setChannel(channel.getOrElse("Release"));
        configuration.setCreateForumPost(createForumPost.getOrElse(true));
        configuration.setDescription(messageBody.get());
        configuration.setAsset(uploadAsset.get());

        if (!configuration.usable())
            throw new TaskRunException("The task was not configured");
        TaskFunctors.runOreTask(configuration, object -> getProject().files(object));
    }

}

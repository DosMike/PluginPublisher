package de.dosmike.sponge.pluginpublisher.gradle;

import de.dosmike.sponge.pluginpublisher.tasks.GitConfiguration;
import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class PublishToGitTask extends DefaultTask {

    @Input
    public Property<String> apiKey = getProject().getObjects().property(String.class);
    /**
     * You GitHub Project Namespace, consists of <tt>UserName/ProjectSlug</tt>
     */
    @Input
    public Property<String> gitSlug = getProject().getObjects().property(String.class);
    /**
     * This is the short version or Tag Version
     */
    @Input
    public Property<String> versionTag = getProject().getObjects().property(String.class);
    /**
     * This is the long version or Release Title
     */
    @Input
    public Property<String> versionName = getProject().getObjects().property(String.class);
    /**
     * Optional, defaults to <tt>master</tt>
     */
    @Input
    public Property<String> commitish = getProject().getObjects().property(String.class);
    @Input
    public Property<String> messageBody = getProject().getObjects().property(String.class);
    @Input
    public Property<Object> uploadAssets = getProject().getObjects().property(Object.class);

    @TaskAction
    public void publish() throws TaskRunException {
        GitConfiguration configuration = new GitConfiguration();
        configuration.setApiKey(apiKey.get());
        configuration.setSlug(gitSlug.get());
        configuration.setTag(versionTag.get());
        configuration.setTagFull(versionName.get());
        configuration.setCommitish(commitish.getOrElse("master"));
        configuration.setDescription(messageBody.get());
        configuration.setAssets(uploadAssets.get());

        if (!configuration.usable())
            throw new TaskRunException("The task was not configured");
        TaskFunctors.runGitTask(configuration, object -> getProject().files(object));
    }

}

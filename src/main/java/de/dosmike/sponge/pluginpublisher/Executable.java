package de.dosmike.sponge.pluginpublisher;

import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Version(value = "1.1.1", build = "20121701")
public class Executable {

    public static void main(String[] args) {
        try {
            Arguments.parse(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Arguments.printHelp();
            System.exit(1);
            return;
        }

        try {
            runFromArguments();
        } catch (TaskRunException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.exit(0); //Is JDA lingering?
    }

    public static void runFromArguments() throws TaskRunException {

        if (Arguments.useDiscordBot() && Arguments.useDiscordHook()) {
            throw new TaskRunException("Discord WebHook and Bot Configuration specified!");
        }

        if (Arguments.useGitHub())
            TaskFunctors.runGitTask(Arguments.git, (strings -> Arrays.stream(strings).map(File::new).collect(Collectors.toSet())));

        if (Arguments.useOre())
            TaskFunctors.runOreTask(Arguments.ore, File::new);

        if (Arguments.useDiscordHook()) {
            TaskFunctors.runDiscordTask(Arguments.discord);
        } else if (Arguments.useDiscordBot()) {
            TaskFunctors.runDiscordTask(Arguments.discordBot);
            TaskFunctors.terminateJDA();
        }

    }

}

package de.dosmike.sponge.pluginpublisher;

import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Version(value = "1.1.2", build = "20121801")
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
            TaskFunctors.runGitTask(Arguments.git, Executable::toFiles);

        if (Arguments.useOre())
            TaskFunctors.runOreTask(Arguments.ore, Executable::toFiles);

        if (Arguments.useDiscordHook()) {
            TaskFunctors.runDiscordTask(Arguments.discord);
        } else if (Arguments.useDiscordBot()) {
            TaskFunctors.runDiscordTask(Arguments.discordBot);
            TaskFunctors.terminateJDA();
        }

    }

    private static List<File> toFiles(Object object) {
        if (object.getClass().isArray()) {
            return Arrays.stream(((Object[]) object)).map(x -> new File(x.toString())).collect(Collectors.toList());
        } else {
            return Collections.singletonList(new File(object.toString()));
        }
    }

}

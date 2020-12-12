package de.dosmike.sponge.pluginpublisher;

import de.dosmike.sponge.pluginpublisher.tasks.TaskFunctors;
import de.dosmike.sponge.pluginpublisher.tasks.TaskRunException;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Version(value="1.0.2", build="20030701")
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
        if (Arguments.useGitHub())
            TaskFunctors.runGitTask(Arguments.git, (strings -> Arrays.stream(strings).map(File::new).collect(Collectors.toSet())));

        if (Arguments.useOre())
            TaskFunctors.runOreTask(Arguments.ore, File::new);

        if (Arguments.useDiscord()) {
            TaskFunctors.runDiscordTask(Arguments.discord);
            TaskFunctors.terminateJDA();
        }
    }

}

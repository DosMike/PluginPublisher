package de.dosmike.sponge.pluginpublisher;

import de.dosmike.sponge.pluginpublisher.tasks.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Arguments {

	static GitConfiguration git;
	static OreConfiguration ore;
	static DiscordBotConfiguration discordBot;
	static DiscordConfiguration discord;

	public static boolean useGitHub() {
		return git.usable();
	}

	public static boolean useOre() {
		return ore.usable();
	}

	public static boolean useDiscordBot() {
		return discordBot.usable();
	}

	public static boolean useDiscordHook() {
		return discord.usable();
	}

	/** @throws IllegalArgumentException if args are critically invalid */
	/**
	 * @throws java.io.FileNotFoundException if args specifies a non-existent asset
	 */
	public static void parse(String[] args) throws IllegalArgumentException, FileNotFoundException {

		List<String> gitAssetsTmp = new LinkedList<>();
		String releaseDescriptionString = null;

	    for (int i = 0; i < args.length; i += 2) {
		    switch (args[i]) {
			    case "--gk":
				    if (System.getenv(args[i + 1]) == null)
					    throw new IllegalArgumentException("The environment variable --gk " + args[i + 1] + " is missing");
				    git.setApiKey(System.getenv(args[i + 1]));
				    break;
			    case "--gs":
				    git.setSlug(args[i + 1]);
				    break;
			    case "--gt":
				    git.setTag(args[i + 1]);
				    break;
			    case "--gn":
				    git.setTagFull(args[i + 1]);
				    break;
                case "--gc":
	                git.setCommitish(args[i + 1]);
	                break;
			    case "--ga": {
				    Path path = Paths.get(args[i + 1]);
				    if (Files.notExists(path))
					    throw new FileNotFoundException("Could not find Git asset: " + args[i + 1]);
				    gitAssetsTmp.add(args[i + 1]);
				    break;
			    }
			    case "--ok":
				    if (System.getenv(args[i + 1]) == null)
					    throw new IllegalArgumentException("The environment variable --ok " + args[i + 1] + " is missing");
				    ore.setApiKey(System.getenv(args[i + 1]));
				    break;
			    case "--op":
				    ore.setProject(args[i + 1]);
				    break;
			    case "--oc":
				    ore.setChannel(args[i + 1]);
				    break;
			    case "--oa": {
				    Path path = Paths.get(args[i + 1]);
				    if (Files.notExists(path))
					    throw new FileNotFoundException("Could not find Ore asset: " + args[i + 1]);
				    ore.setAsset(args[i + 1]);
				    break;
			    }
			    case "--dw":
				    if (System.getenv(args[i + 1]) == null)
					    throw new IllegalArgumentException("The environment variable --dw " + args[i + 1] + " is missing");
				    discord.setWebHook(System.getenv(args[i + 1]));
				    break;
			    case "--dk":
				    if (System.getenv(args[i + 1]) == null)
					    throw new IllegalArgumentException("The environment variable --dk " + args[i + 1] + " is missing");
				    discordBot.setApiKey(System.getenv(args[i + 1]));
				    break;
			    case "--ds":
				    discordBot.setServer(args[i + 1]);
				    break;
			    case "--dc":
				    discordBot.setChannel(args[i + 1]);
				    break;
			    case "--dh":
				    discord.setHeader(args[i + 1]);
				    discordBot.setHeader(args[i + 1]);
				    break;
			    case "--desc": {
				    Path descFile = Paths.get(args[i + 1]);
				    if (Files.notExists(descFile))
					    throw new FileNotFoundException("Could not find the description file: " + args[i + 1]);
				    try {
					    releaseDescriptionString = String.join("\n", Files.readAllLines(descFile));
				    } catch (IOException e) {
					    throw new IllegalArgumentException("Could not read the description file", e);
				    }
                }
			    case "-?":
			    case "--help":
			    case "/?":
				    printHelp();
				    System.exit(0);
			    default:
				    throw new IllegalArgumentException("Unsupported Argument: " + args[i]);
		    }
	    }

		if (!gitAssetsTmp.isEmpty())
			git.setAssets(gitAssetsTmp.toArray(new String[0]));
		releaseDescriptionString = TaskFunctors.validateReleaseDescription(releaseDescriptionString, false);
		git.setDescription(releaseDescriptionString);
		ore.setDescription(releaseDescriptionString);
		discordBot.setDescription(releaseDescriptionString);
    }

    public static void printHelp() {
	    Version version = Executable.class.getAnnotation(Version.class);
	    System.out.println(" Usage: java -jar this.jar -FLAGS");
	    System.out.println("You are using Version " + version.value() + " (build " + version.build() + ")");
	    System.out.println("This application will create a new release on (optionally) GitHub, Sponge and");
	    System.out.println("notify a Discord server. Sorry, GitLab is currently not supported (PRs are");
	    System.out.println("wellcome)");
	    System.out.println("Note that for Discord you can only use WebHooks OR a bot. Use Hooks with --dw");
	    System.out.println("or --dk,--ds,--dc for a discord bot.");
	    System.out.println("  -?, --help or /?  - Interrupt Arg parsing to display this help message.");
	    System.out.println("  --gk <API Key>    - Your personal auth token for GitHub. Make sure it has the");
	    System.out.println("                      repo scope. This is the only required scope.");
	    System.out.println("  --gs <Git Slug>   - The combination of UserName/ProjectName. This is");
	    System.out.println("                      basically the personal part of your GitHub Project URL, ");
	    System.out.println("                      required to create in the Release API endpoint");
	    System.out.println("  --gc <Commitish>  - The commitish this Release is based on. This is optional!");
	    System.out.println("                      The defailt value is 'master'.");
	    System.out.println("  --gt <Git Tag>    - Short Tag Name for GitHub");
	    System.out.println("  --gn <Git Name>   - GitHub calls this Release Title, it's the full Tag Name");
	    System.out.println("  --ga <File>       - Specify an asset to upload to GitHub. These are optional.");
	    System.out.println("  --ok <API key>    - The name of an environment variable that holds your API");
	    System.out.println("                      key. The key must have the permission create_version in");
	    System.out.println("                      the project specified by --op.");
	    System.out.println("                      This is done to keep your API-Key out of build scripts.");
	    System.out.println("  --op <PluginID>   - The Plugin ID used required to create versions with the");
	    System.out.println("                      Ore API");
	    System.out.println("  --oa <File>       - The file to upload to Ore for this release. Has to be a");
	    System.out.println("                      .jar File");
	    System.out.println("  --oc <Channel>    - Specify the release channel for this version. Optional,");
	    System.out.println("                      will default to 'Release'");
	    System.out.println("  --dw <WebHook>    - The name of an environment variable that holds the URL");
	    System.out.println("                      to the Discord WebHook.");
	    System.out.println("  --dk <API key>    - The name of an environment variable that holds your");
	    System.out.println("                      Discord API key. The key must have permission to write");
	    System.out.println("                      Messages in your servers");
	    System.out.println("  --ds <ServerID>   - The id of the server to post in");
	    System.out.println("  --dc <ChannelID>  - The channel id to post in");
	    System.out.println("  --dh <String>     - An additional line of text that will be prepended to the");
	    System.out.println("                      description, allowing you to @mention roles and users.");
	    System.out.println("                      This is optional. If omitted only the description will be");
	    System.out.println("                      used.");
	    System.out.println("  --desc <File>     - Specify a MarkDown file that holds the description for");
	    System.out.println("                      this release. This is optional - A prompt will open, if");
	    System.out.println("                      no file was specified. The same text will be posted on");
        System.out.println("                      all specified release targets (including Discord)");
    }

}

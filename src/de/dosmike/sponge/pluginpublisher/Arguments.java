package de.dosmike.sponge.pluginpublisher;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Arguments {

    public static String gitAPIKey = null;
    public static String gitSlug = null;
    public static String gitTag = null;
    public static String gitTagFull = null;
    public static String gitCommitish = "master";
    public static List<Path> gitAssets = new LinkedList<>();
    public static String oreProject = null;
    public static String oreChannel = "Release";
    public static String oreAPIKey = null;
    public static Path oreAsset = null;
    public static String discordAPIKey = null;
    public static String discordServer = null;
    public static String discordChannel = null;
    public static String discordHeader = null;
    public static String releaseDescriptionString; // user input if no file was specified

    public static boolean useGitHub() {
        return gitAPIKey != null && gitSlug != null && gitTag != null;
    }
    public static boolean useOre() {
        return oreAPIKey != null && oreProject != null && oreAsset != null;
    }
    public static boolean useDiscord() {
        return discordAPIKey != null && discordServer != null && discordChannel != null;
    }

    /** @throws IllegalArgumentException if args are critically invalid */
    /** @throws java.io.FileNotFoundException if args specifies a non-existent asset */
    public static void parse(String[] args) throws IllegalArgumentException, FileNotFoundException {
        for (int i = 0; i < args.length; i+=2) {
            switch (args[i]) {
                case "--gk":
                    gitAPIKey = args[i+1];
                    if (System.getenv(gitAPIKey)==null) throw new IllegalArgumentException("The environment variable --gk "+gitAPIKey+" is missing");
                    break;
                case "--gs":
                    gitSlug = args[i+1];
                    break;
                case "--gt":
                    gitTag = args[i+1];
                    break;
                case "--gn":
                    gitTagFull = args[i+1];
                    break;
                case "--gc":
                    gitCommitish = args[i+1];
                    break;
                case "--ga": {
                    Path path = Paths.get(args[i + 1]);
                    if (Files.notExists(path))
                        throw new FileNotFoundException("Could not find Git asset: " + args[i + 1]);
                    gitAssets.add(path);
                    break;
                }
                case "--ok":
                    oreAPIKey = args[i+1];
                    if (System.getenv(oreAPIKey)==null) throw new IllegalArgumentException("The environment variable --ok "+oreAPIKey+" is missing");
                    break;
                case "--op":
                    oreProject = args[i+1];
                    break;
                case "--oc":
                    oreChannel = args[i+1];
                    break;
                case "--oa": {
                    Path path = Paths.get(args[i + 1]);
                    if (Files.notExists(path))
                        throw new FileNotFoundException("Could not find Ore asset: "+ args[i + 1]);
                    oreAsset = path;
                    break;
                }
                case "--dk":
                    discordAPIKey = args[i+1];
                    if (System.getenv(discordAPIKey)==null) throw new IllegalArgumentException("The environment variable --dk "+discordAPIKey+" is missing");
                    break;
                case "--ds":
                    discordServer = args[i+1];
                    break;
                case "--dc":
                    discordChannel = args[i+1];
                    break;
                case "--dh":
                    discordHeader = args[i+1];
                    break;
                case "--desc": {
                    Path descFile = Paths.get(args[i+1]);
                    if (Files.notExists(descFile))
                        throw new FileNotFoundException("Could not find the description file: "+args[i+1]);
                    try {
                        releaseDescriptionString = String.join("\n",Files.readAllLines(descFile));
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
                    throw new IllegalArgumentException("Unsupported Argument: "+args[i]);
            }
        }
        //validate args read
        if (gitTagFull == null && gitTag != null) gitTagFull = "Automatic Release "+gitTag;
        if (gitAPIKey != null || gitTag != null || gitSlug != null) {
            if (gitAPIKey == null)
                throw new IllegalArgumentException("Missing argument --gk");
            if (gitTag == null)
                throw new IllegalArgumentException("Missing argument --gt");
            if (gitSlug == null)
                throw new IllegalArgumentException("Missing argument --gs");
        }
        if (oreAPIKey != null || oreProject != null || oreAsset != null) {
            if (oreAPIKey == null)
                throw new IllegalArgumentException("Missing argument --ok");
            if (oreProject == null)
                throw new IllegalArgumentException("Missing argument --op");
            if (oreAsset == null)
                throw new IllegalArgumentException("Missing argument --oa");
        }
        if (discordAPIKey != null || discordServer != null || discordChannel != null) {
            if (discordAPIKey == null)
                throw new IllegalArgumentException("Missing argument --dk");
            if (discordServer == null)
                throw new IllegalArgumentException("Missing argument --ds");
            if (discordChannel == null)
                throw new IllegalArgumentException("Missing argument --dc");
        }
        if (releaseDescriptionString == null) {
            if (Desktop.isDesktopSupported()) {
                TextInputPrompt prompt = new TextInputPrompt("Release Description", "<html>Please enter the Release description.<br>The text you input supports the Markdown syntax");
                prompt.waitInput();
                releaseDescriptionString = prompt.getResult();
                if (releaseDescriptionString == null)
                    //The application was cancelled orderly
                    System.exit(0);
            } else {
                System.out.println("End input with <new line>.<new line> (like E-Mails)");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    String line;
                    boolean newline = false;
                    StringBuilder builder = new StringBuilder();
                    while (!".".equals(line = reader.readLine()) && line != null) {
                        if (newline) builder.append("\n");
                        else newline = true;
                        builder.append(line);
                    }
                    releaseDescriptionString = builder.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } //unlikely?
            }
            releaseDescriptionString = releaseDescriptionString.trim();
            if (releaseDescriptionString.isEmpty())
                throw new IllegalArgumentException("The release description is not allowed to be empty");
        }
    }

    public static void printHelp() {
        Version version = Executable.class.getAnnotation(Version.class);
        System.out.println(" Usage: java -jar this.jar -FLAGS");
        System.out.println("You are using Version "+version.value()+" (build "+version.build()+")");
        System.out.println("This application will create a new release on (optionally) GitHub, Sponge and");
        System.out.println("notify a Discord server. Sorry, GitLab is currently not supported (PRs are");
        System.out.println("wellcome)");
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

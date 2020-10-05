package de.dosmike.sponge.pluginpublisher;

import com.google.gson.JsonObject;
import de.dosmike.sponge.github.ReleaseAPI;
import de.dosmike.sponge.oreapi.OreApiV2;
import de.dosmike.sponge.oreapi.v2.OreDeployVersionInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Version(value="1.0.2", build="20030701")
public class Executable {

    public static void main(String[] args) {
        try {
            Arguments.parse(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Arguments.printHelp();
            System.exit(1); return;
        }

        if (Arguments.useGitHub()) try {
            System.out.println("[ GitHub ] Creating Tag...");
            ReleaseAPI githubRelease = new ReleaseAPI();
            JsonObject releaseObject = githubRelease.createReleaseTag(Arguments.gitSlug, Arguments.gitTag, Arguments.gitCommitish, Arguments.gitTagFull, Arguments.releaseDescriptionString, false, false);
            String uploadHere = releaseObject.get("upload_url").getAsString();
            for (Path path : Arguments.gitAssets) {
                System.out.println("[ GitHub ] Uploading asset "+path.getFileName().toString()+"...");
                githubRelease.uploadAsset(uploadHere, path);
            }
        } catch (RuntimeException e) { System.exit(1); return; }

        if (Arguments.useOre()){
            OreApiV2 oreApi = null;
            try { //ore can create new sessions without visual prompt, so i'll just create a new one
                oreApi = new OreApiV2(System.getenv(Arguments.oreAPIKey));
                OreDeployVersionInfo info = OreDeployVersionInfo.builder()
                        .setCreateForumPost(true)
                        .setDescription(Arguments.releaseDescriptionString)
                        .setChannel(Arguments.oreChannel)
                        .build();
                System.out.println("[ Ore ] Creating version...");
                if (!oreApi.createVersion(Arguments.oreProject, info, Arguments.oreAsset).isPresent()) {
                    System.err.println("Could not create version");
                    System.exit(1); return;
                }
            }
            finally {
                if (oreApi!=null) oreApi.destroySession();
            }
        }

        if (Arguments.useDiscord()) {
            JDA jda = null;
            try {
                jda = new JDABuilder(System.getenv(Arguments.discordAPIKey))
                        .setDisabledCacheFlags(EnumSet.of(CacheFlag.EMOTE, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)) //i don't care about that stuff
                        .build();
                jda.awaitReady();
            } catch (LoginException|InterruptedException e) {
                System.err.println("Could not connect with discord ");
                System.exit(1); return;
            }

            try {
                Guild guild = jda.getGuildById(Arguments.discordServer);
                if (guild == null) {
                    System.err.println("Discord guild could not be found. Is the bot not on the server?");
                    System.exit(1);
                    return;
                }
                TextChannel channel = guild.getTextChannelById(Arguments.discordChannel);
                if (channel == null) {
                    System.err.println("Could not find a channel with the specified id");
                    System.exit(1);
                    return;
                }
                if (!channel.canTalk()) {
                    System.err.println("No permissions to read/write in this channel");
                    System.exit(1);
                    return;
                }
                String header = Arguments.discordHeader;
                if (header != null) { //parse @mentions for the header
                    Pattern mentionPattern = Pattern.compile("(?<![^\\s])@([\\w-]+)");
                    Matcher mentionMatch = mentionPattern.matcher(header);
                    while (mentionMatch.find()) {
                        String typedName = mentionMatch.group(1);
                        List<Role> roles = guild.getRolesByName(typedName, true);
                        String rep = mentionMatch.group(0);
                        if (roles.size() > 0) rep = roles.get(0).getAsMention();
                        guild.getRolesByName(mentionMatch.group(1), true).stream()
                                .findFirst().map(IMentionable::getAsMention).orElseGet(() ->
                                guild.getMembersByName(typedName, false).stream()
                                        .findFirst().map(IMentionable::getAsMention).orElse(typedName)
                        );
                        header = mentionMatch.replaceFirst(rep);
                        mentionMatch = mentionPattern.matcher(header);
                    }
                }
                String fullMessage = (header != null) ? (header + "\n" + Arguments.releaseDescriptionString) : Arguments.releaseDescriptionString;
                channel.sendMessage(fullMessage).complete();
            } finally {
                jda.shutdownNow();
                while (!jda.getStatus().equals(JDA.Status.SHUTDOWN)) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore) { }
                }
                System.out.println("JDA shutdown");
            }
        }

        System.exit(0); //Is JDA lingering?
    }

}

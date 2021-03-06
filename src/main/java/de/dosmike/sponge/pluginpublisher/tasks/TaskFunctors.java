package de.dosmike.sponge.pluginpublisher.tasks;

import com.google.gson.JsonObject;
import de.dosmike.sponge.github.ReleaseAPI;
import de.dosmike.sponge.oreapi.OreApiV2;
import de.dosmike.sponge.oreapi.v2.OreDeployVersionInfo;
import de.dosmike.sponge.pluginpublisher.Statics;
import de.dosmike.sponge.pluginpublisher.TextInputPrompt;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.net.ssl.HttpsURLConnection;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskFunctors {

	/**
	 * Validate the passed in description.
	 * If no description is present and this function runs interactive, a input dialog will be shown
	 * or input will be asked for through std in if system is headless.
	 *
	 * @param description          the current description (if any)
	 * @param throwDescriptionNull set true to allow interactive input
	 * @return the validated description
	 * @throws IllegalArgumentException if description is null/empty and throwDescriptionNull is true
	 * @throws NoSuchElementException   if the description input was cancelled by the user
	 */
	public static String validateReleaseDescription(String description, boolean throwDescriptionNull) {
		if (description == null || description.isEmpty()) {
			if (throwDescriptionNull)
				throw new IllegalArgumentException("Missing Description");
			if (Desktop.isDesktopSupported()) {
				TextInputPrompt prompt = new TextInputPrompt("Release Description", "<html>Please enter the Release description.<br>The text you input supports the Markdown syntax");
				prompt.waitInput();
				description = prompt.getResult();
				if (description == null)
					throw new NoSuchElementException("Input dialog for description was cancelled");
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
					description = builder.toString();
				} catch (IOException e) {
					throw new RuntimeException(e);
				} //unlikely?
			}
			description = description.trim();
			if (description.isEmpty())
				throw new IllegalArgumentException("The release description is not allowed to be empty");
		}
		return description;
	}

	public static void runGitTask(GitConfiguration git, Function<Object, Iterable<File>> fileResolver) throws TaskRunException {
		git.validate();
		try {
			System.out.println("[ GitHub ] Creating Tag...");
			Iterable<File> resolved = fileResolver.apply(git.assets);
			ReleaseAPI githubRelease = new ReleaseAPI();
			JsonObject releaseObject = githubRelease.createReleaseTag(git.apiKey, git.slug, git.tag, git.commitish, git.tagFull, git.description, false, false);
			String uploadHere = releaseObject.get("upload_url").getAsString();
			for (File file : resolved) {
				System.out.println("[ GitHub ] Uploading asset " + file.getName() + "...");
				githubRelease.uploadAsset(git.apiKey, uploadHere, file.toPath());
			}
		} catch (Throwable t) {
			throw new TaskRunException("Failed to create GitHub release", t);
		}
	}

	public static void runOreTask(OreConfiguration ore, Function<Object, Iterable<File>> fileResolver) throws TaskRunException {
		ore.validate();
		OreApiV2 oreApi = null;
		try { //ore can create new sessions without visual prompt, so i'll just create a new one
			System.out.println("[ Ore ] Creating version...");
			Path resolved = fileResolver.apply(ore.asset).iterator().next().toPath();
			oreApi = new OreApiV2(ore.apiKey);
			OreDeployVersionInfo info = OreDeployVersionInfo.builder()
					.setCreateForumPost(ore.createForumPost)
					.setDescription(ore.description)
					.setChannel(ore.channel)
					.build();
			if (!oreApi.createVersion(ore.project, info, resolved))
				throw new TaskRunException("Failed to create Ore release");
		} finally {
			if (oreApi != null) oreApi.destroySession();
		}
	}

	public static JsonObject runOreProjectLookup(String oreApiKey, String projectId) throws TaskRunException {
		if (projectId == null)
			throw new NullPointerException("Plugin id to look up was null");
		OreApiV2 oreApi = null;
		try {
			oreApi = new OreApiV2(oreApiKey);
			return oreApi.getProjectById(projectId);
		} catch (Throwable t) {
			throw new TaskRunException("Could not fetch plugin metadata", t);
		} finally {
			if (oreApi != null) oreApi.destroySession();
		}
	}

	private static JDA jda = null;
	private static String activeToken = null;

	public static void runDiscordTask(DiscordConfiguration discord) throws TaskRunException {
		discord.validate();

		String fullMessage = (discord.header != null) ? (discord.header + "\n" + discord.description) : discord.description;
		JsonObject payload = new JsonObject();
		payload.addProperty("content", fullMessage);
		HttpsURLConnection con = null;
		try {
			con = (HttpsURLConnection) new URL(discord.getWebHook()).openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("User-Agent", Statics.USER_AGENT);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
			bw.write(payload.toString());
			bw.flush();
			bw.close();
			if (con.getResponseCode() < 200 || con.getResponseCode() >= 400)
				throw new TaskRunException("Discord API refused the WebHook: " + con.getResponseCode() + " " + con.getResponseMessage());

		} catch (IOException e) {
			throw new TaskRunException("The Discord WebHook seems to be broken", e);
		} finally {
			if (con != null) con.disconnect();
		}
	}

	public static void runDiscordTask(DiscordBotConfiguration discord) throws TaskRunException {
		discord.validate();

		if (jda == null || !activeToken.equals(discord.getApiKey())) {
			if (jda != null) {
				terminateJDA();
			}
			try {
				jda = new JDABuilder(activeToken = discord.apiKey)
						.setDisabledCacheFlags(EnumSet.of(CacheFlag.EMOTE, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)) //i don't care about that stuff
						.build();
				jda.awaitReady();
			} catch (LoginException | InterruptedException e) {
				throw new TaskRunException("Could not connect to discord (Check API Key and permissions)", e);
			}
		}

		Guild guild = jda.getGuildById(discord.server);
		if (guild == null) {
			throw new TaskRunException("Discord guild could not be found. Is the bot not on the server?");
		}
		TextChannel channel = guild.getTextChannelById(discord.channel);
		if (channel == null) {
			throw new TaskRunException("Could not find a channel with the specified id");
		}
		if (!channel.canTalk()) {
			throw new TaskRunException("No permissions to read/write in this channel");
		}
		String header = discord.header;
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
		String fullMessage = (header != null) ? (header + "\n" + discord.description) : discord.description;
		channel.sendMessage(fullMessage).complete();

	}

	public static void terminateJDA() {
		if (jda == null) return;
		jda.shutdown();
		while (!jda.getStatus().equals(JDA.Status.SHUTDOWN)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignore) {
				break;
			}
		}
		System.out.println("JDA shutdown");
	}

}

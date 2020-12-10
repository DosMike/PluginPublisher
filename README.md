# Sponge Plugin Publisher

This tool is mean for Continuous Deployment of Sponge Plugins on GitHub and Ore with automatic Discord notifications.

Each component is optional an can be skipped.

```
 Usage: java -jar this.jar -FLAGS
This application will create a new release on (optionally) GitHub, Sponge and
notify a Discord server. Sorry, GitLab is currently not supported (PRs are
wellcome)
  -?, --help or /?  - Interrupt Arg parsing to display this help message.
  --gk <API Key>    - Your personal auth token for GitHub. Make sure it has the
                      repo scope. This is the only required scope.
  --gs <Git Slug>   - The combination of UserName/ProjectName. This is
                      basically the personal part of your GitHub Project URL, 
                      required to create in the Release API endpoint
  --gc <Commitish>  - The commitish this Release is based on. This is optional!
                      The defailt value is 'master'.
  --gt <Git Tag>    - Short Tag Name for GitHub
  --gn <Git Name>   - GitHub calls this Release Title, it's the full Tag Name
  --ga <File>       - Specify an asset to upload to GitHub. These are optional.
  --ok <API key>    - The name of an environment variable that holds your API
                      key. The key must have the permission create_version in
                      the project specified by --op.
                      This is done to keep your API-Key out of build scripts.
  --op <PluginID>   - The Plugin ID used required to create versions with the
                      Ore API
  --oa <File>       - The file to upload to Ore for this release. Has to be a
                      .jar File
  --oc <Channel>    - Specify the release channel for this version. Optional,
                      will default to 'Release'
  --dk <API key>    - The name of an environment variable that holds your
                      Discord API key. The key must have permission to write
                      Messages in your servers
  --ds <ServerID>   - The id of the server to post in
  --dc <ChannelID>  - The channel id to post in
  --dh <String>     - An additional line of text that will be prepended to the
                      description, allowing you to @mention roles and users.
                      This is optional. If omitted only the description will be
                      used.
  --desc <File>     - Specify a MarkDown file that holds the description for
                      this release. This is optional - A prompt will open, if
                      no file was specified. The same text will be posted on
                      all specified release targets (including Discord)
```

## Setting up GitHub

* Go to https://github.com/settings/tokens and generate a new Token with `repo`
  scope.
* Add the token as Environment variable (either though scripts, process builders or the system itself)
* Enable git usage by specifying `--gk`
* Go to your repository and copy the Slug to `--gs`   
  *Example:* `https://github.com/DosMike/VillagerShops/` -<small>Slug</small>->
  `DosMike/VillagerShops`
* Add the release tag with `--gt` (Plugin version recommended) and a full release name with `--gn`
* You can add as many files as you want to attach to the release by repeating
  `--ga file`

You only need one GitHub API-token for all projects you want to upload with this tool. **Remember that API-tokens should
be treated like passwords!**

## Setting up Ore

* Go to your profile on https://ore.spongepowered.org/ and click the small key next to your name.
* Create a token with the `create_version` permission.
* Now from the right side copy the Key (looks like two UUIDs with a dot in between) and add it as Environment variable (
  either through scripts, process builders or the system itself)
* Enable ore uploads by specifying `--ok`
* Specify the ore project id with `--op`
* Use `--oa` to point to the plugin jar you want to upload

You only need one Ore API-token for all projects you want to upload with this tool. **Remember that API-tokens should be
treated like passwords!**

## Setting up Discord

For Discord integration you'll have to create and invite a bot to your server. You can't use an existing bot because
this application requires the bots API-Token to send messages, and bots don't hand those out. If you do not already run
a custom bot, you can make and invite one, following
[this tutorial](https://discordpy.readthedocs.io/en/latest/discord.html). The required permissions are `Send Messages`
and `Read Message History`. After adding the bot to your server I recommend making it private, since you probably won't
use the bot for anything else.

* Go to https://discordapp.com/developers/applications, click your Application and go to the Bot page.
* After clicking `Click to reveal Token`, copy it and add it as an Environment variable (either through scripts, process
  builders or the system itself).
* Enable the discord notifications by specifying `--dk`
* Specify the guild id (server id) with `--ds` and the channel id with `--dc`
* If you want to mention a role for this release you can add a message with
  `--dh`. This message supports @mentioning roles and users.

If you can't right-click a server or channel to `Copy ID`, go into your Discord settings and enable `Developer Mode`
in `Appearance`.

You only need one Bot API-token for all notifications you want to send with this tool. **Remember that API-tokens should
be treated like passwords!**

## Common release description

GitHub, Ore and Discord all use the same description. You can create a file containing the description and supply it
with `--desc <file>` or ignore the argument and write the description using an interactive prompt. Either way, a
description is necessary.

## As Gradle Plugin

Add the following to your `build.gradle`

```groovy
buildscript {
  repositories {
    maven { url "https://jitpack.io" }
  }
  dependencies {
    classpath 'com.github.dosmike:pluginpublisher:development-SNAPSHOT'
  }
}
apply plugin: 'com.github.dosmike.PluginPublisher'
```

Configure the plugin with the PluginPublisher config

```groovy
PluginPublisher {
  oreAPIKey = !_readFromFile_ !;
  oreAsset = Paths.get("build\\libs\\${jar.archiveFileName}")
  oreChannel = "Release"
  oreProject = pluginid
  gitAPIKey = !_readFromFile_ !;
  gitAssets = Paths.get("build\\libs\\${jar.archiveFileName}")
  gitCommitish = "master"
  gitSlug = "DosMike/VillagerShops"
  gitTag = project.version
  gitTagFull = "Release Build " + project.version
  discordAPIKey = !_readFromFile_ !;
  discordServer = "123456789" //id as string
  discordChannel = "123456789" //id as string
  //role mentions are formatted like this: <@&roleID>
  discordHeader = "<@&123456789> A new version for ${project.name} just released:"
  description = "This message is used for git, ore and discord, but only git and ore support markdown"
}
```

and finally call the PublishPlugin task

## Example in a script:

**Full exmaple:**

`
java -jar PluginPublisher.jar --gk gitApiKey --gs DosMike/VillagerShops --gc master --gt 2.4 --gn "Release Build 2.4" --ga VillagerShops-2.4.zip --ok oreApiKey --op vshop --oc Release --oa VillagerShops-2.4.jar --dk discordApiKey --ds 342942444288999435 --dc 352760019873169408 --dh "@VillagerShops Version 2.4 released on Ore and GitHub" --desc ReleaseNotes-2.4.md
`  
This example should run fully automatic.

**Minimal example:**

`
java -jar PluginPublisher.jar --gk gitApiKey --gs DosMike/VillagerShops --gt 2.4 --ok oreApiKey --op vshop --oa VillagerShops-2.4.jar --dk discordApiKey --ds 342942444288999435 --dc 352760019873169408
`  
This example will require user input because --desc was not specified. A new window will open with a small text input
area. If you cancel the input nothing will happen.

**Gradle task example:**

```groovy
task zPublish(type: Exec, group: '_Plugin', dependsOn: uberJar) {
  file('..\\PluginPublisher\\.apikeys').readLines().each() {
    def (key, value) = it.tokenize('=')
    environment key, value
  }
  def spp_git_slug = 'DosMike/VillagerShops'
  def spp_discord_server = '342942444288999435'
  def spp_discord_channel = '352760019873169408'
  def spp_discord_mention = '<@&644225680833249320>'
  def spp_discord_header = "${spp_discord_mention} Version ${version} released on Ore and GitHub"
  def outputFile = "build\\libs\\${jar.archiveName}"
  commandLine 'java', '-jar', '..\\PluginPublisher\\PluginPublisher.jar',
          '--gk', 'gitkey', '--ok', 'orekey', '--dk', 'discordkey',
          '--gs', spp_git_slug, '--gt', version, '--gn', "Release Build ${version}", '--ga', outputFile,
          '--op', pluginid, '--oa', outputFile,
          '--ds', spp_discord_server, '--dc', spp_discord_channel, '--dh', spp_discord_header
}
```

# License

### This Project is MIT Licensed

> Copyright 2020 DosMike
>
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# Libraries

## [Java Discord API](https://github.com/DV8FromTheWorld/JDA/)

> Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
> > http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.

## [Log4j 2](https://logging.apache.org/log4j/2.x/)

> Copyright 1999-2005 The Apache Software Foundation
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
> > http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
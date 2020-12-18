package de.dosmike.sponge.pluginpublisher;

/**
 * Values used at multiple places, lazily patched in
 */
public class Statics {

    public static String USER_AGENT;

    static {
        Version v = Executable.class.getAnnotation(Version.class);
        USER_AGENT = "PluginPublisher/" + v.value() + " (by DosMike#4103 aka github.com/DosMike)";
    }

}

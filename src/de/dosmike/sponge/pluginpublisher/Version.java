package de.dosmike.sponge.pluginpublisher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** I just want the version to be written at the class def of Executable.
 * Makes it easier for me to edit later */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Version {
    String value();
    String build();
}

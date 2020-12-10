package de.dosmike.sponge.utils.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Supports version in the format major[.minor[.build[.revision[-[stage][patch]]
 * where stage is sorted alphabetically (alpha, beta, rc, release).<br>
 * you may use a underscore instead of the dash and another optional underscore/dash between stage and patch.<br>
 * sort priority is left to right. */
public class Version implements Comparable<Version> {

    private int[] n;
    private String s;

    // difference to maven version: can optionally start with 'v', dash after revision is optional
    private static final Pattern versionPattern = Pattern.compile("(?:[Vv]?\\s*)?(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?)?(?:[-_]?([a-zA-Z]+)?[-_]?(\\d+)?)?");
    public Version (String version) {
        Matcher m = versionPattern.matcher(version);
        if (!m.matches())
            throw new IllegalArgumentException("Invalid version string: "+version);
        String major = m.group(1);
        String minor = m.group(2);
        String build = m.group(3);
        String revis = m.group(4);
        String stage = m.group(5);
        String patch = m.group(6);
        n = new int[]{
                Integer.parseInt(major),
                minor == null ? -1 : Integer.parseInt(minor),
                build == null ? -1 : Integer.parseInt(build),
                revis == null ? -1 : Integer.parseInt(revis),
                patch == null ? -1 : Integer.parseInt(patch)
        };
        s = stage==null?"":stage;
    }

    /** @return <i><b>Major</b></i>.Minor.Build.Revision-StagePatch */
    public int getMajor() {
        return n[0];
    }
    /** @return Major.<i><b>Minor</b></i>.Build.Revision-StagePatch or -1 if not set */
    public int getMinor() {
        return n[1];
    }
    /** @return Major.Minor.<i><b>Build</b></i>.Revision-StagePatch or -1 if not set */
    public int getBuild() {
        return n[2];
    }
    /** @return Major.Minor.Build.<i><b>Revision</b></i>-StagePatch or -1 if not set */
    public int getRevision() {
        return n[3];
    }
    /** @return Major.Minor.Build.Revision-Stage<i><b>Patch</b></i> or -1 if not set */
    public int getPatch() {
        return n[4];
    }
    /** @return Major.Minor.Build.Revision-<i><b>Stage</b></i>Patch or empty String if not set */
    public String getStage() {
        return s;
    }

    /** @return Major.Minor.Build.Revision-StagePatch up until the last missing elements counting from right.<br>
     *          e.g. Major 1, Minor 2, Stage Release would translate to 1.2-Release, Major 1, Revision 5 would translate to 1.0.0.5
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(n[0]);
        if (n[1]>=0) { sb.append('.'); sb.append(n[1]); }
        else if (n[2]>0 || n[3]>0) { sb.append(".0"); }
        if (n[2]>=0) { sb.append('.'); sb.append(n[2]); }
        else if (n[3]>0) { sb.append(".0"); }
        if (n[3]>=0) { sb.append('.'); sb.append(n[3]); }
        if (n[4]>=0 || !s.isEmpty()) {
            sb.append('-');
            if (!s.isEmpty()) sb.append(s);
            if (n[4]>=0) sb.append(n[4]);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, s);
    }

    @Override
    public int compareTo(Version o) {
        int c;
        if ((c=Integer.compare(n[0], o.n[0]))!=0) return c;
        if ((c=Integer.compare(n[1], o.n[1]))!=0) return c;
        if ((c=Integer.compare(n[2], o.n[2]))!=0) return c;
        if ((c=Integer.compare(n[3], o.n[3]))!=0) return c;
        if ((c=s.compareTo(o.s))!=0) return c;
        if ((c=Integer.compare(n[4], o.n[4]))!=0) return c;
        return 0;
    }
}
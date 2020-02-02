package de.dosmike.sponge.utils.version;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionRange {

    private interface BiComparison {
        boolean test(String candidate, String b);
    }
    private interface TriComparison {
        boolean test(String candidate, String a, String b);
    }
    private static int getCompValue(String versionA, String versionB) {
        try { return new Version(versionA).compareTo(new Version(versionB)); }
        catch (Exception e) { return versionA.compareToIgnoreCase(versionB); }
    }
    private static final BiComparison GTR = (a, b)->getCompValue(a,b)>0;
    private static final BiComparison GEQ = (a, b)->getCompValue(a,b)>=0;
    private static final BiComparison LSS = (a, b)->getCompValue(a,b)<0;
    private static final BiComparison LEQ = (a, b)->getCompValue(a,b)<=0;

    private static TriComparison compound (BiComparison compA, BiComparison compB) {
        return ((candidate, a, b) -> compA.test(candidate, a) && compB.test(candidate, b));
    }

    private static final Pattern pattern0 = Pattern.compile("([^,\\]\\[)(]*)"); // single version gt without specified bounds
    private static final Pattern pattern1 = Pattern.compile("([\\[(])\\s*([^,\\]\\[)(]*)\\s*,\\s*\\)"); // single version gt with specified bounds
    private static final Pattern pattern2 = Pattern.compile("\\(\\s*,\\s*([^,\\]\\[)(]*)\\s*([])])"); // single version ls with specified bounds
    private static final Pattern pattern3 = Pattern.compile("([\\[(])\\s*([^,\\]\\[)(]*)\\s*,\\s*([^,\\]\\[)(]*)\\s*([])])"); // version range with specified bounds

    /** Test if a version is valid */
    public interface VersionTest extends Predicate<String> {
        boolean test(String version);
    }

    /**
     * returns a VersionTest, that no matter what the version string supplied or tested is, will return true if the
     * tested version fits in the supplied range. A big Problem with sponge versions, is that they do not have to follow
     * a fixed scheme. If the fallow a reduced maven Scheme, we can actually compare them. Otherwise a simple string
     * comparison will be returned.<br>
     * Neither this function nor the returned tests will ever throw. If a test can not convert string to versions, it
     * will automatically fall back to a string comparison as well.
     * @param string supplied version string used for comparison
     */
    public static VersionTest parseString(String string) {
        Matcher matcher;

        try {

            matcher = pattern0.matcher(string);
            if (matcher.matches()) {
                String vleft = matcher.group(1);
                return (v) -> GEQ.test(v, vleft);
            }

            matcher = pattern1.matcher(string);
            if (matcher.matches()) {
                boolean leftInclusive = matcher.group(1).equals("[");
                String vleft = matcher.group(2);
                return leftInclusive ? (v) -> GEQ.test(v, vleft) : (v) -> GTR.test(v, vleft);
            }

            matcher = pattern2.matcher(string);
            if (matcher.matches()) {
                boolean rightInclusive = matcher.group(2).equals("]");
                String vright = matcher.group(1);
                return rightInclusive ? (v) -> LEQ.test(v, vright) : (v) -> LSS.test(v, vright);
            }

            matcher = pattern3.matcher(string);
            if (matcher.matches()) {
                boolean leftInclusive = matcher.group(1).equals("[");
                String vleft = matcher.group(2);
                String vright = matcher.group(3);
                boolean rightInclusive = matcher.group(4).equals("]");
                TriComparison comp = compound(leftInclusive ? GEQ : GTR, rightInclusive ? LEQ : LSS);
                return (v) -> comp.test(v, vleft, vright);
            }

        } catch (Exception ignore) {

        }

        // Fallback comparator
        return (v) -> v.equalsIgnoreCase(string);

    }

}

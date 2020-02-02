package de.dosmike.sponge.utils.version;

import de.dosmike.sponge.oreapi.OreApiV2;
import de.dosmike.sponge.oreapi.v2.*;

import java.util.Optional;

public class VersionFilter {

    static boolean testProjectStable(OreTag[] tags) {
        for (OreTag tag : tags) {
            if (tag.getName().equalsIgnoreCase("Unstable"))
                return false;
        }
        return true;
    }
    static boolean testProjectReleaseChannel(OreTag[] tags) {
        for (OreTag tag : tags) {
            if (tag.getName().equalsIgnoreCase("Channel"))
                return tag.getData().equalsIgnoreCase("Release");
        }
        return false; //is no channel possible?
    }

    public static Optional<OreVersion> getFirstStable(OreVersion[] versions) {
        for (OreVersion v : versions) {
            if (testProjectStable(v.getTags()) && testProjectReleaseChannel(v.getTags()))
                return Optional.of(v);
        }
        return Optional.empty();
    }

    public static Optional<OreVersion> getAnyLatest(OreVersion[] versions) {
        for (OreVersion v : versions) {
            return Optional.of(v);
        }
        return Optional.empty();
    }

    /** partial only lists the Sponge tag */
    public static Optional<OrePartialVersion> getAnyLatest(OrePartialVersion[] versions) {
        for (OrePartialVersion v : versions) {
            return Optional.of(v);
        }
        return Optional.empty();
    }

    public static Optional<OreVersion> getLatestStableVersion(OreApiV2 apiInstance, OreProject project) {
        Optional<OreResultList<OreVersion>> page = apiInstance.waitFor(()->apiInstance.listVersions(project.getPluginId(), null));
        while(page.isPresent() && page.get().getResult().length>0) { // we can theoretically paginate beyond the last page, break
            //scan page
            Optional<OreVersion> stable = getFirstStable(page.get().getResult());
            //if version is stable return
            if (stable.isPresent()) return stable;
            //if page is last page break
            if (page.get().getPagination().getPage() == page.get().getPagination().getLastPage()) break;
            String nextPageQuery = page.get().getPagination().getQueryNext();
            page = apiInstance.waitFor(()->apiInstance.listVersions(project.getPluginId(), nextPageQuery));
        }
        return Optional.empty();
    }

}

package de.dosmike.sponge.oreapi.v2;

import com.google.gson.JsonObject;

public class OrePagination {

    int limit;
    int offset;
    /** num results total, if offset+limit >= count you're on the last page */
    int count;

    public OrePagination(JsonObject object) {
        limit = object.get("limit").getAsInt();
        offset = object.get("offset").getAsInt();
        count = object.get("count").getAsInt();
    }

    /** generate generic query parameters for the specified page without initial
     * concatinator
     * @return format limit=LIMIT&offset=OFFSET */
    public String getQueryPage(int page) {
        if (page < 1) throw new IllegalArgumentException("Page has to be positive integer");
        int targetoffset = (page-1)*limit;
        return String.format("limit=%d&offset=%d", limit, targetoffset);
    }
    /** calculate current page based on offset
     * @return current page number */
    public int getPage() {
        return (offset/limit)+1;
    }
    public int getLastPage() {
        return (int)Math.ceil((double)count/limit);
    }
    /** create the query for the next page (with max getLastPage()), see {@link #getQueryPage} for more info
     * @return format limit=LIMIT&offset=OFFSET */
    public String getQueryNext() {
        int page = getPage();
        int lastPage = getLastPage();
        return getQueryPage(page>=lastPage ? lastPage : getPage()+1);
    }
    /** create the query for the previous page (with min page 1),
     * see {@link #getQueryPage} for more info
     * @return format limit=LIMIT&offset=OFFSET */
    public String getQueryPrevious() {
        int page = getPage();
        return getQueryPage(page <= 2 ? 1 : page-1);
    }

    /** @return the actual amount of total results*/
    public int getResultCount() {
        return count;
    }

    /** @return the result limit per page */
    public int getPageLimit() {
        return limit;
    }
    /** @return the result offset of entries for this page */
    public int getPageOffset() {
        return offset;
    }

}

/*
 * SkyTube
 * Copyright (C) 2019  Zsombor Gegesy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package free.rm.skytube.businessobjects.YouTube.newpipe;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import free.rm.skytube.businessobjects.Logger;


/**
 * Class to parse a channel screen for searching for videos and returning as pages with {@link #getNextPage()} }.
 *
 * @author zsombor
 */
public abstract class Pager<I extends InfoItem, O> implements PagerBackend<O> {
    private final StreamingService streamingService;
    private final ListExtractor<I> channelExtractor;
    private String nextPageUrl;
    private boolean hasNextPage = true;
    private Exception lastException;

    Pager(StreamingService streamingService, ListExtractor<I> channelExtractor) {
        this.streamingService = streamingService;
        this.channelExtractor = channelExtractor;
    }


    StreamingService getStreamingService() {
        return streamingService;
    }

    /**
     * @return true, if there could be more videos available in the next page.
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }

    @Override
    public Exception getLastException() {
        return lastException;
    }

    /**
     * @return the next page of videos.
     * @throws ParsingException
     * @throws IOException
     * @throws ExtractionException
     */
    public List<O> getNextPage() throws ParsingException, IOException, ExtractionException {
        if (!hasNextPage) {
            return Collections.emptyList();
        }
        if (nextPageUrl == null || nextPageUrl.isEmpty()) {
            return process(channelExtractor.getInitialPage());
        } else {
            return process(channelExtractor.getPage(nextPageUrl));
        }
    }

    @Override
    public List<O> getSafeNextPage() {
        try {
            return getNextPage();
        } catch (Exception e) {
            lastException = e;
            Logger.e(this.getClass().getSimpleName(), "Error: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    protected List<O> process(ListExtractor.InfoItemsPage<I> page) throws ParsingException {
        nextPageUrl = page.getNextPageUrl();
        hasNextPage = nextPageUrl != null && nextPageUrl.isEmpty();
        return extract(page);
    }


    protected abstract List<O> extract(InfoItemsPage<I> page) throws ParsingException;

}

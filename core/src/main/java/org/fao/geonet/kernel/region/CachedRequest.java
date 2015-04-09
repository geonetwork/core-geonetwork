package org.fao.geonet.kernel.region;

import com.google.common.base.Optional;

import java.util.Collection;

class CachedRequest extends Request {

    private final Optional<Long> lastModified;
    private Collection<Region> regions;

    public CachedRequest(Request createSearchRequest) throws Exception {
        this.regions = createSearchRequest.execute();
        this.lastModified = createSearchRequest.getLastModified();
    }

    @Override
    public Request label(String labelParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Request categoryId(String categoryIdParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Request maxRecords(int maxRecordsParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Region> execute() throws Exception {
        return regions;
    }

    @Override
    public Request id(String regionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Long> getLastModified() {
        return this.lastModified;
    }

}

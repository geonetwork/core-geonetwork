package org.fao.geonet.kernel.harvest.harvester.localfilesystem;

import org.fao.geonet.kernel.harvest.BaseAligner;

import java.util.concurrent.atomic.AtomicBoolean;

public class LocalFileSytemAligner extends BaseAligner<LocalFilesystemParams> {

    public LocalFileSytemAligner(AtomicBoolean cancelMonitor, LocalFilesystemParams params) {
        super(cancelMonitor);
        this.params = params;
    }
}

package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.beans.factory.annotation.Autowired;

public class HarvesterSettingRepositoryTest extends AbstractSettingsRepositoryTest<HarvesterSetting> {
    @Autowired
    private HarvesterSettingRepository repo;

    protected HarvesterSettingRepository getRepository() {
        return repo;
    }
    protected HarvesterSetting newSetting() {
        int id = nextId.incrementAndGet();
        return new HarvesterSetting().setName("name " + id).setValue("value " + id);
    }


}

package org.fao.geonet.repository;

import org.fao.geonet.domain.Setting;
import org.springframework.beans.factory.annotation.Autowired;

public class SettingRepositoryTest extends AbstractSettingsRepositoryTest<Setting> {
    @Autowired
    private SettingRepository repo;

    protected SettingRepository getRepository() {
        return repo;
    }
    protected Setting newSetting() {
        int id = nextId.incrementAndGet();
        return new Setting().setName("name " + id).setValue("value " + id);
    }

}

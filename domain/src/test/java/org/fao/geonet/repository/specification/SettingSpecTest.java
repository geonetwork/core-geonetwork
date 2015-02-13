package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SettingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SettingSpecTest extends AbstractSpringDataTest {

    public static final Function<Setting, String> SETTING_NAME_FUNCTION = new Function<Setting, String>() {
        @Nullable
        @Override
        public String apply(Setting input) {
            return input.getName();
        }
    };
    @Autowired
    private SettingRepository settingRepository;

    @Test
    public void testNameStartsWith() throws Exception {
        final Setting s1 = settingRepository.save(new Setting().setName("NAME/abc").setValue("val"));
        final Setting s2 = settingRepository.save(new Setting().setName("NAME/abc2").setValue("val"));
        final Setting s3 = settingRepository.save(new Setting().setName("OTHER/abc").setValue("val"));

        List<Setting> found = settingRepository.findAll(SettingSpec.nameStartsWith("NAME/"));
        List<String> ids = Lists.transform(found, SETTING_NAME_FUNCTION);
        assertEquals(ids.toString(), 2, found.size());
        assertTrue(ids.contains(s1.getName()));
        assertTrue(ids.contains(s2.getName()));

        found = settingRepository.findAll(SettingSpec.nameStartsWith("OTHER/"));
        ids = Lists.transform(found, SETTING_NAME_FUNCTION);
        assertEquals(1, found.size());
        assertTrue(ids.contains(s3.getName()));

    }

}
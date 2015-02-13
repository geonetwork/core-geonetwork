package org.fao.geonet.services.sources;

import com.google.common.collect.Lists;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.SettingSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import static org.fao.geonet.repository.HarvesterSettingRepository.ID_PREFIX;
import static org.fao.geonet.repository.HarvesterSettingRepository.SEPARATOR;

/**
 * Update the sources translations.
 *
 * @author Jesse on 2/3/2015.
 */
@Controller("sources")
public class SourcesController {

    public static final String PREFIX = "translations-";
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private HarvesterSettingsManager harvesterSettingsManager;
    @Autowired
    private HarvesterSettingRepository harvesterSettingRepository;

    @RequestMapping("/{lang}/source/{uuid}")
    @ResponseBody
    public OkResponse updateTranslations(
            @PathVariable String lang,
            @PathVariable String uuid,
            final HttpServletRequest request) {
        if (settingRepository.findOne(SettingManager.SYSTEM_SITE_SITE_ID_PATH).getValue().equals(uuid)) {
            updateSite(request);
        }

        if (sourceRepository.exists(uuid)) {
            updateNormalSource(uuid, request);
        }
        return new OkResponse();
    }

    private void updateSite(HttpServletRequest request) {
        settingRepository.deleteAll(SettingSpec.nameStartsWith(SettingManager.SYSTEM_SITE_LABEL_PREFIX));

        List<Setting> translationSettings = Lists.newArrayList();
        for (Map.Entry<String, String[]> stringEntry : request.getParameterMap().entrySet()) {
            String paramName = stringEntry.getKey();
            final String[] values = stringEntry.getValue();
            if (paramName.startsWith(PREFIX)) {
                String lang = paramName.substring(PREFIX.length());
                if (values.length > 0) {
                    Setting setting = new Setting().
                            setDataType(SettingDataType.STRING).
                            setInternal(true).
                            setName(SettingManager.SYSTEM_SITE_LABEL_PREFIX + lang).
                            setValue(values[0]);
                    translationSettings.add(setting);
                }
            }
        }

        settingRepository.save(translationSettings);

    }

    public void updateNormalSource(String uuid, final HttpServletRequest request) {
        final HarvesterSetting harvesterUuidSetting = harvesterSettingRepository.findOneByNameAndValue("uuid", uuid);
        final String translationsIdPath;
        if (harvesterUuidSetting != null) {
            HarvesterSetting harvesterSite = harvesterUuidSetting.getParent();
            final String pathToTranslations = ID_PREFIX + harvesterSite.getId() + SEPARATOR + AbstractParams.TRANSLATIONS;
            final List<HarvesterSetting> translationsSettings = harvesterSettingRepository.findAllByPath(pathToTranslations);
            String translationsSettingId;
            if (translationsSettings.isEmpty()) {
                translationsSettingId = harvesterSettingsManager.add(ID_PREFIX + harvesterSite.getId(), AbstractParams.TRANSLATIONS, "");
            } else {
                translationsSettingId = String.valueOf(translationsSettings.get(0).getId());
            }
            translationsIdPath = ID_PREFIX + translationsSettingId;
            harvesterSettingsManager.removeChildren(translationsIdPath);
        } else {
            translationsIdPath = null;
        }

        sourceRepository.update(uuid, new Updater<Source>() {
            @Override
            public void apply(@Nonnull Source source) {
                for (Map.Entry<String, String[]> stringEntry : request.getParameterMap().entrySet()) {
                    String paramName = stringEntry.getKey();
                    final String[] values = stringEntry.getValue();
                    if (paramName.startsWith(PREFIX)) {
                        String lang = paramName.substring(PREFIX.length());
                        if (values.length > 0) {
                            if (translationsIdPath != null) {
                                harvesterSettingsManager.add(translationsIdPath, lang, values[0]);
                            }
                            source.getLabelTranslations().put(lang, values[0]);
                        }
                    }
                }
            }
        });
    }
}

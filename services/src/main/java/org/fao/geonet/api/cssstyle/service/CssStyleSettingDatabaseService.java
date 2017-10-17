package org.fao.geonet.api.cssstyle.service;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.CssStyleSettings;
import org.fao.geonet.repository.CssStyleSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class CssStyleSettingDatabaseService implements ICssStyleSettingService {

	@Override
	public void saveSettings(CssStyleSettings cssStyleSettings) {
		final CssStyleSettingsRepository cssStyleSettingRepository = ApplicationContextHolder.get()
				.getBean(CssStyleSettingsRepository.class);
		
		CssStyleSettings published = cssStyleSettingRepository.findOne(CssStyleSettings.Status.PUBLISHED);
		if(published!=null) {
			cssStyleSettingRepository.delete(CssStyleSettings.Status.PUBLISHED);
		}
		cssStyleSettingRepository.save(cssStyleSettings);
	}

	@Override
	public String getCustomCssSetting() {
		final CssStyleSettingsRepository cssStyleSettingRepository = ApplicationContextHolder.get()
				.getBean(CssStyleSettingsRepository.class);
		CssStyleSettings published = cssStyleSettingRepository.findOne(CssStyleSettings.Status.PUBLISHED);
		
		if(published==null || published.getCustomCss()==null) return "";
		
		return published.getCustomCss();
		
	}
	
	@Override
	public CssStyleSettings getCssStyleSettings() {
		final CssStyleSettingsRepository cssStyleSettingRepository = ApplicationContextHolder.get()
				.getBean(CssStyleSettingsRepository.class);
		CssStyleSettings published = cssStyleSettingRepository.findOne(CssStyleSettings.Status.PUBLISHED);
		
		return published;
		
	}
}

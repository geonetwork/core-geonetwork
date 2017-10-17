package org.fao.geonet.api.cssstyle.service;

import org.fao.geonet.domain.CssStyleSettings;

public interface ICssStyleSettingService {

	 public void saveSettings(CssStyleSettings cssStyleSettings);
	 
	 public String getCustomCssSetting();
	 
	 public CssStyleSettings getCssStyleSettings();
}

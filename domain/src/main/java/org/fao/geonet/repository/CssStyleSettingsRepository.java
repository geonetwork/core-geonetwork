package org.fao.geonet.repository;

import org.fao.geonet.domain.CssStyleSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CssStyleSettingsRepository  extends JpaRepository<CssStyleSettings, CssStyleSettings.Status>  {

}

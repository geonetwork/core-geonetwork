package org.fao.geonet.domain;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Class CssStyleSettingsModel.
 */
@Entity(name = "Settings_CssStyle")
@Table(name = "Settings_CssStyle")
public class CssStyleSettings extends GeonetEntity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6799485605075101098L;

	/**
	 * The Enum Status.
	 */
	public enum Status {
		
		/** The draft. */
		DRAFT,
		
		/** The published. */
		PUBLISHED;
	}

	/** The status. */
	@Id
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	/** The backgroud color. */
	@Column
	@Nullable
	private String backgroudColor;
	
	/** The font color. */
	@Column
	@Nullable
	private String fontColor;

	/** The color combo. */
	@Column
	@Nullable
	private String colorScheme;
	
	/** The font combo. */
	@Column
	@Nullable
	private String fontCombo;

	/** The custom css. */
	@Column
	@Nullable
	private String customCss;
	
	/**
	 * Instantiates a new css style settings model.
	 */
	public CssStyleSettings() {
		// Should be removed when the preview functionality is ready
		status = Status.PUBLISHED;
	}

	/**
	 * Gets the backgroud color.
	 *
	 * @return the backgroud color
	 */
	public String getBackgroudColor() {
		return backgroudColor;
	}

	/**
	 * Sets the backgroud color.
	 *
	 * @param backgroudColor the new backgroud color
	 */
	public void setBackgroudColor(String backgroudColor) {
		this.backgroudColor = backgroudColor;
	}

	/**
	 * Gets the font color.
	 *
	 * @return the font color
	 */
	public String getFontColor() {
		return fontColor;
	}

	/**
	 * Sets the font color.
	 *
	 * @param fontColor the new font color
	 */
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	/**
	 * Gets the color combo.
	 *
	 * @return the color combo
	 */
	public String getColorScheme() {
		return colorScheme;
	}

	/**
	 * Sets the color combo.
	 *
	 * @param colorCombo the new color combo
	 */
	public void setColorScheme(String colorScheme) {
		this.colorScheme = colorScheme;
	}

	/**
	 * Gets the font combo.
	 *
	 * @return the font combo
	 */
	public String getFontCombo() {
		return fontCombo;
	}

	/**
	 * Sets the font combo.
	 *
	 * @param fontCombo the new font combo
	 */
	public void setFontCombo(String fontCombo) {
		this.fontCombo = fontCombo;
	}

	/**
	 * Gets the custom css.
	 *
	 * @return the custom css
	 */
	public String getCustomCss() {
		return customCss;
	}

	/**
	 * Sets the custom css.
	 *
	 * @param customCss the new custom css
	 */
	public void setCustomCss(String customCss) {
		this.customCss = customCss;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}




}

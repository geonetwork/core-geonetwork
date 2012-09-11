<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="schedule-widget">
		<xsl:param name="type" />

		<table border="0">
			<xsl:call-template name="schedule-widget-notable">
				<xsl:with-param name="type" select="$type" />
			</xsl:call-template>
		</table>
	</xsl:template>

	<xsl:template name="schedule-widget-notable">
		<xsl:param name="type" />

		<tr>
			<td class="padded">
				<xsl:value-of select="/root/gui/harvesting/at" />
			</td>
			<td class="padded">
				<select id="{$type}.atHour" class="content">
					<xsl:apply-templates mode="selectoptions"
						select="/root/gui/harvesting/hours/hour" />
				</select>:<select id="{$type}.atMin" class="content">
					<xsl:apply-templates mode="selectoptions"
						select="/root/gui/harvesting/minutes/minute" />
				</select>
				&#160;
				<xsl:value-of select="/root/gui/harvesting/atSpec" />
			</td>
		</tr>
		<tr>
			<td class="padded">
				<xsl:value-of select="/root/gui/harvesting/interval" />
			</td>
			<td class="padded">
				<select id="{$type}.atIntervalHours" class="content">
					<xsl:apply-templates mode="selectoptions"
						select="/root/gui/harvesting/hourintervals/hour" />
				</select>
				&#160;
				<xsl:value-of select="/root/gui/harvesting/intervalSpec" />
			</td>
		</tr>
		<tr>
			<td class="padded">
				
			</td>
			<td class="padded">
				<xsl:for-each select="/root/gui/harvesting/dayofweek/day">
					<input class="content {$type}_day" id="{$type}.{text()}" type="checkbox" value="" checked="true"/>
					<label for="{$type}.{text()}"><xsl:value-of select="@label" /></label>
					&#160;
				</xsl:for-each>
			</td>
		</tr>

		<tr>
			<td class="padded">
				<xsl:value-of select="/root/gui/harvesting/oneRun" />
			</td>
			<td class="padded">
				<input id="{$type}.oneRunOnly" type="checkbox" value="" />
			</td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
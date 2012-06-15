<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" />
	<xsl:include href="main.xsl" />
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="/root/gui/strings/metadata.expired/formTitle" />
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:apply-templates mode="report" />
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<button class="content" type="button"
					onclick="load('{/root/gui/locService}/metadata.expired.form');">
					<xsl:value-of select="/root/gui/strings/back" />
				</button>
			</xsl:with-param>
			<xsl:with-param name="formfooter" select="'false'" />
		</xsl:call-template>
	</xsl:template>


	<xsl:template mode="report"
		match="/root/oldElements[unsentNotifications]">
		<table style="text-align:left" width="100%">
          <tr><th><xsl:value-of select="/root/gui/strings/metadata.expired/recipient"/></th>
            <th><xsl:value-of select="/root/gui/strings/metadata.expired/numExpiredElements" /></th>
          </tr>
			<xsl:for-each select="unsentNotifications/owner">
				<xsl:variable name="items" select="metadata" />

				<tr>
					<td width="10%">
						<xsl:value-of select="@ownerUsername" />
					</td>
					<td width="10%">
						<xsl:value-of select="count($items)" />
					</td>
					<td style="color:red">
						<xsl:value-of
							select="/root/gui/strings/metadata.expired/unsentNotifications" />
					</td>
				</tr>
			</xsl:for-each>
            <xsl:for-each select="sentNotifications/owner">
                <xsl:variable name="items" select="metadata" />
                <tr>
                    <td width="10%">
                        <xsl:value-of select="@ownerUsername" />
                    </td>
                    <td width="10%">
                        <xsl:value-of select="count($items)" />
                    </td>
                    <td>
                        <xsl:value-of
                            select="/root/gui/strings/metadata.expired/sentNotifications" />
                    </td>
                </tr>
            </xsl:for-each>
		</table>
	</xsl:template>


	<xsl:template mode="report" match="/root[unpublishedItems]">
		<table style="text-align:left" width="100%">
          <tr><th><xsl:value-of select="/root/gui/strings/metadata.expired/owner"/></th>
            <th><xsl:value-of select="/root/gui/strings/metadata.expired/numExpiredElements" /></th>
          </tr>
			<xsl:for-each select="unpublishedItems/owner">
				<xsl:variable name="items" select="metadata" />
				<tr>
					<td width="10%">
						<xsl:value-of select="@ownerUsername" />
					</td>
					<td width="10%">
						<xsl:value-of select="count($items)" />
					</td>
					<td>
						<xsl:value-of select="/root/gui/strings/metadata.expired/unpublished" />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template mode="report" match="/root/unpublished">
		<xsl:value-of select="/root/gui/strings/sentNofications"></xsl:value-of>
		<xsl:for-each select="item">

		</xsl:for-each>
	</xsl:template>

	<xsl:template mode="report" match="text()"></xsl:template>

</xsl:stylesheet>
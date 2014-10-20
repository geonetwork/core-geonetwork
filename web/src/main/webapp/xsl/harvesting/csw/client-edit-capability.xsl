<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the remote node -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/search">
		<div id="{@id}">
			<p/>
			<xsl:apply-templates select="." mode="data"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="data">

    <!-- If no search filters are defined, don't render the element -->
    <xsl:if test="count(/root/search/*) > 0">
		<table>
			<tr>
				<td>
					<a onclick="harvesting.csw.removeSearchRow('{@id}')">
						<img style="cursor:hand; cursor:pointer"
							src="{/root/env/url}/images/fileclose.png" alt="Remove"/>
					</a>

				</td>
				<td class="padded" bgcolor="#D0E0FF">
					<b>
						<xsl:value-of select="/root/strings/search"/>
					</b>
				</td>
			</tr>



			<xsl:for-each select="/root/search/*">
				<xsl:sort select="local-name()"/>

          <!-- Queryable fields with a namespace are stored replacing : with __ to avoid issues in the SettingsManager -->
          <xsl:variable name="nameVal">
            <xsl:choose>
              <xsl:when test="contains(name(), ':')">
                <xsl:call-template name="replace-string">
                  <xsl:with-param name="text" select="name()"/>
                  <xsl:with-param name="replace" select="':'" />
                  <xsl:with-param name="with" select="'__'"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="name()" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="fieldId" select="concat('csw.',normalize-space($nameVal))"/>

        <tr>
					<td/>
					<td class="padded">
            <label for="{$fieldId}">
              <!-- Queryable fields with a namespace are stored replacing : with __ to avoid issues in the SettingsManager -->
              <xsl:choose>
                <xsl:when test="contains(local-name(), '__')">
                  <xsl:value-of select="substring-after(local-name(), '__')"/>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="local-name()"/></xsl:otherwise>
              </xsl:choose>
            </label>
					</td>
					<td class="padded">
						<input type="text">
							<xsl:attribute name="id">
								<xsl:value-of select="$fieldId"/>
							</xsl:attribute>
							<xsl:attribute name="class">content</xsl:attribute>
							<xsl:attribute name="value">
								<xsl:value-of select="value"/>
							</xsl:attribute>
							<xsl:attribute name="size">30</xsl:attribute>
						</input>
					</td>
				</tr>
			</xsl:for-each>


		</table>
    </xsl:if>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

    <!-- ============================================================================================= -->

    <xsl:template name="replace-string">
      <xsl:param name="text"/>
      <xsl:param name="replace"/>
      <xsl:param name="with"/>
      <xsl:choose>
        <xsl:when test="contains($text,$replace)">
          <xsl:value-of select="substring-before($text,$replace)"/>
          <xsl:value-of select="$with"/>
          <xsl:call-template name="replace-string">
            <xsl:with-param name="text"
                            select="substring-after($text,$replace)"/>
            <xsl:with-param name="replace" select="$replace"/>
            <xsl:with-param name="with" select="$with"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$text"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>

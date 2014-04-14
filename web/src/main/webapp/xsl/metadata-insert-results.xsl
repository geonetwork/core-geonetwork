<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl">

  <xsl:include href="main.xsl"/>

  <!--
	page content
	-->
  <xsl:template name="content">
    <xsl:call-template name="formLayout">
      <xsl:with-param name="title" select="/root/gui/strings/metadataInsertResults"/>
      <xsl:with-param name="content">

        <table width="100%">
          <tr>
            <td align="left">
              <xsl:choose>
                <xsl:when test="/root/response/id">
                  <xsl:value-of
                    select="concat(/root/gui/strings/metadataAdded,' ',/root/response/id)"/>
                  <br/>
                  <br/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="errors" select="count(/root/response/exceptions/exception)"/>
                  <xsl:value-of
                    select="concat(/root/gui/strings/metadataRecordsProcessed,' ',/root/response/records)"/>
                  <br/>
                  <xsl:value-of
                    select="concat(/root/gui/strings/metadataRecordsAdded,' ', (/root/response/records - $errors))"/>
                  <br/>
                  <xsl:value-of select="concat($errors, ' ', /root/gui/strings/errors)"/>
                  <br/>
                  <ul>
                    <xsl:for-each select="/root/response/exceptions/exception">
                      <xsl:sort select="@file"/>
                      <li>
                        <strong><xsl:value-of select="@file"/></strong>:
                        <!-- Handle different types of errors -->
                        <xsl:choose>
                            <xsl:when test="geonet:schematronerrors">
                                <xsl:call-template name="metadata-validation-report">
                                    <xsl:with-param name="report" select="geonet:schematronerrors/geonet:report"/>
                                </xsl:call-template>
                            </xsl:when>

                            <xsl:when test="xsderrors">
                               <xsl:for-each select="xsderrors/error">
			                        <xsl:value-of select="message"/>
  		                            <br/><br/>
		                        </xsl:for-each>
                            </xsl:when>

                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                      </li>
                    </xsl:for-each>
                  </ul>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </tr>
        </table>
      </xsl:with-param>

      <xsl:with-param name="buttons">
        <xsl:if test="/root/response/id">
          
          <xsl:choose>
            <xsl:when test="/root/gui/config/client/@widget='true'">
              <button class="content" onclick="goBack()" id="back"><xsl:value-of
                select="/root/gui/strings/back"/></button> &#160; <button class="content"
                  onclick="load('{/root/gui/config/client/@url}?hl={/root/gui/language}#id={/root/response/id}')"
                  ><xsl:value-of select="/root/gui/strings/show"/></button> &#160; <button
                    class="content"
                    onclick="load('{/root/gui/config/client/@url}?hl={/root/gui/language}#edit={/root/response/id}')"
                    ><xsl:value-of select="/root/gui/strings/edit"/></button>
            </xsl:when>
            <xsl:otherwise>
              <button class="content" onclick="goBack()" id="back"><xsl:value-of
                select="/root/gui/strings/back"/></button> &#160; <button class="content"
                  onclick="load('{/root/gui/locService}/metadata.show?id={/root/response/id}')"
                  ><xsl:value-of select="/root/gui/strings/show"/></button> &#160; <button
                    class="content"
                    onclick="load('{/root/gui/locService}/metadata.edit?id={/root/response/id}')"
                    ><xsl:value-of select="/root/gui/strings/edit"/></button>
            </xsl:otherwise>
          </xsl:choose>
          
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


	<!-- templates used by xslts that need to present validation output -->
	<xsl:template name="metadata-validation-report">
		<xsl:param name="report"/>

		<div style="display:block; text-align:left" class="content">
			<xsl:apply-templates mode="validation-report" select="$report"/>
		</div>
	</xsl:template>

	<xsl:template match="svrl:active-pattern" mode="validation-report">
		<xsl:variable name="preceding-ap" select="count(following-sibling::svrl:active-pattern)"/>

		<xsl:if test="following-sibling::*[(name(.)='svrl:failed-assert')
					and count(following-sibling::svrl:active-pattern) = $preceding-ap]">

            <span class="arrow"><xsl:value-of select="@name"/></span>
            <div>
                <ul>
                    <xsl:apply-templates mode="validation-report"
                        select="following-sibling::*[(name(.)='svrl:failed-assert')
                        and count(following-sibling::svrl:active-pattern) = $preceding-ap]"/>
                </ul>
            </div>
		</xsl:if>
	</xsl:template>


	<xsl:template match="svrl:failed-assert" mode="validation-report">
		<li>
			<xsl:attribute name="name">error</xsl:attribute>
			<a href="#" title="{@location}" alt="{@location}">
				<img src="../../images/schematron.gif" style="border:none;"/>
			</a><xsl:text> </xsl:text>
			<xsl:value-of select="svrl:text"/>
		</li>
	</xsl:template>

	<xsl:template match="svrl:successful-report" mode="validation-report" />

	<xsl:template match="geonet:report" mode="validation-report">
		<xsl:variable name="rule" select="@geonet:rule"/>
		<xsl:variable name="count" select="count(svrl:schematron-output/svrl:failed-assert)"/>

		<xsl:if test="$count != 0">

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/><xsl:text> </xsl:text>
				(<img src="../../images/schematron.gif" alt="failed" title="failed"/>
						<xsl:value-of select="$count"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="/root/gui/strings/errors"/>)
			</legend>

			<xsl:apply-templates mode="validation-report" select="svrl:schematron-output/svrl:active-pattern"/>

		</fieldset>
		</xsl:if>

        <xsl:if test="string(geonet:schematronVerificationError)">

		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/rules[@name=$rule]"/><xsl:text> </xsl:text>
				<img src="../../images/schematron.gif" alt="failed" title="failed"/>
			</legend>

            <xsl:apply-templates mode="validation-report" select="geonet:schematronVerificationError"/>

		</fieldset>
		</xsl:if>
	</xsl:template>

	<xsl:template match="geonet:schematronerrors" mode="validation-report">
		<fieldset class="validation-report">
			<legend class="block-legend">
				<xsl:value-of select="/root/gui/strings/schematronReport"/>
			</legend>
			<xsl:apply-templates select="*" mode="validation-report"/>
		</fieldset>
	</xsl:template>

     <xsl:template match="geonet:schematronVerificationError" mode="validation-report">
       <span class="arrow">
            <xsl:value-of select="."/>
        </span>
	</xsl:template>


</xsl:stylesheet>

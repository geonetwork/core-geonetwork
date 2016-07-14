<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>

  <xsl:import href="modal.xsl"/>

  <xsl:key name="schemaName"
           match="/root/gui/schemas/*/codelists/codelist[@name='gmd:MD_KeywordTypeCode']/entry"
           use="."/>

  <!--
    page content
    -->
  <xsl:template name="content">
    <xsl:call-template name="formLayout">
      <xsl:with-param name="content">
        <xsl:variable name="thesauriType"
                      select="/root/gui/thesaurusTypes/thesauri/thesaurus[contains(key,/root/request/uuid)]/dname"/>

        <div id="createthesaurus">
          <input name="uuid" type="hidden" value="{/root/request/uuid}"/>
          <table width="100%">
            <tr>
              <xsl:choose>
                <xsl:when test="normalize-space($thesauriType)=''">
                  <td class="padded" align="left">
                    <xsl:value-of select="/root/gui/strings/thesaurusType"/>
                  </td>
                  <td class="padded" align="left">
                    <select class="md" name="type">
                      <xsl:for-each
                        select="/root/gui/schemas/*/codelists/codelist[@name='gmd:MD_KeywordTypeCode']/entry[generate-id()=generate-id(key('schemaName',.)[1])]">
                        <option value="{code}">
                          <xsl:value-of select="concat(label,':: ',description)"/>
                        </option>
                      </xsl:for-each>
                    </select>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td class="padded" align="center" colspan="2">
                    <input name="type" type="hidden" value="{$thesauriType}"/>
                    <xsl:value-of select="concat(/root/gui/strings/thesaurusType,'  ')"/>
                    <b>
                      <xsl:value-of select="$thesauriType"/>
                    </b>
                    <xsl:value-of select="'  already exists for this register'"/>
                  </td>
                </xsl:otherwise>
              </xsl:choose>
            </tr>
            <tr>
              <td class="padded" align="center" colspan="2">
                <button class="content"
                        onclick="radioModalUpdate('createthesaurus','thesaurus.add.register',null,'')">
                  <xsl:choose>
                    <xsl:when test="normalize-space($thesauriType)=''">
                      <xsl:value-of select="/root/gui/strings/create"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="/root/gui/strings/update"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </button>
              </td>
            </tr>
          </table>
        </div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:exslt="http://exslt.org/common"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="1.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="exslt saxon geonet java">

  <xsl:include href="blanks/metadata-schema01/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema02/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema03/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema04/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema05/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema06/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema07/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema08/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema09/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema10/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema11/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema12/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema13/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema14/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema15/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema16/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema17/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema18/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema19/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema20/present/metadata.xsl"/>

  <xsl:template mode="schema" match="*">
    <xsl:choose>
      <xsl:when test="string(geonet:info/schema)!=''">
        <xsl:value-of select="geonet:info/schema"/>
      </xsl:when>
      <xsl:otherwise>UNKNOWN</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- summary: copy it -->
  <xsl:template match="summary" mode="brief">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- brief -->
  <xsl:template match="*" mode="brief">
    <xsl:param name="schema">
      <xsl:apply-templates mode="schema" select="."/>
    </xsl:param>

    <xsl:variable name="briefSchemaCallBack" select="concat($schema,'Brief')"/>
    <saxon:call-template name="{$briefSchemaCallBack}"/>
  </xsl:template>

  <!--
    standard metadata buttons (edit/delete/privileges/categories)
    -->
  <xsl:template name="buttons" match="*">
    <xsl:param name="metadata" select="."/>
    <xsl:param name="buttonBarId" select="''"/>
    <xsl:param name="ownerbuttonsonly" select="false()"/>

    <!-- Title is truncated if longer than maxLength.  -->
    <xsl:variable name="maxLength" select="'40'"/>

    <xsl:variable name="ltitle">
      <xsl:call-template name="escapeString">
        <xsl:with-param name="expr">
          <xsl:choose>
            <xsl:when test="string-length($metadata/title) &gt; $maxLength">
              <xsl:value-of
                select="concat(substring(normalize-space($metadata/title), 1, $maxLength), ' ...')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="normalize-space($metadata/title)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>

      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="readonly" select="/root/gui/env/readonly = 'true'"/>
    <xsl:if test="not($readonly) and not($ownerbuttonsonly) and
   /root/gui/schemalist/name[.=$metadata/geonet:info/schema]/@edit='true'">
      &#160;
      <!-- create button -->
      <xsl:variable name="duplicate" select="concat(/root/gui/strings/duplicate,': ',$ltitle)"/>
      <xsl:if
        test="string(geonet:info/isTemplate)!='s' and (geonet:info/isTemplate='y' or geonet:info/source=/root/gui/env/site/siteId) and java:isAccessibleService('metadata.duplicate.form')">
        <button class="content"
                onclick="load('{/root/gui/locService}/metadata.duplicate.form?id={$metadata/geonet:info/id}')">
          <xsl:value-of select="/root/gui/strings/create"/>
        </button>
      </xsl:if>

      <!-- edit button -->
      <xsl:if test="
    (/root/gui/config/harvester/enableEditing = 'true' and geonet:info/isHarvested = 'y' and geonet:info/edit='true')
    or (geonet:info/isHarvested = 'n' and geonet:info/edit='true')">
        &#160;
        <button class="content"
                onclick="load('{/root/gui/locService}/metadata.edit?id={$metadata/geonet:info/id}')">
          <xsl:value-of select="/root/gui/strings/edit"/>
        </button>
      </xsl:if>
    </xsl:if>

    <!-- delete button -->
    <xsl:if test="not($readonly) and ((/root/gui/config/harvester/enableEditing = 'true' and geonet:info/isHarvested = 'y' and geonet:info/edit='true')
    or (geonet:info/isHarvested = 'n' and geonet:info/edit='true'))">
      &#160;
      <button class="content"
              onclick="return doConfirmDelete('{/root/gui/locService}/metadata.delete?id={$metadata/geonet:info/id}', '{/root/gui/strings/confirmDelete}','{$ltitle}','{$metadata/geonet:info/id}', '{/root/gui/strings/deleteConfirmationTitle}')">
        <xsl:value-of select="/root/gui/strings/delete"/>
      </button>
    </xsl:if>

    <xsl:if test="not($readonly) and geonet:info/edit='true'">
      &#160;
      <!-- =========================  -->
      <!-- Add other actions list     -->
      <button id="{$buttonBarId}oAc{$metadata/geonet:info/id}"
              name="{$buttonBarId}oAc{$metadata/geonet:info/id}" class="content"
              onclick="oActions('{$buttonBarId}oAc',{$metadata/geonet:info/id});"
              style="width:150px;" title="{/root/gui/strings/otherActions}">
        <img id="{$buttonBarId}oAcImg{$metadata/geonet:info/id}"
             name="{$buttonBarId}oAcImg{$metadata/geonet:info/id}"
             src="{/root/gui/url}/images/plus.gif" style="padding-right:3px;"/>
        <xsl:value-of select="/root/gui/strings/otherActions"/>
      </button>
      <div id="{$buttonBarId}oAcEle{$metadata/geonet:info/id}" class="oAcEle"
           style="display:none;width:250px"
           onClick="oActions('{$buttonBarId}oAc',{$metadata/geonet:info/id});">

        <!-- privileges button -->
        <xsl:if test="java:isAccessibleService('metadata.admin.form')">
          <xsl:variable name="privileges"
                        select="concat(/root/gui/strings/privileges,': ',$ltitle)"/>
          <button
            onclick="doOtherButton('{/root/gui/locService}/metadata.admin.form?id={$metadata/geonet:info/id}','{$privileges}',600)">
            <xsl:value-of select="/root/gui/strings/privileges"/>
          </button>
        </xsl:if>

        <!-- categories button -->
        <xsl:if
          test="java:isAccessibleService('metadata.category.form') and /root/gui/config/category/admin">
          <xsl:variable name="categories"
                        select="concat(/root/gui/strings/categories,': ',$ltitle)"/>
          <button
            onclick="doOtherButton('{/root/gui/locService}/metadata.category.form?id={$metadata/geonet:info/id}','{$categories}',300)">
            <xsl:value-of select="/root/gui/strings/categories"/>
          </button>
        </xsl:if>

        <!-- status button -->
        <xsl:if test="java:isAccessibleService('metadata.status.form')">
          <xsl:variable name="statusTitle" select="concat(/root/gui/strings/status,': ',$ltitle)"/>
          <button
            onclick="doOtherButton('{/root/gui/locService}/metadata.status.form?id={$metadata/geonet:info/id}','{$statusTitle}',300)">
            <xsl:value-of select="/root/gui/strings/status"/>
          </button>
        </xsl:if>

        <!-- versioning button -->
        <xsl:if
          test="java:isAccessibleService('metadata.version') and /root/gui/svnmanager/enabled='true'">
          <xsl:variable name="versionTitle"
                        select="concat(/root/gui/strings/versionTitle,': ',$ltitle)"/>
          <button
            onclick="doOtherButton('{/root/gui/locService}/metadata.version?id={$metadata/geonet:info/id}','{$versionTitle}',300)">
            <xsl:value-of select="/root/gui/strings/startVersion"/>
          </button>
        </xsl:if>

        <!-- Create child option only for iso19139 schema based metadata -->
        <xsl:variable name="duplicateChild"
                      select="concat(/root/gui/strings/createChild,': ',$ltitle)"/>
        <xsl:if test="contains(geonet:info/schema, 'iso19139')">
          <button
            onclick="load('{/root/gui/locService}/metadata.duplicate.form?uuid={$metadata/geonet:info/uuid}&amp;child=y')">
            <xsl:value-of select="/root/gui/strings/createChild"/>
          </button>
        </xsl:if>

        <!-- Create/Update thesaurus option and extract register elements
                     only for iso19135 metadata -->
        <xsl:if test="contains(geonet:info/schema, 'iso19135')">
          <xsl:variable name="createThesaurus"
                        select="concat(/root/gui/strings/createThesaurus,': ',$ltitle)"/>
          <button
            onclick="doOtherButton('{/root/gui/locService}/metadata.create.thesaurus.form?uuid={$metadata/geonet:info/uuid}','{$createThesaurus}',600,150)">
            <xsl:value-of select="/root/gui/strings/createThesaurus"/>
          </button>

          <xsl:variable name="extractRegisterItems"
                        select="concat(/root/gui/strings/extractRegisterItems,': ',$ltitle)"/>
          <button
            onclick="doOtherButton('{/root/gui/locService}/metadata.batch.extract.subtemplates?uuid={$metadata/geonet:info/uuid}&amp;xpath=/grg:RE_Register/grg:containedItem/*[gco:isoType%3D&quot;grg:RE_RegisterItem&quot;]|/grg:RE_Register/grg:containedItem/grg:RE_RegisterItem&amp;extractTitle={/root/gui/schemalist/name[text()='iso19135']/@schemaConvertDirectory}extract-title.xsl&amp;category=_none_&amp;doChanges=on','{$extractRegisterItems}',600,150)">
            <xsl:value-of select="/root/gui/strings/extractRegisterItems"/>
          </button>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>


  <!-- Create a div with class name set to extentViewer in
      order to generate a new map.  -->

  <xsl:template name="showMap">
    <xsl:param name="edit"/>
    <xsl:param name="coords"/>
    <!-- Indicate which drawing mode is used (ie. bbox or polygon) -->
    <xsl:param name="mode"/>
    <xsl:param name="targetPolygon"/>
    <xsl:param name="watchedBbox"/>
    <xsl:param name="eltRef"/>
    <div class="extentViewer"
         style="width:{/root/gui/config/map/metadata/width}; height:{/root/gui/config/map/metadata/height};"
         edit="{$edit}"
         target_polygon="{$targetPolygon}"
         watched_bbox="{$watchedBbox}"
         elt_ref="{$eltRef}"
         mode="{$mode}">
      <div style="display:none;" id="coords_{$eltRef}">
        <xsl:value-of select="$coords"/>
      </div>
    </div>
  </xsl:template>

  <!-- show metadata export icons eg. in search results or metadata viewer -->
  <xsl:template name="showMetadataExportIcons">
    <xsl:param name="forBrief" select="false()"/>

    <xsl:variable name="schema" select="string(geonet:info/schema)"/>
    <xsl:variable name="mid" select="string(geonet:info/id)"/>
    <xsl:variable name="url"
                  select="concat(/root/gui/env/server/protocol,'://',/root/gui/env/server/host,':',/root/gui/env/server/port,/root/gui/locService)"/>

    <xsl:for-each select="/root/gui/schemalist/name[text()=$schema]/conversions/converter">
      <xsl:variable name="serviceName" select="concat('',@name)"/>
      <xsl:if test="java:isAccessibleService($serviceName)">
        <xsl:variable name="serviceUrl"
                      select="concat($url,'/',$serviceName,'?id=',$mid,'&amp;styleSheet=',@xslt)"/>
        <xsl:variable name="exportLabel"
                      select="/root/gui/schemas/*[name()=$schema]/strings/*[name()=$serviceName]"/>
        <xsl:choose>
          <xsl:when test="$forBrief">
            <xsl:element name="link">
              <xsl:attribute name="href">
                <xsl:value-of select="$serviceUrl"/>
              </xsl:attribute>
              <xsl:attribute name="title">
                <xsl:value-of select="$exportLabel"/>
              </xsl:attribute>
              <xsl:attribute name="type">application/xml</xsl:attribute>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <a href="{$serviceUrl}" target="_blank" title="{$exportLabel}">
              <img src="{/root/gui/url}/images/xml.png" alt="{$exportLabel}" title="{$exportLabel}"
                   border="0"/>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>

    <!-- add pdf link -->
    <xsl:variable name="pdfUrl" select="concat($url,'/pdf?id=',$mid)"/>

    <xsl:choose>
      <xsl:when test="$forBrief">
        <xsl:element name="link">
          <xsl:attribute name="href">
            <xsl:value-of select="$pdfUrl"/>
          </xsl:attribute>
          <xsl:attribute name="title">PDF</xsl:attribute>
          <xsl:attribute name="type">application/pdf</xsl:attribute>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$pdfUrl}" title="PDF">
          <img src="{/root/gui/url}/images/pdf.gif" alt="PDF" title="PDF"
               style="border:0px;max-height:16px;"/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->
  <!-- === This stylesheet transforms an harvesting node from settings XML to output XML -->
  <!-- ============================================================================================= -->

  <xsl:template match="node">
    <xsl:variable name="site" select="children/site/children"/>
    <xsl:variable name="opt" select="children/options/children"/>
    <xsl:variable name="con" select="children/content/children"/>
    <xsl:variable name="priv" select="children/privileges/children"/>
    <xsl:variable name="categ" select="children/categories/children"/>
    <xsl:variable name="info" select="children/info/children"/>
    <xsl:variable name="owner" select="children/owner"/>
    <xsl:variable name="ownerGroup" select="children/ownerGroup"/>
    <xsl:variable name="ownerUser" select="children/ownerUser"/>
    <xsl:variable name="filters" select="children/filters/children"/>
    <xsl:variable name="bboxFilter" select="children/bboxFilter/children"/>

    <node id="{@id}" type="{value}">
      <owner>
        <id>
          <xsl:value-of select="$site/ownerId/value"/>
        </id>
      </owner>
      <ownerGroup>
        <id>
          <xsl:value-of select="$site/ownerGroup/value"/>
        </id>
      </ownerGroup>
      <ownerUser>
        <id>
          <xsl:value-of select="$site/ownerUser/value"/>
        </id>
      </ownerUser>

      <site>
        <name>
          <xsl:value-of select="$site/name/value"/>
        </name>
        <uuid>
          <xsl:value-of select="$site/uuid/value"/>
        </uuid>
        <account>
          <use>
            <xsl:value-of select="$site/useAccount/value"/>
          </use>
          <username>
            <xsl:value-of select="$site/useAccount/children/username/value"/>
          </username>
          <password>
            <xsl:value-of select="$site/useAccount/children/password/value"/>
          </password>
        </account>

        <xsl:apply-templates select="$site" mode="site"/>
      </site>

      <content>
        <validate>
          <xsl:value-of select="$con/validate/value"/>
        </validate>
        <importxslt>
          <xsl:value-of select="$con/importxslt/value"/>
        </importxslt>
        <batchEdits>
          <xsl:value-of select="$con/batchEdits/value"/>
        </batchEdits>
        <translateContent>
          <xsl:value-of select="$con/translateContent/value"/>
        </translateContent>
        <translateContentLangs>
          <xsl:value-of select="$con/translateContentLangs/value"/>
        </translateContentLangs>
        <translateContentFields>
          <xsl:value-of select="$con/translateContentFields/value"/>
        </translateContentFields>
      </content>

      <options>
        <every>
          <xsl:value-of select="$opt/every/value"/>
        </every>
        <oneRunOnly>
          <xsl:value-of select="$opt/oneRunOnly/value"/>
        </oneRunOnly>
        <overrideUuid>
          <xsl:value-of select="$opt/overrideUUID/value"/>
        </overrideUuid>
        <status>
          <xsl:value-of select="$opt/status/value"/>
        </status>

        <xsl:apply-templates select="$opt" mode="options"/>
      </options>

      <xsl:apply-templates select="." mode="searches"/>
      <xsl:apply-templates select="children" mode="rawFilter"/>
      <xsl:apply-templates select="$filters" mode="filters"/>
      <xsl:apply-templates select="$bboxFilter" mode="bboxFilter"/>

      <xsl:apply-templates select="$priv" mode="privileges"/>
      <ifRecordExistAppendPrivileges>
        <xsl:value-of select="$opt/ifRecordExistAppendPrivileges/value"/>
      </ifRecordExistAppendPrivileges>
      <xsl:apply-templates select="$categ" mode="categories"/>
      <xsl:apply-templates select="." mode="other"/>

      <info>
        <lastRun>
          <xsl:value-of select="$info/lastRun/value"/>
        </lastRun>

        <lastRunSuccess>
          <xsl:value-of select="$info/lastRunSuccess/value"/>
        </lastRunSuccess>

        <elapsedTime>
          <xsl:value-of select="$info/elapsedTime/value"/>
        </elapsedTime>

        <xsl:apply-templates select="$info" mode="info"/>
      </info>
    </node>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="privileges">
    <privileges>
      <xsl:for-each select="group">
        <group id="{value}">
          <xsl:for-each select="children/operation">
            <xsl:choose>
              <xsl:when test="value = '0'">
                <operation name="view"/>
              </xsl:when>
              <xsl:when test="value = '1'">
                <operation name="download"/>
              </xsl:when>
              <xsl:when test="value = '3'">
                <operation name="notify"/>
              </xsl:when>
              <xsl:when test="value = '5'">
                <operation name="dynamic"/>
              </xsl:when>
              <xsl:when test="value = '6'">
                <operation name="featured"/>
              </xsl:when>
            </xsl:choose>
          </xsl:for-each>
        </group>
      </xsl:for-each>
    </privileges>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="categories">
    <categories>
      <xsl:for-each select="category">
        <category id="{value}"/>
      </xsl:for-each>
    </categories>
  </xsl:template>
  <!-- ============================================================================================= -->
  <!-- === Hooks -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site"/>
  <xsl:template match="*" mode="options"/>
  <xsl:template match="*" mode="searches"/>
  <xsl:template match="*" mode="filters"/>
  <xsl:template match="*" mode="rawFilter"/>
  <xsl:template match="*" mode="bboxFilter"/>
  <xsl:template match="*" mode="other"/>
  <xsl:template match="*" mode="info"/>

  <!-- ============================================================================================= -->

</xsl:stylesheet>

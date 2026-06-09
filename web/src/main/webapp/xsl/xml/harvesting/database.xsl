<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->

  <xsl:import href="common.xsl"/>

  <!-- ============================================================================================= -->
  <!-- === Database harvesting node -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site">
    <server>
      <xsl:value-of select="server/value"/>
    </server>
    <port>
      <xsl:value-of select="port/value"/>
    </port>
    <username>
      <xsl:value-of select="username/value"/>
    </username>
    <password>
      <xsl:value-of select="password/value"/>
    </password>
    <database>
      <xsl:value-of select="database/value"/>
    </database>
    <tableName>
      <xsl:value-of select="tableName/value"/>
    </tableName>
    <metadataField>
      <xsl:value-of select="metadataField/value"/>
    </metadataField>
    <databaseType>
      <xsl:value-of select="databaseType/value"/>
    </databaseType>

    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="options"/>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="searches">
    <filter>
      <field>
        <xsl:value-of select="children/filter/children/field/value" />
      </field>
      <value>
        <xsl:value-of select="children/filter/children/value/value" />
      </value>
      <operator>
        <xsl:value-of select="children/filter/children/operator/value" />
      </operator>
    </filter>
  </xsl:template>

  <!-- ============================================================================================= -->

</xsl:stylesheet>

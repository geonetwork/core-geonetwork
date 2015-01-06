<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                version="2.0">


  <xsl:function name="gn-fn-render:get-schema-strings" as="xs:string">
    <xsl:param name="strings" as="node()"/>
    <xsl:param name="key" as="xs:string"/>

    <xsl:variable name="nameInStrings"
                  select="$strings/*[name() = $key]"/>

    <xsl:value-of select="if ($nameInStrings != '')
                          then $nameInStrings
                          else $key"/>
  </xsl:function>

  <!-- Render coordinates of bbox and an images of the geometry
  using the region API -->
  <xsl:function name="gn-fn-render:bbox">
    <xsl:param name="west" as="xs:double"/>
    <xsl:param name="south" as="xs:double"/>
    <xsl:param name="east" as="xs:double"/>
    <xsl:param name="north" as="xs:double"/>

    <xsl:variable name="boxGeometry"
                  select="concat('POLYGON((',
                  $east, ' ', $south, ',',
                  $east, ' ', $north, ',',
                  $west, ' ', $north, ',',
                  $west, ' ', $south, ',',
                  $east, ' ', $south, '))')"/>
    <xsl:variable name="numberFormat" select="'0.00'"/>

    <div class="thumbnail extent">
      <span>
        <div class="input-group coord coord-north">
          <input type="text" class="form-control"
                 value="{format-number($north, $numberFormat)}" readonly=""/>
          <span class="input-group-addon">N</span>
        </div>
        <div class="input-group coord coord-south">
          <input type="text" class="form-control"
                 value="{format-number($south, $numberFormat)}" readonly=""/>
          <span class="input-group-addon">S</span>
        </div>
        <div class="input-group coord coord-east">
          <input type="text" class="form-control"
                 value="{format-number($east, $numberFormat)}" readonly=""/>
          <span class="input-group-addon">E</span>
        </div>
        <div class="input-group coord coord-west">
          <input type="text" class="form-control"
                 value="{format-number($west, $numberFormat)}" readonly=""/>
          <span class="input-group-addon">W</span>
        </div>
      </span>
      <xsl:copy-of select="gn-fn-render:geometry($boxGeometry)"/>
    </div>
  </xsl:function>


  <!-- Use region API to display an image -->
  <xsl:function name="gn-fn-render:geometry">
    <xsl:param name="geometry" as="xs:string"/>

    <!-- TODO get system config -->
    <xsl:if test="$geometry">
      <img class="gn-img-extent"
           src="region.getmap.png?mapsrs=EPSG:3857&amp;width=250&amp;background=osm&amp;geomsrs=EPSG:4326&amp;geom={$geometry}"/>
    </xsl:if>

  </xsl:function>


</xsl:stylesheet>
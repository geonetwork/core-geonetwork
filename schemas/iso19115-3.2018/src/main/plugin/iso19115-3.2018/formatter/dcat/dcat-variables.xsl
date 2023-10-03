<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all">

  <!-- https://www.dublincore.org/specifications/dublin-core/dcmi-terms/#section-7 -->
  <xsl:variable name="dcmiTypeVocabularyToIso"
                as="node()*">
    <entry key="Collection">series</entry>
    <entry key="Dataset">dataset</entry>
    <entry key="Dataset">nonGeographicDataset</entry>
    <entry key="Event"></entry>
    <entry key="Image"></entry>
    <entry key="InteractiveResource"></entry>
    <entry key="MovingImage"></entry>
    <entry key="PhysicalObject"></entry>
    <entry key="Service">service</entry>
    <entry key="Software">software</entry>
    <entry key="Sound"></entry>
    <entry key="StillImage"></entry>
    <entry key="Text"></entry>
  </xsl:variable>

</xsl:stylesheet>

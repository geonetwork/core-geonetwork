<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">

  <sch:title xmlns="http://www.w3.org/2001/XMLSchema">URL checks</sch:title>
  <sch:ns prefix="srv" uri="http://standards.iso.org/iso/19115/-3/srv/2.0"/>
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/2.0"/>
  <sch:ns prefix="gex" uri="http://standards.iso.org/iso/19115/-3/gex/1.0"/>
  <sch:ns prefix="mco" uri="http://standards.iso.org/iso/19115/-3/mco/1.0"/>
  <sch:ns prefix="mdb" uri="http://standards.iso.org/iso/19115/-3/mdb/2.0"/>
  <sch:ns prefix="mex" uri="http://standards.iso.org/iso/19115/-3/mex/1.0"/>
  <sch:ns prefix="mmi" uri="http://standards.iso.org/iso/19115/-3/mmi/1.0"/>
  <sch:ns prefix="gmw" uri="http://standards.iso.org/iso/19115/-3/gmw/1.0"/>
  <sch:ns prefix="mrc" uri="http://standards.iso.org/iso/19115/-3/mrc/2.0"/>
  <sch:ns prefix="mrd" uri="http://standards.iso.org/iso/19115/-3/mrd/1.0"/>
  <sch:ns prefix="mri" uri="http://standards.iso.org/iso/19115/-3/mri/1.0"/>
  <sch:ns prefix="mrs" uri="http://standards.iso.org/iso/19115/-3/mrs/1.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="lan" uri="http://standards.iso.org/iso/19115/-3/lan/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
  <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>
  <sch:ns prefix="xsi" uri="http://www.w3.org/2001/XMLSchema"/>
  <sch:ns prefix="xslutil" uri="java:org.fao.geonet.util.XslUtil" />

  <sch:pattern>
    <sch:title>$loc/strings/invalidURLCheck</sch:title>
    <!-- Check specification names and status -->
    <sch:rule context="//cit:linkage//gco:CharacterString[starts-with(text(), 'http')]">

      <sch:let name="status" value="xslutil:getURLStatusAsString(text())" />
      <sch:let name="isValidUrl" value="xslutil:validateURL(text())" />
      <sch:assert test="$isValidUrl = true()">
        <sch:value-of select="$loc/strings/alert.invalidURL/div" />
        <sch:value-of select="$status"/> -
        <sch:value-of select="string(.)"/>
      </sch:assert>
      <sch:report test="$isValidUrl = true()">
        <sch:value-of select="$loc/strings/alert.validURL/div" />
        '<sch:value-of select="string(.)"/>'
      </sch:report>
    </sch:rule>
  </sch:pattern>

</sch:schema>

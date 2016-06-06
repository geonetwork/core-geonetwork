<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tds-range="java:ucar.nc2.units.DateRange"
                xmlns:tds-date="java:ucar.nc2.units.DateType"
                xmlns:tds-duration="java:ucar.nc2.units.TimeDuration"
                xmlns:tds-dateunit="java:ucar.nc2.units.DateUnit"
                xmlns:tds-dateformatter="java:ucar.nc2.units.DateFormatter"
                xmlns:sdf="java:java.text.SimpleDateFormat"
                version="2.0"
                exclude-result-prefixes="xs tds-range tds-date tds-duration tds-dateunit tds-dateformatter sdf">

  <!-- ===  Convert NetCDF date to ISO Date - refer ucar.nc2.units.DateType constructor  === -->
  <!-- ===  can't use here due to issues passing nulls to extension functions                    === -->

  <xsl:template name="getThreddsDateAsUTC">
    <xsl:param name="sourceDate"/>

    <xsl:choose>
      <xsl:when test="normalize-space($sourceDate)=''"/>
      <xsl:when test="lower-case($sourceDate) = 'present'">
        <xsl:call-template name="getUtcDateTime">
          <xsl:with-param name="dateTime" select="current-dateTime()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$sourceDate/@format">
        <xsl:call-template name="getUtcDateTime">
          <xsl:with-param name="dateTime"
                          select="sdf:parse(sdf:new($sourceDate/@format),$sourceDate)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains(lower-case($sourceDate),'since')">
        <xsl:call-template name="getUtcDateTime">
          <xsl:with-param name="dateTime" select="tds-dateunit:getStandardDate($sourceDate)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="getUtcDateTime">
          <xsl:with-param name="dateTime"
                          select="tds-dateformatter:getISODate(tds-dateformatter:new(),$sourceDate)"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Convert NetCDF date range into a simple start/end date range -->

  <xsl:template name="resolveThreddsDateRange">
    <xsl:param name="start"/>
    <xsl:param name="end"/>
    <xsl:param name="duration"/>

    <!-- get start date as DateType -->

    <xsl:variable name="startDate">
      <xsl:call-template name="getThreddsDateAsUTC">
        <xsl:with-param name="sourceDate" select="$start"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="sdfStartDate">
      <xsl:if test="normalize-space($startDate)!=''">
        <xsl:value-of
          select="format-dateTime($startDate,'[Y0001]-[M01]-[D01] [H01]:[m01]:[s01] [z]')"/>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="tdsStartDate"
                  select="tds-date:new($sdfStartDate, 'yyyy-MM-dd HH:mm:ss z', '')"/>

    <!-- get end date as DateType -->

    <xsl:variable name="endDate">
      <xsl:call-template name="getThreddsDateAsUTC">
        <xsl:with-param name="sourceDate" select="$end"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="sdfEndDate">
      <xsl:if test="normalize-space($endDate)!=''">
        <xsl:value-of
          select="format-dateTime($endDate,'[Y0001]-[M01]-[D01] [H01]:[m01]:[s01] [z]')"/>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="tdsEndDate"
                  select="tds-date:new($sdfEndDate, 'yyyy-MM-dd HH:mm:ss z', '')"/>

    <!-- get DateRange  -->

    <xsl:variable name="tdsDuration" select="tds-duration:new(string($duration))"/>
    <xsl:variable name="tdsResolution" select="tds-duration:new('')"/>
    <xsl:variable name="dateRange"
                  select="tds-range:new($tdsStartDate,$tdsEndDate,$tdsDuration,$tdsResolution)"/>

    <!-- return as fragment -->

    <startDate>
      <xsl:value-of select="tds-date:toDateTimeStringISO(tds-range:getStart($dateRange))"/>
    </startDate>
    <endDate>
      <xsl:value-of select="tds-date:toDateTimeStringISO(tds-range:getEnd($dateRange))"/>
    </endDate>

  </xsl:template>


  <!-- === Function to return dateTime in UTC time zone === -->

  <xsl:template name="getUtcDateTime">
    <xsl:param name="dateTime"/>
    <xsl:value-of select="adjust-dateTime-to-timezone($dateTime,xs:dayTimeDuration('-PT0H'))"/>
  </xsl:template>

</xsl:stylesheet>

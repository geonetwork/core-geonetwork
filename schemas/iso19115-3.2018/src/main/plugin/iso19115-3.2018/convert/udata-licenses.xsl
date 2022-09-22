<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- taken from https://www.data.gouv.fr/api/1/datasets/licenses/ -->
    <xsl:variable name="udataLicenses">
        <license>
            <id>cc-by</id>
            <title>Creative Commons Attribution</title>
            <url>http://www.opendefinition.org/licenses/cc-by</url>
        </license>
        <license>
            <id>cc-by-sa</id>
            <title>Creative Commons Attribution Share-Alike</title>
            <url>http://www.opendefinition.org/licenses/cc-by-sa</url>
        </license>
        <license>
            <id>cc-zero</id>
            <title>Creative Commons CCZero</title>
            <url>http://www.opendefinition.org/licenses/cc-zero</url>
        </license>
        <license>
            <id>fr-lo</id>
            <title>Licence Ouverte / Open Licence</title>
            <url>https://www.etalab.gouv.fr/wp-content/uploads/2014/05/Licence_Ouverte.pdf</url>
        </license>
        <license>
            <id>lov2</id>
            <title>Licence Ouverte / Open Licence version 2.0</title>
            <url>https://www.etalab.gouv.fr/licence-ouverte-open-licence</url>
        </license>
        <license>
            <id>notspecified</id>
            <title>License Not Specified</title>
            <url></url>
        </license>
        <license>
            <id>odc-by</id>
            <title>Open Data Commons Attribution License</title>
            <url>http://opendatacommons.org/licenses/by/summary/</url>
        </license>
        <license>
            <id>odc-odbl</id>
            <title>Open Data Commons Open Database License (ODbL)</title>
            <url>http://opendatacommons.org/licenses/odbl/summary/</url>
        </license>
        <license>
            <id>odc-pddl</id>
            <title>Open Data Commons Public Domain Dedication and Licence (PDDL)</title>
            <url>http://opendatacommons.org/licenses/pddl/summary/</url>
        </license>
        <license>
            <id>other-at</id>
            <title>Other (Attribution)</title>
            <url></url>
        </license>
        <license>
            <id>other-open</id>
            <title>Other (Open)</title>
            <url></url>
        </license>
        <license>
            <id>other-pd</id>
            <title>Other (Public Domain)</title>
            <url></url>
        </license>
    </xsl:variable>
</xsl:stylesheet>

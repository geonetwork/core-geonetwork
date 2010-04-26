How to execute the CSW ISO AP tests
-----------------------------------

1) Load in the catalog the metadata provided in cswtest\testdata folder

2) Configure the catalog server URL in the test scripts (cswtest\scripts\csw_ap_iso1.0\ctl\main.ctl)

      <xsl:variable name="csw.capabilities.url">
		http://SERVER_URL/srv/en/csw?request=GetCapabilities&amp;service=CSW
      </xsl:variable>

3) Execute ant command in cswtest folder
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="#all">

	<xsl:template match="/" priority="5">
		<html>
			<!-- Set some vars. -->
			<xsl:variable name="title"
				select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />

			<head>
        <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
				<title>
					Metadata:
					<xsl:value-of select="$title" />
				</title>
				
				<link rel="stylesheet" type="text/css" href="{root/url}../../apps/js/ext/resources/css/ext-all.css"/>
				<link rel="stylesheet" type="text/css" href="{root/url}../../apps/js/ext/resources/css/xtheme-gray.css"/>
				<link rel="stylesheet" type="text/css" href="{root/url}../../apps/sextant/css/gndefault.css"/>
				<link rel="stylesheet" type="text/css" href="{root/url}../../apps/sextant/css/gnmetadatadefault.css"/>
				<link rel="stylesheet" type="text/css" href="{root/url}../../apps/sextant/css/main.css"/>
				<link rel="stylesheet" type="text/css" href="{root/url}../../apps/sextant/css/schema/main.css"/>
			</head>
			<body class="view-win">

			</body>
      <script >
        <xsl:variable name="apos">'</xsl:variable>
        var title = '<xsl:value-of select="translate($title,$apos,concat('\', $apos))" />';
      </script>

      <script src="{root/url}../../apps/js/ext/adapter/ext/ext-base.js">//</script>
      <script src="{root/url}../../apps/js/ext/ext-all.js">//</script>
      <script src="{root/url}../../static/geonetwork-client-mini-nomap.js">//</script>

      <script src="{root/url}../../apps/sextant/js/cat.lang/fr.js">//</script>
      <script src="{root/url}../../apps/sextant/js/cat.lang/en.js">//</script>

      <script src="{root/url}../../apps/sextant/js/Settings.js">//</script>
      <script src="{root/url}../../apps/sextant/js/cat.map/Settings.js">//</script>
      <script src="{root/url}../../apps/sextant/js/cat.ViewWindow.js">//</script>
      <script src="{root/url}../../apps/sextant/js/cat.ViewPanel.js">//</script>

      <script src="{root/url}md.formatter.resource?id=mdviewer&amp;fname=js/fmt-mdviewer-main.js">//</script>

    </html>
	</xsl:template>

</xsl:stylesheet>
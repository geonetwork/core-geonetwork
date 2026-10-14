This formatter is based on XSLT provided by SEMIC-EU.
See https://github.com/SEMICeu/iso-19139-to-dcat-ap/blob/main/iso-19139-to-dcat-ap.xsl

When updating it, preserve the only addition made here ie. `mode="iso19139-to-dcatap"` added to the `xsl:template` element.
This is required for the formatter to be used in other XSLT eg. in `iso19115-3` schema.

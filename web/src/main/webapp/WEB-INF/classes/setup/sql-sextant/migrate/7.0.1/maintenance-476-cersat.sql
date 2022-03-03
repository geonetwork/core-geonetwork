WITH ns AS (
  select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
  ARRAY['mri', 'http://standards.iso.org/iso/19115/-3/mri/1.0'],
  ARRAY['cit', 'http://standards.iso.org/iso/19115/-3/cit/2.0'],
  ARRAY['mcc', 'http://standards.iso.org/iso/19115/-3/mcc/1.0'],
  ARRAY['gco', 'http://standards.iso.org/iso/19115/-3/gco/1.0']] AS n
  )

SELECT uuid,
       xpath('//cit:alternateTitle/gco:CharacterString[1]/text()', XMLPARSE(DOCUMENT data), n) as alternateTitle,
       xpath('//mri:citation/*/cit:identifier/*/mcc:code/gco:CharacterString[1]/text()', XMLPARSE(DOCUMENT data), n) as doi
FROM metadata, ns
       WHERE schemaId = 'iso19115-3.2018' and isharvested = 'n';


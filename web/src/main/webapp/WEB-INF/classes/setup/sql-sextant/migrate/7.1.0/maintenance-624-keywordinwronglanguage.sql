WITH ns AS (
  select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
  ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
  ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
  ),
   vars(target, replacement)
    AS (VALUES ('Habitats et biotopes', 'Habitats and biotopes'))

UPDATE metadata SET data = replace(data,
  '>' || vars.target || '<',
  '>' || vars.replacement || '<')
FROM vars
WHERE uuid IN (
  SELECT uuid FROM
    (SELECT uuid,
            xpath('/*/gmd:language/*/@codeListValue',
                  XMLPARSE(DOCUMENT data), n) AS recordLanguage,
            xpath('/*/gmd:locale/*/@id',
                  XMLPARSE(DOCUMENT data), n) AS otherLanguage
     FROM metadata, ns, vars
     WHERE isharvested = 'n'
       AND data LIKE '%>' || vars.target || '<%') AS list
  WHERE array_length(otherLanguage, 1) IS NULL
    AND recordLanguage[1]::text = 'eng'
);


WITH ns AS (
  select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
           ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
           ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
),
     vars(target, replacement)
       AS (VALUES ('Répartition des espèces', 'Species distribution'))

UPDATE metadata SET data = replace(data,
                                   '>' || vars.target || '<',
                                   '>' || vars.replacement || '<')
FROM vars
WHERE uuid IN (
  SELECT uuid FROM
    (SELECT uuid,
            xpath('/*/gmd:language/*/@codeListValue',
                  XMLPARSE(DOCUMENT data), n) AS recordLanguage,
            xpath('/*/gmd:locale/*/@id',
                  XMLPARSE(DOCUMENT data), n) AS otherLanguage
     FROM metadata, ns, vars
     WHERE isharvested = 'n'
       AND data LIKE '%>' || vars.target || '<%') AS list
  WHERE array_length(otherLanguage, 1) IS NULL
    AND recordLanguage[1]::text = 'eng'
);

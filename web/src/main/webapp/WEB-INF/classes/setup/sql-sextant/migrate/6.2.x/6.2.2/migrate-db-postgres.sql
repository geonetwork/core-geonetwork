
UPDATE metadata SET data = REPLACE(data, '>WFS<', '>OGC:WFS<') WHERE data LIKE '%>WFS<%';
UPDATE metadata SET data = REPLACE(data, '>WCS<', '>OGC:WCS<') WHERE data LIKE '%>WCS<%';
UPDATE metadata SET data = REPLACE(data, 'WWW:LINK-1.0-http--link', 'WWW:LINK') WHERE data LIKE '%WWW:LINK-1.0-http--link%';
UPDATE metadata SET data = REPLACE(data, 'OGC:WMS-1.1.1-http-get-map', 'OGC:WMS') WHERE data LIKE '%OGC:WMS-1.1.1-http-get-map%';

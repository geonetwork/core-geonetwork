
UPDATE Metadata SET data = replace(data, '>FTP<', '>WWW:FTP<')
 WHERE data LIKE '%>FTP<%' AND schemaId = 'iso19115-3.2018';
UPDATE Metadata SET data = replace(data, '>OPENDAP<', '>WWW:OPENDAP<')
 WHERE data LIKE '%>OPENDAP<%' AND schemaId = 'iso19115-3.2018';
UPDATE Metadata SET data = replace(data, '>THREDDS<', '>WWW:LINK<')
 WHERE data LIKE '%>THREDDS<%' AND schemaId = 'iso19115-3.2018';



UPDATE metadata SET data = replace(data, '>Ascii<', '>XYZ Ascii<');

UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.emodnet.hydrography';

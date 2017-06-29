

UPDATE metadata SET data = replace(data, '>Ascii<', '>XYZ Ascii<');

UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.emodnet.hydrography';

UPDATE metadata SET data = replace(data,
  'Max elapsed time between last input data records  update and product creation date',
  'Max elapsed time between last input data records  update and product creation date. Minimum value 1/24 day.')
  WHERE schemaid = 'iso19115-3';

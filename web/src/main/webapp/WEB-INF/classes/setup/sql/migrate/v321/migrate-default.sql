INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/cors/allowedHosts', '*', 0, 561, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/bingKey', 'AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D', 0, 9595, 'n');

UPDATE Settings SET value='3.2.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


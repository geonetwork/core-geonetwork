UPDATE Settings SET internal = 'n' WHERE name = 'system/server/sitemapLinkUrl';

UPDATE Settings SET value='4.2.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- Set default timezone to UTC if not set.
UPDATE Settings SET value = 'Etc/UTC' WHERE name = 'system/server/timeZone' AND VALUE is null;

-- eg. CET
-- UPDATE Settings SET value = 'Europe/Copenhagen' WHERE name = 'system/server/timeZone' AND VALUE = '';

-- Check
-- SELECT value FROM Settings WHERE name = 'system/server/timeZone';

-- From 4.0.2 version, db dates MUST be in UTC.

-- Check db dates timezone (usually server timezone)
-- * Server timezone eg. date +"%Z %z"
-- UTC +0000
-- CET +0100
--
-- * DB timezone
-- SHOW timezone;
-- UTC

-- * Server timezone setting = DB timezone = UTC = Nothing to update. Will only add Z
-- 2020-10-29T16:11:55 > 2020-10-29T16:11:55Z
-- * Server timezone setting != UTC.
-- Shift old DB dates from server timezone to UTC
-- eg. for CET
-- UPDATE metadata SET (createdate, changedate) = (
--        to_char(
--            timezone('UTC',
--         to_timestamp(createDate, 'YYYY-MM-DDThh24:mi:ss')
--         AT TIME ZONE (SELECT value FROM Settings WHERE name = 'system/server/timeZone')), 'YYYY-MM-DDThh24:mi:ssZ'),
--        to_char(
--         timezone('UTC',
--          to_timestamp(changedate, 'YYYY-MM-DDThh24:mi:ss')
--          AT TIME ZONE (SELECT value FROM Settings WHERE name = 'system/server/timeZone')), 'YYYY-MM-DDThh24:mi:ssZ')
--        ) WHERE length(createdate) = 19 AND length(changedate) = 19;

-- column GUF_UserFeedback_uuid is magically added by the application (Hibernate?)
--ALTER TABLE guf_userfeedbacks_guf_rating ADD GUF_UserFeedback_uuid varchar(255);
UPDATE guf_userfeedbacks_guf_rating SET GUF_UserFeedback_uuid = GUF_UserFeedbacks_uuid;
ALTER TABLE guf_userfeedbacks_guf_rating DROP COLUMN GUF_UserFeedbacks_uuid;

UPDATE Settings SET value='4.0.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

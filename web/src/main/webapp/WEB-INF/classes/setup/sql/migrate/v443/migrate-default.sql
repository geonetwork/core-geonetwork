INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/documentation/url', 'https://docs.geonetwork-opensource.org/{{version}}/{{lang}}', 0, 570, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/metadata/enable', 'false', 2, 1913, 'n');


UPDATE Settings SET value='4.4.3' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

CREATE TABLE spg_page_group
(
    language VARCHAR(255) not null,
    linktext VARCHAR(255) not null,
    groupid int not null,
    primary key(language,linktext, groupid),
    foreign key(language, linktext) references spg_page(language, linktext),
    foreign key(groupid) references groups(id)
);


SELECT *
FROM (SELECT metadataid, filename, count(*) AS c
      FROM metadatafileuploads
      GROUP BY metadataid, filename) AS s
WHERE c > 1;
-- = 580

CREATE TABLE metadatafileuploadsbackup AS SELECT * FROM metadatafileuploads;

DELETE FROM metadatafileuploads WHERE id IN (
  SELECT id
  FROM (
         SELECT *,
                ROW_NUMBER() OVER(PARTITION BY metadataid, filename ORDER BY uploaddate DESC) AS row_number
         FROM metadatafileuploads
         WHERE metadataid IN (SELECT metadataid
                              FROM (SELECT metadataid, filename, count(*) AS c
                                    FROM metadatafileuploads
                                    GROUP BY metadataid, filename) AS s
                              WHERE c > 1)
         ORDER BY metadataid, filename) AS F
  WHERE row_number > 1);


SELECT *
FROM (SELECT metadataid, filename, count(*) AS c
      FROM metadatafileuploads
      GROUP BY metadataid, filename) AS s
WHERE c > 1;
-- = 0



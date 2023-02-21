-- Column names change in GeoNetwork 4.x for spg_sections table
ALTER TABLE spg_sections RENAME COLUMN page_language TO spg_page_language;
ALTER TABLE spg_sections RENAME COLUMN page_linktext TO spg_page_linktext;

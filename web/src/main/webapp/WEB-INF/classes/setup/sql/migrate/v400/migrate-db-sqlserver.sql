-- Column names change in GeoNetwork 4.x for spg_sections table
EXEC sp_rename 'spg_sections.page_language', 'spg_sections.spg_page_language';
EXEC sp_rename 'spg_sections.page_linktext', 'spg_sections.spg_page_linktext';

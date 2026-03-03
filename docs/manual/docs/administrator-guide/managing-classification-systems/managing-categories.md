# Managing categories

The catalog has a concept of categories that can be assigned to metadata documents, but these are not represented in the metadata (not encoded in the XML document). So when the metadata is exported or harvested, the category is not available. You can use these categories to separate documents into groups, without changing the actual content of the metadata.

Categories can be used to filter a search result, or limit the output of a custom portal.

To assign a category to a metadata document. Go to the metadata modification form and select the requested category from the pull down in the menu. Then save your metadata.

To modify the available categories in the catalog, from the admin page, open the "classification systems" and then the "category" tab.

Note: If you add or modify categories, they may not obtain an appropriate icon. These icon are managed in `/catalog/style/gn_icons.less`. In this file category-classes are mapped to font-awesome variables that map to a certain [font-awesome icon](https://fontawesome.io).

Usually, it is recommended to use keywords with a thesaurus instead of categories. The main benefit of categories is that it is not encoded in the metadata. 
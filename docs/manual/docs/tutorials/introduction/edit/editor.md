# Customize Editor (Optional) {#tuto-introduction-editor}

Each schema can configure different editor views. This configurations can be found on the file layout/config-editor.xml of each schema.

This file has four main blocks:

Fields:

``` xml
<!-- Form field type configuration. Default is text. -->
<fields>
  <for name="gmd:abstract" use="textarea"/>
  <for name="gco:Distance" use="number"/>
  <for name="gmd:onLine" addDirective="data-gn-directory-entry-selector">
  <directiveAttributes data-template-add-action="true" data-template-type="onLine" data-filter='{"_root": "gmd:CI_OnlineResource"}'/>
  </for>
</fields>
```

This defines for each XSD defined type, which type of input to use. As with gmd:onLine, a customized angular input type can be defined.

After defining all the simple fields, we define a list of fieldsets, which the editor will recognize as something to put together:

``` xml
<fieldsWithFieldset>
  <name>gmd:MD_Metadata</name>
  <name>gmd:identificationInfo</name>
</fieldsWithFieldset>
```

Now we define what fields are multilingual. Usually most of the fields are multilingual, so we can just define which ones to exclude.

``` xml
<multilingualFields>
  <!-- In multilingual mode, define which mode the widget should have. If expanded, then one field per language is displayed. -->
  <expanded>
    <name>gmd:title</name>
    <name>gmd:abstract</name>
  </expanded>
  <exclude>
    <name>gmd:identifier</name>
  </exclude>
</multilingualFields>
```

Finally, we define the views, which will customize which fields will be shown when editing the metadata. We can define more than one view per schema, so the editor user can select which one to use when editing the metadata.

When a non-final field is defined on a view, the editor will automatically generate all the input fields inside it. So, you don't have to explicitly define all the fields you want to show. The simplest view is the one based on the XSD of the schema.

The xml view can also be easily defined:

``` xml
<view name="xml">
  <tab id="xml" default="true"/>
</view>
```

If the view is too big, we can also define a set of tabs inside the editor, so not all fields are shown at the same time. Remember that when the user changes the tab, the content will be saved. So we should place related fields inside the same tab.

Remember that you have to restart the application container (Tomcat) every time you modify this file.

See more on [Customizing editor](../../../customizing-application/editor-ui/creating-custom-editor.md).

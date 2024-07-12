# Suggestion for improving metadata content {#metadata_suggestion}

The metadata editor can be configured to analyse metadata and make suggestions to improve it. Examples of this include exploding a list of comma-separated keywords in a single element into multiple individual elements.

The processes available in a given metadata schema are defined in the schemas `suggest.xsl` file, for example <https://github.com/metadata101/iso19139.mcp/blob/master/src/main/plugin/iso19139.mcp/suggest.xsl>

The xsl files for each process are in the `process` folder within the schema.

To use the metadata suggestions service, it must be defined within at least one of the editing layout views for the schema.

In editing mode, click the icon in the suggestions wizard to run the service on your metadata:

![](img/suggestion-wizard.png)

If there are no valid suggestions then the process will return no results. If there are valid suggestions, such as the keyword exploder above, this will be shown in the results:

![](img/suggestion-results.png)

Click the task to run the xsl and transform the metadata. Using the keyword exploder service this will result in going from:

![](img/keywords-concatenated.png)

to:

![](img/keywords-exploded.png)

## Creating a new process

See <https://geonetwork-opensource.org/manuals/3.6.x/en/user-guide/workflow/batchupdate-xsl.html?highlight=process#adding-batch-process> for information on how to add a new process. The new process must then be registered in `suggest.xsl`

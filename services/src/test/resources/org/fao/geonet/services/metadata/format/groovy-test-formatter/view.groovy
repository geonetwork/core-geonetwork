// The view.groovy script is a groovy script which must configure the handlers object
// (org.fao.geonet.services.metadata.format.groovy.Handlers).  To script has
// the following variables bound before execution:
//
// * handlers - an org.fao.geonet.services.metadata.format.groovy.Handlers object
// * f - an org.fao.geonet.services.metadata.format.groovy.Functions object




// the handlers.roots method allows you to select the elements in the document where the recursive processing
// of the xml should begin.  If this is not present then there will be a single root which is
// root element of the metadata.
// The strings in the roots method are XPath selectors as take by org.fao.geonet.utils.Xml#selectNodes()
// All nodes selected by the selectors will be the root nodes
//
// the method roots will set the roots, the method root will add to the existing roots
handlers.roots ('gmd:identificationInfo/*', 'gmd:referenceSystemInfo')

// According to Groovy the brackets in a method call are optional (in many cases) so roots can be written:
// handlers.roots 'gmd:identificationInfo/*', 'gmd:distributionInfo//*[gco:CharacterString]'

// a root can also be added by calling: 
handlers.root 'gmd:DataQuality'


// the handlers object is used to register handlers for the identified elements
// there are a few ways to specify what a handler can handle.
//
// The first and most performant is to specify the name of the element
// in the following any time a gmd:abstract element is encountered the 
// function/closer will be executed passing in the selected element
// as mentioned above a handle function can return a string, file or XML
// in the case below it returns a string which will be parsed into XML
// groovy has multi-line strings with interpolation (see below)
//
// exact name have a default priority of 1 where as matchers that use functions to do the matching have a priority of 0
// the matchers are checked to see if they are applied first by priority and then in the order that they are defined.
//
// The parameters passed to the handler are:
// * the GPathResult representing the current node
// * a groovy.xml.MarkupBuilder - if null any data is written to this then all other return values are ignored
// * A string representing the data obtained from processing all child data.
//   Children are processed only if
//     1. There are at least 3 parameters in the handler
//     2. processChildren is true (the default) (see later example for configuring extra parameters on the handlers)
//   If the child parameter is present but the handler is configured with processChildren as false then "" will be passed as childData
//
// Like Javascript you only need to specify as many parameters as needed.
//
// Handlers can return:
// * a string
// * a org.fao.geonet.services.metadata.format.groovy.FileResult
// * if any data is written to the html parameter below (name is not important, the object is a groovy.xml.MarkupBuilder) then
//   the return value is ignored and the data written to the markup builder is used.
handlers.add 'gmd:abstract', { el ->
    // Don't need a return because last expression of a function is
    // always returned in groovy
    """<p class="abstract">
         <span class="label">${f.label('gmd:abstract')}</span>
         <span class="value">${el.'gco:CharacterString'.text()}</span>
       </p>"""
}

// A start handler is executed when the view creation process begins.  This is required when
// there are multiple roots or if there will be multiple top level elements.
// For example if the root is gmd:spatialRepresentation and there are multiple spatialRepresentation
// elements, then start and end handlers are required to wrap the elements with a single top-level
// element
//
// There is ever only one start and one end handler calling the method multiple times will replace the previous handler and thus
// the first call will have no effect.
//
// There are no parameters to a start and end handler
handlers.start {
    '''<html>
    <body>
'''
}
// End handlers are executed when the metadata is finished being processed
handlers.end {
    '''    </body>
</html>
'''
}

// In addition to matching an element name exactly a regexp can be used for the matching
// Like Javascript regular expression in groovy start and end with /
handlers.add ~/...:title/, { el ->
    """<p class="title">
         <span class="label">${f.label(el)}</span>
         <span class="value">${el.'gco:CharacterString'.text()}</span>
       </p>"""
}

// it is also possible to match on the path of the node.  When matching against a path the path separator is
// > instead of / because / is the terminator of a regular expression in Groovy
//
// The function in this case turns a static method in the class Iso19139Functions into a function (done by the & operator)
// and passes that function as the handler.
//
// The following directories will be scanned for groovy files and made available to the script
// * format bundle directory
// * formatter/groovy directory
// * schema_plugins/<schema>/formatter/groovy
handlers.withPath ~/[^>]+>gmd:identificationInfo>.+extent>.+>gmd:geographicElement/, Iso19139Functions.&handleExtent

// This example is similar but the class is from the <root formatter dir>/groovy
handlers.withPath ~/[^>]+>gmd:identificationInfo>[^>]+>gmd:pointOfContact/, SharedFunctions.&text

// Methods can be defined which can used anywhere in the script.
// This method will take an element which has gco:CharacterString and/or gmd:PT_FreeText
// children and finds the translation that best matches the UI language
// the UI language is a global variable available to the script as f.lang3 and f.lang2
// where they are the 3 and 2 letter language codes respectively
def isoText = { el ->
    def uiCode = "#f.lang2.toUpperCase()" // using interpolation to create a code like #DE
    def locStrings = el.'**'.find{ it.name == 'gmd:LocalisedCharacterString'}
    def ptEl = locStrings.find{it.'@locale' == uiCode}
    if (ptEl != null) return ptEl.text()
    if (el.'gco:CharacterString') return el.'gco:CharacterString'.text()
    if (!locStrings.isEmpty) return locStrings[0].text()
    ""
}

// A second way to define a handler is to provide a function as the first parameter which returns a
// boolean. (Again you don't need a return because return is implicit)
//  In groovy functions there is a magic _it_ variable which refers to the single parameter
// passed in.  You can either simply use it in the function or define a parameter like el ->
// The matcher function can have 0 - 2 parameters, they are:
// * GPathResult - the current node
// * String - the full path of the node
def isRefSysCode = {el, path -> el.name() == 'gmd:code' && path.contains ('gmd:referenceSystemInfo')}

// due to a limitation of the groovy language the closure must always be the last argument so the matcher
// must either be a method and provide the &methodName reference or be assigned to a variable like in this
// example
handlers.add isRefSysCode, { el, html ->
    html.p ('class' : 'code') {
        span ('class' : 'label', f.label(el.name())) // translate is a method provided by framework
        span ('class' : 'value', isoText(el))
    }
}

// This example illustrates another way of configuring a handler. this add method take a map of values and
// constructs a handler from them.  The values that will be used from the map are:
//
// * select - the function for determining if this handler should be applied
// * priority - handlers with a higher priority will be evaluated before handlers with a lower priority
// * processChildren - if true the handler function takes at least 3 parameters then all children of this node will be processed
//                     and that data passed to the function for use by the handler
handlers.add select: { it.children().size() > 0 }, priority: 10, processChildren: true, { el, html, childData ->
    // we are returning a FileResult which has a path to the file as first parameter and takes a map
    // of String -> Object which are the replacements.  When this is returned the file will be loaded
    // (UTF-8 by default) and all parts of the file with the pattern: ${key} will be replaced with the
    // the value in the replacement map.  So in this example ${label} will be replaced with the
    // translated node name and ${children} will be replaced with the children XML.
    //
    // the files are relative to the view.groovy file so this example should be in the same directory as view.groovy
    if (!childData.isEmpty()) {
        return handlers.fileResult("block.html", [label: f.label(el.name()), childData: childData])
    }

    // return null if we don't want to add this element, just because it matches doesn't mean it has to produce data
}

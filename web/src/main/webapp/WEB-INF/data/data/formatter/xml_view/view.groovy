import groovy.xml.XmlUtil

new common.Handlers(handlers, f, env).addDefaultStartAndEndHandlers()

handlers.add {true} { el ->
    def out = new StringWriter()
    new XmlNodePrinter(new PrintWriter(out)).print(new XmlParser().parseText(XmlUtil.serialize(el)))
    def xmlString = XmlUtil.escapeXml(out.toString())
    """
<link rel='stylesheet' href='${env.resourceUrl}highlightjs.css' />
 <pre>
  <code class='html'>
  $xmlString
  </code>
 </pre>
 <script src="${env.resourceUrl}highlight-json-xml.js"></script>
 <script>hljs.initHighlightingOnLoad();</script>
"""
}
import iso19139.SxtSummaryFactory

def isoHandlers = new iso19139.Handlers(handlers, f, env)

SxtSummaryFactory.summaryHandler({it.parent() is it.parent()}, isoHandlers)

isoHandlers.addDefaultHandlers()

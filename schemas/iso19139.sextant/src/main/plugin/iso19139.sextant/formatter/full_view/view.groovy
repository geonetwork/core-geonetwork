import iso19139sxt.SummaryFactory

def isoHandlers = new iso19139sxt.Handlers(handlers, f, env)

SummaryFactory.summaryHandler({it.parent() is it.parent()}, isoHandlers)

isoHandlers.addDefaultHandlers()

import iso19139.SummaryFactory

def isoHandlers = new iso19139.Handlers(handlers, f, env)

SummaryFactory.summaryHandler('gmd:distributionInfo', isoHandlers)

isoHandlers.addDefaultHandlers()

handlers.roots("gmd:distributionInfo")
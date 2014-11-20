import iso19139.SummaryFactory

def isoHandlers = new iso19139.Handlers(handlers, f, env)

SummaryFactory.summaryHandler('gmd:referenceSystemInfo', isoHandlers)

isoHandlers.addDefaultHandlers()

handlers.roots("gmd:referenceSystemInfo")
import iso19139.SummaryFactory

def isoHandlers = new iso19139.Handlers(handlers, f, env)

isoHandlers.addDefaultHandlers()

SummaryFactory.summaryHandler('gmd:identificationInfo', isoHandlers)

handlers.roots("gmd:identificationInfo")
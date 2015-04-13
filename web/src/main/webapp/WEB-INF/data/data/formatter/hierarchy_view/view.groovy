handlers.add select: ~/.*/, {els ->
    handlers.fileResult("html/associated.html", ["associated" : new common.Handlers(handlers, f, env).loadHierarchyLinkBlocks()])
}
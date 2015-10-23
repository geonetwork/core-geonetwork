package org.fao.geonet.services.main;

import com.google.common.collect.Sets;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Set;

/**
 * Select a list of elements stored in session
 * Returns status
 */
@Controller("selection")
@ReadWriteController
public class Select implements ApplicationContextAware {
    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Select one or more metadata
     *
     * @param id       One or more id parameters. Use metadata UUID.
     * @param selected Type of action on selection ({@link SelectionManager#updateSelection(String, UserSession, Element, ServiceContext)})
     * @param type     Only metadata can be selected. Unused for now. Leave blank
     * @return The number of select records
     * @throws Exception
     */
    @RequestMapping(value = {"/{lang}/metadata.select", "/{lang}/md.select"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    String[] select(@RequestParam(required = false) String[] id,
                   @RequestParam(required = false) String selected,
                   @RequestParam(required = false, defaultValue = "metadata") String type)
            throws Exception {
        ServiceContext serviceContext = ServiceContext.get();

        int nbSelected = SelectionManager.updateSelection(type,
                serviceContext.getUserSession(),
                selected, id != null ? Arrays.asList(id) : null,
                serviceContext);

        return new String[]{nbSelected + ""};
    }

    /**
     *
     * @param type
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{lang}/md.selected",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    Set<String> getCurrentSelection(
                    @RequestParam(required = false, defaultValue = "metadata") String type)
            throws Exception {
        ServiceContext serviceContext = ServiceContext.get();

        SelectionManager selectionManager =
                SelectionManager.getManager(serviceContext.getUserSession());

        synchronized (selectionManager.getSelection("metadata")) {
            return selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
        }
    }

    /**
     * @deprecated Use the service with JSON format as output
     *
     * @throws Exception
     */
    @RequestMapping(value = "/{lang}/metadata.select",
            produces = {MediaType.APPLICATION_XML_VALUE})
    public
    @ResponseBody
    SelectServiceResponse selectAsXML(@RequestParam(required = false) String[] id,
                    @RequestParam(required = false) String selected,
                    @RequestParam(required = false, defaultValue = "metadata") String type)
            throws Exception {
        ServiceContext serviceContext = ServiceContext.get();

        int nbSelected = SelectionManager.updateSelection(type,
                serviceContext.getUserSession(),
                selected, id != null ? Arrays.asList(id) : null,
                serviceContext);

        return new SelectServiceResponse().setSelected("" + nbSelected);
    }


}
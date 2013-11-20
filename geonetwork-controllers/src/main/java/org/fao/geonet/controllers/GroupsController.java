package org.fao.geonet.controllers;

import jeeves.constants.Jeeves;
import org.fao.geonet.beans.GroupBean;
import org.fao.geonet.domain.Group;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.services.GroupsService;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.data.jpa.domain.Specifications.not;

@Controller
@Transactional
public class GroupsController extends BaseController {

    @Autowired
    GroupsService service;

    @RequestMapping(value = "groups/list.html", method = RequestMethod.GET)
    @ResponseBody
    public String list(ModelMap map, HttpServletResponse response) throws Exception {
        Element elRes = context.getBean(GroupRepository.class).findAllAsXml(not(GroupSpecs.isReserved()));

        //Element elOper = params.getChild(Jeeves.Elem.OPERATION);

        //if (elOper != null)
        //    elRes.addContent(elOper.detach());

        elRes.setName(Jeeves.Elem.RESPONSE);

        return buildHtmlPage(elRes, "xsl/group-list.xsl");
    }


    @RequestMapping(value = "groups/edit.html", method = RequestMethod.GET)
    @ResponseBody
    public String edit(@RequestParam(value="id", required=true) String id, ModelMap map, HttpServletResponse response) throws Exception {

        final Group group = context.getBean(GroupRepository.class).findOne(Integer.valueOf(id));

        Element responseEl = new Element(Jeeves.Elem.RESPONSE);

        if (group != null) {
            Element groupEl = group.asXml();
            responseEl.addContent(groupEl);
        }

        return buildHtmlPage(responseEl, "xsl/group-update.xsl");
    }


    @RequestMapping(value = "groups/update.html", method = RequestMethod.POST)
    @ResponseBody
    public String update(@ModelAttribute("group") final GroupBean group, ModelMap map, HttpServletResponse response) throws Exception {
        final Element elRes = new Element(Jeeves.Elem.RESPONSE);

        service.update(group);

        elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.UPDATED));

        return buildHtmlPage(elRes, "xsl/group-list.xsl");
    }
}

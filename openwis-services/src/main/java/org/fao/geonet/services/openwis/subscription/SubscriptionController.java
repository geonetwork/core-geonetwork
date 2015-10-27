package org.fao.geonet.services.openwis.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.OpenwisDownload;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OpenwisDownloadRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.openwis.subscription.job.DirectDownloadJob;
import org.fao.geonet.services.openwis.util.Request;
import org.fao.geonet.services.openwis.util.Request.ColumnCriterias;
import org.fao.geonet.services.openwis.util.Request.OrderCriterias;
import org.fao.geonet.services.openwis.util.Response;
import org.openwis.request.client.AdHoc;
import org.openwis.request.client.RequestClient;
import org.openwis.subscription.client.ProductMetadata;
import org.openwis.subscription.client.SortDirection;
import org.openwis.subscription.client.Subscription;
import org.openwis.subscription.client.SubscriptionColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

/**
 * Controller class for Subscriptions.
 *
 * @author Jose García
 */
@Controller("openwis.subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionManager manager;

    @Autowired
    private RequestClient requestClient;

    @Autowired
    private OpenwisDownloadRepository openwisRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private ServiceManager serviceManager;

    @RequestMapping(value = {
            "/{lang}/openwis.subscription.search" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Response search(@ModelAttribute Request request,
            HttpServletRequest httpRequest,
            @RequestParam(required = false, defaultValue="null") String group,
            @RequestParam(required = false, defaultValue="false") Boolean myself,
            @PathVariable String lang) {

        // Setup fields to search, filter and order.
        SortDirection sort = SortDirection.ASC;
        SubscriptionColumn column = SubscriptionColumn.TITLE;

        if (!request.getOrder().isEmpty()) {
            sort = SortDirection.valueOf(request.getOrder().get(0)
                    .get(OrderCriterias.dir).toUpperCase());
            Integer tmpIndex = Integer.valueOf(
                    request.getOrder().get(0).get(OrderCriterias.column));
            if (request.getColumns().size() >= tmpIndex) {
                String tmp = request.getColumns().get(tmpIndex)
                        .get(ColumnCriterias.name);
                if (tmp != null && !tmp.trim().isEmpty()) {
                    column = SubscriptionColumn.fromValue(tmp.toUpperCase());
                }
            }
        }
        List<String> usernames = new LinkedList<String>();

        final Group g = groupRepository.findByName(group);

        if (g != null) {
            Specification<UserGroup> userGroupSpec = new Specification<UserGroup>() {
                public Predicate toPredicate(Root<UserGroup> root,
                        CriteriaQuery<?> query, CriteriaBuilder builder) {
                    return builder.equal(root.get("group"), g);
                }
            };
            for (User u : userRepository
                    .findAllUsersInUserGroups(userGroupSpec)) {
                usernames.add(u.getUsername());
            }

            // Also, add all administrators (who doesn't appear on usergroup)
            for (User u : userRepository
                    .findAllByProfile(Profile.Administrator)) {
                usernames.add(u.getUsername());
            }
        }

        if (myself) {
            ServiceContext context = serviceManager.createServiceContext(
                    "openwis.subscription.search", lang, httpRequest);
            UserSession session = context.getUserSession();
            User user = session.getPrincipal();
            usernames.add(user.getUsername());
        }

        List<Subscription> subscriptions = manager.retrieveSubscriptionsByUsers(
                request.getStart(), request.getLength(), sort, column,
                usernames);

        Response response = new Response();
        response.setDraw(request.getDraw());
        response.setRecordsTotal((long) manager
                .retrieveSubscriptionsByUsersCount(new ArrayList<String>(0)));
        response.setRecordsFiltered(
                (long) manager.retrieveSubscriptionsByUsersCount(usernames));

        for (Subscription s : subscriptions) {
            Map<String, String> element = new HashMap<String, String>();
            element.put("user", s.getUser());
            element.put("id", s.getId().toString());

            XMLGregorianCalendar lastEventDate = s.getLastEventDate();
            if (lastEventDate != null) {
                element.put("lastEventDate",
                        lastEventDate.getDay() + "/" + lastEventDate.getMonth()
                                + "/" + lastEventDate.getYear());
            } else {
                element.put("lastEventDate", "");
            }
            lastEventDate = s.getStartingDate();
            if (lastEventDate != null) {
                element.put("starting_date",
                        lastEventDate.getDay() + "/" + lastEventDate.getMonth()
                                + "/" + lastEventDate.getYear());
            } else {
                element.put("starting_date", "");
            }

            ProductMetadata productMetadata = s.getProductMetadata();
            if (productMetadata != null) {
                element.put("title", productMetadata.getTitle());
                element.put("urn", productMetadata.getUrn());
            } else {
                element.put("title", "");
                element.put("urn", "");
            }

            element.put("status", s.getState().name());

            if (s.getSubscriptionBackup() != null) {
                element.put("backup",
                        s.getSubscriptionBackup().getDeployment());
            } else {
                element.put("backup", "");
            }
            response.addData(element);
        }

        return response;
    }

    @RequestMapping(value = { "/{lang}/openwis.subscription.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Subscription retrieve(
            @RequestParam Long subscriptionId) {

        Subscription subscription = manager
                .retrieveSubscription(subscriptionId);
        if (subscription == null)
            throw new SubscriptionNotFoundException();

        return subscription;
    }

    @RequestMapping(value = { "/{lang}/openwis.subscription.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody OkResponse save(@PathVariable String lang,
            @RequestParam Long subscriptionId) {

        Subscription subscription = manager
                .retrieveSubscription(subscriptionId);

        if (subscription != null) {
            // TODO: Set subscription fields

            manager.save(subscription);
        } else {
            throw new SubscriptionNotFoundException();
        }

        return new OkResponse();
    }

    @RequestMapping(value = { "/{lang}/openwis.subscription.new" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Long create(HttpServletRequest request) {

        DisseminationPair disseminationPair = conversionService
                .convert(request.getParameter("data"), DisseminationPair.class);

        Subscription subscription = new Subscription();

        subscription.setUser(disseminationPair.getUsername());
        subscription.setPrimaryDissemination(disseminationPair.getPrimary());
        subscription
                .setSecondaryDissemination(disseminationPair.getSecondary());
        subscription.setExtractMode(disseminationPair.getExtractMode());

        return manager.create(disseminationPair.getMetadataUrn(), subscription);
    }

    @RequestMapping(value = {
            "/{lang}/openwis.requestdeliver.new" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Long requestdeliver(HttpServletRequest request) {

        DisseminationPairRequest disseminationPair = conversionService.convert(
                request.getParameter("data"), DisseminationPairRequest.class);

        AdHoc adHoc = new AdHoc();
        adHoc.setExtractMode(disseminationPair.getExtractMode());
        adHoc.setPrimaryDissemination(disseminationPair.getPrimary());
        adHoc.setSecondaryDissemination(disseminationPair.getSecondary());
        adHoc.setClassOfService(disseminationPair.getClassOfService());
        adHoc.setEmail(disseminationPair.getEmail());
        adHoc.setRequestType(disseminationPair.getRequestType());
        adHoc.setUser(disseminationPair.getUsername());

        String urn = (disseminationPair.getMetadataUrn());

        return requestClient.create(urn, adHoc);
    }

    public ConversionService getConversionService() {
        return conversionService;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * If there is no pending request associated to the user and the metadata,
     * returns null. If there is a pending request, returns the id. If there is
     * a processed request finished, returns the url.
     * 
     * @param urn
     * @return
     */
    @RequestMapping(value = {
            "/{lang}/openwis.processrequest.check" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody String getDownloadURL(HttpServletRequest request,
            @PathVariable String lang, @RequestParam String urn) {
        String res = null;

        ServiceContext context = serviceManager.createServiceContext(
                "openwis.processrequest.check", lang, request);
        UserSession session = context.getUserSession();
        User user = session.getPrincipal();

        try {
            OpenwisDownload od = openwisRepository.findByUserAndUuid(user, urn);

            if (od != null) {
                if (od.getUrl() != null) {
                    res = od.getUrl();
                } else {
                    res = od.getId().toString();
                }
            }
        } catch (Throwable t) {
        }
        return res;
    }

    /**
     * Start a new processed request for download.
     * 
     * @see DirectDownloadJob
     * @param urn
     * @param adHoc
     * @return
     */
    @RequestMapping(value = {
            "/{lang}/openwis.processrequest.new" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Boolean startProcessedRequest(
            HttpServletRequest request, @PathVariable String lang) {

        ServiceContext context = serviceManager.createServiceContext(
                "openwis.processrequest.new", lang, request);
        UserSession session = context.getUserSession();
        User user = session.getPrincipal();

        user = userRepository.findOne(user.getId());

        DisseminationPairRequest disseminationPair = conversionService.convert(
                request.getParameter("data"), DisseminationPairRequest.class);

        AdHoc adHoc = new AdHoc();
        adHoc.setExtractMode(disseminationPair.getExtractMode());
        adHoc.setClassOfService(disseminationPair.getClassOfService());
        adHoc.setEmail(disseminationPair.getEmail());
        adHoc.setRequestType(disseminationPair.getRequestType());
        adHoc.setUser(disseminationPair.getUsername());

        String urn = (disseminationPair.getMetadataUrn());

        Long idReq = requestClient.create(urn, adHoc);

        OpenwisDownload od = new OpenwisDownload();
        od.setUser(user);
        od.setUrn(urn);
        od.setRequestId(idReq.intValue());

        openwisRepository.saveAndFlush(od);

        // DirectDownloadJobjava will do the work of preparing the values for
        // openwis.processrequest.check

        return true;
    }
}

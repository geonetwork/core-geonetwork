package org.fao.geonet.services.openwis.subscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.services.openwis.util.Request;
import org.fao.geonet.services.openwis.util.Request.OrderCriterias;
import org.fao.geonet.services.openwis.util.Response;
import org.openwis.request.client.AdHoc;
import org.openwis.request.client.RequestClient;
import org.openwis.subscription.client.ProductMetadata;
import org.openwis.subscription.client.SortDirection;
import org.openwis.subscription.client.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private ConversionService conversionService;

    @RequestMapping(value = {
            "/{lang}/openwis.subscription.search" }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Response search(@ModelAttribute Request request,
            HttpServletRequest httpRequest) {

        SortDirection sort = SortDirection.DESC;

        sort = SortDirection.valueOf(request.getOrder().get(0)
                .get(OrderCriterias.dir).toUpperCase());

        List<Subscription> subscriptions = manager.retrieveSubscriptionsByUsers(
                request.getStart(), request.getLength(), sort);

        // TODO total
        Response response = new Response();
        response.setDraw(request.getDraw());
        // response.setRecordsTotal(manager.getTotal());
        // response.setRecordsFiltered(manager.getTotalCurrentQuery(startWith));

        for (Subscription s : subscriptions) {
            Map<String, String> element = new HashMap<String, String>();
            element.put("email", s.getEmail());
            element.put("requestType", s.getRequestType());
            element.put("user", s.getUser());
            element.put("classOfService", s.getClassOfService().name());
            element.put("extractMode", s.getExtractMode().name());
            element.put("frequency", s.getFrequency().isZipped().toString());
            element.put("id", s.getId().toString());

            XMLGregorianCalendar lastEventDate = s.getLastEventDate();
            if (lastEventDate != null) {
                element.put("lastEventDate",
                        lastEventDate.getDay() + "/" + lastEventDate.getMonth()
                                + "/" + lastEventDate.getYear());
            } else {
                element.put("lastEventDate", "");
            }

            if (s.getParameters() != null && !s.getParameters().isEmpty()) {
                StringBuilder params = new StringBuilder();

                // for (Parameter p : s.getParameters()) {
                // // TODO do we need all the elements?
                // }

                params.append("]");

                element.put("parameters", params.toString());
            } else {
                element.put("parameters", "[]");
            }

            if (s.getPrimaryDissemination() != null) {
                element.put("primaryDissemination",
                        s.getPrimaryDissemination().getId().toString());
                element.put("primaryDisseminationZipMode",
                        s.getPrimaryDissemination().getZipMode().name());
            } else {
                element.put("primaryDissemination", "");
                element.put("primaryDisseminationZipMode", "");
            }

            ProductMetadata productMetadata = s.getProductMetadata();
            if (productMetadata != null) {
                element.put("productMetadataDataPolicy",
                        productMetadata.getDataPolicy());
                element.put("productMetadataFileExtension",
                        productMetadata.getFileExtension());
                element.put("productMetadataFncPattern",
                        productMetadata.getFncPattern());
                element.put("productMetadataGtsCategory",
                        productMetadata.getGtsCategory());
                element.put("productMetadataLocalDataSource",
                        productMetadata.getLocalDataSource());
                element.put("productMetadataOriginator",
                        productMetadata.getOriginator());
                element.put("productMetadataOverridenDataPolicy",
                        productMetadata.getOverridenDataPolicy());
                element.put("productMetadataOverridenFileExtension",
                        productMetadata.getOverridenFileExtension());
                element.put("productMetadataOverridenFncPattern",
                        productMetadata.getOverridenFncPattern());
                element.put("productMetadataOverridenGtsCategory",
                        productMetadata.getOverridenGtsCategory());
                element.put("productMetadataProcess",
                        productMetadata.getProcess());
                element.put("productMetadataTitle", productMetadata.getTitle());
                element.put("productMetadataUrn", productMetadata.getUrn());

                XMLGregorianCalendar creationDate = productMetadata
                        .getCreationDate();
                if (creationDate != null) {
                    element.put("productMetadataCreationDate",
                            creationDate.getDay() + "/"
                                    + creationDate.getMonth() + "/"
                                    + creationDate.getYear());
                } else {
                    element.put("productMetadataCreationDate", "");
                }

                element.put("productMetadataId",
                        productMetadata.getId().toString());
                element.put("productMetadataOverridenPriority",
                        productMetadata.getOverridenPriority().toString());
                element.put("productMetadataPriority",
                        productMetadata.getPriority().toString());
                element.put("productMetadataupdateFrequency",
                        productMetadata.getUpdateFrequency().toString());
            } else {
                element.put("productMetadataDataPolicy", "");
                element.put("productMetadataFileExtension", "");
                element.put("productMetadataFncPattern", "");
                element.put("productMetadataGtsCategory", "");
                element.put("productMetadataLocalDataSource", "");
                element.put("productMetadataOriginator", "");
                element.put("productMetadataOverridenDataPolicy", "");
                element.put("productMetadataOverridenFileExtension", "");
                element.put("productMetadataOverridenFncPattern", "");
                element.put("productMetadataOverridenGtsCategory", "");
                element.put("productMetadataProcess", "");
                element.put("productMetadataTitle", "");
                element.put("productMetadataUrn", "");
                element.put("productMetadataCreationDate", "");
                element.put("productMetadataId", "");
                element.put("productMetadataOverridenPriority", "");
                element.put("productMetadataPriority", "");
                element.put("productMetadataupdateFrequency", "");
            }

            if (s.getSecondaryDissemination() != null) {
                element.put("secondaryDissemination",
                        s.getSecondaryDissemination().getId().toString());
                element.put("secondaryDisseminationZipMode",
                        s.getSecondaryDissemination().getZipMode().name());
            } else {
                element.put("secondaryDissemination", "");
                element.put("secondaryDisseminationZipMode", "");
            }

            XMLGregorianCalendar startingDate = s.getStartingDate();
            if (lastEventDate != null) {
                element.put("startingDate",
                        startingDate.getDay() + "/" + startingDate.getMonth()
                                + "/" + startingDate.getYear());
            } else {
                element.put("startingDate", "");
            }

            element.put("state", s.getState().name());

            if (s.getSubscriptionBackup() != null) {
                element.put("subscriptionBackup",
                        s.getSubscriptionBackup().getDeployment());
                element.put("subscriptionBackupId",
                        Long.toString(s.getSubscriptionBackup().getId()));
                element.put("subscriptionBackupSubscriptionId", Long.toString(
                        s.getSubscriptionBackup().getSubscriptionId()));
            } else {
                element.put("subscriptionBackup", "");
                element.put("subscriptionBackupId", "");
                element.put("subscriptionBackupSubscriptionId", "");
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
        subscription.setSecondaryDissemination(disseminationPair.getSecondary());
        subscription.setExtractMode(disseminationPair.getExtractMode());

        return manager.create(disseminationPair.getMetadataUrn(), subscription);
    }

    @RequestMapping(value = { "/{lang}/openwis.requestdeliver.new" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody Long requestdeliver(HttpServletRequest request) {
        
        DisseminationPairRequest disseminationPair = conversionService
                .convert(request.getParameter("data"), DisseminationPairRequest.class);

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

}

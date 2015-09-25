package org.openwis.blacklist.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * Client for BlackList web service.
 *
 * @author Jose García
 */
public class BlacklistClient {

    @Autowired
    private WebServiceTemplate webServiceTemplate;

    /**
     * Retrieves a paginated list of users black listed, group by user.
     *
     * @param firstResult
     * @param maxResults
     * @param sort
     * @return A paginated list of users black listed.
     */
    public List<BlacklistInfo> retrieveUsersBlackListInfoByUser(int firstResult,
            int maxResults, SortDirection sort) {
        ObjectFactory objFact = new ObjectFactory();

        GetUsersBlackListInfoByUser request = objFact
                .createGetUsersBlackListInfoByUser();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortDirection(sort);

        @SuppressWarnings("unchecked")
        JAXBElement<GetUsersBlackListInfoByUserResponse> response = (JAXBElement<GetUsersBlackListInfoByUserResponse>) webServiceTemplate
                .marshalSendAndReceive(
                        objFact.createGetUsersBlackListInfoByUser(request));
        GetUsersBlackListInfoByUserResponse responseType = response.getValue();

        return responseType.getReturn().getList();
    }

    /**
     * Updates a user black listing information.
     * 
     * @param info
     * @return
     */
    public BlacklistInfo updateUserBlackListInfo(BlacklistInfo info) {
        ObjectFactory objFact = new ObjectFactory();

        UpdateUserBlackListInfo request = objFact
                .createUpdateUserBlackListInfo();
        request.setBlacklistInfo(info);

        @SuppressWarnings("unchecked")
        JAXBElement<UpdateUserBlackListInfoResponse> response = (JAXBElement<UpdateUserBlackListInfoResponse>) webServiceTemplate
                .marshalSendAndReceive(
                        objFact.createUpdateUserBlackListInfo(request));
        UpdateUserBlackListInfoResponse responseType = response.getValue();

        return responseType.getReturn();
    }

    public WebServiceTemplate getWebServiceTemplate() {
        return webServiceTemplate;
    }

    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }
}

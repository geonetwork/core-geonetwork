package org.openwis.blacklist.client;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * Client for BlackList web service.
 *
 * @author Jose García
 */
public class BlacklistClient {

    private WebServiceTemplate webServiceTemplate;

    /**
     * Retrieves a paginated list of users black listed starting with string
     *
     * @param firstResult
     * @param maxResults
     * @param sort
     * @return A paginated list of users black listed.
     */
    public BlacklistInfoResult retrieveUsersBlackListInfoByUser(int firstResult,
            int maxResults, SortDirection sort, String startWith) {
        ObjectFactory objFact = new ObjectFactory();

        GetUsersBlackListInfoByUser request = objFact
                .createGetUsersBlackListInfoByUser();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortDirection(sort);
        request.setStartWith(startWith);

        @SuppressWarnings("unchecked")
        JAXBElement<GetUsersBlackListInfoByUserResponse> response = (JAXBElement<GetUsersBlackListInfoByUserResponse>) webServiceTemplate
                .marshalSendAndReceive(
                        objFact.createGetUsersBlackListInfoByUser(request));
        GetUsersBlackListInfoByUserResponse responseType = response.getValue();

        return responseType.getReturn();
    }
    
    /**
     * Retrieves a paginated list of users black listed.
     *
     * @param firstResult
     * @param maxResults
     * @param sort
     * @return A paginated list of users black listed.
     */
    public BlacklistInfoResult retrieveUsersBlackListInfoByUser(int firstResult,
            int maxResults, SortDirection sort) {
        ObjectFactory objFact = new ObjectFactory();

        GetUsersBlackListInfo request = objFact
                .createGetUsersBlackListInfo();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortDirection(sort);

        @SuppressWarnings("unchecked")
        JAXBElement<GetUsersBlackListInfoResponse> response = (JAXBElement<GetUsersBlackListInfoResponse>) webServiceTemplate
                .marshalSendAndReceive(
                        objFact.createGetUsersBlackListInfo(request));
        GetUsersBlackListInfoResponse responseType = response.getValue();

        return responseType.getReturn();
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

    /**
     * Updates a user blacklist status.
     * 
     * @param isBlacklisted
     * @return
     */
    public SetUserBlacklistedResponse setBlacklisted(Boolean isBlacklisted, String user) {
        ObjectFactory objFact = new ObjectFactory();

        SetUserBlacklisted request = objFact.createSetUserBlacklisted();
        request.setBlacklisted(isBlacklisted);
        request.setUser(user);

        @SuppressWarnings("unchecked")
        JAXBElement<SetUserBlacklistedResponse> response = (JAXBElement<SetUserBlacklistedResponse>) webServiceTemplate
                .marshalSendAndReceive(
                        objFact.createSetUserBlacklisted(request));
        return response.getValue();

    }
    
    /**
     * Check if a user is blacklisted.
     * 
     * @param isBlacklisted
     * @return
     */
    public IsUserBlacklistedResponse getBlacklisted(String user) {
        ObjectFactory objFact = new ObjectFactory();

        IsUserBlacklisted request = objFact.createIsUserBlacklisted();
        request.setUser(user);

        @SuppressWarnings("unchecked")
        JAXBElement<IsUserBlacklistedResponse> response = (JAXBElement<IsUserBlacklistedResponse>) webServiceTemplate
                .marshalSendAndReceive(
                        objFact.createIsUserBlacklisted(request));
        return response.getValue();

    }

    public WebServiceTemplate getWebServiceTemplate() {
        return webServiceTemplate;
    }

    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }
}

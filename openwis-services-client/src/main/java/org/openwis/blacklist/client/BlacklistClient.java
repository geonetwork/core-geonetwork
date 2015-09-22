package org.openwis.blacklist.client;


import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;
import java.util.List;

/**
 * Client for BlackList web service.
 *
 * @author Jose García
 */
public class BlacklistClient extends WebServiceGatewaySupport {


    /**
     * Retrieves a paginated list of users black listed.
     *
     * @param firstResult
     * @param maxResults
     * @param sort
     * @return  A paginated list of users black listed.
     */
    public List<BlacklistInfo> retrieveUsersBlackListInfo(int firstResult, int maxResults, SortDirection sort) {
        ObjectFactory objFact = new ObjectFactory();

        GetUsersBlackListInfo request =  objFact.createGetUsersBlackListInfo();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortDirection(sort);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetUsersBlackListInfo(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetUsersBlackListInfoResponse responseType = (GetUsersBlackListInfoResponse) response.getValue();

        return responseType.getReturn().getList();
    }

    /**
     * Retrieves a paginated list of users black listed, group by user.
     *
     * @param firstResult
     * @param maxResults
     * @param sort
     * @return  A paginated list of users black listed.
     */
    public List<BlacklistInfo> retrieveUsersBlackListInfoByUser(int firstResult, int maxResults, SortDirection sort) {
        ObjectFactory objFact = new ObjectFactory();


        GetUsersBlackListInfoByUser request =  objFact.createGetUsersBlackListInfoByUser();
        request.setFirstResult(firstResult);
        request.setMaxResults(maxResults);
        request.setSortDirection(sort);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetUsersBlackListInfoByUser(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetUsersBlackListInfoByUserResponse responseType = (GetUsersBlackListInfoByUserResponse) response.getValue();

        return responseType.getReturn().getList();
    }

    /**
     * Retrieves a user black listed info.
     *
     * @param user
     * @return
     */
    public BlacklistInfo retrieveUserBlackListInfo(String user) {
        ObjectFactory objFact = new ObjectFactory();


        GetUserBlackListInfo request =  objFact.createGetUserBlackListInfo();
        // TODO: set arg0, arg1, check what values mean ...
        request.setArg0("");
        request.setArg1(true);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetUserBlackListInfo(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetUserBlackListInfoResponse responseType = (GetUserBlackListInfoResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Retrieves a user black listed info if exists.
     *
     * @param user
     * @return
     */
    public BlacklistInfo retrieveUserBlackListInfoIfExists(String user) {
        ObjectFactory objFact = new ObjectFactory();


        GetUserBlackListInfoIfExists request =  objFact.createGetUserBlackListInfoIfExists();
        request.setUser(user);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createGetUserBlackListInfoIfExists(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        GetUserBlackListInfoIfExistsResponse responseType = (GetUserBlackListInfoIfExistsResponse) response.getValue();

        return responseType.getReturn();
    }


    /**
     * Returns if a user is black listed.
     *
     * @param user
     * @return
     */
    public boolean isUserBlacklisted(String user) {
        ObjectFactory objFact = new ObjectFactory();


        IsUserBlacklisted request =  objFact.createIsUserBlacklisted();
        request.setUser(user);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createIsUserBlacklisted(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        IsUserBlacklistedResponse responseType = (IsUserBlacklistedResponse) response.getValue();

        return responseType.isReturn();
    }

    /**
     * Updates a user black listing information.
     * @param info
     * @return
     */
    public BlacklistInfo updateUserBlackListInfo(BlacklistInfo info) {
        ObjectFactory objFact = new ObjectFactory();


        UpdateUserBlackListInfo request =  objFact.createUpdateUserBlackListInfo();
        request.setBlacklistInfo(info);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createUpdateUserBlackListInfo(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        UpdateUserBlackListInfoResponse responseType = (UpdateUserBlackListInfoResponse) response.getValue();

        return responseType.getReturn();
    }

    /**
     * Sets a user black listed status.
     *
     * @param user
     * @return
     */
    public void setUserBlacklisted(String user, boolean blackListed) {
        ObjectFactory objFact = new ObjectFactory();


        SetUserBlacklisted request =  objFact.createSetUserBlacklisted();
        request.setUser(user);
        request.setBlacklisted(blackListed);

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createSetUserBlacklisted(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        SetUserBlacklistedResponse responseType = (SetUserBlacklistedResponse) response.getValue();
    }


    /**
     *
     * @return
     */
    // TODO: Check if required to be implemented and clarify parameters
    public boolean checkAndUpdateDisseminatedData() {
        return false;

        /*ObjectFactory objFact = new ObjectFactory();

        CheckAndUpdateDisseminatedData request =  objFact.createCheckAndUpdateDisseminatedData();

        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(
                objFact.createCheckAndUpdateDisseminatedData(request),
                new SoapActionCallback(
                        "http://localhost:8088/mockProductMetadataServiceSoapBinding"));
        CheckAndUpdateDisseminatedDataResponse responseType = (CheckAndUpdateDisseminatedDataResponse) response.getValue();

        return responseType.isReturn();*/
    }
}

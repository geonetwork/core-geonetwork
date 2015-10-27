package org.fao.geonet.services.openwis.subscription;

import java.util.List;

import org.openwis.subscription.client.SortDirection;
import org.openwis.subscription.client.Subscription;
import org.openwis.subscription.client.SubscriptionClient;
import org.openwis.subscription.client.SubscriptionColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionManager {

    @Autowired
    private SubscriptionClient serviceClient;

    public List<Subscription> retrieveSubscriptionsByUsers(int firstResult,
            int maxResults, SortDirection sort, SubscriptionColumn column,
            List<String> usernames) {
        return getServiceClient().retrieveSubscriptionsByUsers(firstResult,
                maxResults, sort, column, usernames);
    }

    public int retrieveSubscriptionsByUsersCount(
            List<String> usernames) {
        return getServiceClient().retrieveSubscriptionsByUsersCount(usernames);
    }

    public Subscription retrieveSubscription(Long subscriptionId) {
        return getServiceClient().retrieveSubscription(subscriptionId);
    }

    public Subscription save(Subscription subscription) {
        return getServiceClient().updateSubscription(subscription);
    }

    public void discard(Long subscriptionId) {
        getServiceClient().deleteSubscription(subscriptionId);
    }

    public Long create(String metadataUrn, Subscription subscription) {
        return getServiceClient().createSubscription(metadataUrn, subscription);
    }

    public void resume(Long subscriptionId) {
        getServiceClient().resumeSubscription(subscriptionId);
    }

    public void suspend(Long subscriptionId) {
        getServiceClient().suspendSubscription(subscriptionId);
    }

    public SubscriptionClient getServiceClient() {
        return serviceClient;
    }

    public void setServiceClient(SubscriptionClient serviceClient) {
        this.serviceClient = serviceClient;
    }

}

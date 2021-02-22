package org.fao.geonet.api.records.formatters.cache;

import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@EnableAsync
public interface AsynchAfterCommitListener extends ApplicationListener<MetadataIndexCompleted> {
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void handleAsync(MetadataIndexCompleted event);
}

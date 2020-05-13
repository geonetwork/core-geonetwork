Example of class that listen to events in GeoNetwork.


It has to have a @Component annotation to be automatically loaded and fired. As it is handled by Spring, you can use the @Autowire annotation.

```
package org.fao.geonet.events.user;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Component
public class RecordValidationTriggeredEventTest implements ApplicationListener<RecordValidationTriggeredEvent> {
  @Override
  public void onApplicationEvent(RecordValidationTriggeredEvent event) {
    //Your code here
  }
}
```

Example of class that listen to database events in GeoNetwork. This has more granularity than the previous one:

It can have as many functions as desired, but they need the @TransactionalEventListener and the proper event as argument to be fired. The default overrided onApplicationEvent function does not care about transaction, so it is not safe to use it to access the database.


```
package org.fao.geonet.events.user;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Component
public class UserDeletedEventTest implements ApplicationListener<UserDeleted> {
  
  @TransactionalEventListener
  public void afterCommit(UserDeleted event) {
    //Things to do after the transaction commit
    //any call to the database will work as expected
    //as things are flushed
  }
  
  @TransactionalEventListener(phase=TransactionPhase.BEFORE_COMMIT)
  public void beforeCommit(UserDeleted event) {
   //Things to do before the transaction commit
   //Maybe we need some check that may stop 
   //the transaction and throw a rollback before storing
   //or we want to do some modification to the object before
   //storing on the database
  }
  
  @TransactionalEventListener(phase=TransactionPhase.AFTER_ROLLBACK)
  public void beforeCommit(UserDeleted event) {
   //Things to do in case of a rollback in the transaction
   //Maybe we need to file an error?
  }
}
```
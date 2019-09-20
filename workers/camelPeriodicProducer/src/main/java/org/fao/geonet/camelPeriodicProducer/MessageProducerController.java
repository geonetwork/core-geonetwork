package org.fao.geonet.camelPeriodicProducer;

import org.fao.geonet.domain.MessageProducerEntity;
import org.fao.geonet.repository.MessageProducerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/{portal}/api/msg_producers")
public class MessageProducerController {
    @Autowired
    protected MessageProducerFactory messageProducerFactory;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected MessageProducerService messageProducerService;

    protected MessageProducerRepository msgProducerRepository;

    class ErrorResponse {
        ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        private String error;
        private String message;
    }

    public MessageProducerController(MessageProducerRepository msgProducerRepository) {
        this.msgProducerRepository = msgProducerRepository;
    }

    @PreAuthorize("hasRole('Administrator')")
    @GetMapping
    public List findAll() {
        return msgProducerRepository.findAll();
    }

    @PreAuthorize("hasRole('Administrator')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MessageProducerEntity> findById(@PathVariable long id) {
        if (msgProducerRepository.exists(id)) {
            return (ResponseEntity.ok().body(msgProducerRepository.findOne(id)));
        } else {
            return (new ResponseEntity(HttpStatus.NOT_FOUND));
        }
    }

    @PreAuthorize("hasRole('Administrator')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MessageProducerEntity messageProducerEntity) {
        InnerEntityManager innerEntityManager = new InnerEntityManager();
        try {
            messageProducerEntity = innerEntityManager.beginMergeAndPersist(messageProducerEntity);
            messageProducerService.registerAndStart(messageProducerEntity);
            innerEntityManager.commit();
            return ResponseEntity.ok().body(messageProducerEntity);
        } catch (PersistenceException e) {
            innerEntityManager.rollback();
            ErrorResponse response = new ErrorResponse("Could not create object in database", sqlCause(e));
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            innerEntityManager.rollback();
            ErrorResponse response = new ErrorResponse("An unknown error occurred", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } finally {
            innerEntityManager.close();
        }
    }

    @PreAuthorize("hasRole('Administrator')")
    @PutMapping(value="/{id}")
    public  ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody MessageProducerEntity messageProducerEntity) {
        if (msgProducerRepository.exists(id)) {
            InnerEntityManager innerEntityManager = new InnerEntityManager();
            try {
                messageProducerEntity.setId(id);
                messageProducerEntity = innerEntityManager.beginMergeAndPersist(messageProducerEntity);
                messageProducerService.changeMessageAndReschedule(messageProducerEntity);
                innerEntityManager.commit();
                return ResponseEntity.ok().body(messageProducerEntity);
            } catch (PersistenceException e) {
                innerEntityManager.rollback();
                ErrorResponse response = new ErrorResponse("Could not update object in database", sqlCause(e));
                return ResponseEntity.badRequest().body(response);
            } catch (Exception e) {
                innerEntityManager.rollback();
                ErrorResponse response = new ErrorResponse("An unknown error occurred", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            } finally {
                innerEntityManager.close();
            }
        } else {
            return (new ResponseEntity(HttpStatus.NOT_FOUND));
        }
    }

    @PreAuthorize("hasRole('Administrator')")
    @DeleteMapping(path ={"/{id}"})
    public ResponseEntity<?> delete(@PathVariable("id") long id) throws Exception {
        if (msgProducerRepository.exists(id)) {
            msgProducerRepository.delete(id);
            messageProducerService.destroy(id);
            return ResponseEntity.ok().build();
        } else {
            return (new ResponseEntity(HttpStatus.NOT_FOUND));
        }
    }

    private String sqlCause(PersistenceException e) {
        Throwable cause = e;
        int level = 0;
        while (level < 5 && cause.getCause() !=null && ! (cause instanceof SQLException)) {
            cause = cause.getCause();
            level ++;
        }
        if (cause instanceof SQLException) {
            return cause.getMessage();
        } else {
            return e.getMessage();
        }
    }

    private class InnerEntityManager implements Closeable {
        private EntityManager innerEntityMananger;

        public InnerEntityManager() {
            innerEntityMananger = entityManager.getEntityManagerFactory().createEntityManager();
        }

        public MessageProducerEntity beginMergeAndPersist(MessageProducerEntity messageProducerEntity) {
            innerEntityMananger.getTransaction().begin();
            messageProducerEntity = innerEntityMananger.merge(messageProducerEntity);
            innerEntityMananger.persist(messageProducerEntity);
            return messageProducerEntity;
        }

        public void rollback() {
            if (innerEntityMananger.getTransaction().isActive()) {
                innerEntityMananger.getTransaction().rollback();
            }
        }

        public void commit() {
            innerEntityMananger.getTransaction().commit();
        }

        public void close () {
            innerEntityMananger.close();
        }
    }
}

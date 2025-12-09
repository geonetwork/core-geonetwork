/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.camelPeriodicProducer;

import io.swagger.v3.oas.annotations.Operation;
import org.fao.geonet.domain.MessageProducerEntity;
import org.fao.geonet.repository.MessageProducerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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

    public MessageProducerController(MessageProducerRepository msgProducerRepository) {
        this.msgProducerRepository = msgProducerRepository;
    }

    @PreAuthorize("hasAuthority('Administrator')")
    @GetMapping
    public List findAll() {
        return msgProducerRepository.findAll();
    }

    @PreAuthorize("hasAuthority('Administrator')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MessageProducerEntity> findById(@PathVariable long id) {
        Optional<MessageProducerEntity> message = msgProducerRepository.findById(id);
        if (message.isPresent()) {
            return (ResponseEntity.ok().body(message.get()));
        } else {
            return (new ResponseEntity(HttpStatus.NOT_FOUND));
        }
    }

    @PreAuthorize("hasAuthority('Administrator')")
    @GetMapping(path = "/find")
    public ResponseEntity<MessageProducerEntity> findByUrlAndFeatureType(@RequestParam String url, @RequestParam String featureType) {
        MessageProducerEntity msg = msgProducerRepository.findOneByUrlAndFeatureType(url, featureType);
        if (msg != null) {
            return (ResponseEntity.ok().body(msg));
        } else {
            return (new ResponseEntity(HttpStatus.NOT_FOUND));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create and save a message to trigger data indexing.",
        description = "If existing (same URL, same feature type), update and start the process."
    )
    @PreAuthorize("hasAuthority('Administrator')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MessageProducerEntity messageProducerEntity) {
        MessageProducerEntity message = msgProducerRepository.findOneByUrlAndFeatureType(
            messageProducerEntity.getWfsHarvesterParam().getUrl(),
            messageProducerEntity.getWfsHarvesterParam().getTypeName());
        if (message != null) {
            // Maybe we should return IllegalArgumentException for consistency with other API?
            update(message.getId(), messageProducerEntity, true);
            return ResponseEntity.ok().body(messageProducerEntity);
        } else {
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
    }

    @PreAuthorize("hasAuthority('Administrator')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody MessageProducerEntity messageProducerEntity, boolean registerAndStart) {
        if (msgProducerRepository.existsById(id)) {
            InnerEntityManager innerEntityManager = new InnerEntityManager();
            try {
                messageProducerEntity.setId(id);
                messageProducerEntity = innerEntityManager.beginMergeAndPersist(messageProducerEntity);
                if (registerAndStart) {
                    messageProducerService.registerAndStart(messageProducerEntity);
                } else {
                    messageProducerService.changeMessageAndReschedule(messageProducerEntity);
                }
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

    @PreAuthorize("hasAuthority('Administrator')")
    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<?> delete(@PathVariable("id") long id) throws Exception {
        if (msgProducerRepository.existsById(id)) {
            msgProducerRepository.deleteById(id);
            messageProducerService.destroy(id);
            return ResponseEntity.ok().build();
        } else {
            return (new ResponseEntity(HttpStatus.NOT_FOUND));
        }
    }

    private String sqlCause(PersistenceException e) {
        Throwable cause = e;
        int level = 0;
        while (level < 5 && cause.getCause() != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
            level++;
        }
        if (cause instanceof SQLException) {
            return cause.getMessage();
        } else {
            return e.getMessage();
        }
    }

    class ErrorResponse {
        private String error;
        private String message;

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

        public void close() {
            innerEntityMananger.close();
        }
    }
}

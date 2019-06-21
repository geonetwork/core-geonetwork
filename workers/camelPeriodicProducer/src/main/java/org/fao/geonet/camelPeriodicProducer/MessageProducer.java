package org.fao.geonet.camelPeriodicProducer;

public class MessageProducer <M> {

    private Long id;
    private M message;
    private String cronExpession;
    private String targetUri;

    public MessageProducer<M> setMessage(M message) {
        this.message = message;
        return this;
    }

    public MessageProducer<M> setCronExpession(String cronExpession) {
        this.cronExpession = cronExpession;
        return this;
    }

    public MessageProducer<M> setId(Long id) {
        this.id = id;
        return this;
    }
    public MessageProducer<M> setTarget(String targetUri) {
        this.targetUri = targetUri;
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public String getCronExpession() {
        return this.cronExpession;
    }

    public M getMessage() {
        return message;
    }

    public String getTargetUri() {
        return targetUri;
    }
}

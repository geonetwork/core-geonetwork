package org.fao.geonet.api.records.model.related;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the org.fao.geonet.api.records.model.related package. <p>An ObjectFactory allows you
 * to programatically construct new instances of the Java representation for XML content. The Java
 * representation of XML content can consist of schema derived interfaces and classes representing
 * the binding of schema type definitions, element declarations and model groups.  Factory methods
 * for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Related_QNAME = new QName("", "related");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes
     * for package: org.fao.geonet.api.records.model.related
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RelatedLinkItem }
     */
    public RelatedLinkItem createRelatedLinkItem() {
        return new RelatedLinkItem();
    }

    /**
     * Create an instance of {@link RelatedMetadataItem }
     */
    public RelatedMetadataItem createRelatedMetadataItem() {
        return new RelatedMetadataItem();
    }

    /**
     * Create an instance of {@link RelatedResponse }
     */
    public RelatedResponse createRelatedResponse() {
        return new RelatedResponse();
    }

    /**
     * Create an instance of {@link RelatedThumbnailItem }
     */
    public RelatedThumbnailItem createRelatedThumbnailItem() {
        return new RelatedThumbnailItem();
    }

    /**
     * Create an instance of {@link LocalizedString }
     */
    public LocalizedString createLocalizedString() {
        return new LocalizedString();
    }

    /**
     * Create an instance of {@link RelatedSiblingMetadataItem }
     */
    public RelatedSiblingMetadataItem createRelatedSiblingMetadataItem() {
        return new RelatedSiblingMetadataItem();
    }

    /**
     * Create an instance of {@link RelatedItem.MultilingualValue }
     */
    public RelatedItem.MultilingualValue createRelatedItemTitle() {
        return new RelatedItem.MultilingualValue();
    }

    /**
     * Create an instance of {@link RelatedItem.MultilingualValue }
     */
    public RelatedItem.MultilingualValue createRelatedItemUrl() {
        return new RelatedItem.MultilingualValue();
    }

    /**
     * Create an instance of {@link RelatedLinkItem.Description }
     */
    public RelatedLinkItem.Description createRelatedLinkItemDescription() {
        return new RelatedLinkItem.Description();
    }

    /**
     * Create an instance of {@link RelatedMetadataItem.Description }
     */
    public RelatedMetadataItem.Description createRelatedMetadataItemDescription() {
        return new RelatedMetadataItem.Description();
    }

    /**
     * Create an instance of {@link RelatedResponse.Children }
     */
    public RelatedResponse.Children createRelatedResponseChildren() {
        return new RelatedResponse.Children();
    }

    /**
     * Create an instance of {@link RelatedResponse.Parent }
     */
    public RelatedResponse.Parent createRelatedResponseParent() {
        return new RelatedResponse.Parent();
    }

    /**
     * Create an instance of {@link RelatedResponse.Siblings }
     */
    public RelatedResponse.Siblings createRelatedResponseSiblings() {
        return new RelatedResponse.Siblings();
    }

    /**
     * Create an instance of {@link RelatedResponse.Associated }
     */
    public RelatedResponse.Associated createRelatedResponseAssociated() {
        return new RelatedResponse.Associated();
    }

    /**
     * Create an instance of {@link RelatedResponse.Service }
     */
    public RelatedResponse.Service createRelatedResponseService() {
        return new RelatedResponse.Service();
    }

    /**
     * Create an instance of {@link RelatedResponse.Dataset }
     */
    public RelatedResponse.Dataset createRelatedResponseDataset() {
        return new RelatedResponse.Dataset();
    }

    /**
     * Create an instance of {@link RelatedResponse.Fcat }
     */
    public RelatedResponse.Fcat createRelatedResponseFcat() {
        return new RelatedResponse.Fcat();
    }

    /**
     * Create an instance of {@link RelatedResponse.Source }
     */
    public RelatedResponse.Source createRelatedResponseSource() {
        return new RelatedResponse.Source();
    }

    /**
     * Create an instance of {@link RelatedResponse.Hassource }
     */
    public RelatedResponse.Hassource createRelatedResponseHassource() {
        return new RelatedResponse.Hassource();
    }

    /**
     * Create an instance of {@link RelatedResponse.Related }
     */
    public RelatedResponse.Related createRelatedResponseRelated() {
        return new RelatedResponse.Related();
    }

    /**
     * Create an instance of {@link RelatedResponse.Online }
     */
    public RelatedResponse.Online createRelatedResponseOnline() {
        return new RelatedResponse.Online();
    }

    /**
     * Create an instance of {@link RelatedResponse.Thumbnail }
     */
    public RelatedResponse.Thumbnail createRelatedResponseThumbnail() {
        return new RelatedResponse.Thumbnail();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RelatedResponse }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "related")
    public JAXBElement<RelatedResponse> createRelated(RelatedResponse value) {
        return new JAXBElement<RelatedResponse>(_Related_QNAME, RelatedResponse.class, null, value);
    }

}

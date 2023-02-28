package org.fao.geonet.api.selections;

import org.fao.geonet.domain.FavouriteMetadataList;
import org.fao.geonet.domain.ISODate;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View Model for UserMetadataSelectionList.
 *
 * Changes:
 *    + User -> just the userName
 *    + Selection -> just a list of IDs
 *    + isEditable -> boolean: owned by the requester
 *    + isMyList -> boolean: owned by the requester
 */
public class FavouriteMetadataListVM {

    private int id;
    private String name;
    private String userName;
    private String sessionId;
    private FavouriteMetadataList.ListType listType;
    private boolean isPublic;
    private ISODate createDate;
    private ISODate changeDate;
    private List<String> selections;
    private boolean isEditable;
    private  boolean isMyList;
    private int nItems;


    public FavouriteMetadataListVM() {
        this.selections = new ArrayList<>();
    }
    /**
     * doesn't set isEditable
     * @param model
     */
    public FavouriteMetadataListVM(FavouriteMetadataList model) {
        this.id = model.getId();
        this.name = model.getName();
        this.userName = model.getUser() !=null ? model.getUser().getUsername() : null;
        this.sessionId = model.getSessionId();
        this.listType = model.getListType();
        this.isPublic = model.getIsPublic();
        this.createDate = model.getCreateDate();
        this.changeDate = model.getChangeDate();
        if (model.getSelections() !=null) {
            this.selections = model.getSelections() .stream()
                .map(x->x.getMetadataUuid())
                .collect(Collectors.toList());
        }
        else {
            this.selections = new ArrayList<>();
        }
        this.nItems = this.selections.size();
    }

    public FavouriteMetadataListVM(FavouriteMetadataList model, boolean isEditable, boolean isMyList) {
        this(model);
        this.isEditable = isEditable;
        this.isMyList = isMyList;
    }


    //--------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public FavouriteMetadataList.ListType getListType() {
        return listType;
    }

    public void setListType(FavouriteMetadataList.ListType listType) {
        this.listType = listType;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public ISODate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(ISODate createDate) {
        this.createDate = createDate;
    }

    public ISODate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(ISODate changeDate) {
        this.changeDate = changeDate;
    }

    public List<String> getSelections() {
        return selections;
    }

    public void setSelections(List<String> selections) {
        this.selections = selections;
        if (selections == null) {
            this.selections = new ArrayList<>();
        }
        this.nItems = selections.size();
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public boolean isMyList() {
        return isMyList;
    }

    public void setMyList(boolean myList) {
        isMyList = myList;
    }

    public int getnItems() {
        return nItems;
    }

    public void setnItems(int nItems) {
        this.nItems = nItems;
    }
}

package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import java.util.Objects;

/**
 * Base class for all items that have an ID and href from the server.
 */
public class IdItemBase {
    // NOTE: Make numbers Long/Integer, not long/int because only the former will
    //       deserialize if the value is null from the server.
    protected Long id;
    protected Boolean hasFullData;
    protected String href;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHasFullData() {
        return hasFullData;
    }

    public void setHasFullData(Boolean hasFullData) {
        this.hasFullData = hasFullData;
    }

    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdItemBase idItem = (IdItemBase) o;
        return Objects.equals(getId(), idItem.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }


    @Override
    public String toString() {
        return "IdItemBase{" +
                "id=" + id +
                ", hasFullData=" + hasFullData +
                ", href='" + href + '\'' +
                '}';
    }

}
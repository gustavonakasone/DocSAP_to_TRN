package Model;

import java.util.ArrayList;
import java.util.List;

public class IDoc {
    private String documentType;
    private List<Group> groups;

    public IDoc(String documentType) {
        this.documentType = documentType;
        this.groups = new ArrayList<>();
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    // Getters e Setters
    public String getDocumentType() {
        return documentType;
    }

    public List<Group> getGroups() {
        return groups;
    }
}

package Model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private int level;
    private String status;
    private int loopMax;
    private List<Segment> segments;
    private List<Group> groups;

    public Group(int level, String status, int loopMax) {
        this.level = level;
        this.status = status;
        this.loopMax = loopMax;
        this.segments = new ArrayList<>();
        this.groups = new ArrayList<>();
    }
    public Group() {
        
    }

    public void addSegment(Segment segment) {
        this.segments.add(segment);
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    // Getters e Setters
    public int getLevel() {
        return level;
    }

    public String getStatus() {
        return status;
    }

    public int getLoopMax() {
        return loopMax;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public List<Group> getGroups() {
        return groups;
    }
}

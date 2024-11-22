package Model;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    private String segmentName;
    private String status;
    private int level;
    private int loopMax;
    private int seq;
    private List<Element> elements;
    private String type;

    public Segment(String segmentName, int level, String status, int loopMax, List<Element> elements, int seq, String type) {
        
        this.segmentName = segmentName;
        this.level = level;
        this.status = status;
        this.loopMax = loopMax;
        this.elements = elements;
        this.seq = seq;
        this.type = type;
    }


    // Getters e Setters
    public String getsegmentName() {
        return segmentName;
    }

    public int getseq() {
        return seq;
    }

    public String gettype() {
        return type;
    }

    public String getstatus() {
        return status;
    }

    public int getLevel() {
        return level;
    }

    public int getLoopMax() {
        return loopMax;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setseq(int seq) {
        this.seq = seq;
    }
    
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }
    public void setsegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public void settype(String type) {
        this.type = type;
    }
}


package Model;

import java.util.List;

public class ElementAndSegmentContainer {
    private String IDOCTYPE;
    private List<Element> elements;
    private List<Segment> segmentStructure;
    private List<Segment> segments;

    public ElementAndSegmentContainer(String IDOCTYPE, List<Element> elements, List<Segment> segmentStructure, List<Segment> segments) {
        this.IDOCTYPE = IDOCTYPE;
        this.elements = elements;
        this.segmentStructure = segmentStructure;
        this.segments = segments;
    }

    public void setElements(List<Element> elements) {
        this.elements =  elements;
    }

    public void setSegments(List<Segment> segments) {
        this.segments =  segments;
    }
    
    public void setIDOCTYPE(String IDOCTYPE) {
        this.IDOCTYPE =  IDOCTYPE;
    }

    public List<Element> getElements() {
        return elements;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public String getIDOCTYPE() {
        return IDOCTYPE;
    }

    public void setSegmentStructure(List<Segment> segmentStructure) {
        this.segmentStructure =  segmentStructure;
    }


    public List<Segment> getsegmentStructure() {
        return segmentStructure;
    }
}

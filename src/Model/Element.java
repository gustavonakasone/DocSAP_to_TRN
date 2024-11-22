package Model;



public class Element {
    private int fieldSeq;
    private String fieldName;
    private String description;
    private String type;
    private int length;
    // Construtor
    public Element(String fieldName, String description, String type, int length) {
        this.fieldName = fieldName;
        this.description = description;
        this.type = type;
        this.length = length;
    }
    public Element(String fieldName, String description, String type, int length, int fieldSeq) {
        this.fieldName = fieldName;
        this.description = description;
        this.type = type;
        this.length = length;
        this.fieldSeq = fieldSeq;
    }

    // Getters
    public String getfieldName() {
        return fieldName;
    }

    public String getdescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    // Setters
    public void setfieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
    public void reset(){
        this.fieldName = "";
        this.description = "";
        this.type = "";
        this.length = 0;
    }

    public void setfieldSeq(int fieldSeq){
        this.fieldSeq = fieldSeq;
    }

    public int getfieldSeq(){
        return fieldSeq;
    }

    
}


package biz.isphere.joblogexplorer.api;

public class JobLogEntry {

    private String id;
    private String type;
    private String severity;
    private String date;
    private String time;
    private String text;
    private String help;

    private String toLibrary;
    private String toProgram;
    private String toModule;
    private String toProcedure;
    private String toStatement;

    private String fromLibrary;
    private String fromProgram;
    private String fromModule;
    private String fromProcedure;
    private String fromStatement;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getHelp() {
        return help;
    }
    
    public void setHelp(String help) {
        this.help = help;
    }
    
    public String getToLibrary() {
        return toLibrary;
    }
    
    public void setToLibrary(String toLibrary) {
        this.toLibrary = toLibrary;
    }
    
    public String getToProgram() {
        return toProgram;
    }
    
    public void setToProgram(String toProgram) {
        this.toProgram = toProgram;
    }
    
    public String getToModule() {
        return toModule;
    }
    
    public void setToModule(String toModule) {
        this.toModule = toModule;
    }
    
    public String getToProcedure() {
        return toProcedure;
    }
    
    public void setToProcedure(String toProcedure) {
        this.toProcedure = toProcedure;
    }
    
    public String getToStatement() {
        return toStatement;
    }
    
    public void setToStatement(String toStatement) {
        this.toStatement = toStatement;
    }
    
    public String getFromLibrary() {
        return fromLibrary;
    }
    
    public void setFromLibrary(String fromLibrary) {
        this.fromLibrary = fromLibrary;
    }
    
    public String getFromProgram() {
        return fromProgram;
    }
    
    public void setFromProgram(String fromProgram) {
        this.fromProgram = fromProgram;
    }
    
    public String getFromModule() {
        return fromModule;
    }
    
    public void setFromModule(String fromModule) {
        this.fromModule = fromModule;
    }
    
    public String getFromProcedure() {
        return fromProcedure;
    }
    
    public void setFromProcedure(String fromProcedure) {
        this.fromProcedure = fromProcedure;
    }
    
    public String getFromStatement() {
        return fromStatement;
    }
    
    public void setFromStatement(String fromStatement) {
        this.fromStatement = fromStatement;
    }
    
}

package com.howlstudio.reports;
import java.time.*;import java.time.format.*;
public class Report {
    private final String id,reporter,reported,reason;
    private final long timestamp;
    private String status; // open, resolved, dismissed
    public Report(String id,String reporter,String reported,String reason){
        this.id=id;this.reporter=reporter;this.reported=reported;this.reason=reason;
        this.timestamp=System.currentTimeMillis();this.status="open";
    }
    public String getId(){return id;} public String getReporter(){return reporter;}
    public String getReported(){return reported;} public String getReason(){return reason;}
    public String getStatus(){return status;} public void setStatus(String s){status=s;}
    public String getTime(){return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));}
    public String toConfig(){return id+"|"+reporter+"|"+reported+"|"+reason.replace("|",";")+"|"+timestamp+"|"+status;}
    public static Report fromConfig(String s){String[]p=s.split("\\|",6);if(p.length<6)return null;Report r=new Report(p[0],p[1],p[2],p[3]);r.status=p[5];return r;}
}

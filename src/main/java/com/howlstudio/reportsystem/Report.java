package com.howlstudio.reportsystem;
import java.time.*; import java.time.format.*;
public class Report {
    private static int nextId=1;
    private final int id;
    private final String reporter,target,reason;
    private final long timestamp;
    private boolean resolved;
    public Report(String reporter,String target,String reason){
        this.id=nextId++;this.reporter=reporter;this.target=target;this.reason=reason;
        this.timestamp=System.currentTimeMillis();this.resolved=false;
    }
    public int getId(){return id;} public String getReporter(){return reporter;}
    public String getTarget(){return target;} public String getReason(){return reason;}
    public boolean isResolved(){return resolved;} public void resolve(){resolved=true;}
    public String getTime(){return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));}
    public String toConfig(){return id+"|"+reporter+"|"+target+"|"+reason+"|"+timestamp+"|"+(resolved?"1":"0");}
    public static Report fromConfig(String s){String[]p=s.split("\\|",6);if(p.length<6)return null;Report r=new Report(p[1],p[2],p[3]);if("1".equals(p[5]))r.resolve();return r;}
}

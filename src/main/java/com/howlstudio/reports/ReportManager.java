package com.howlstudio.reports;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public class ReportManager {
    private final Path dataDir;
    private final List<Report> reports=new ArrayList<>();
    private final AtomicInteger nextId=new AtomicInteger(1);
    public ReportManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}
    public long getCount(){return reports.stream().filter(r->r.getStatus().equals("open")).count();}
    public void addReport(Report r){reports.add(r);save();}
    public void save(){try{StringBuilder sb=new StringBuilder();for(Report r:reports)sb.append(r.toConfig()).append("\n");Files.writeString(dataDir.resolve("reports.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("reports.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){Report r=Report.fromConfig(l);if(r!=null){reports.add(r);nextId.updateAndGet(v->Math.max(v,Integer.parseInt(r.getId().replace("R",""))+1));}}}catch(Exception e){}}
    private void notifyStaff(String msg){for(PlayerRef p:Universe.get().getPlayers())p.sendMessage(Message.raw("§c[REPORT] "+msg));}
    public AbstractPlayerCommand getReportCommand(){
        return new AbstractPlayerCommand("report","Report a rule-breaking player. /report <player> <reason>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                if(args.length<2){playerRef.sendMessage(Message.raw("Usage: /report <player> <reason>"));return;}
                String target=args[0],reason=args[1];
                // Rate limit: max 3 reports per player
                long myReports=reports.stream().filter(r->r.getReporter().equalsIgnoreCase(playerRef.getUsername())&&r.getStatus().equals("open")).count();
                if(myReports>=3){playerRef.sendMessage(Message.raw("[Reports] You already have 3 open reports. Wait for staff to review."));return;}
                String id="R"+nextId.getAndIncrement();
                Report r=new Report(id,playerRef.getUsername(),target,reason);
                addReport(r);
                playerRef.sendMessage(Message.raw("[Reports] Report submitted ("+id+"). Staff have been notified."));
                notifyStaff("§6"+playerRef.getUsername()+"§c reported §6"+target+"§c: "+reason+" ["+id+"]");
            }
        };
    }
    public AbstractPlayerCommand getReportsAdminCommand(){
        return new AbstractPlayerCommand("reports","[Admin] View and manage reports. /reports list|resolve <id>|dismiss <id>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                String sub=args.length>0?args[0].toLowerCase():"list";
                switch(sub){
                    case"list"->{List<Report> open=reports.stream().filter(r->r.getStatus().equals("open")).toList();
                        if(open.isEmpty()){playerRef.sendMessage(Message.raw("[Reports] No open reports."));break;}
                        playerRef.sendMessage(Message.raw("=== Open Reports ("+open.size()+") ==="));
                        for(Report r:open)playerRef.sendMessage(Message.raw("  §6"+r.getId()+"§r ["+r.getTime()+"] "+r.getReporter()+" → "+r.getReported()+": "+r.getReason()));}
                    case"resolve"->{if(args.length<2)break;String id=args[1].toUpperCase();reports.stream().filter(r->r.getId().equals(id)).findFirst().ifPresentOrElse(r->{r.setStatus("resolved");save();playerRef.sendMessage(Message.raw("[Reports] Resolved: "+id));},()->playerRef.sendMessage(Message.raw("[Reports] Not found: "+id)));}
                    case"dismiss"->{if(args.length<2)break;String id=args[1].toUpperCase();reports.stream().filter(r->r.getId().equals(id)).findFirst().ifPresentOrElse(r->{r.setStatus("dismissed");save();playerRef.sendMessage(Message.raw("[Reports] Dismissed: "+id));},()->playerRef.sendMessage(Message.raw("[Reports] Not found: "+id)));}
                    default->playerRef.sendMessage(Message.raw("Usage: /reports list | resolve <id> | dismiss <id>"));
                }
            }
        };
    }
}

package com.howlstudio.reportsystem;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
import java.util.stream.Collectors;
public class ReportManager {
    private final Path dataDir;
    private final List<Report> reports=new ArrayList<>();
    private final Map<String,Long> cooldowns=new HashMap<>();
    private static final long COOLDOWN_MS=60_000L;
    public ReportManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}
    public long getOpenCount(){return reports.stream().filter(r->!r.isResolved()).count();}
    public void addReport(Report r){reports.add(r);save();}
    public void save(){try{StringBuilder sb=new StringBuilder();for(Report r:reports)sb.append(r.toConfig()).append("\n");Files.writeString(dataDir.resolve("reports.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("reports.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){Report r=Report.fromConfig(l);if(r!=null)reports.add(r);}}catch(Exception e){}}
    private void notifyStaff(String msg){for(PlayerRef p:Universe.get().getPlayers())p.sendMessage(Message.raw(msg));System.out.println("[Reports] "+msg);}
    public AbstractPlayerCommand getReportCommand(){
        return new AbstractPlayerCommand("report","Report a player. /report <player> <reason>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                if(args.length<2){playerRef.sendMessage(Message.raw("Usage: /report <player> <reason>"));return;}
                String key=playerRef.getUsername().toLowerCase();long now=System.currentTimeMillis();
                if(cooldowns.containsKey(key)&&(now-cooldowns.get(key))<COOLDOWN_MS){playerRef.sendMessage(Message.raw("[Report] Please wait before submitting another report."));return;}
                cooldowns.put(key,now);
                Report r=new Report(playerRef.getUsername(),args[0],args[1]);addReport(r);
                playerRef.sendMessage(Message.raw("[Report] §aSubmitted! Report #"+r.getId()+" will be reviewed by staff."));
                notifyStaff("§c[Report #"+r.getId()+"] §e"+playerRef.getUsername()+"§r reported §c"+args[0]+"§r: "+args[1]);
            }
        };
    }
    public AbstractPlayerCommand getReportsAdminCommand(){
        return new AbstractPlayerCommand("reports","[Admin] View/handle reports. /reports list|handle <id>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                String sub=args.length>0?args[0].toLowerCase():"list";
                if(sub.equals("handle")&&args.length>1){
                    try{int id=Integer.parseInt(args[1]);Report r=reports.stream().filter(x->x.getId()==id).findFirst().orElse(null);
                        if(r==null){playerRef.sendMessage(Message.raw("[Reports] Not found: #"+id));return;}
                        r.resolve();save();playerRef.sendMessage(Message.raw("[Reports] §aResolved #"+id+"."));}
                    catch(Exception e){playerRef.sendMessage(Message.raw("Usage: /reports handle <id>"));}
                }else{
                    List<Report> open=reports.stream().filter(r->!r.isResolved()).collect(Collectors.toList());
                    if(open.isEmpty()){playerRef.sendMessage(Message.raw("[Reports] No open reports."));return;}
                    playerRef.sendMessage(Message.raw("=== Open Reports ("+open.size()+") ==="));
                    for(Report r:open)playerRef.sendMessage(Message.raw("  #"+r.getId()+" ["+r.getTime()+"] "+r.getReporter()+" → §c"+r.getTarget()+"§r: "+r.getReason()));
                }
            }
        };
    }
}

package com.howlstudio.reportsystem;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
/** ReportSystem — Players report others for review. Admin queue with /reports view and /report handle. */
public final class ReportSystemPlugin extends JavaPlugin {
    private ReportManager mgr;
    public ReportSystemPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[Reports] Loading...");
        mgr=new ReportManager(getDataDirectory());
        CommandManager cmd=CommandManager.get();
        cmd.register(mgr.getReportCommand());
        cmd.register(mgr.getReportsAdminCommand());
        System.out.println("[Reports] Ready. "+mgr.getOpenCount()+" open reports.");
    }
    @Override protected void shutdown(){if(mgr!=null)mgr.save();System.out.println("[Reports] Stopped.");}
}

package lielietea.mirai.plugin.admintools;

import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminCommandDispatcher {
    static AdminCommandDispatcher INSTANCE = new AdminCommandDispatcher();
    final ExecutorService executor;

    public AdminCommandDispatcher() {
        this.executor = Executors.newCachedThreadPool();
    }

    public static AdminCommandDispatcher getInstance() {
        return INSTANCE;
    }

    public void handleMessage(MessageEvent event){
        //TODO 先临时丢这里，回头再改
        if(event instanceof FriendMessageEvent) AdminTools.getINSTANCE().handleAdminCommand((FriendMessageEvent) event);
    }

    public void close() {
        executor.shutdown();
    }
}

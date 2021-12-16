package lielietea.mirai.plugin.utils;


import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GroupPolice {

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    final static GroupPolice INSTANCE = new GroupPolice();

    GroupPolice() {
        executor.scheduleAtFixedRate(new AutoClear(), (long)0.5, 3, TimeUnit.HOURS);
    }

    static public GroupPolice getINSTANCE() {
        return INSTANCE;
    }

    static class AutoClear implements Runnable {
        @Override
        public void run() {
            for (Bot bot : Bot.getInstances()) {
                for (Group group : bot.getGroups()) {
                    if (IdentityUtil.DevGroup.DEFAULT.isDevGroup(group.getId())) continue;
                    if (group.getMembers().getSize() <= 10) {
                        group.sendMessage("��ͲĿǰ�����ܼ���10�����µ�Ⱥ�ģ����Զ���Ⱥ����������Ⱥ��ʹ����Ͳ����л��ʹ����Ͳ�ķ���");
                        MessageUtil.notifyDevGroup("����Ⱥ����������10�ˣ���Ͳ�Ѿ���"+group.getName()+"("+group.getId()+")���뿪��");
                        executor.schedule(() -> {
                            Objects.requireNonNull(bot.getGroup(group.getId())).quit();
                        }, 15, TimeUnit.SECONDS);
                    }

                    for (NormalMember nm : group.getMembers()) {
                        for (Bot bot2 : Bot.getInstances()) {
                            if (bot2.getId() == (bot.getId())) continue;
                            if (nm.getId() == bot2.getId()) {
                                if(nm.getId()<bot.getId()) continue;
                                group.sendMessage("��⵽����������Ͳ�˻��ڴ�Ⱥ���У����Զ���Ⱥ��");
                                MessageUtil.notifyDevGroup("���ڼ�⵽������Ͳ����Ͳ�Ѿ���"+group.getName()+"("+group.getId()+")���뿪��");
                                executor.schedule(() -> {
                                    Objects.requireNonNull(bot.getGroup(group.getId())).quit();
                                }, 15, TimeUnit.SECONDS);
                            }
                        }
                    }
                }
            }
        }
    }

    public void ini(){}
}

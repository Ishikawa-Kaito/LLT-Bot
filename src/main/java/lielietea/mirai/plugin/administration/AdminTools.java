package lielietea.mirai.plugin.administration;

import lielietea.mirai.plugin.core.responder.ResponderManager;
import lielietea.mirai.plugin.utils.IdentityUtil;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class AdminTools {

    static final AdminTools INSTANCE = new AdminTools();

    public static AdminTools getINSTANCE() {
        return INSTANCE;
    }

    public void handleAdminCommand(MessageEvent event) {
        if (event.getMessage().contentToString().contains("/group")) {
            try {
                getGroupList(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (event.getMessage().contentToString().contains("/friend")) {
            try {
                getFriendList(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (event.getMessage().contentToString().contains("/reload")) {
            reloadManually(event);
        }
        if (event.getMessage().contentToString().contains("/optimize")) {
            optimizeManually(event);
        }

        if (event.getMessage().contentToString().contains("/coverage")) {
            getCoverage(event);
        }
        if (event.getMessage().contentToString().contains("/numf")) {
            getFriendNum(event);
        }
        if (event.getMessage().contentToString().contains("/numg")) {
            getGroupNum(event);
        }
        if (event.getMessage().contentToString().contains("/adminhelper")) {
            event.getSubject().sendMessage("/group\n/friend\n/reload\n/optimize\n/coverage覆盖人数\n/numf好友人数\n/numg群聊人数\n");
        }
    }

    void reloadManually(MessageEvent event) {
        // TODO 资源重载管理器已经被移除了
        //  - 这里需要新方法
        MessageChainBuilder messages = new MessageChainBuilder();
        messages.append("该功能正在重写中");
        event.getSubject().sendMessage(messages.build());
    }

    void optimizeManually(MessageEvent event) {
        MessageChainBuilder messages = new MessageChainBuilder();
        String result = ResponderManager.getINSTANCE().optimizeHandlerSequence(false);
        messages.append(result);
        event.getSubject().sendMessage(messages.build());
    }

    String addGroupInfo(Iterator<Group> listIter, String allGroupInfo) {
        Group next = listIter.next();
        String allGroupInfo2 = allGroupInfo + "\n群ID " + next.getId() + "\n群名称 " + next.getName() + "\n群主ID " + next.getOwner().getId() + "\n群主昵称 " + next.getOwner().getNick() + "\n机器人权限 " + next.getBotPermission().name() + "\n";
        allGroupInfo2 = allGroupInfo2 + "----------\n";
        return allGroupInfo2;
    }

    void getGroupList(MessageEvent event) throws InterruptedException {
        if (IdentityUtil.isAdmin(event)) {
            Iterator<Group> listIter = event.getBot().getGroups().stream().iterator();
            int size = event.getBot().getGroups().getSize();
            String[] allGroupInfo = new String[size / 20 + 1];
            Arrays.fill(allGroupInfo, "");
            int count = 0;
            int countString = 0;
            while (listIter.hasNext()) {
                if (count > 19) {
                    count = 0;
                    countString += 1;
                }
                allGroupInfo[countString] = addGroupInfo(listIter, allGroupInfo[countString]);
                count += 1;
            }
            for (int i = 0; i <= countString; i++) {
                event.getSubject().sendMessage(allGroupInfo[i]);
                Thread.sleep(1000);
            }
            event.getSubject().sendMessage("七筒目前的群数量是：" + size);
        }
    }

    String addFriendInfo(Iterator<Friend> listIter, String allFriendInfo) {
        Friend next = listIter.next();
        return allFriendInfo + "\n好友ID " + next.getId() + "\n好友名称 " + next.getNick() + "\n";
    }

    void getFriendList(MessageEvent event) throws InterruptedException {
        if (IdentityUtil.isAdmin(event)) {
            Iterator<Friend> listIter = event.getBot().getFriends().stream().iterator();
            int size = event.getBot().getFriends().getSize();
            String[] allFriendInfo = new String[size / 30 + 1];
            Arrays.fill(allFriendInfo, "");
            int count = 0;
            int countString = 0;
            while (listIter.hasNext()) {
                if (count > 29) {
                    count = 0;
                    countString += 1;
                }
                allFriendInfo[countString] = addFriendInfo(listIter, allFriendInfo[countString]);
                count += 1;
            }
            for (int i = 0; i <= countString; i++) {
                event.getSubject().sendMessage(allFriendInfo[i]);
                Thread.sleep(1000);
            }
            event.getSubject().sendMessage("七筒目前的好友数量是：" + size);
        }
    }

    void getFriendNum(MessageEvent event) {
        if (IdentityUtil.isAdmin(event)) {
            int size = event.getBot().getFriends().getSize();
            event.getSubject().sendMessage("七筒目前的好友数量是：" + size);
        }
    }

    void getGroupNum(MessageEvent event) {
        if (IdentityUtil.isAdmin(event)) {
            int size = event.getBot().getGroups().getSize();
            event.getSubject().sendMessage("七筒目前的群数量是：" + size);
        }
    }

    void getCoverage(MessageEvent event) {
        if (IdentityUtil.isAdmin(event)) {
            Iterator<Group> listIter = event.getBot().getGroups().stream().iterator();
            ArrayList<Long> list = new ArrayList<>();
            while (listIter.hasNext()) {
                Iterator<NormalMember> listIterMember = listIter.next().getMembers().stream().iterator();
                while (listIterMember.hasNext()) {
                    long userID = listIterMember.next().getId();
                    if (!list.contains(userID)) {
                        list.add(userID);
                    }
                }
            }
            int size = list.size();
            event.getSubject().sendMessage("七筒目前的覆盖人数是：" + size);
            list.clear();
        }
    }

}

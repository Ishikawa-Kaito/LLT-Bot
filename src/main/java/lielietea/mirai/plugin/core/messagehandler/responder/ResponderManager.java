package lielietea.mirai.plugin.core.messagehandler.responder;


import lielietea.mirai.plugin.administration.statistics.MPSEHandler.MPSEStatistics;
import lielietea.mirai.plugin.core.MessageChainPackage;
import lielietea.mirai.plugin.core.messagehandler.responder.autoreply.AntiDirtyWord;
import lielietea.mirai.plugin.core.messagehandler.responder.autoreply.AntiOverwatch;
import lielietea.mirai.plugin.core.messagehandler.responder.autoreply.Goodbye;
import lielietea.mirai.plugin.core.messagehandler.responder.autoreply.Greeting;
import lielietea.mirai.plugin.core.messagehandler.responder.dice.PlayDice;
import lielietea.mirai.plugin.core.messagehandler.responder.feastinghelper.dinnerpicker.MealPicker;
import lielietea.mirai.plugin.core.messagehandler.responder.feastinghelper.dinnerpicker.PizzaPicker;
import lielietea.mirai.plugin.core.messagehandler.responder.feastinghelper.drinkpicker.DrinkPicker;
import lielietea.mirai.plugin.core.messagehandler.responder.fursona.FursonaPunk;
import lielietea.mirai.plugin.core.messagehandler.responder.help.DisclTemporary;
import lielietea.mirai.plugin.core.messagehandler.responder.help.FunctTemporary;
import lielietea.mirai.plugin.core.messagehandler.responder.help.Help;
import lielietea.mirai.plugin.core.messagehandler.responder.lotterywinner.LotteryBummerMessageHandler;
import lielietea.mirai.plugin.core.messagehandler.responder.lotterywinner.LotteryC4MessageHandler;
import lielietea.mirai.plugin.core.messagehandler.responder.lotterywinner.LotteryWinnerMessageHandler;
import lielietea.mirai.plugin.core.messagehandler.responder.lovelypicture.LovelyImage;
import lielietea.mirai.plugin.core.messagehandler.responder.mahjong.FortuneTeller;
import lielietea.mirai.plugin.core.messagehandler.responder.overwatch.HeroLinesSelector;
import lielietea.mirai.plugin.utils.exception.MessageEventTypeException;
import lielietea.mirai.plugin.utils.MessageUtil;
import lielietea.mirai.plugin.utils.StandardTimeUtil;
import lielietea.mirai.plugin.utils.multibot.MultiBotHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.*;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 这个类管理所有回复处理器，并将回复事件传递给回复处理器。回复处理器是用来处理消息事件，并根据情况进行回复的组件(包括自动打招呼，关键词触发，指令 etc)，
 *
 * <p>该管理器是线程安全的.</p>
 *
 * <p>所有回复处理器(也就是不同功能的回复模组)，都需要实现 {@link MessageResponder} 接口，并在使用 {@link #register(Supplier)} 进行注册。推荐在 {@link #ini()} 方法内进行注册</p>
 */
public class ResponderManager {
    static final Lock LOCK = new ReentrantLock();
    static final Timer TIMER = new Timer(true);

    static {
        //TODO:这个通知的工作不应该由这个类来做
        TIMER.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               String result = ResponderManager.getINSTANCE().optimizeHandlerSequence(true);
                               //Notify Devs
                               MessageUtil.notifyDevGroup(result, MultiBotHandler.BotName.Chitung1.getValue());
                               for(Bot bot: Bot.getInstances()){
                                   MessageUtil.notifyDevGroup(MPSEStatistics.buildMPSEStatistics(bot.getId()));
                               }

                           }
                       },
                StandardTimeUtil.getStandardFirstTime(0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(0, 6, 0, 0));
    }

    final List<BoxedHandler> handlers;
    final Map<UUID, BoxedHandler> addressMap;

    ResponderManager() {
        handlers = new ArrayList<>();
        addressMap = new HashMap<>();
    }

    static final ResponderManager INSTANCE = new ResponderManager();

    public static ResponderManager getINSTANCE() {
        return INSTANCE;
    }

    public MessageChainPackage handle(MessageEvent event, UUID handler) {
        return addressMap.get(handler).handle(event);
    }

    public Optional<UUID> match(MessageEvent event) {
        LOCK.lock();
        try {
            MessageResponder.MessageType type = null;
            try {
                type = getType(event);
            } catch (MessageEventTypeException e) {
                e.printStackTrace();
            }
            if (type == null) return Optional.empty();
            for (BoxedHandler handler : handlers) {
                if (handler.isBetaFeature()) {
                    if (true/*TODO:这里缺个Group Config的判断*/) {
                        if (handler.match(event, type)) {
                            return Optional.of(handler.getUUID());
                        }
                    }
                } else if (handler.needPermission()) {
                    if (true/*TODO:这里缺个Group Config的判断*/) {
                        if (handler.match(event, type)) {
                            return Optional.of(handler.getUUID());
                        }
                    }
                } else {
                    if (handler.match(event, type)) {
                        return Optional.of(handler.getUUID());
                    }
                }
            }
            return Optional.empty();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 注册回复处理器，处理器都需要实现 {@link MessageResponder} 接口，
     */
    @SuppressWarnings("unchecked")
    public void register(Supplier<MessageResponder<? extends MessageEvent>> handler) {
        BoxedHandler registry = new BoxedHandler((MessageResponder<MessageEvent>) handler.get());
        handlers.add(registry);
        addressMap.put(registry.getUUID(), registry);
    }

    /**
     * 初始化该管理器类。必须在插件启动时调用。
     */
    public void ini() {
        register(FursonaPunk::new);
        register(Help::new);
        register(LotteryWinnerMessageHandler::new);
        register(LotteryBummerMessageHandler::new);
        register(LotteryC4MessageHandler::new);
        register(DrinkPicker::new);
        register(MealPicker::new);
        register(PizzaPicker::new);
        register(FortuneTeller::new);
        register(PlayDice::new);
        register(Goodbye::new);
        register(AntiOverwatch::new);
        register(AntiDirtyWord::new);
        register(Greeting::new);
        register(HeroLinesSelector::new);
        register(FunctTemporary::new);
        register(DisclTemporary::new);
        register(LovelyImage::getINSTANCE);
    }

    /**
     * 优化回复处理器的调用顺序
     *
     * @return 优化后的回复处理器顺序与调用统计
     */
    public String optimizeHandlerSequence(boolean reset) {
        LOCK.lock();
        try {
            handlers.sort(new BoxedHandlerRearrangeComparator());
            StringBuilder builder = new StringBuilder("Newly optimized sequence: \n");
            for (BoxedHandler handler : handlers) {
                builder.append("|Functions:").append(handler.getName()).append("-Times:").append(handler.getCount()).append("|\n");
                if (reset) handler.resetCount();
            }
            return builder.toString();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 用此方法完成关闭Mirai时的收尾工作，必须在Mirai关闭时调用。
     */
    public void close() {
        for (BoxedHandler handler : handlers) {
            handler.close();
        }
    }

    /**
     * 通过UUID来获取回复处理器的名字
     *
     * @return 如果对应UUID的回复处理器存在，那么返回一个包含名字的Optional
     */
    public Optional<String> getName(UUID uuid) {
        LOCK.lock();
        try {
            for (BoxedHandler handler : handlers) {
                if (handler.getUUID().equals(uuid))
                    return Optional.of(handler.getName());
            }
            return Optional.empty();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 通过名字来获取回复处理器的UUID
     *
     * @return 如果对应名字的回复处理器存在，那么返回一个包含UUID的Optional
     */
    public Optional<UUID> getUUID(String name) {
        LOCK.lock();
        try {
            for (BoxedHandler handler : handlers) {
                if (handler.getName().equals(name))
                    return Optional.of(handler.getUUID());
            }
            return Optional.empty();
        } finally {
            LOCK.unlock();
        }
    }

    MessageResponder.MessageType getType(MessageEvent event) throws MessageEventTypeException {
        if (event instanceof GroupMessageEvent) return MessageResponder.MessageType.GROUP;
        else if (event instanceof FriendMessageEvent) return MessageResponder.MessageType.FRIEND;
        else if (event instanceof GroupTempMessageEvent) return MessageResponder.MessageType.TEMP;
        else if (event instanceof StrangerMessageEvent) return MessageResponder.MessageType.STRANGER;
        else {
            throw new MessageEventTypeException(event);
        }
    }

    static class BoxedHandler {
        final MessageResponder<MessageEvent> handler;
        final List<MessageResponder.MessageType> types;
        int count;

        BoxedHandler(MessageResponder<MessageEvent> handler) {
            this.handler = handler;
            types = handler.types();
            count = 0;
        }

        UUID getUUID() {
            return handler.getUUID();
        }

        String getName() {
            return handler.getName();
        }

        boolean match(MessageEvent event, MessageResponder.MessageType messageType) {
            if (fit(messageType))
                return handler.match(event);
            return false;
        }

        MessageChainPackage handle(MessageEvent event) {
            count++;
            return handler.handle(event);
        }

        boolean needPermission() {
            return handler.isPermissionRequired();
        }

        boolean isBetaFeature() {
            return handler.isOnBeta();
        }

        boolean fit(MessageResponder.MessageType type) {
            return types.contains(type);
        }

        int getCount() {
            return count;
        }

        void close() {
            handler.onclose();
        }

        void resetCount() {
            count = 0;
        }
    }

    static class BoxedHandlerRearrangeComparator implements Comparator<BoxedHandler> {

        @Override
        public int compare(BoxedHandler o1, BoxedHandler o2) {
            if (o1.getCount() > o2.getCount()) return -1;
            else if (o1.getCount() == o2.getCount()) return 0;
            return 1;
        }
    }

}

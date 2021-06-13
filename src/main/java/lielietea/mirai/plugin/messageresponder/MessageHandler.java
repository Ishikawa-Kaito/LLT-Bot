package lielietea.mirai.plugin.messageresponder;

import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 回复处理器接口，如果要让 {@link MessageRespondCenter} 对回复处理器进行托管，那么必须实现该类并注册
 *
 * <p>如果实现该接口同时实现 {@link Reloadable} 接口，那么 {@link MessageRespondCenter} 可以在需要时重载回复处理器的配置</p>
 *
 * @param <T> 必须继承自MessageEvent
 */
public interface MessageHandler<T extends MessageEvent> {

    /**
     * 这个方法是用来处理传入回复处理器的消息事件的，是回复处理器的入口。
     * @param event 传入消息事件
     * @return 该类必须返回一个布尔值，代表传入的消息事件是否由该处理器处理。
     */
    boolean handleMessage(T event);

    /**
     * 这个方法返回该回复处理器可以处理的消息事件的类型。返回值不能为空。
     * {@link MessageType}
     * @return 一个消息事件类型组成的列表
     */
    @NotNull
    List<MessageType> types();


    enum MessageType{
        GROUP,
        FRIEND,
        STRANGER,
        TEMP
    }
}
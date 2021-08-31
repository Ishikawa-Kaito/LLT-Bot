package lielietea.mirai.plugin.core.messagehandler.responder.autoreply;


import lielietea.mirai.plugin.core.MessageChainPackage;
import lielietea.mirai.plugin.core.messagehandler.responder.MessageResponder;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Goodbye implements MessageResponder<MessageEvent> {
    static final List<MessageType> TYPES = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));
    static final List<Pattern> REG_PATTERN = new ArrayList<>();

    static {
        {
            REG_PATTERN.add(Pattern.compile(".*" + "下线了" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "我走了" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "拜拜" + ".*"));
        }
    }

    @Override
    public boolean match(MessageEvent event) {
        for (Pattern pattern : REG_PATTERN) {
            if (pattern.matcher(event.getMessage().contentToString()).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MessageChainPackage handle(MessageEvent event) {
        return MessageChainPackage.getDefaultImpl(event, AutoReplyLinesCluster.reply(AutoReplyLinesCluster.ReplyType.GOODBYE), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }


    @Override
    public String getName() {
        return "自动回复：告别";
    }
}

package me.zeeplockd.freegenschatlink.discord;

import me.zeeplockd.freegenschatlink.modules.ChatLinker;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageHandler extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.getChannel().getId().equals(ChatLinker.getChannelId())) return;

        String content = event.getMessage().getContentStripped();
        String username = event.getAuthor().getEffectiveName();

        if (content.length() > 150) {
            event.getMessage().addReaction(Emoji.fromUnicode("🔥")).queue();
            return;
        }

        String formattedMessage = username + " » " + content;
        ChatUtils.sendPlayerMsg(formattedMessage);
    }
}

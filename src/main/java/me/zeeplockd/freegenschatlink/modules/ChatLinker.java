package me.zeeplockd.freegenschatlink.modules;

import me.zeeplockd.freegenschatlink.FreeGensChatLinkAddon;
import me.zeeplockd.freegenschatlink.discord.MessageHandler;
import me.zeeplockd.freegenschatlink.discord.ReadyListener;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ChatLinker extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private JDA jda;
    private TextChannel discordChannel;
    private ScheduledExecutorService autoJoinExecutor;
    private static ChatLinker instance;

    private static final Pattern FORMATTING_PATTERN = Pattern.compile("§[0-9a-fk-or]");

    private final Setting<String> botToken = sgGeneral.add(new StringSetting.Builder()
        .name("bot-token")
        .description("The bot token to use for the chat link.")
        .defaultValue("")
        .build());

    private final Setting<String> channelId = sgGeneral.add(new StringSetting.Builder()
        .name("channel-id")
        .description("The channel ID to link to.")
        .defaultValue("")
        .build());

    private final Setting<Boolean> autoJoin = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-join")
        .description("Whether the bot should automatically join FreeGens every 5 seconds.")
        .defaultValue(true)
        .build());

    public ChatLinker() {
        super(FreeGensChatLinkAddon.CATEGORY, "chat-linker", "Links Minecraft chat with Discord and auto-joins FreeGens.");
        instance = this;
    }

    @Override
    public void onActivate() {
        if (botToken.get().isEmpty() || channelId.get().isEmpty()) {
            error("Please set the bot token and channel ID in the settings.");
            toggle();
            return;
        }

        try {
            jda = JDABuilder.createDefault(botToken.get())
                .addEventListeners(new MessageHandler(), new ReadyListener())
                .build();

            jda.awaitReady();

            discordChannel = jda.getTextChannelById(channelId.get());
            if (discordChannel == null) {
                error("Could not find Discord channel with ID: " + channelId.get());
                toggle();
                return;
            }

            info("Connected to Discord channel: " + discordChannel.getName());

            if (autoJoin.get()) {
                startAutoJoin();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error("Failed to connect to Discord: Connection was interrupted");
            toggle();
        } catch (Exception e) {
            error("Failed to connect to Discord: " + e.getMessage());
            toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (autoJoinExecutor != null && !autoJoinExecutor.isShutdown()) {
            autoJoinExecutor.shutdown();
        }

        if (jda != null) {
            jda.shutdown();
            jda = null;
        }

        discordChannel = null;
    }

    private void startAutoJoin() {
        autoJoinExecutor = Executors.newSingleThreadScheduledExecutor();
        autoJoinExecutor.scheduleAtFixedRate(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.player.networkHandler != null) {
                client.player.networkHandler.sendChatCommand("join freegens");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (discordChannel == null) return;

        String messageText = event.getMessage().getString();

        // FORMATTING_PATTERN.matcher(messageText).replaceAll("");

        if (messageText.startsWith("[") || messageText.startsWith("!") ||
            messageText.startsWith("/") || messageText.trim().isEmpty() ||
            messageText.startsWith("Sending you to") || messageText.startsWith("You are already connected")) {
            return;
        }

        discordChannel.sendMessage(messageText).queue(
            success -> {},
            error -> FreeGensChatLinkAddon.LOG.error("Failed to send message to Discord: " + error.getMessage())
        );
    }

    public static String getChannelId() {
        return instance != null ? instance.channelId.get() : "";
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onGameJoined(GameJoinedEvent event) {
        if (isActive() && autoJoin.get()) {
            if (autoJoinExecutor != null && !autoJoinExecutor.isShutdown()) {
                autoJoinExecutor.shutdown();
            }
            startAutoJoin();
        }
    }
}

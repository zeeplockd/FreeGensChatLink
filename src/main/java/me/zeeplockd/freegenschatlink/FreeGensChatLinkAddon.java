package me.zeeplockd.freegenschatlink;

import me.zeeplockd.freegenschatlink.modules.ChatLinker;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class FreeGensChatLinkAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("FreeGensChatLink");

    @Override
    public void onInitialize() {
        Modules.get().add(new ChatLinker());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.zeeplockd.freegenschatlink";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("zeeplockd", "FreeGensChatLink");
    }
}

package org.mvplugins.multiverse.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.api.MVConfig;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.configuration.handle.CommentedYamlConfigHandle;
import org.mvplugins.multiverse.core.configuration.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.configuration.migration.BooleanMigratorAction;
import org.mvplugins.multiverse.core.configuration.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.configuration.migration.IntegerMigratorAction;
import org.mvplugins.multiverse.core.configuration.migration.InvertBoolMigratorAction;
import org.mvplugins.multiverse.core.configuration.migration.MoveMigratorAction;
import org.mvplugins.multiverse.core.configuration.migration.VersionMigrator;

@Service
public class MVCoreConfig implements MVConfig {
    public static final String CONFIG_FILENAME = "config.yml";
    public static final double CONFIG_VERSION = 5.0;

    private final Path configPath;
    private final MVCoreConfigNodes configNodes;
    private final CommentedYamlConfigHandle configHandle;
    private final StringPropertyHandle stringPropertyHandle;

    @Inject
    MVCoreConfig(@NotNull MultiverseCore core, @NotNull PluginManager pluginManager, @NotNull MVCommandManager commandManager) {
        this.configPath = Path.of(core.getDataFolder().getPath(), CONFIG_FILENAME);
        this.configNodes = new MVCoreConfigNodes(pluginManager, commandManager);
        this.configHandle = CommentedYamlConfigHandle.builder(configPath, configNodes.getNodes())
                .logger(Logging.getLogger())
                .migrator(ConfigMigrator.builder(configNodes.VERSION)
                        .addVersionMigrator(VersionMigrator.builder(5.0)
                                .addAction(MoveMigratorAction.of("multiverse-configuration.enforceaccess", "world.enforce-access"))
                                .addAction(BooleanMigratorAction.of("world.enforce-access"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.prefixchat", "messaging.enable-chat-prefix"))
                                .addAction(BooleanMigratorAction.of("messaging.enable-chat-prefix"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.prefixchatformat", "messaging.chat-prefix-format"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.teleportintercept", "world.teleport-intercept"))
                                .addAction(BooleanMigratorAction.of("world.teleport-intercept"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.firstspawnoverride", "spawn.first-spawn-override"))
                                .addAction(BooleanMigratorAction.of("spawn.first-spawn-override"))
                                //.addAction(MoveMigratorAction.of("multiverse-configuration.displaypermerrors", ""))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.globaldebug", "misc.global-debug"))
                                .addAction(IntegerMigratorAction.of("misc.global-debug"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.silentstart", "misc.silent-start"))
                                .addAction(BooleanMigratorAction.of("misc.silent-start"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.firstspawnworld", "spawn.first-spawn-location"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.defaultportalsearch", "portal.use-custom-portal-search"))
                                .addAction(BooleanMigratorAction.of("portal.use-custom-portal-search"))
                                .addAction(InvertBoolMigratorAction.of("portal.use-custom-portal-search"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.portalsearchradius", "portal.custom-portal-search-radius"))
                                .addAction(IntegerMigratorAction.of("portal.custom-portal-search-radius"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.autopurge", "world.auto-purge-entities"))
                                .addAction(BooleanMigratorAction.of("world.auto-purge-entities"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.idonotwanttodonate", "misc.show-donation-message"))
                                .addAction(BooleanMigratorAction.of("misc.show-donation-message"))
                                .addAction(InvertBoolMigratorAction.of("misc.show-donation-message"))
                                .build())
                        .build())
                .build();
        this.stringPropertyHandle = new StringPropertyHandle(configHandle);
        load();
        save();
    }

    private void migrateFromOldConfigFile() {
        String content;
        try {
            content = Files.readString(configPath);
        } catch (IOException e) {
            return;
        }
        // Remove the old config section if it is still in the old ConfigurationSerializable.
        content = content.replace("==: com.onarandombox.MultiverseCore.MultiverseCoreConfiguration", "");
        try {
            Files.writeString(configPath, content);
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public Try<Void> load() {
        migrateFromOldConfigFile();
        return configHandle.load();
    }

    @Override
    public boolean isLoaded() {
        return configHandle.isLoaded();
    }

    @Override
    public Try<Void> save() {
        return configHandle.save();
    }

    @Override
    public StringPropertyHandle getStringPropertyHandle() {
        return stringPropertyHandle;
    }

    @Override
    public void setEnforceAccess(boolean enforceAccess) {
        configHandle.set(configNodes.ENFORCE_ACCESS, enforceAccess);
    }

    @Override
    public boolean getEnforceAccess() {
        return configHandle.get(configNodes.ENFORCE_ACCESS);
    }

    @Override
    public void setEnforceGameMode(boolean enforceGameMode) {
        configHandle.set(configNodes.ENFORCE_GAMEMODE, enforceGameMode);
    }

    @Override
    public boolean getEnforceGameMode() {
        return configHandle.get(configNodes.ENFORCE_GAMEMODE);
    }

    @Override
    public void setAutoPurgeEntities(boolean autopurge) {
        configHandle.set(configNodes.AUTO_PURGE_ENTITIES, autopurge);
    }

    @Override
    public boolean isAutoPurgeEntities() {
        return configHandle.get(configNodes.AUTO_PURGE_ENTITIES);
    }

    @Override
    public void setTeleportIntercept(boolean teleportIntercept) {
        configHandle.set(configNodes.TELEPORT_INTERCEPT, teleportIntercept);
    }

    @Override
    public boolean getTeleportIntercept() {
        return configHandle.get(configNodes.TELEPORT_INTERCEPT);
    }

    @Override
    public void setFirstSpawnOverride(boolean firstSpawnOverride) {
        configHandle.set(configNodes.FIRST_SPAWN_OVERRIDE, firstSpawnOverride);
    }

    @Override
    public boolean getFirstSpawnOverride() {
        return configHandle.get(configNodes.FIRST_SPAWN_OVERRIDE);
    }

    @Override
    public void setFirstSpawnLocation(String firstSpawnWorld) {
        configHandle.set(configNodes.FIRST_SPAWN_LOCATION, firstSpawnWorld);
    }

    @Override
    public String getFirstSpawnLocation() {
        return configHandle.get(configNodes.FIRST_SPAWN_LOCATION);
    }

    @Override
    public void setEnableJoinDestination(boolean enableJoinDestination) {
        configHandle.set(configNodes.ENABLE_JOIN_DESTINATION, enableJoinDestination);
    }

    @Override
    public boolean getEnableJoinDestination() {
        return  configHandle.get(configNodes.ENABLE_JOIN_DESTINATION);
    }

    @Override
    public void setJoinDestination(String alwaysSpawnDestination) {
        configHandle.set(configNodes.JOIN_DESTINATION, alwaysSpawnDestination);
    }

    @Override
    @Nullable
    public String getJoinDestination() {
        if (Objects.equals(configHandle.get(configNodes.JOIN_DESTINATION), "")) {
            return null;
        }
        return  configHandle.get(configNodes.JOIN_DESTINATION);
    }

    @Override
    public void setUseCustomPortalSearch(boolean useDefaultPortalSearch) {
        configHandle.set(configNodes.USE_CUSTOM_PORTAL_SEARCH, useDefaultPortalSearch);
    }

    @Override
    public boolean isUsingCustomPortalSearch() {
        return configHandle.get(configNodes.USE_CUSTOM_PORTAL_SEARCH);
    }

    @Override
    public void setCustomPortalSearchRadius(int searchRadius) {
        configHandle.set(configNodes.CUSTOM_PORTAL_SEARCH_RADIUS, searchRadius);
    }

    @Override
    public int getCustomPortalSearchRadius() {
        return configHandle.get(configNodes.CUSTOM_PORTAL_SEARCH_RADIUS);
    }

    @Override
    public void setEnablePrefixChat(boolean prefixChat) {
        configHandle.set(configNodes.ENABLE_CHAT_PREFIX, prefixChat);
    }

    @Override
    public boolean isEnablePrefixChat() {
        return configHandle.get(configNodes.ENABLE_CHAT_PREFIX);
    }

    @Override
    public void setPrefixChatFormat(String prefixChatFormat) {
        configHandle.set(configNodes.CHAT_PREFIX_FORMAT, prefixChatFormat);
    }

    @Override
    public String getPrefixChatFormat() {
        return configHandle.get(configNodes.CHAT_PREFIX_FORMAT);
    }

    @Override
    public void setRegisterPapiHook(boolean registerPapiHook) {
        configHandle.set(configNodes.REGISTER_PAPI_HOOK, registerPapiHook);
    }

    @Override
    public boolean isRegisterPapiHook() {
        return configHandle.get(configNodes.REGISTER_PAPI_HOOK);
    }

    @Override
    public void setGlobalDebug(int globalDebug) {
        configHandle.set(configNodes.GLOBAL_DEBUG, globalDebug);
    }

    @Override
    public int getGlobalDebug() {
        return configHandle.get(configNodes.GLOBAL_DEBUG);
    }

    @Override
    public void setSilentStart(boolean silentStart) {
        configHandle.set(configNodes.SILENT_START, silentStart);
    }

    @Override
    public boolean getSilentStart() {
        return configHandle.get(configNodes.SILENT_START);
    }

    @Override
    public void setShowDonateMessage(boolean showDonateMessage) {
        configHandle.set(configNodes.SHOW_DONATION_MESSAGE, showDonateMessage);
    }

    @Override
    public boolean isShowingDonateMessage() {
        return configHandle.get(configNodes.SHOW_DONATION_MESSAGE);
    }
}

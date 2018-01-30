package dk.xakeps.simplescavenger;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.translation.locale.Locales;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "simplescavenger",
        name = "Simple Scavenger",
        version = "2.0",
        description = "Keeps inventory and xp for players with permission",
        url = "https://spongeapi.com",
        authors = "Xakep_SDK")
public class SimpleScavenger {

    private CommentedConfigurationNode rootNode;

    private boolean respawnSoundEnabled;
    private double respawnSoundVolume;
    private SoundType respawnSound;

    private boolean deathSoundEnabled;
    private double deathSoundVolume;
    private SoundType deathSound;

    private boolean respawnMessageEnabled;
    private String respawnDefaultMessage;

    private boolean deathMessageEnabled;
    private String deathDefaultMessage;

    @Inject
    public SimpleScavenger(@DefaultConfig(sharedRoot = true) ConfigurationLoader<CommentedConfigurationNode> loader,
                           @ConfigDir(sharedRoot = false) Path configDir,
                           PluginContainer container) throws IOException {
        saveConfig(configDir, container);
        reloadConfig(loader);

        CommandSpec spec = CommandSpec.builder()
                .permission("simplescavenger.reload")
                .description(Text.of("Reloads plugin"))
                .extendedDescription(Text.of("Reloads plugin configuration"))
                .executor((src, args) -> {
                    try {
                        reloadConfig(loader);
                        Text successMsg = Text.of(TextColors.GRAY, '[', TextColors.GOLD, "SimpleScavenger", TextColors.GRAY, ']',
                                TextColors.WHITE, " Plugin reloaded!");
                        src.sendMessage(successMsg);
                        return CommandResult.success();
                    } catch (IOException e) {
                        Text errorMsg = Text.of(TextColors.GRAY, '[', TextColors.GOLD, "SimpleScavenger", TextColors.GRAY, ']',
                                TextColors.WHITE, " Error while reloading plugin. Check console.");
                        src.sendMessage(errorMsg);
                        return CommandResult.empty();
                    }
                })
                .build();
        Sponge.getCommandManager().register(container, spec, "simplescavenger", "scavenger");
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player player) {
        if(player.hasPermission("simplescavenger.use")) {
            event.setKeepInventory(true);
            if (deathMessageEnabled) {
                String msg = rootNode.getNode("messages", "death", player.getLocale().getLanguage())
                        .getString(deathDefaultMessage);
                Text text = TextSerializers.FORMATTING_CODE.deserialize(msg);
                player.sendMessage(text);
            }

            if (deathSoundEnabled) {
                player.playSound(deathSound, player.getLocation().getPosition(), deathSoundVolume);
            }
        }
    }

    @Listener
    public void onPlayerRespawn(RespawnPlayerEvent event) {
        Player player = event.getTargetEntity();
        if (respawnMessageEnabled) {
            String msg = rootNode.getNode("messages", "respawn", player.getLocale().getLanguage())
                    .getString(respawnDefaultMessage);
            Text text = TextSerializers.FORMATTING_CODE.deserialize(msg);
            player.sendMessage(text);
        }

        if (respawnSoundEnabled) {
            player.playSound(respawnSound, player.getLocation().getPosition(), respawnSoundVolume);
        }
    }

    private void reloadConfig(ConfigurationLoader<CommentedConfigurationNode> loader) throws IOException {
        this.rootNode = loader.load();

        this.respawnSoundEnabled = rootNode.getNode("sounds", "respawn", "enabled").getBoolean(true);
        this.respawnSound = SoundType.of(rootNode.getNode("sounds", "respawn", "sound").getString(SoundTypes.ENTITY_PLAYER_LEVELUP.getId()));
        this.respawnSoundVolume = rootNode.getNode("sounds", "respawn", "volume").getDouble(1);

        this.deathSoundEnabled = rootNode.getNode("sounds", "death", "enabled").getBoolean(true);
        this.deathSound = SoundType.of(rootNode.getNode("sounds", "death", "sound").getString(SoundTypes.BLOCK_ANVIL_LAND.getId()));
        this.deathSoundVolume = rootNode.getNode("sounds", "death", "volume").getDouble(1);

        this.respawnMessageEnabled = rootNode.getNode("messages", "respawn", "enabled").getBoolean(true);
        String resDefLang = rootNode.getNode("messages", "respawn", "defaultLang").getString(Locales.EN_US.getLanguage());
        this.respawnDefaultMessage = rootNode.getNode("messages", "respawn", resDefLang).getString("&7[&6SimpleScavenger&7]&f Respawned!");

        this.deathMessageEnabled = rootNode.getNode("messages", "death", "enabled").getBoolean(true);
        String deathDefLang = rootNode.getNode("messages", "death", "defaultLang").getString(Locales.EN_US.getLanguage());
        this.deathDefaultMessage = rootNode.getNode("messages", "death", deathDefLang).getString("&7[&6SimpleScavenger&7]&f Your inventory was saved!");
    }

    private void saveConfig(Path configDir, PluginContainer container) throws IOException {
        Optional<Asset> asset = container.getAsset("simplescavenger.conf");
        if (asset.isPresent()) {
            asset.get().copyToDirectory(configDir, true);
        } else {
            throw new RuntimeException("Can't save plugin config!");
        }
    }
}

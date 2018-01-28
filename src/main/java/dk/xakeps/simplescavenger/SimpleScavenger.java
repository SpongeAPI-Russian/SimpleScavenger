package dk.xakeps.simplescavenger;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "simplescavenger",
        name = "Simple Scavenger",
        version = "1.0",
        description = "Keeps inventory and xp for players with permission",
        url = "https://spongeapi.com",
        authors = "Xakep_SDK")
public class SimpleScavenger {
    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player player) {
        if(player.hasPermission("simplescavenger.use")) {
            event.setKeepInventory(true);
        }
    }
}

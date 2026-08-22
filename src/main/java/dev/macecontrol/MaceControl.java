package dev.macecontrol;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * MaceControl – Paper 1.21.11 (Mounts of Mayhem)
 *
 * <p>Gives server owners full control over:</p>
 * <ul>
 *   <li>Mace base (ground) damage</li>
 *   <li>Smash-attack fall-damage tier values and tier sizes</li>
 *   <li>Minimum fall distance required to trigger a smash attack</li>
 *   <li>Density enchantment bonus per block per level (levels 1–5)</li>
 *   <li>Breach enchantment armor-penetration fraction per level (levels 1–4)</li>
 * </ul>
 *
 * <p>All vanilla defaults are documented in config.yml so server owners always
 * know what "normal" Minecraft behaviour looks like.</p>
 */
public final class MaceControl extends JavaPlugin {

    /** Shared configuration object, populated on enable and on each reload. */
    private final MaceConfig maceConfig = new MaceConfig();

    @Override
    public void onEnable() {
        // Save default config.yml if it doesn't exist yet
        saveDefaultConfig();

        // Load values
        loadConfig();

        // Register damage listener
        getServer().getPluginManager().registerEvents(
                new MaceDamageListener(maceConfig), this);

        // Register /macecontrol command
        MaceControlCommand commandHandler = new MaceControlCommand(this);
        Objects.requireNonNull(getCommand("macecontrol"))
               .setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("macecontrol"))
               .setTabCompleter(commandHandler);

        getLogger().info("MaceControl enabled – Mace damage is now configurable.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MaceControl disabled.");
    }

    /**
     * Reloads config.yml from disk and re-applies all values.
     * Called on enable and by {@code /macecontrol reload}.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        loadConfig();
        getLogger().log(Level.INFO, "MaceControl config reloaded.");
    }

    // ─── PRIVATE ──────────────────────────────────────────────────────────────

    private void loadConfig() {
        try {
            maceConfig.load(getConfig());
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE,
                    "Failed to load config.yml – using vanilla defaults.", ex);
        }
    }
}

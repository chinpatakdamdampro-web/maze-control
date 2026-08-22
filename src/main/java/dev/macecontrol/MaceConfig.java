package dev.macecontrol;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Loads and caches every tunable value from config.yml.
 *
 * Vanilla Minecraft 1.21 (Mounts of Mayhem) defaults are documented
 * both here (as Java constants) and in config.yml so server owners can
 * always see what the game normally does.
 */
public final class MaceConfig {

    // ─── VANILLA DEFAULTS ─────────────────────────────────────────────────────
    // These constants mirror the actual 1.21 server values and serve as
    // fallbacks if a config key is missing or mis-typed.

    /** Default mace base-attack damage: 6 HP (3 hearts). */
    public static final double DEFAULT_BASE_DAMAGE = 6.0;

    /** Default minimum fall distance to trigger a smash attack: 1.5 blocks. */
    public static final double DEFAULT_MIN_FALL_BLOCKS = 1.5;

    /** Vanilla Tier 1: first 3 blocks fallen give 4 HP each. */
    public static final int    DEFAULT_TIER1_MAX_BLOCKS = 3;
    public static final double DEFAULT_TIER1_DAMAGE     = 4.0;

    /** Vanilla Tier 2: next 5 blocks fallen give 2 HP each. */
    public static final int    DEFAULT_TIER2_MAX_BLOCKS = 5;
    public static final double DEFAULT_TIER2_DAMAGE     = 2.0;

    /** Vanilla Tier 3: every subsequent block gives 1 HP. */
    public static final double DEFAULT_TIER3_DAMAGE     = 1.0;

    /** Vanilla Density bonus per level (index 0 = level 1). Values in HP/block. */
    public static final double[] DEFAULT_DENSITY = { 0.5, 1.0, 1.5, 2.0, 2.5 };

    /** Vanilla Breach armor-reduction fraction per level (index 0 = level 1). */
    public static final double[] DEFAULT_BREACH  = { 0.15, 0.30, 0.45, 0.60 };

    // ─── LOADED VALUES ────────────────────────────────────────────────────────

    private double baseDamage;
    private double minFallBlocks;
    private int    tier1MaxBlocks;
    private double tier1Damage;
    private int    tier2MaxBlocks;
    private double tier2Damage;
    private double tier3Damage;

    /** Density bonus HP per block at levels 1–5 (array index 0 = level 1). */
    private double[] densityBonusPerBlock = new double[5];

    /** Breach armor-reduction fraction at levels 1–4 (array index 0 = level 1). */
    private double[] breachArmorReduction = new double[4];

    // ─── LOAD / RELOAD ────────────────────────────────────────────────────────

    /**
     * Reads all values from the provided {@link FileConfiguration}.
     * Falls back to the vanilla default for any key that is absent or invalid.
     *
     * @param cfg the configuration object (already loaded from disk)
     */
    public void load(FileConfiguration cfg) {

        baseDamage    = cfg.getDouble("mace.base-damage",              DEFAULT_BASE_DAMAGE);
        minFallBlocks = cfg.getDouble("mace.smash-attack.min-fall-blocks", DEFAULT_MIN_FALL_BLOCKS);

        tier1MaxBlocks = cfg.getInt   ("mace.smash-attack.tier-1-max-blocks",    DEFAULT_TIER1_MAX_BLOCKS);
        tier1Damage    = cfg.getDouble("mace.smash-attack.damage-per-block-tier-1", DEFAULT_TIER1_DAMAGE);
        tier2MaxBlocks = cfg.getInt   ("mace.smash-attack.tier-2-max-blocks",    DEFAULT_TIER2_MAX_BLOCKS);
        tier2Damage    = cfg.getDouble("mace.smash-attack.damage-per-block-tier-2", DEFAULT_TIER2_DAMAGE);
        tier3Damage    = cfg.getDouble("mace.smash-attack.damage-per-block-tier-3", DEFAULT_TIER3_DAMAGE);

        // Density – 5 levels
        for (int i = 0; i < 5; i++) {
            int level = i + 1;
            densityBonusPerBlock[i] = cfg.getDouble(
                "enchantments.density.level-" + level + "-bonus-per-block",
                DEFAULT_DENSITY[i]
            );
        }

        // Breach – 4 levels
        for (int i = 0; i < 4; i++) {
            int level = i + 1;
            breachArmorReduction[i] = cfg.getDouble(
                "enchantments.breach.level-" + level + "-armor-reduction",
                DEFAULT_BREACH[i]
            );
        }
    }

    // ─── ACCESSORS ────────────────────────────────────────────────────────────

    /** Configured mace base-attack damage in HP. */
    public double getBaseDamage()    { return baseDamage;    }

    /** Minimum blocks fallen before a smash attack triggers. */
    public double getMinFallBlocks() { return minFallBlocks; }

    // Smash-attack tier sizing/damage
    public int    getTier1MaxBlocks() { return tier1MaxBlocks; }
    public double getTier1Damage()    { return tier1Damage;    }
    public int    getTier2MaxBlocks() { return tier2MaxBlocks; }
    public double getTier2Damage()    { return tier2Damage;    }
    public double getTier3Damage()    { return tier3Damage;    }

    /**
     * Returns the Density enchantment bonus (HP per block fallen) for a given
     * enchantment level (1–5).  Returns 0 for out-of-range levels.
     */
    public double getDensityBonus(int level) {
        if (level < 1 || level > 5) return 0.0;
        return densityBonusPerBlock[level - 1];
    }

    /**
     * Returns the Breach armor-reduction fraction for a given enchantment level
     * (1–4).  A value of 0.60 means 60% of the target's armor is ignored.
     * Returns 0 for out-of-range levels.
     */
    public double getBreachReduction(int level) {
        if (level < 1 || level > 4) return 0.0;
        return breachArmorReduction[level - 1];
    }

    /**
     * Calculates the total smash-attack bonus damage for a given fall distance.
     * This replicates the vanilla tier formula but uses the configurable values.
     *
     * @param fallBlocks  number of blocks fallen before impact
     * @param densityLevel Density enchantment level (0 = unenchanted)
     * @return total bonus damage to add on top of the base-damage value
     */
    public double calculateSmashBonus(double fallBlocks, int densityLevel) {
        if (fallBlocks < minFallBlocks) {
            return 0.0; // did not meet the minimum fall distance
        }

        double bonus = 0.0;

        // Tier 1
        double tier1Blocks = Math.min(fallBlocks, tier1MaxBlocks);
        bonus += tier1Blocks * tier1Damage;

        // Tier 2
        if (fallBlocks > tier1MaxBlocks) {
            double tier2Blocks = Math.min(fallBlocks - tier1MaxBlocks, tier2MaxBlocks);
            bonus += tier2Blocks * tier2Damage;
        }

        // Tier 3
        double tier1And2Cap = tier1MaxBlocks + tier2MaxBlocks;
        if (fallBlocks > tier1And2Cap) {
            double tier3Blocks = fallBlocks - tier1And2Cap;
            bonus += tier3Blocks * tier3Damage;
        }

        // Density enchantment adds a flat HP-per-block bonus across ALL fallen blocks
        if (densityLevel > 0) {
            bonus += fallBlocks * getDensityBonus(densityLevel);
        }

        return bonus;
    }
}

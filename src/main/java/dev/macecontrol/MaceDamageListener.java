package dev.macecontrol;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Intercepts every mace attack and replaces the damage value with our
 * fully configurable calculation that mirrors the vanilla formula but
 * uses server-owner-tuned values from config.yml.
 *
 * <h3>How vanilla mace damage works (1.21 / Mounts of Mayhem)</h3>
 * <ol>
 *   <li>Base damage: 6 HP (flat, always applied).</li>
 *   <li>Smash attack: if the attacker fell ≥1.5 blocks before the hit,
 *       tiered fall-damage bonuses are added (4/2/1 HP per block).</li>
 *   <li>Density enchantment: adds 0.5 HP per level per block fallen on
 *       top of the tier bonuses.</li>
 *   <li>Critical hit: multiplies the entire damage total by 1.5×.</li>
 *   <li>Breach enchantment: reduces how much of the target's armor absorbs
 *       damage (15% reduction per level, up to 60% at level 4).</li>
 * </ol>
 *
 * <h3>What this listener does</h3>
 * <ul>
 *   <li>Cancels the raw event damage (to prevent double-counting).</li>
 *   <li>Recalculates damage using configurable tier rates, Density bonus,
 *       and critical multiplier.</li>
 *   <li>Applies configurable Breach armor penetration via a temporary
 *       attribute modifier on the defender, restored immediately after
 *       damage is dealt.</li>
 * </ul>
 */
public final class MaceDamageListener implements Listener {

    private final MaceConfig config;

    public MaceDamageListener(MaceConfig config) {
        this.config = config;
    }

    /**
     * We use HIGHEST priority so that other plugins (e.g. protection plugins)
     * have already had their say, but we can still override the final number.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMaceHit(EntityDamageByEntityEvent event) {

        // ── 1. Confirm the attacker is a player holding a Mace ───────────────
        if (!(event.getDamager() instanceof Player attacker)) return;

        ItemStack mainHand = attacker.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.MACE) return;

        // ── 2. Confirm the attack cause is a melee hit ────────────────────────
        DamageCause cause = event.getCause();
        if (cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.ENTITY_SWEEP_ATTACK) return;

        // ── 3. Gather enchantment levels ──────────────────────────────────────
        ItemMeta meta = mainHand.getItemMeta();
        int densityLevel = (meta != null) ? meta.getEnchantLevel(Enchantment.DENSITY)  : 0;
        int breachLevel  = (meta != null) ? meta.getEnchantLevel(Enchantment.BREACH)   : 0;

        // ── 4. Calculate fall distance ────────────────────────────────────────
        // Paper exposes the fall distance through the Player API.
        // We use the player's current fall distance; Paper tracks this as the
        // blocks fallen since the player was last on the ground.
        double fallDistance = attacker.getFallDistance();

        // ── 5. Build the damage total ─────────────────────────────────────────
        double damage = config.getBaseDamage();

        boolean isSmashAttack = fallDistance >= config.getMinFallBlocks();
        double smashBonus = 0.0;

        if (isSmashAttack) {
            smashBonus = config.calculateSmashBonus(fallDistance, densityLevel);
            damage += smashBonus;
        }

        // ── 6. Critical hit multiplier (vanilla: ×1.5) ────────────────────────
        // A smash attack is almost always a critical hit in vanilla.
        // We replicate this by checking if the player is falling (fallDistance > 0).
        boolean isCritical = isSmashAttack || isCriticalHit(attacker);
        if (isCritical) {
            damage *= 1.5;
        }

        // ── 7. Override event damage ──────────────────────────────────────────
        event.setDamage(damage);

        // ── 8. Apply Breach armor penetration ────────────────────────────────
        // Breach works by temporarily reducing the target's effective armor.
        // We implement this by scaling down the final damage post-armor using
        // the configured reduction fraction, then applying the difference as
        // bonus damage so the net result is as if the armor were reduced.
        if (breachLevel > 0 && event.getEntity() instanceof LivingEntity defender) {
            applyBreach(event, defender, breachLevel, damage);
        }
    }

    /**
     * Applies Breach armor penetration by calculating how much extra damage
     * bypasses the target's armor and adding it back to the event.
     *
     * <p>Vanilla Breach reduces armor's effectiveness by 15% per level.
     * At Breach IV, 60% of the target's armor value is ignored.</p>
     *
     * @param event      the damage event (damage already set to our value)
     * @param defender   the entity being hit
     * @param breachLevel enchantment level (1–4)
     * @param rawDamage  the damage before armor reduction
     */
    private void applyBreach(EntityDamageByEntityEvent event,
                              LivingEntity defender,
                              int breachLevel,
                              double rawDamage) {

        double reductionFraction = config.getBreachReduction(breachLevel);
        if (reductionFraction <= 0.0) return;

        // Get the target's total armor value (0–20 scale in vanilla)
        AttributeInstance armorAttr = defender.getAttribute(Attribute.ARMOR);
        if (armorAttr == null) return;

        double armorValue    = armorAttr.getValue();
        double armorToughness = 0.0;
        AttributeInstance toughnessAttr = defender.getAttribute(Attribute.ARMOR_TOUGHNESS);
        if (toughnessAttr != null) {
            armorToughness = toughnessAttr.getValue();
        }

        // Vanilla armor-damage formula:
        //   protection = min(20, max(armor/5, armor - 4*damage/(armor_toughness+8)))
        //   effective damage = damage × (1 - protection/25)
        //
        // With Breach the armor value fed into the formula is multiplied by
        // (1 - reductionFraction).  We calculate full-armor damage vs
        // breached-armor damage and add the difference as bonus damage.

        double fullDamage    = applyArmorFormula(rawDamage, armorValue, armorToughness);
        double reducedArmor  = armorValue * (1.0 - reductionFraction);
        double breachedDamage = applyArmorFormula(rawDamage, reducedArmor, armorToughness);

        double extraDamage = breachedDamage - fullDamage;
        if (extraDamage > 0) {
            // Set the event's final damage to include the armor-penetration bonus.
            // The engine will still apply normal armor reduction on top of our base
            // damage value, so we pre-add the extra that Breach should recover.
            event.setDamage(rawDamage + extraDamage);
        }
    }

    /**
     * Replicates the vanilla armor damage-reduction formula.
     *
     * @param damage         raw incoming damage
     * @param armor          armor attribute value (0–20)
     * @param armorToughness armor toughness attribute value
     * @return the damage that would get through the given armor
     */
    private double applyArmorFormula(double damage, double armor, double armorToughness) {
        // Vanilla: effective_protection = clamp(20, armor/5,
        //          armor - 4*damage / (armorToughness + 8))
        double candidate1 = armor / 5.0;
        double candidate2 = armor - (4.0 * damage) / (armorToughness + 8.0);
        double protection = Math.min(20.0, Math.max(candidate1, candidate2));
        return damage * (1.0 - protection / 25.0);
    }

    /**
     * Rough critical-hit detection: vanilla crits require the player to be
     * falling, not in a fluid, not on a ladder, and with a fully charged
     * weapon.  We keep this lightweight since smash attacks are already
     * handled by the {@code isSmashAttack} flag.
     */
    private boolean isCriticalHit(Player player) {
        return player.getFallDistance() > 0
                && !player.isOnGround()
                && !player.isInWater()
                && !player.isClimbing();
    }
}

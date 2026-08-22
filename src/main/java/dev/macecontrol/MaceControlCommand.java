package dev.macecontrol;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handles the {@code /macecontrol} command.
 *
 * <ul>
 *   <li>{@code /macecontrol reload} – reloads config.yml and logs confirmation.</li>
 * </ul>
 */
public final class MaceControlCommand implements CommandExecutor, TabCompleter {

    private final MaceControl plugin;

    public MaceControlCommand(MaceControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("macecontrol.reload")) {
                sender.sendMessage(Component.text(
                        "You don't have permission to do that.", NamedTextColor.RED));
                return true;
            }

            plugin.reloadPluginConfig();
            sender.sendMessage(Component.text(
                    "[MaceControl] Configuration reloaded successfully.", NamedTextColor.GREEN));
            return true;
        }

        // Default / unknown sub-command → show usage
        sender.sendMessage(Component.text(
                "Usage: /macecontrol reload", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}

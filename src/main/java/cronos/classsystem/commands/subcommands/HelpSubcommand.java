package cronos.classsystem.commands.subcommands;

import cronos.classsystem.ClassSystemPlugin;
import cronos.classsystem.commands.base.AbstractSubcommand;
import cronos.classsystem.commands.base.Subcommand;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lista dostępnych subkomend, filtrowana wg uprawnień nadawcy.
 */
public class HelpSubcommand extends AbstractSubcommand {

    private final Map<String, Subcommand> subcommands;
    private final Map<String, String> aliasToPrimary;

    public HelpSubcommand(ClassSystemPlugin plugin, Map<String, Subcommand> subcommands, Map<String, String> aliasToPrimary) {
        super(plugin, "help");
        this.subcommands = subcommands;
        this.aliasToPrimary = aliasToPrimary;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(plugin.getConfigManager().getMessageNoPrefix("info.help-header"));

        Set<Subcommand> shown = new LinkedHashSet<>();
        for (Map.Entry<String, Subcommand> entry : subcommands.entrySet()) {
            String primary = aliasToPrimary.getOrDefault(entry.getKey(), entry.getKey());
            if (!sender.hasPermission(plugin.getConfigManager().getCommandPermission(primary))) {
                continue;
            }
            Subcommand sub = entry.getValue();
            if (!shown.add(sub)) {
                continue; // każda subkomenda raz, nawet gdy ma kilka aliasów
            }
            sender.sendMessage(plugin.getConfigManager().getMessageNoPrefix("info.help-entry",
                    "{usage}", sub.getUsage(),
                    "{description}", sub.getDescription()));
        }
        return true;
    }
}

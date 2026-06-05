package cronos.classsystem.commands;

import cronos.classsystem.ClassSystemPlugin;
import cronos.classsystem.commands.base.AbstractCommand;
import cronos.classsystem.commands.base.Subcommand;
import cronos.classsystem.commands.subcommands.HelpSubcommand;
import cronos.classsystem.commands.subcommands.ReloadSubcommand;
import cronos.classsystem.utils.DebugLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatch root komendy {@code /klasa}. Subkomendy rejestrowane z tłumaczalnymi
 * aliasami; alias mapowany na nazwę kanoniczną do sprawdzania uprawnień.
 */
public class ClassMainCommand extends AbstractCommand {

    private final ClassSystemPlugin plugin;
    private final Map<String, Subcommand> subcommands = new HashMap<>();
    private final Map<String, String> aliasToPrimary = new HashMap<>();

    public ClassMainCommand(ClassSystemPlugin plugin) {
        super(plugin.getConfigManager().getMainCommandName(),
              "/" + plugin.getConfigManager().getMainCommandName() + " [subkomenda]",
              plugin.getConfigManager().getCommandDescription(),
              plugin.getConfigManager().getPermissionMessage(),
              plugin.getConfigManager().getCommandAliases());
        this.plugin = plugin;
        registerSubcommands();
        register();
    }

    private void registerSubcommands() {
        registerSubcommandWithConfigAliases("help", new HelpSubcommand(plugin, subcommands, aliasToPrimary));
        registerSubcommandWithConfigAliases("reload", new ReloadSubcommand(plugin));
    }

    private void registerSubcommandWithConfigAliases(String subcommandName, Subcommand subcommand) {
        List<String> aliases = plugin.getConfigManager().getSubcommandAliases(subcommandName);
        if (aliases != null && !aliases.isEmpty()) {
            for (String alias : aliases) {
                subcommands.put(alias.toLowerCase(), subcommand);
                aliasToPrimary.put(alias.toLowerCase(), subcommandName.toLowerCase());
            }
        } else {
            subcommands.put(subcommandName.toLowerCase(), subcommand);
            aliasToPrimary.put(subcommandName.toLowerCase(), subcommandName.toLowerCase());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isSystemEnabled()) {
            sender.sendMessage(plugin.getMessage("errors.system-disabled"));
            return true;
        }

        if (!sender.hasPermission("ClassSystem.help")) {
            sender.sendMessage(plugin.getMessage("errors.no-permission-command"));
            return true;
        }

        // Bez argumentu — pokaż pomoc.
        String inputName = args.length == 0 ? "help" : args[0].toLowerCase();
        Subcommand subcommand = subcommands.get(inputName);
        String primaryName = aliasToPrimary.getOrDefault(inputName, inputName);

        if (subcommand == null) {
            sender.sendMessage(plugin.getMessage("errors.invalid-arguments"));
            return true;
        }

        if (!sender.hasPermission(plugin.getConfigManager().getCommandPermission(primaryName))) {
            sender.sendMessage(plugin.getMessage("errors.no-permission-command"));
            return true;
        }

        try {
            String[] subArgs = args.length == 0 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
            return subcommand.execute(sender, subArgs);
        } catch (Exception e) {
            DebugLogger.getInstance().debugException("COMMAND", "ClassMainCommand", e);
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Błąd podczas wykonywania komendy", e);
            sender.sendMessage(plugin.getMessage("errors.internal-error"));
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String name : subcommands.keySet()) {
                String primary = aliasToPrimary.getOrDefault(name, name);
                if (name.startsWith(input) && sender.hasPermission(plugin.getConfigManager().getCommandPermission(primary))) {
                    completions.add(name);
                }
            }
            completions.sort(String.CASE_INSENSITIVE_ORDER);
        } else if (args.length > 1) {
            Subcommand subcommand = subcommands.get(args[0].toLowerCase());
            if (subcommand != null) {
                List<String> sub = subcommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
                if (sub != null) completions.addAll(sub);
            }
        }
        return completions;
    }
}

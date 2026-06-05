package cronos.classsystem.commands.base;

import cronos.classsystem.ClassSystemPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Bazowa subkomenda — wpina permission/description/usage do tłumaczeń, tak by
 * konkretna subkomenda implementowała tylko {@link #execute}.
 */
public abstract class AbstractSubcommand implements Subcommand {

    protected final ClassSystemPlugin plugin;
    protected final String subcommandName;
    protected final String usageSuffix;

    public AbstractSubcommand(ClassSystemPlugin plugin, String subcommandName) {
        this(plugin, subcommandName, null);
    }

    public AbstractSubcommand(ClassSystemPlugin plugin, String subcommandName, String usageSuffix) {
        this.plugin = plugin;
        this.subcommandName = subcommandName;
        this.usageSuffix = usageSuffix != null ? " " + usageSuffix : "";
    }

    @Override
    public String getPermission() {
        return plugin.getConfigManager().getCommandPermission(subcommandName);
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getSubcommandDescription(subcommandName);
    }

    @Override
    public String getUsage() {
        String mainCommand = plugin.getConfigManager().getMainCommandName();
        List<String> aliases = plugin.getConfigManager().getSubcommandAliases(subcommandName);
        String alias = aliases.isEmpty() ? subcommandName : aliases.get(0);
        return "/" + mainCommand + " " + alias + usageSuffix;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}

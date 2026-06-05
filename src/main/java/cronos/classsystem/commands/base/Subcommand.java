package cronos.classsystem.commands.base;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Kontrakt subkomendy dispatchowanej przez {@link AbstractCommand}.
 */
public interface Subcommand {

    boolean execute(CommandSender sender, String[] args);

    String getPermission();

    String getDescription();

    String getUsage();

    default List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}

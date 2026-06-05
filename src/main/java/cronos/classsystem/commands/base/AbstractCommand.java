package cronos.classsystem.commands.base;

import cronos.classsystem.ClassSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa bazowa komend top-level. Samorejestracja przez refleksję na Bukkit
 * {@code CommandMap} — komendy NIE są deklarowane w paper-plugin.yml.
 */
public abstract class AbstractCommand implements org.bukkit.command.TabExecutor {

    protected final String command;
    protected final String description;
    protected final List<String> alias;
    protected final String usage;
    protected final String permMessage;
    protected static CommandMap cmap;

    public AbstractCommand(String command) {
        this(command, null, null, null, null);
    }

    public AbstractCommand(String command, String usage, String description, String permissionMessage, List<String> aliases) {
        this.command = command.toLowerCase();
        this.usage = usage;
        this.description = description;
        this.permMessage = permissionMessage;
        this.alias = aliases;
    }

    @SuppressWarnings("deprecation")
    public void register() {
        ReflectCommand cmd = new ReflectCommand(this.command);
        if (this.alias != null) {
            cmd.setAliases(this.alias);
        }
        if (this.description != null) {
            cmd.setDescription(this.description);
        }
        if (this.usage != null) {
            cmd.setUsage(this.usage);
        }
        if (this.permMessage != null) {
            cmd.setPermissionMessage(this.permMessage);
        }
        cmd.setPermission("ClassSystem." + this.command);
        this.getCommandMap().register(this.command, cmd);
        cmd.setExecutor(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    final CommandMap getCommandMap() {
        if (cmap == null) {
            try {
                Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap) f.get(Bukkit.getServer());
                return this.getCommandMap();
            } catch (Exception e) {
                // Loguj przez plugin.getLogger() żeby trafiło do logs/errors-YYYY-MM-DD.log.
                ClassSystemPlugin plugin = ClassSystemPlugin.getInstance();
                if (plugin != null) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Nie udało się pobrać CommandMap przez refleksję", e);
                } else {
                    Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "Nie udało się pobrać CommandMap przez refleksję (plugin instance == null)", e);
                }
                return null;
            }
        } else {
            return cmap;
        }
    }

    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    /**
     * Tab completion z graczami online widocznymi dla nadawcy.
     */
    public static List<String> onlinePlayers(CommandSender sender, String[] args) {
        String lastWord = args[args.length - 1];
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        ArrayList<String> matchedPlayers = new ArrayList<>();

        for (Player player : sender.getServer().getOnlinePlayers()) {
            String name = player.getName();
            if (senderPlayer != null && !senderPlayer.canSee(player) || !StringUtil.startsWithIgnoreCase(name, lastWord))
                continue;
            matchedPlayers.add(name);
        }
        matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }

    private static final class ReflectCommand extends Command {
        private AbstractCommand exe;

        private ReflectCommand(String command) {
            super(command);
            this.exe = null;
        }

        public void setExecutor(AbstractCommand exe) {
            this.exe = exe;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (this.exe != null) {
                return this.exe.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            if (this.exe != null) {
                return this.exe.onTabComplete(sender, this, alias, args);
            }
            return null;
        }
    }
}

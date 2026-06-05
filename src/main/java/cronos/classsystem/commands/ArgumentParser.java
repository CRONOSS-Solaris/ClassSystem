package cronos.classsystem.commands;

import cronos.classsystem.ClassSystemPlugin;
import org.bukkit.entity.Player;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Parsing argumentów subkomend z automatyczną wysyłką komunikatu błędu — eliminuje
 * powtarzalne try/catch wokół {@code Integer.parseInt} / {@code Double.parseDouble}.
 *
 * Każda metoda zwraca {@code Optional*}; empty oznacza że argument był niepoprawny
 * a graczowi wysłano już komunikat:
 * <pre>{@code
 * OptionalInt n = ArgumentParser.parseInt(plugin, player, args[0], 1, 100, "errors.invalid-arguments");
 * if (n.isEmpty()) return true;
 * }</pre>
 */
public final class ArgumentParser {

    private ArgumentParser() {}

    public static OptionalInt parseInt(ClassSystemPlugin plugin, Player player, String input,
                                       int min, int max, String errorKey) {
        try {
            int val = Integer.parseInt(input);
            if (val < min || val > max) {
                plugin.sendMessage(player, errorKey);
                return OptionalInt.empty();
            }
            return OptionalInt.of(val);
        } catch (NumberFormatException e) {
            plugin.sendMessage(player, errorKey);
            return OptionalInt.empty();
        }
    }

    public static OptionalDouble parsePositiveDouble(ClassSystemPlugin plugin, Player player,
                                                     String input, double max, String errorKey) {
        try {
            double val = Double.parseDouble(input);
            if (!Double.isFinite(val) || val <= 0 || val > max) {
                plugin.sendMessage(player, errorKey);
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(val);
        } catch (NumberFormatException e) {
            plugin.sendMessage(player, errorKey);
            return OptionalDouble.empty();
        }
    }

    public static boolean requireMinArgs(ClassSystemPlugin plugin, Player player, String[] args,
                                         int minimum, String usageKey) {
        if (args.length < minimum) {
            plugin.sendMessage(player, "errors.invalid-arguments");
            if (usageKey != null) {
                plugin.sendMessage(player, usageKey);
            }
            return false;
        }
        return true;
    }
}

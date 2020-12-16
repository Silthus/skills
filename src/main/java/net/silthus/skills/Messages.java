package net.silthus.skills;

import co.aikar.commands.CommandIssuer;
import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.silthus.skills.entities.Level;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public final class Messages {

    public static void send(UUID playerId, Component message) {
        if (SkillsPlugin.isTesting()) return;
        BukkitAudiences.create(SkillsPlugin.instance())
                .player(playerId)
                .sendMessage(message);
    }

    public static void send(UUID playerId, Consumer<TextComponent.Builder> message) {

        TextComponent.Builder builder = text();
        message.accept(builder);
        send(playerId, builder.build());
    }

    public static void send(Object commandIssuer, Component message) {

        if (commandIssuer instanceof Player) {
            sendPlayer((Player) commandIssuer, message);
        } else if (commandIssuer instanceof ConsoleCommandSender) {
            sendConsole((ConsoleCommandSender) commandIssuer, message);
        } else if (commandIssuer instanceof RemoteConsoleCommandSender) {
            sendRemote((RemoteConsoleCommandSender) commandIssuer, message);
        } else if (commandIssuer instanceof CommandIssuer) {
            send((Object) ((CommandIssuer) commandIssuer).getIssuer(), message);
        }
    }

    public static void sendPlayer(Player player, Component message) {
        send(player.getUniqueId(), message);
    }

    public static void sendConsole(ConsoleCommandSender sender, Component message) {

        sender.sendMessage(PlainComponentSerializer.plain().serialize(message));
    }

    public static void sendRemote(RemoteConsoleCommandSender sender, Component message) {

        sender.sendMessage(PlainComponentSerializer.plain().serialize(message));
    }

    public static Component addSkill(SkilledPlayer player, PlayerSkill skill) {

        return text().append(player(player))
                .append(text(" hat den Skill ", GREEN))
                .append(skill(skill))
                .append(text(" erhalten.", GREEN)).build();
    }

    public static Component removeSkill(PlayerSkill playerSkill) {

        return text("Der Skill ", RED)
                .append(skill(playerSkill))
                .append(text(" wurde von ", RED))
                .append(player(playerSkill.player()))
                .append(text(" entfernt.", RED));
    }

    public static Component addSkillpoints(SkilledPlayer player, int skillpoints) {

        return text("Die Skillpunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden um ", YELLOW))
                .append(text(skillpoints, AQUA))
                .append(text(" Skillpunkt(e) auf ", YELLOW))
                .append(text(player.skillPoints(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setSkillpoints(SkilledPlayer player, int skillpoints) {

        return text("Die Skillpunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden auf ", YELLOW))
                .append(text(skillpoints, AQUA))
                .append(text(" Skillpunkt(e) gesetzt.", YELLOW));
    }

    public static Component addExp(SkilledPlayer player, int exp) {

        return text("Die Erfahrungspunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden um ", YELLOW))
                .append(text(exp + " EXP", AQUA)).append(text(" auf ", YELLOW))
                .append(text(player.level().getTotalExp() + " EXP", AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setExp(SkilledPlayer player, int exp) {

        return text("Die Erfahrungspunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden auf ", YELLOW))
                .append(text(exp + " EXP", AQUA))
                .append(text(" gesetzt.", YELLOW));
    }

    public static Component addLevel(SkilledPlayer player, int level) {

        return text("Das Level von ", YELLOW)
                .append(player(player))
                .append(text(" wurde um ", YELLOW))
                .append(text(level, AQUA)).append(text(" Level auf ", YELLOW))
                .append(text(player.level().getLevel(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setLevel(SkilledPlayer player, int level) {

        return text("Das Level von ", YELLOW)
                .append(player(player))
                .append(text(" wurde auf ", YELLOW))
                .append(text("Level " + level, AQUA))
                .append(text(" gesetzt.", YELLOW));
    }

    public static Component levelUpSelf(SkilledPlayer player, int level) {

        return text().append(text("Du", GOLD, BOLD).hoverEvent(playerInfo(player)))
                .append(text(" bist im Level aufgestiegen: ", GREEN))
                .append(text("Level " + level, AQUA))
                .append(text(" erreicht.", GREEN)).build();
    }

    public static Component levelDownSelf(SkilledPlayer player, int level) {

        return text().append(text("Du", GOLD, BOLD).hoverEvent(playerInfo(player)))
                .append(text(" bist im Level abgestiegen: ", RED))
                .append(text("Level " + level, AQUA)).build();
    }

    public static Component levelUp(SkilledPlayer player) {

        return text().append(player(player))
                .append(text(" ist im Level aufgestiegen: ", GREEN))
                .append(text("Level " + player.level().getLevel(), AQUA))
                .append(text(" erreicht.", GREEN)).build();
    }

    public static Component levelDown(SkilledPlayer player) {

        return text().append(player(player))
                .append(text(" ist im Level abgestiegen: ", RED))
                .append(text("Level " + player.level().getLevel(), AQUA)).build();
    }

    public static Component level(Level level) {

        return text("Level: ", YELLOW).append(text(level.getLevel(), AQUA)).append(newline())
                .append(text("EXP: ", YELLOW)).append(text(level.getTotalExp(), AQUA));
    }

    public static Component skillPoints(SkilledPlayer player) {

        return text("Skillpunkte: ", YELLOW).append(text(player.skillPoints(), AQUA));
    }

    public static Component player(SkilledPlayer player) {

        return text(player.name(), GOLD, BOLD)
                .hoverEvent(showText(playerInfo(player)));
    }

    public static Component playerInfo(SkilledPlayer player) {

        return text().append(text("--- [ ", DARK_AQUA))
                .append(text(player.name(), GOLD))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(level(player.level())).append(newline())
                .append(skillPoints(player)).append(newline()).append(newline())
                .append(text("Freigeschaltete Skills: ", YELLOW)).append(newline())
                .append(skills(player.skills().stream().filter(PlayerSkill::unlocked).collect(Collectors.toList())))
                .build();
    }

    public static Component skills(Collection<PlayerSkill> skills) {

        TextComponent.Builder builder = text();
        for (PlayerSkill skill : skills) {
            builder.append(skill(skill));
        }
        return builder.build();
    }

    public static Component skill(PlayerSkill skill) {


        return text(skill.name(), skill.active() ? GREEN : RED, BOLD)
                .hoverEvent(skillInfo(skill));
    }

    public static Component skillInfo(PlayerSkill skill) {

        return text("--- [ ", DARK_AQUA)
                .append(text(skill.name(), skillColor(skill), BOLD))
                .append(text(" (" + skill.alias() + ")", GRAY, ITALIC))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(text(skill.description(), GRAY, ITALIC)).append(newline()).append(newline())
                .append(text("Vorraussetzungen:", YELLOW)).append(newline())
                .append(requirements(skill));
    }

    public static Component requirements(PlayerSkill skill) {

        TextComponent.Builder text = text();
        for (Requirement requirement : skill.skill().requirements()) {
            text.append(text(" - ", YELLOW))
                    .append(text(requirement.name(), requirementColor(requirement, skill.player()), BOLD))
                    .hoverEvent(showText(requirement(requirement, skill.player())))
                    .append(newline());
        }
        return text.build();
    }

    public static Component requirement(Requirement requirement, SkilledPlayer player) {

        return text("--- [ ", AQUA).append(text(requirement.name(), requirementColor(requirement, player)))
                .append(newline())
                .append(text(requirement.description(), GRAY, ITALIC));
    }

    public static TextColor requirementColor(Requirement requirement, SkilledPlayer player) {

        if (player == null) return YELLOW;
        return requirement.test(player).success() ? GREEN : RED;
    }

    public static TextReplacementConfig replacePlayer(SkilledPlayer player) {

        return TextReplacementConfig.builder()
                .matchLiteral("{player}").replacement(player(player))
                .matchLiteral("{player_name}").replacement(player.name())
                .matchLiteral("{player_id}").replacement(player.id().toString())
                .matchLiteral("{skills_count}").replacement(player.skills().size() + "")
                .build();
    }

    public static TextReplacementConfig replaceLevel(Level level) {

        return TextReplacementConfig.builder()
                .matchLiteral("{level}").replacement(level(level)).build();
    }

    public static TextReplacementConfig replaceSkill(PlayerSkill skill) {

        return TextReplacementConfig.builder()
                .matchLiteral("{skill}").replacement(skillInfo(skill))
                .matchLiteral("{skill_name}").replacement(skill.name())
                .matchLiteral("{skill_alias}").replacement(skill.alias())
                .build();
    }

    public static TextColor skillColor(PlayerSkill skill) {

        switch (skill.status()) {
            case ACTIVE:
                return GREEN;
            case UNLOCKED:
                return DARK_GREEN;
            case REMOVED:
                return DARK_GRAY;
            case INACTIVE:
                return GRAY;
            case DISABLED:
                return DARK_RED;
            default:
                return WHITE;
        }
    }

    @Setter(AccessLevel.PACKAGE)
    private static Messages instance;

    public static String msg(String key) {

        return msg(key, "");
    }

    public static String msg(String key, String defaultValue) {
        if (instance == null) {
            return "";
        }
        return instance.getMessage(key, defaultValue);
    }
    private final File file;

    private final YamlConfiguration config;

    Messages(File file) throws InvalidConfigurationException {

        try {
            this.file = file;
            this.config = new YamlConfiguration();
            this.config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    public String getMessage(String key) {

        return getMessage(key, "");
    }

    public String getMessage(String key, String defaultValue) {

        if (!config.contains(key)) {
            try {
                config.set(key, defaultValue);
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return config.getString(key, defaultValue);
    }
}
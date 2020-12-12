package net.silthus.skills;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.skills.entities.PlayerHistory;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.events.SetPlayerExpEvent;
import net.silthus.skills.events.SetPlayerLevelEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.codehaus.janino.CompilerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Log(topic = "RCSkills")
public final class LevelManager implements Listener {

    @Getter
    private final SkillsPlugin plugin;
    private final Map<UUID, Map<Integer, Integer>> cache = new HashMap<>();
    private Map<Integer, Integer> levelToExpMap = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    @Accessors(fluent = true)
    private IExpressionEvaluator ee;
    private double x;
    private double y;
    private double z;

    public LevelManager(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public SkillPluginConfig.LevelConfig getConfig() {

        return getPlugin().getPluginConfig().getLevelConfig();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExpGain(SetPlayerExpEvent event) {

        int level = getLevelForExp(event.getNewExp());
        event.setLevel(level);

        PlayerHistory.of(event.getPlayer())
                .oldExp(event.getOldExp())
                .newExp(event.getNewExp())
                .newLevel(event.getLevel())
                .reason(event.getReason())
                .save();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLevelUp(SetPlayerLevelEvent event) {

        event.getPlayer().getBukkitPlayer().ifPresent(player -> {

            int minExp = calculateTotalExpForLevel(event.getNewLevel());
            int maxExp = calculateTotalExpForLevel(event.getNewLevel() + 1);
            if (event.getExp() < minExp || event.getExp() >= maxExp) {
                event.setExp(minExp);
            }

            if (event.getNewLevel() > event.getOldLevel()) {
                Messages.send(player, Messages.levelUpSelf(event.getLevel(), event.getNewLevel()));
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .forEach(p -> Messages.send(p, Messages.levelUp(event.getLevel())));
            } else if (event.getNewLevel() < event.getOldLevel()) {
                Messages.send(player, Messages.levelDownSelf(event.getLevel(), event.getNewLevel()));
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .forEach(p -> Messages.send(p, Messages.levelDown(event.getLevel())));
            }
        });
    }

    public void load() throws CompileException {

        cache.clear();

        this.x = getConfig().getX();
        this.y = getConfig().getY();
        this.z = getConfig().getZ();

        ee = new CompilerFactory().newExpressionEvaluator();
        ee.setExpressionType(double.class);
        ee.setParameters(new String[] {
                "x",
                "y",
                "z",
                "level"
        }, new Class[] {
                double.class,
                double.class,
                double.class,
                int.class
        });

        ee.cook(getConfig().getExpToNextLevel());
        this.levelToExpMap = calculateTotalExpMap(getConfig().getMaxLevel());
    }

    private Map<Integer, Integer> calculateTotalExpMap(int maxLevel) {

        if (ee == null) return new HashMap<>();

        HashMap<Integer, Integer> expMap = new HashMap<>();

        for (int i = 1; i < maxLevel + 1; i++) {
            expMap.put(i, calculateTotalExpForLevel(i));
        }
        return expMap;
    }

    public Optional<Integer> getCache(SkilledPlayer player) {

        if (!cache.containsKey(player.id())) {
            return Optional.empty();
        }

        Map<Integer, Integer> playerCache = cache.getOrDefault(player.id(), new HashMap<>());
        if (playerCache.containsKey(player.level().level())) {
            return Optional.of(playerCache.get(player.level().level()));
        }

        return Optional.empty();
    }

    public Map<Integer, Integer> clearCache(UUID player) {

        if (!cache.containsKey(player)) {
            return new HashMap<>();
        }
        return cache.remove(player);
    }

    private Integer cache(UUID playerId, int level, int result) {

        if (!cache.containsKey(playerId)) {
            cache.put(playerId, new HashMap<>());
        }
        Map<Integer, Integer> playerCache = cache.getOrDefault(playerId, new HashMap<>());
        playerCache.put(level, result);
        cache.put(playerId, playerCache);
        return result;
    }

    public int calculateExpToNextLevel(SkilledPlayer player, boolean clearCache) {

        if (clearCache) clearCache(player.id());

        return getCache(player).orElseGet(() ->
                cache(player.id(), player.level().level(),
                        calculateExpForNextLevel(player.level().level())));
    }

    public int calculateExpToNextLevel(SkilledPlayer player) {

        return calculateExpToNextLevel(player, false);
    }

    public int calculateExpForNextLevel(int level) {

        try {
            return (int) Math.round((double) ee.evaluate(
                    x,
                    y,
                    z,
                    level));
        } catch (InvocationTargetException e) {
            log.severe("failed to calculate exp for level " + level + ": " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public int getTotalExpForLevel(int level) {

        return levelToExpMap.getOrDefault(level, -1);
    }

    public int getLevelForExp(long totalExp) {

        int lastLevel = 1;
        for (Map.Entry<Integer, Integer> entry : levelToExpMap.entrySet()) {
            int level = entry.getKey();
            int exp = entry.getValue();

            if (exp <= totalExp && level > lastLevel) {
                lastLevel = level;
            }
        }

        return lastLevel;
    }

    private int calculateTotalExpForLevel(final int level) {

        int sum = 0;
        for (int i = 1; i < level + 1; i++) {
            sum += calculateExpForNextLevel(i);
        }
        return sum;
    }
}

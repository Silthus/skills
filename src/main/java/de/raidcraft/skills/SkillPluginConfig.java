package de.raidcraft.skills;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import de.exlll.configlib.format.FieldNameFormatters;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SkillPluginConfig extends BukkitYamlConfiguration {

    @Comment("The relative path where your skill configs are located.")
    private String skillsPath = "skills";
    @Comment("The relative path where your skill and effect modules (jar files) are located.")
    private String modulePath = "modules";
    @Comment("Set to true to automatically load skill classes and factories from other plugins.")
    private boolean loadClassesFromPlugins = false;
    @Comment("Set to false if you want to disable broadcasting players leveling up to everyone.")
    private boolean broadcastLevelup = true;
    @Comment("The time in ticks until the /rcs buy command confirmation times out.")
    private long buyCommandTimeout = 600L;
    @Comment("The time in ticks how long the progress bar should be displayed.")
    private long expProgressBarDuration = 120L;
    private DatabaseConfig database = new DatabaseConfig();
    @Comment("Define the expression that calculates the required exp for each level here.")
    private LevelConfig levelConfig = new LevelConfig();
    @Comment("Define what a player automatically gets when he levels up.")
    private LevelUpConfig levelUpConfig = new LevelUpConfig();

    public SkillPluginConfig(Path path) {

        super(path, BukkitYamlProperties.builder().setFormatter(FieldNameFormatters.LOWER_UNDERSCORE).build());
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class DatabaseConfig {

        private String username = "sa";
        private String password = "sa";
        private String driver = "h2";
        private String url = "jdbc:h2:~/skills.db";
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class LevelConfig {

        private int maxLevel = 100;
        @Comment({
                "You can use any of the following variables and all java Math.* expressions: ",
                "  - x: settable in this config",
                "  - y: settable in this config",
                "  - z: settable in this config",
                "  - level: the current level of the player"
        })
        private String expToNextLevel = "(-0.4 * Math.pow(level, 2)) + (x * Math.pow(level, 2))";
        private double x = 10.4;
        private double y = 0;
        private double z = 0;
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class LevelUpConfig {

        @Comment("The number of skillpoints a player should get each level")
        private int skillPointsPerLevel = 1;
        @Comment("The number of new skill slots a player should get each level")
        private int slotsPerLevel = 0;
        @ElementType(LevelUp.class)
        private Map<Integer, LevelUp> levels = new HashMap<>();
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class LevelUp {

        private int skillpoints = 0;
        private int slots = 0;
        private List<String> commands = new ArrayList<>();
    }
}
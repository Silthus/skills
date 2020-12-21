package de.raidcraft.skills.entities;

import de.raidcraft.skills.Requirement;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.TestResult;
import de.raidcraft.skills.requirements.LevelRequirement;
import de.raidcraft.skills.requirements.MoneyRequirement;
import de.raidcraft.skills.requirements.PermissionRequirement;
import de.raidcraft.skills.requirements.SkillPointRequirement;
import io.ebean.Finder;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.Index;
import io.ebean.text.json.EJson;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Entity
@Getter
@Setter
@Table(name = "rcs_skills")
@Accessors(fluent = true)
public class ConfiguredSkill extends BaseEntity {

    static {
        try {
            EJson.write(new Object());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static Optional<ConfiguredSkill> findByAliasOrName(String alias) {

        return find.query()
                .where().eq("alias", alias)
                .findOneOrEmpty();
    }

    public static ConfiguredSkill getOrCreate(UUID id) {

        return Optional.ofNullable(find.byId(id)).orElse(new ConfiguredSkill(id));
    }

    public static final Finder<UUID, ConfiguredSkill> find = new Finder<>(ConfiguredSkill.class);

    @Index
    private String alias;
    @Index
    private String name;
    private String type;
    private String description;
    private int level = 1;
    private double money = 0d;
    private int skillpoints = 0;
    private int skillslots = 1;
    private boolean hidden = false;
    private boolean enabled = true;
    private List<String> categories = new ArrayList<>();

    @DbJson
    private Map<String, Object> config = new HashMap<>();
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<PlayerSkill> playerSkills = new ArrayList<>();

    @Transient
    private List<Requirement> requirements = new ArrayList<>();
    @Transient
    private List<Requirement> costRequirements = new ArrayList<>();

    private transient boolean loaded = false;

    ConfiguredSkill(UUID id) {
        this.id(id);
    }

    public List<String> categories() {

        if (categories == null) {
            this.categories = new ArrayList<>();
            load();
        }
        return categories;
    }

    public List<Requirement> requirements() {

        if (requirements == null) {
            this.requirements = new ArrayList<>();
            load();
        }
        return requirements;
    }

    public List<Requirement> costRequirements() {

        if (costRequirements == null) {
            this.costRequirements = new ArrayList<>();
            load();
        }
        return costRequirements;
    }

    public ConfiguredSkill load(ConfigurationSection config) {
        this.config = new HashMap<>();
        config.getKeys(true).forEach(key -> this.config.put(key, config.get(key)));

        load();
        save();

        return this;
    }

    public ConfigurationSection getConfig() {

        if (this.config == null) return new MemoryConfiguration();

        MemoryConfiguration config = new MemoryConfiguration();
        for (Map.Entry<String, Object> entry : this.config.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        return config;
    }

    public ConfigurationSection getSkillConfig() {

        ConfigurationSection config = getConfig();
        return Objects.requireNonNullElse(config.getConfigurationSection("with"), config.createSection("with"));
    }

    @PostLoad
    public void load() {

        if (loaded) return;

        ConfigurationSection config = getConfig();

        this.alias = config.getString("alias", alias);
        this.name = config.getString("name", alias());
        this.type = config.getString("type", "permission");
        this.description = config.getString("description", description);
        this.level = config.getInt("level", level);
        this.money = config.getDouble("money", money);
        this.skillpoints = config.getInt("skillpoints", skillpoints);
        this.skillslots = config.getInt("skillslots", skillslots);
        this.hidden = config.getBoolean("hidden", hidden);
        this.enabled = config.getBoolean("enabled", enabled);
        if (config.isSet("categories"))
            this.categories = config.getStringList("categories");

        this.requirements = SkillsPlugin.instance().getSkillManager()
                .loadRequirements(config.getConfigurationSection("requirements"));
        this.costRequirements = new ArrayList<>();

        requirements.add(new PermissionRequirement().add(SkillsPlugin.SKILL_PERMISSION_PREFIX + alias));

        if (level > 0) {
            LevelRequirement levelRequirement = new LevelRequirement();
            levelRequirement.load(new MemoryConfiguration());
            levelRequirement.setLevel(level);
            requirements.add(levelRequirement);
        }

        if (money > 0) {
            MoneyRequirement moneyRequirement = new MoneyRequirement();
            moneyRequirement.load(new MemoryConfiguration());
            moneyRequirement.setAmount(money);
            costRequirements.add(moneyRequirement);
        }

        if (skillpoints > 0) {
            SkillPointRequirement skillPointRequirement = new SkillPointRequirement();
            skillPointRequirement.load(new MemoryConfiguration());
            skillPointRequirement.setSkillpoints(this.skillpoints);
            costRequirements.add(skillPointRequirement);
        }

        this.loaded = true;
    }

    public ConfiguredSkill enabled(boolean enabled) {

        if (enabled == this.enabled) return this;
        this.enabled = enabled;
        if (enabled()) {
            playerSkills().forEach(PlayerSkill::enable);
        } else {
            playerSkills().forEach(PlayerSkill::disable);
        }
        return this;
    }

    public void addRequirement(Requirement... requirements) {
        this.requirements().addAll(Arrays.asList(requirements));
    }

    public TestResult test(SkilledPlayer player) {

        return Stream.concat(requirements().stream(), costRequirements().stream())
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    public TestResult testRequirements(SkilledPlayer player) {

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    public TestResult testCosts(SkilledPlayer player) {

        return costRequirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }
}
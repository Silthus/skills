package de.raidcraft.skills.entities;

import com.google.common.base.Strings;
import de.raidcraft.skills.RCSkills;
import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.actions.BuySkillAction;
import de.raidcraft.skills.events.*;
import de.raidcraft.skills.settings.Setting;
import io.ebean.Finder;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Accessors(fluent = true)
@Getter
@Setter
@Table(name = "rcs_players")
public class SkilledPlayer extends BaseEntity {

    public static final Finder<UUID, SkilledPlayer> find = new Finder<>(SkilledPlayer.class);

    /**
     * Gets an existing player from the database or creates a new record from the given player.
     * <p>This method takes an {@link OfflinePlayer} for easier access to skills while players are offline.
     * However the skill can only be applied to the player if he is online. Any interaction will fail silently while offline.
     *
     * @param player the player that should be retrieved or created
     * @return a skilled player from the database
     */
    public static SkilledPlayer getOrCreate(OfflinePlayer player) {

        return Optional.ofNullable(find.byId(player.getUniqueId()))
                .orElseGet(() -> {
                    SkilledPlayer skilledPlayer = new SkilledPlayer(player);
                    skilledPlayer.insert();
                    return skilledPlayer;
                });
    }

    /**
     * Tries to find a skilled player with the given name.
     * <p>The name can be case insensitive.
     *
     * @param name the name of the player. can be case insensitive.
     * @return the player if found
     */
    public static Optional<SkilledPlayer> byName(String name) {

        if (Strings.isNullOrEmpty(name)) return Optional.empty();

        return find.query().where()
                .ieq("name", name)
                .findOneOrEmpty();
    }

    private String name;
    private int skillPoints = 0;
    @DbDefault("0")
    private int resetCount = 0;
    @DbDefault("0")
    private int freeResets = 0;

    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    private Level level = new Level();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerSkill> skills = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SkillSlot> skillSlots = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private DataStore settings = new DataStore();

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemBinding> itemBindings = new ArrayList<>();

    SkilledPlayer(OfflinePlayer player) {

        id(player.getUniqueId());
        name(player.getName());
    }

    /**
     * Returns the number of free skill slots the player has.
     * <p>Free slots are based off the number of active skills and total skill slots.
     *
     * @return the number of free skill slots.
     *         Can be negative if the player has too many active skills.
     */
    public int freeSkillSlots() {

        return (int) skillSlots().stream().filter(SkillSlot::free).count();
    }

    public int slotCount() {

        return (int) skillSlots().stream()
                .filter(skillSlot -> skillSlot.free() || skillSlot.status() == SkillSlot.Status.IN_USE)
                .count();
    }

    public int activeSlotCount() {

        return (int) skillSlots().stream()
                .filter(SkillSlot::inUse)
                .count();
    }

    public int skillCount() {

        return unlockedSkills().size();
    }

    public OfflinePlayer offlinePlayer() {

        return Bukkit.getOfflinePlayer(id());
    }

    public Optional<Player> bukkitPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(id()));
    }

    public List<PlayerSkill> activeSkills() {

        return skills.stream()
                .filter(PlayerSkill::active)
                .collect(Collectors.toUnmodifiableList());
    }

    public DataStore settings() {

        if (settings == null) {
            this.settings = new DataStore();
        }

        return settings;
    }

    /**
     * Sets the value of the given setting for this player.
     *
     * @param setting the setting to set
     * @param value the value of the setting
     * @param <TType> the value type of the setting
     * @return this player
     */
    public <TType> SkilledPlayer setting(Setting<TType> setting, TType value) {

        settings().set(setting.getKey(), value);
        return this;
    }

    /**
     * Gets the given setting if it exists.
     *
     * @param setting the setting to get
     * @param <TType> the type of the setting
     * @return the value of the setting or an empty optional
     */
    public <TType> Optional<TType> setting(Setting<TType> setting) {

        return settings().get(setting.getKey(), setting.getType());
    }

    /**
     * @return the item to skill bindings of this player
     */
    public ItemBindings bindings() {

        return new ItemBindings(this);
    }

    /**
     * Adds the given skill to this player performing checks but without subtracting costs.
     * <p>Use the {@link #buySkill(ConfiguredSkill)} method to subtract the costs.
     * <p>Use the {@link #addSkill(ConfiguredSkill, boolean)} with the true flag to bypass all checks when adding the skill.
     *
     * @param skill the skill to add
     * @return the result for adding the skill
     * @see AddSkillAction
     */
    public AddSkillAction.Result addSkill(ConfiguredSkill skill) {

        return addSkill(skill, false);
    }

    /**
     * Adds the given skill to the player without subtracting potential costs.
     * <p>Use the {@link #buySkill(ConfiguredSkill)} method to subtract the costs.
     *
     * @param skill the skill to buy
     * @param bypassChecks set to true to bypass all checks and force add the skill
     * @return the result of the add operation
     * @see AddSkillAction
     */
    public AddSkillAction.Result addSkill(ConfiguredSkill skill, boolean bypassChecks) {

        return new AddSkillAction(this, skill).execute(bypassChecks);
    }

    public AddSkillAction.Result buySkill(ConfiguredSkill skill) {

        return buySkill(skill, false);
    }

    public AddSkillAction.Result buySkill(ConfiguredSkill skill, boolean bypassChecks) {

        return new BuySkillAction(this, skill).execute(bypassChecks);
    }

    public Optional<PlayerSkill> getSkill(String alias) {

        return ConfiguredSkill.findByAliasOrName(alias)
                .map(this::getSkill);
    }

    public PlayerSkill getSkill(ConfiguredSkill skill) {

        return skills().stream()
                .filter(s -> s.configuredSkill().equals(skill))
                .findAny()
                .orElseGet(() -> PlayerSkill.getOrCreate(this, skill));
    }

    public PlayerSkill removeSkill(ConfiguredSkill skill) {

        PlayerSkill playerSkill = getSkill(skill);
        playerSkill.delete();
        return playerSkill;
    }

    public boolean hasActiveSkill(String alias) {

        return getSkill(alias).map(PlayerSkill::active).orElse(false);
    }

    public boolean hasActiveSkill(ConfiguredSkill skill) {

        return getSkill(skill).active();
    }

    public boolean hasSkill(ConfiguredSkill skill) {

        return getSkill(skill).unlocked();
    }

    public boolean hasSkill(String alias) {

        return getSkill(alias).map(PlayerSkill::unlocked).orElse(false);
    }

    public List<PlayerSkill> unlockedSkills() {

        return skills().stream()
                .filter(PlayerSkill::unlocked)
                .collect(Collectors.toList());
    }

    @Transactional
    public SkilledPlayer setLevel(int level) {

        if (this.level().getLevel() == level) return this;

        Level playerLevel = level();
        SetPlayerLevelEvent event = new SetPlayerLevelEvent(this, playerLevel.getLevel(), level, playerLevel.getTotalExp());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        playerLevel.setLevel(event.getNewLevel());

        if (event.getExp() != playerLevel.getTotalExp()) {
            playerLevel.setTotalExp(event.getExp());
        }

        Bukkit.getPluginManager().callEvent(new PlayerLeveledEvent(this, event.getOldLevel(), event.getNewLevel(), event.getExp()));

        save();

        return this;
    }

    public SkilledPlayer addLevel(int level) {

        return setLevel(this.level().getLevel() + level);
    }

    public SkilledPlayer setExp(long exp) {

        return setExp(exp, null);
    }

    @Transactional
    public SkilledPlayer setExp(long exp, String reason) {

        if (this.level().getTotalExp() == exp) return this;

        SetPlayerExpEvent event = new SetPlayerExpEvent(this, level().getTotalExp(), exp, level().getLevel(), reason);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        level().setTotalExp(event.getNewExp());
        if (event.getLevel() != level().getLevel()) {
            setLevel(event.getLevel());
        }

        save();

        return this;
    }

    public SkilledPlayer addExp(long exp, String reason) {

        return this.setExp(level().getTotalExp() + exp, reason);
    }

    @Transactional
    public SkilledPlayer setSkillPoints(int skillPoints) {

        if (this.skillPoints() == skillPoints) return this;

        SetPlayerSkillPointsEvent event = new SetPlayerSkillPointsEvent(this, this.skillPoints(), skillPoints);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        if (event.getNewSkillPoints() < 0) event.setNewSkillPoints(0);

        this.skillPoints(event.getNewSkillPoints());

        save();

        return this;
    }

    public SkilledPlayer addSkillPoints(int skillPoints) {

        return this.setSkillPoints(this.skillPoints() + skillPoints);
    }

    public SkilledPlayer removeSkillPoints(int skillPoints) {

        int points = this.skillPoints() - skillPoints;
        return this.setSkillPoints(points);
    }

    @Transactional
    public List<PlayerSkill> resetSkillSlots() {

        List<SkillSlot> skillSlots = skillSlots().stream()
                .filter(skillSlot -> skillSlot.status() == SkillSlot.Status.IN_USE)
                .collect(Collectors.toList());

        List<PlayerSkill> skills = Stream.concat(skillSlots.stream()
                .map(SkillSlot::skill)
                .flatMap(Optional::stream), activeSkills().stream()
                .filter(skill -> skill.configuredSkill().skillSlot()))
                .collect(Collectors.toList());

        skills.forEach(PlayerSkill::deactivate);

        save();

        return skills;
    }

    @Transactional
    public SkilledPlayer setSkillSlots(int slots, SkillSlot.Status status) {

        int currentSlotSize = this.skillSlots().size();
        if (currentSlotSize == slots) return this;

        SetPlayerSkillSlotsEvent event = new SetPlayerSkillSlotsEvent(this, currentSlotSize, slots);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        if (event.getNewSkillSlots() < 0) event.setNewSkillSlots(0);
        if (currentSlotSize == event.getNewSkillSlots()) return this;

        if (event.getNewSkillSlots() < currentSlotSize) {
            for (int i = 0; i < currentSlotSize - event.getNewSkillSlots(); i++) {
                removeSkillSlot();
            }
        } else {
            addSkillSlots(event.getNewSkillSlots() - currentSlotSize, status);
        }

        Bukkit.getPluginManager().callEvent(new PlayerSkillSlotsChangedEvent(this, event.getOldSkillSlots(), event.getNewSkillSlots()));

        save();

        return this;
    }

    @Transactional
    public SkilledPlayer addSkillSlots(int slots, SkillSlot.Status status) {

        for (int i = 0; i < slots; i++) {
            SkillSlot skillSlot = new SkillSlot(this).status(status);
            skillSlot.save();
            skillSlots().add(skillSlot);
        }
        return this;
    }

    private void removeSkillSlot() {

        List<SkillSlot> freeSlots = skillSlots().stream().filter(SkillSlot::free).collect(Collectors.toList());
        if (freeSlots.size() > 0) {
            freeSlots.get(0).delete();
        } else {
            skillSlots().stream().findAny().ifPresent(SkillSlot::delete);
        }
        save();
    }

    /**
     * Checks if the player does not own the skill and if he meets all requirements to buy it.
     *
     * @param skill the skill to check
     * @return true if the skill can be bought
     */
    public boolean canBuy(ConfiguredSkill skill) {

        return !hasSkill(skill) && skill.test(this).success();
    }

    /**
     * Checks if the player has unlocked the skill and can activate it.
     *
     * @param skill the skill that should checked for activation
     * @return true if the skill can be activated
     *         false if the player has not unlocked the skill or cannot activate it
     */
    public boolean canActivate(ConfiguredSkill skill) {

        return hasSkill(skill) && PlayerSkill.getOrCreate(this, skill).canActivate();
    }

    public boolean hasFreeSkillSlot() {

        return skillSlots().stream().anyMatch(SkillSlot::free);
    }

    public SkillSlot freeSkillSlot() {

        return skillSlots().stream()
                .filter(SkillSlot::free)
                .findFirst().orElse(new SkillSlot(this).status(SkillSlot.Status.FREE));
    }

    @Override
    public boolean delete() {

        RCSkills.instance().getSkillManager().clearPlayerCache(id());

        resetSkillSlots();
        refresh();

        skills().clear();
        skillSlots().clear();

        save();
        refresh();

        return super.delete();
    }
}

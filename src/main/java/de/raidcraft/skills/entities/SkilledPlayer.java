package de.raidcraft.skills.entities;

import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.actions.BuySkillAction;
import de.raidcraft.skills.events.*;
import io.ebean.Finder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private String name;
    @Setter(AccessLevel.NONE)
    private int skillPoints = 0;
    @Setter(AccessLevel.NONE)
    private int skillSlots = 0;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Level level = new Level();

    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<PlayerSkill> skills = new HashSet<>();

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

        return skillSlots - activeSkills().stream()
                .map(PlayerSkill::configuredSkill)
                .mapToInt(ConfiguredSkill::skillslots)
                .sum();
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
     */
    public AddSkillAction.Result addSkill(ConfiguredSkill skill, boolean bypassChecks) {

        return new AddSkillAction(this, skill).execute(bypassChecks);
    }

    public BuySkillAction.Result buySkill(ConfiguredSkill skill) {

        return buySkill(skill, false);
    }

    public BuySkillAction.Result buySkill(ConfiguredSkill skill, boolean bypassChecks) {

        return new BuySkillAction(this, skill).execute(bypassChecks);
    }

    public Optional<PlayerSkill> getSkill(String alias) {

        return ConfiguredSkill.findByAliasOrName(alias)
                .map(this::getSkill);
    }

    public PlayerSkill getSkill(ConfiguredSkill skill) {

        return PlayerSkill.getOrCreate(this, skill);
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

    public Collection<PlayerSkill> unlockedSkills() {

        return skills().stream()
                .filter(PlayerSkill::unlocked)
                .collect(Collectors.toList());
    }

    public SkilledPlayer setLevel(int level) {

        if (this.level.getLevel() == level) return this;

        Level playerLevel = level();
        SetPlayerLevelEvent event = new SetPlayerLevelEvent(this, playerLevel.getLevel(), level, playerLevel.getTotalExp());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        playerLevel.setLevel(event.getNewLevel());

        if (event.getExp() != playerLevel.getTotalExp()) {
            playerLevel.setTotalExp(event.getExp());
        }

        Bukkit.getPluginManager().callEvent(new PlayerLeveledEvent(this, event.getOldLevel(), event.getNewLevel(), event.getExp()));

        return this;
    }

    public SkilledPlayer addLevel(int level) {

        return setLevel(this.level.getLevel() + level);
    }

    public SkilledPlayer setExp(long exp) {

        return setExp(exp, null);
    }

    public SkilledPlayer setExp(long exp, String reason) {

        if (this.level.getTotalExp() == exp) return this;

        SetPlayerExpEvent event = new SetPlayerExpEvent(this, level.getTotalExp(), exp, level.getLevel(), reason);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        level.setTotalExp(event.getNewExp());
        if (event.getLevel() != level.getLevel()) {
            setLevel(event.getLevel());
        }
        return this;
    }

    public SkilledPlayer addExp(long exp, String reason) {

        return this.setExp(level().getTotalExp() + exp, reason);
    }

    public SkilledPlayer setSkillPoints(int skillPoints) {

        if (this.skillPoints == skillPoints) return this;

        SetPlayerSkillPointsEvent event = new SetPlayerSkillPointsEvent(this, this.skillPoints, skillPoints);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        if (event.getNewSkillPoints() < 0) event.setNewSkillPoints(0);

        this.skillPoints = event.getNewSkillPoints();
        return this;
    }

    public SkilledPlayer addSkillPoints(int skillPoints) {

        return this.setSkillPoints(this.skillPoints + skillPoints);
    }

    public SkilledPlayer removeSkillPoints(int skillPoints) {

        int points = this.skillPoints - skillPoints;
        return this.setSkillPoints(points);
    }

    public SkilledPlayer setSkillSlots(int slots) {

        if (this.skillSlots == slots) return this;

        SetPlayerSkillSlotsEvent event = new SetPlayerSkillSlotsEvent(this, this.skillSlots, slots);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        if (event.getNewSkillSlots() < 0) event.setNewSkillSlots(0);

        this.skillSlots = event.getNewSkillSlots();

        Bukkit.getPluginManager().callEvent(new PlayerSkillSlotsChangedEvent(this, event.getOldSkillSlots(), event.getNewSkillSlots()));

        return this;
    }

    public SkilledPlayer addSkillSlots(int slots) {

        return this.setSkillSlots(this.skillSlots + slots);
    }

    public boolean canBuy(ConfiguredSkill skill) {

        return !hasSkill(skill) && skill.test(this).success();
    }
}
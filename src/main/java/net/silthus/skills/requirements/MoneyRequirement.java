package net.silthus.skills.requirements;

import de.raidcraft.economy.Economy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementInfo;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

import static net.silthus.skills.Messages.msg;

@Data
@RequirementInfo("money")
@EqualsAndHashCode(callSuper = true)
public class MoneyRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private double amount = 0d;

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Du benötigst mindestens %s um diesen Skill kaufen zu können."), Economy.get().format(amount));
    }

    @Override
    protected void loadConfig(ConfigurationSection config) {

        this.amount = config.getDouble("amount", 0d);
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer target) {

        Economy economy = Economy.get();
        return TestResult.of(economy.has(target.getOfflinePlayer(), amount),
                "Du benötigst mindestens " + economy.format(amount) + " um den Skill zu kaufen.");
    }
}

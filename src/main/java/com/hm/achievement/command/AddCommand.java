package com.hm.achievement.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.StatisticIncreaseHandler;

/**
 * Class in charge of increase a statistic of an achievement by command.
 * 
 * @author Phoetrix
 */
public class AddCommand extends AbstractParsableCommand {

	private String langErrorValue;
	private String langStatisticIncreased;
	private String langCategoryDoesNotExist;

	private final StatisticIncreaseHandler statistic;

	public AddCommand(AdvancedAchievements plugin) {
		super(plugin);
		statistic = new StatisticIncreaseHandler(plugin);

	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();
		langErrorValue = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("error-value", "The value VALUE must to be an integer!");
		langStatisticIncreased = plugin.getChatHeader() + plugin.getPluginLang().getString("statistic-increased",
				"Statistic ACH increased by AMOUNT for PLAYER!");
		langCategoryDoesNotExist = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("category-does-not-exist", "The category CAT  does not exist.");
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		int value = 0;

		if (NumberUtils.isNumber(args[1])) {
			value = Integer.parseInt(args[1]);
		} else {
			sender.sendMessage(StringUtils.replaceOnce(langErrorValue, "VALUE", args[1]));
			return;
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (args[2].equalsIgnoreCase(categoryName) && category != NormalAchievements.CONNECTIONS) {
				long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(),
						value);
				statistic.checkThresholdsAndAchievements(player, category.toString(), amount);
				sender.sendMessage(StringUtils.replaceEach(langStatisticIncreased,
						new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
				return;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();

			for (String subcategory : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				String categoryPath = categoryName + "." + StringUtils.replace(subcategory, " ", "");

				if (args[2].equalsIgnoreCase(categoryPath)) {
					long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
							player.getUniqueId(), value);
					statistic.checkThresholdsAndAchievements(player, category + "." + subcategory, amount);
					sender.sendMessage(StringUtils.replaceEach(langStatisticIncreased,
							new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
					return;
				}
			}
		}

		sender.sendMessage(StringUtils.replaceOnce(langCategoryDoesNotExist, "CAT", args[2]));
	}
}

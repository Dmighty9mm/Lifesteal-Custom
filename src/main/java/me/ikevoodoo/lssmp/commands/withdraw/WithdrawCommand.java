package me.ikevoodoo.lssmp.commands.withdraw;

import me.ikevoodoo.lssmp.config.CommandConfig;
import me.ikevoodoo.lssmp.config.MainConfig;
import me.ikevoodoo.lssmp.config.bans.BanConfig;
import me.ikevoodoo.lssmp.utils.Util;
import me.ikevoodoo.smpcore.SMPPlugin;
import me.ikevoodoo.smpcore.commands.CommandUsable;
import me.ikevoodoo.smpcore.commands.Context;
import me.ikevoodoo.smpcore.commands.SMPCommand;
import me.ikevoodoo.smpcore.commands.arguments.Argument;
import me.ikevoodoo.smpcore.commands.arguments.OptionalFor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand extends SMPCommand {
    public WithdrawCommand(SMPPlugin plugin) {
        super(plugin, CommandConfig.WithdrawCommand.name, CommandConfig.WithdrawCommand.perms);
        setUsable(CommandUsable.PLAYER);
        setArgs(new Argument("amount", false, Integer.class, OptionalFor.ALL));
    }

    @Override
    public boolean execute(Context<?> context) {
        Player player = context.source(Player.class);
        var amount = Math.abs(context.args().get("amount", Integer.class, 1));
        if (amount < 1) {
            player.sendMessage(CommandConfig.WithdrawCommand.Messages.withdrawnTooLittle);
            return true;
        }

        var maxHealth = Bukkit.spigot().getConfig().getDouble("settings.attribute.maxHealth.max", 2048);
        var maxHearts = Math.floor(maxHealth / MainConfig.Elimination.getHeartScale());

        // At 10 hearts it's 20 / (1 * 2) for the default config
        var max = getPlugin().getHealthHelper().getMaxHealth(player) / MainConfig.Elimination.getHeartScale();

        if (amount >= maxHearts) {
            player.sendMessage(CommandConfig.WithdrawCommand.Messages.withdrawnTooMuch.replace("%max%", String.valueOf(max)));
            return true;
        }

        if (amount > max) {
            if (CommandConfig.WithdrawCommand.withdrawEliminates) {
                var data = BanConfig.INSTANCE.findHighest(player,
                        MainConfig.Elimination.Bans.banMessage,
                        MainConfig.Elimination.Bans.getBanTime()
                );

                getPlugin().getEliminationHandler().eliminate(player, data);
                player.kickPlayer(data.message());
                return true;
            }

            player.sendMessage(CommandConfig.WithdrawCommand.Messages.withdrawnTooMuch.replace("%max%", String.valueOf(max)));
            return true;
        }

        var result = getPlugin().getHealthHelper().decreaseMaxHealthIfOver(
                player,
                amount * MainConfig.Elimination.getHeartScale(),
                MainConfig.Elimination.getMinHearts()
        );

        if (result.isBelowMin()) {
            var data = BanConfig.INSTANCE.findHighest(player,
                    MainConfig.Elimination.Bans.banMessage,
                    MainConfig.Elimination.Bans.getBanTime()
            );

            getPlugin().getEliminationHandler().eliminate(player, data);
            player.kickPlayer(data.message());
            return true;
        }

        int sum = player.getInventory()
                .addItem(getPlugin().getItem("heart_item").orElseThrow()
                        .getItemStack(amount))
                .values()
                .stream()
                .map(ItemStack::getAmount)
                .reduce(0, Integer::sum);

        if (!MainConfig.Elimination.fullInventoryHeartDrop && sum > 0) {
            var toAdd = sum * MainConfig.Elimination.getHeartScale();
            var newHealth = getPlugin().getHealthHelper().increaseMaxHealth(player, toAdd);
            player.setHealth(Math.min(toAdd, newHealth));
            player.sendMessage(CommandConfig.WithdrawCommand.Messages.notEnoughSpace.replace("%amount%", String.valueOf(sum)));
            return true;
        }

        while (sum > 0) {
            int count = Math.min(sum, 64);

            Util.drop(
                    getPlugin()
                            .getItem("heart_item")
                            .orElseThrow()
                            .getItemStack(count),
                    player.getEyeLocation()
            );

            sum -= count;
        }
        player.sendMessage(CommandConfig.WithdrawCommand.Messages.withdraw.replace("%amount%", String.valueOf(amount)));
        return true;
    }
}
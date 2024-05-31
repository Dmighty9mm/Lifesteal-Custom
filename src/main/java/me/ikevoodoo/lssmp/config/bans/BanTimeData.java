package me.ikevoodoo.lssmp.config.bans;

import me.ikevoodoo.lssmp.config.errors.ConfigError;
import me.ikevoodoo.lssmp.config.errors.Some;
import me.ikevoodoo.smpcore.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

public record BanTimeData(
        String banMessage,

        boolean broadcastBan,
        String broadcastBanMessage,

        String permission,
        long time
) {

    public static Some<BanTimeData> fromConfig(ConfigurationSection section) {
        if (section == null)
            return Some.error(new ConfigError(null, "Section is null"));

        var banMessage = section.getString("ban-message");
        if (banMessage == null)
            return Some.error(new ConfigError(section.getName(), "Ban message is null"));

        var broadcastBan = section.getBoolean("broadcast-ban");
        var broadcastBanMessage = section.getString("broadcast-ban-message");
        if (broadcastBan && broadcastBanMessage == null)
            return Some.error(new ConfigError(section.getName(), "Broadcast ban message is enabled but message is null!"));

        var permission = section.getString("permission");
        if (permission == null)
            return Some.error(new ConfigError(section.getName(), "Permission is null"));

        var isPermanent = section.getBoolean("permanent");
        long banTime = Long.MAX_VALUE;
        if (!isPermanent) {
            var time = section.getString("time");
            if (time == null)
                return Some.error(new ConfigError(section.getName(), "Time is null"));


            var parsedTime = parseBanTime(time);
            if (parsedTime.isError())
                return Some.error(parsedTime.error().addKey(section.getName()));

            banTime = parsedTime.key();
        }


        return Some.of(new BanTimeData(
                StringUtils.color(banMessage),
                broadcastBan,
                StringUtils.color(broadcastBanMessage),
                permission,
                banTime
        ));
    }

    private static Some<Long> parseBanTime(String time) {
        var value = StringUtils.parseBanTime(time);
        if (value == -1) {
            return Some.error(new ConfigError(null, "Time is not in the format hh:mm:ss"));
        }

        if (value == -2) {
            return Some.error(new ConfigError("hours", "Could not parse number"));
        }

        if (value == -3) {
            return Some.error(new ConfigError("minutes", "Could not parse number"));
        }

        if (value == -4) {
            return Some.error(new ConfigError("seconds", "Could not parse number"));
        }

        if (value == -5) {
            return Some.error(new ConfigError("millis", "Could not parse number"));
        }

        return Some.of(value);
    }

    private static Some<Long> parseLong(String str) {
        try {
            return Some.of(Long.parseLong(str));
        } catch (NumberFormatException e) {
            return Some.error(new ConfigError(null, "Could not parse number"));
        }
    }

}

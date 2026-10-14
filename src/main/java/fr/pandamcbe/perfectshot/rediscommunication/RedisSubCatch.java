package fr.pandamcbe.perfectshot.rediscommunication;

import com.google.gson.Gson;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.core.localmemory.DirtyManager;
import fr.pandamcbe.mgapi.core.rediscache.RedisAccountsManager;
import fr.pandamcbe.mgapi.data.management.redis.datas_manipulation.RedisMessage;
import fr.pandamcbe.mgapi.data.management.redis.datas_manipulation.RedisPubSubManager;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AccountType;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;

import java.util.UUID;

public class RedisSubCatch extends RedisPubSubManager {

    private final Gson gson = new Gson();

    @Override
    public void subChannels() {
        String[] channels = {AllNames.STATSCOMMAND.getName()};
        subCore(channels);
    }

    @Override
    public void handleRedisMessage(String channelName, String message) {
        AllNames allNames = AllNames.fromName(channelName);
        RedisMessage redisMessage = gson.fromJson(message, RedisMessage.class);
        UUID uuid = redisMessage.getUuid();

        if (allNames != null) {
            if (allNames == AllNames.STATSCOMMAND) {
                String accountName = redisMessage.getString();
                String json = (String) redisMessage.getObject();
                if (Main.getInstance().localCacheManager.containsKey(uuid)) {
                    new DirtyManager().cancelPending(uuid, accountName);
                    Main.getInstance().manipulation.sendAccountToMemory(Main.getInstance().localCacheManager, uuid, gson.fromJson(json, AccountType.fromName(RedisAccountsManager.ifContainsCategory(accountName)).getType()), accountName);
                }
            }

        }
    }
}
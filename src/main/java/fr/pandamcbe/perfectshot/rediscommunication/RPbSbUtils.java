package fr.pandamcbe.perfectshot.rediscommunication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.commons.WornAccount;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.core.localmemory.MemAccountsManipulation;
import fr.pandamcbe.mgapi.core.rediscache.CallAccountsFromRedis;
import fr.pandamcbe.mgapi.data.management.redis.connection.RedisAccess;
import fr.pandamcbe.mgapi.data.management.redis.datas_manipulation.RedisMessage;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;
import fr.pandamcbe.perfectshot.PerfectShot;
import org.bukkit.Bukkit;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RPbSbUtils {

    MemAccountsManipulation memoryProvider = Main.getInstance().manipulation;
    public RPbSbUtils() {
        System.out.println("Subscription Processing...");
        RedissonClient redisson = RedisAccess.INSTANCE.getRedissonClient();
        if (redisson == null) {
            System.out.println("Erreur: RedissonClient est null !");
            return;
        }

        String[] channels = {AllNames.PFTSHOT.getName(),AllNames.SAVESIGNAL.getName()};
            for(String channel : channels) {
                RTopic topic = redisson.getTopic(channel);
                topic.addListener(String.class, (channelName, message) -> handleRedisMessage(channelName.toString(), message));
            }

        System.out.println("Subscription method finished");
    }

    private void handleRedisMessage(String channelName, String message) {
        PerfectShot main = PerfectShot.getInstance();
        Gson gson = Main.getInstance().gson;
        MemAccountsManipulation manipulation = Main.getInstance().manipulation;
            AllNames allNames = AllNames.fromName(channelName);
            switch (allNames){
                case PFTSHOT:
                    RedisMessage redisMessage = gson.fromJson(message, RedisMessage.class);
                    UUID uuid = redisMessage.getUuid();
                    Map<String, JsonObject> accounts = redisMessage.getAccounts();
                    for (String accountNames : accounts.keySet()) {

                        AllNames allNames2 = AllNames.fromName(CallAccountsFromRedis.ifContainsCategory(accountNames));
                        switch (allNames2){
                            case WORNPOJO:
                                WornAccount wornAccount = gson.fromJson(accounts.get(accountNames), WornAccount.class);
                                manipulation.sendAccountToMemory(main.mainMap, uuid, wornAccount, AllNames.WORNPOJO.getName());
                                break;
                            case STATSPOJO:
                                StatsAccount statsAccount = gson.fromJson(accounts.get(accountNames), StatsAccount.class);
                                manipulation.sendAccountToMemory(main.mainMap, uuid, statsAccount, AllNames.statsPojo(AllNames.PFTSHOT.getName()));
                                break;
                            default:
                                break;
                        }

                    }
                    break;
                case SAVESIGNAL:
                    if (message.equalsIgnoreCase("SAVING NOW!")) {
                        Bukkit.getScheduler().runTaskLater(main, () -> {
                            sendAccountsToProxy(main);
                        }, 20L * 10);

                    }
                    break;
                default:
                    break;
            }
    }

    public void sendAccountsToProxy(PerfectShot main){
        Gson gson = Main.getInstance().gson;
        Map<String, String> mainMapJson = new HashMap<>();
        Map<String, JsonObject> subMapJson = new HashMap<>();
        for(UUID uuid : main.mainMap.keySet()){
            Map<String, Object> subMap = main.mainMap.get(uuid);
            for (String accountsName : subMap.keySet()) {
                    JsonObject JsonAccounts = gson.toJsonTree(subMap.get(accountsName)).getAsJsonObject();
                    subMapJson.put(accountsName, JsonAccounts);
            }
            String uuidString = uuid.toString();
            mainMapJson.put(uuidString, gson.toJson(subMapJson));
        }

        Main.getInstance().redisPbSb.publishMessage(AllNames.PROXY.getName(), gson.toJson(new RedisMessage(mainMapJson)));
    }

}
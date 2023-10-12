package dev.tolja.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.tolja.Data.HypixelProfile;
import dev.tolja.Data.ProxyInfo;
import net.querz.nbt.io.NamedTag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static dev.tolja.Nchecker.configManager;

public class HypixelUtils {

    private static final List<String> greatThings = Arrays.asList(
            "Storm's",
            "Warden Helmet",
            "Elegant Tuxedo",
            "Necron",
            "Diamond Necron Head",
            "Goldor's",
            "Shadow Assassin",
            "Hyperion",
            "Axe Of The Shredded",
            "Scylla",
            "Astraea",
            "Giant's Sword",
            "Valkyrie",
            "Bonemerang",
            "Daedalus Axe",
            "Flower Of Truth",
            "Hegemony Artifact",
            "Golden Jerry Artifact",
            "Ender Dragon",
            "Baby Yeti",
            "Reaper Orb",
            "Seal Of The Family",
            "Superior Dragon",
            "Protector Dragon",
            "Old Dragon",
            "Unstable Dragon",
            "Holy Dragon",
            "Wise Dragon",
            "Young Dragon",
            "Strong Dragon",
            "Maxor's");

    private static final List<String> uhcThings = Arrays.asList(
            "Artemis_Bow",
            "Flask_of_Ichor",
            "Exodus",
            "Hide_of_Leviathan",
            "Tablets_of_Destiny",
            "Axe_of_Perun",
            "Excalibur",
            "Anduril",
            "Death's_Scythe",
            "Chest_of_Fate",
            "Cornucopia",
            "Essence_of_Yggdrasil",
            "Voidbox",
            "Deus_Ex_Machina",
            "Dice_of_God",
            "Kings_Rod",
            "Daredevil",
            "Flask_of_Cleansing",
            "Shoes_of_Vidar",
            "Potion_of_Vitality",
            "Miners_Blessing",
            "Ambrosia",
            "Bloodlust",
            "Modular_Bow",
            "Expert_Seal",
            "Hermes_Boots",
            "Barbarian_Chestplate",
            "Fates_Call",
            "The_Mark",
            "Warlock_Pants"
    );


    public static List<String> skyblockProfile(String apiKey, String uuid, int timeout, ProxyInfo proxyInfo) {
        JSONObject profile;
        profile = HttpUtils.doGet("https://api.hypixel.net/player?key=" + apiKey + "&uuid=" + uuid, proxyInfo, false, timeout);
        List<String> list = new ArrayList();
        if (profile != null && profile.getJSONObject("player") != null) {
            list.add(profile.getJSONObject("player").getString("uuid"));
            list.addAll(profile.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").keySet());
        }
        return list;
    }

    public static HypixelProfile getHypixelProfile(String apiKey, String uuid, int timeout, ProxyInfo proxyInfo) {
        JSONObject normalProfile;
        try {
            normalProfile = HttpUtils.doGet("https://api.hypixel.net/player?key=" + apiKey + "&uuid=" + uuid, proxyInfo, false, timeout);
            if (configManager.getSettingsConfig().isHypixelJson()) {
                System.out.println(normalProfile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error in Getting");
            return null;
        }
        HypixelProfile hypixelProfile = new HypixelProfile();
        if (normalProfile != null && normalProfile.getJSONObject("player") != null) {
            try {
                int UHCstar = normalProfile.getJSONObject("player").getJSONObject("stats").getJSONObject("UHC").getInteger("score");
                hypixelProfile.setUhcStar(HypixelUtils.getUHCStar(UHCstar));
            } catch (Exception e) {
                hypixelProfile.setUhcStar(0);
            }

            try {
                String skywarstar = normalProfile.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyWars").getString("levelFormatted");
                String removecolor = skywarstar.substring(2);
                hypixelProfile.setSkywarsprofile(removecolor);
            } catch (Exception e) {
                hypixelProfile.setSkywarsprofile("0");
            }

            try {
                int bedwarsstar = normalProfile.getJSONObject("player").getJSONObject("stats").getJSONObject("Bedwars").getInteger("Experience");
                hypixelProfile.setBedwarsStar(bedwarsstar/5000);
            } catch (Exception e) {
                hypixelProfile.setBedwarsStar(0);
            }

            try {
                int megawallprofile = normalProfile.getJSONObject("player").getJSONObject("stats").getJSONObject("Walls3").getInteger("coins");
                hypixelProfile.setMwcoins(megawallprofile);
            } catch (Exception e) {
                hypixelProfile.setMwcoins(0);
            }

            try {
                JSONArray UHCprofile = normalProfile.getJSONObject("player").getJSONObject("stats").getJSONObject("UHC").getJSONArray("packages");
                if (UHCprofile.toString().contains("efficiency_pickaxe") && UHCprofile.toString().contains("apprentice_sword") && UHCprofile.toString().contains("tarnhelm") && UHCprofile.toString().contains("golden_head")) {
                    hypixelProfile.setUhcBasic(true);
                }
            } catch (Exception e) {
                hypixelProfile.setUhcBasic(false);
            }



            try {
                JSONArray UHCprofile = normalProfile.getJSONObject("player").getJSONObject("stats").getJSONObject("UHC").getJSONArray("packages");
                for (String t : uhcThings) {
                    if (UHCprofile.toString().contains(t.toLowerCase())) {
                        hypixelProfile.setUhcTopest(true);
                        hypixelProfile.setUhcBasic(false);
                    }
                }
            } catch (Exception e) {
                hypixelProfile.setUhcTopest(false);
            }

            try {
                int hypixelLevel = (int) ((Math.sqrt((2 * normalProfile.getJSONObject("player").getInteger("networkExp")) + 30625) / 50) - 2.5);
                hypixelProfile.setNetworkLevel(hypixelLevel);
            } catch (Exception e) {
                hypixelProfile.setNetworkLevel(0);
            }
            if (normalProfile.getJSONObject("player").getString("newPackageRank") != null) {
                if (normalProfile.getJSONObject("player").getString("monthlyPackageRank") != null) {
                    hypixelProfile.setHypixelRank(normalProfile.getJSONObject("player").getString("newPackageRank").replace("_PLUS", "++"));
                } else {
                    hypixelProfile.setHypixelRank(normalProfile.getJSONObject("player").getString("newPackageRank").replace("_PLUS", "+"));
                }
            }
        } else {
            hypixelProfile = null;
        }
        return hypixelProfile;

    }


    public static HypixelProfile getskyblockProfile(String profile, String uuid, String apiKey, int timeout, ProxyInfo proxy) {
        HypixelProfile hypixelProfile = new HypixelProfile();
        JSONObject skyblockProfile = HttpUtils.doGet("https://api.hypixel.net/skyblock/profile?key=" + apiKey + "&profile=" + profile, proxy, false, timeout);
        if (skyblockProfile == null) {
            return null;
        }
        if (skyblockProfile.getJSONObject("profile").getJSONObject("members") == null) {
            return null;
        }
        skyblockProfile = skyblockProfile.getJSONObject("profile").getJSONObject("members").getJSONObject(uuid);

        try {
            Double bank = skyblockProfile.getJSONObject("profile").getJSONObject("banking").getDouble("balance");
            hypixelProfile.setSkyblockCoins(skyblockProfile.getDouble("coin_purse") + bank);
        } catch (Exception e) {
            hypixelProfile.setSkyblockCoins(skyblockProfile.getDouble("coin_purse"));
        }
        StringBuilder things = new StringBuilder();

        String enderChest = skyblockProfile.getJSONObject("ender_chest_contents").getString("data");
        NamedTag enderChestData = null;
        try {
            enderChestData = NBTUtils.readNBTData(new ByteArrayInputStream(Base64.getDecoder().decode(enderChest.getBytes())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String t : greatThings) {
            if (enderChestData.getTag().toString().toLowerCase().contains(t.toLowerCase())) {
                things.append(t).append(", ");
            }
        }


        String wardrobe = skyblockProfile.getJSONObject("wardrobe_contents").getString("data");
        NamedTag wardrobedata = null;
        try {
            wardrobedata = NBTUtils.readNBTData(new ByteArrayInputStream(Base64.getDecoder().decode(wardrobe.getBytes())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String t : greatThings) {
            if (wardrobedata.getTag().toString().toLowerCase().contains(t.toLowerCase())) {
                things.append(t).append(", ");
            }
        }

        String inventory = skyblockProfile.getJSONObject("inv_contents").getString("data");
        NamedTag inventorydata = null;
        try {
            inventorydata = NBTUtils.readNBTData(new ByteArrayInputStream(Base64.getDecoder().decode(inventory.getBytes())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String t : greatThings) {
            if (inventorydata.getTag().toString().toLowerCase().contains(t.toLowerCase())) {
                things.append(t).append(", ");
            }
        }

        for (int i = 0; i < skyblockProfile.getJSONArray("pets").size(); i++) {
            for (String t : greatThings) {
                try {
                    if (skyblockProfile.getJSONArray("pets").getJSONObject(i).getString("type").toLowerCase().contains(t.toLowerCase())) {
                        things.append(t).append(", ");
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }


        String armor = skyblockProfile.getJSONObject("inv_armor").getString("data");
        NamedTag armordata = null;
        try {
            armordata = NBTUtils.readNBTData(new ByteArrayInputStream(Base64.getDecoder().decode(armor.getBytes())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String t : greatThings) {
            if (armordata.getTag().toString().toLowerCase().contains(t.toLowerCase())) {
                things.append(t).append(", ");
            }
        }


        hypixelProfile.setSkyblockArmor(things.toString());
        return hypixelProfile;
    }

    public static int getUHCStar(int score) {
        if (score >= 25210) {
            return 15;
        } else if (score >= 22210) {
            return 14;
        } else if (score >= 19210) {
            return 13;
        } else if (score >= 16210) {
            return 12;
        } else if (score >= 13210) {
            return 11;
        } else if (score >= 10210) {
            return 10;
        } else if (score >= 5210) {
            return 9;
        } else if (score >= 2710) {
            return 8;
        } else if (score >= 1710) {
            return 7;
        } else if (score >= 960) {
            return 6;
        } else if (score >= 460) {
            return 5;
        } else if (score >= 210) {
            return 4;
        } else if (score >= 60) {
            return 3;
        } else if (score >= 10) {
            return 2;
        } else if (score >= 0) {
            return 1;
        }
        return 1;
    }

}

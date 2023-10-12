package dev.tolja.Data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HypixelProfile {

    private String hypixelRank;
    private String skyblockArmor;
    private double skyblockCoins = 0;
    private int networkLevel = 0;
    private int uhcStar = 0;
    private int mwcoins = 0;
    private int bedwarsStar = 0;
    private Boolean isUnban;
    private Boolean Apikeys;
    private Boolean uhcBasic;
    private Boolean uhcTopest;
    private String bannedReason;
    private String skyblockprofile;
    private String skywarsprofile;

}
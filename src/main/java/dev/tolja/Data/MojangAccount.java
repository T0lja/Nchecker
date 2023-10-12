package dev.tolja.Data;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MojangAccount {

    private final String account; //email or username
    private final String password;

    @Setter
    private boolean isCracked;
    @Setter
    private String playerName;
    @Setter
    private String uuid;
    @Setter
    private String accessToken;
    @Setter
    private String capetype;
    @Setter
    private boolean isSecurity;
    @Setter
    private boolean isMfa;
    @Setter
    private boolean ELetterName;



    public MojangAccount(String account, String password) {
        this.account = account;
        this.password = password;
    }
}

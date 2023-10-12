package dev.tolja.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProxyInfo {

    private final String host;
    private final int port;

    private final boolean hasAuthentication;
    private final boolean isSocks;
    private final String username;
    private final String password;

    private final String rawSelf;

    @Override
    public String toString() {
        return this.rawSelf;
    }
}

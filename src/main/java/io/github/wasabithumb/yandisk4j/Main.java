package io.github.wasabithumb.yandisk4j;

import io.github.wasabithumb.yandisk4j.auth.AuthHandler;
import io.github.wasabithumb.yandisk4j.auth.AuthResponse;
import io.github.wasabithumb.yandisk4j.auth.AuthScheme;
import io.github.wasabithumb.yandisk4j.auth.AuthScope;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        final String token = getToken();
        YanDisk yd = YanDisk.yanDisk(token);

        yd.download("disk:/rock_gaming.png")
                        .read(new File("/home/wasabi/Desktop/rock_gaming.png"));
    }

    private static @NotNull String getToken() {
        final AuthHandler auth = YanDisk.auth(AuthScheme.SCREEN_CODE)
                .clientID("6b7a4a728a624228b2d93abe697ef726")
                .clientSecret("0fd69c030af349c4bec206126fd5b01d")
                .scopes(AuthScope.INFO, AuthScope.READ, AuthScope.WRITE)
                .build();

        auth.openURL();

        System.out.println("Enter code: ");
        String code;
        try (Scanner s = new Scanner(System.in)) {
            code = s.nextLine();
        }

        AuthResponse response;
        try {
            response = auth.exchange(code);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        return response.accessToken();
    }

}

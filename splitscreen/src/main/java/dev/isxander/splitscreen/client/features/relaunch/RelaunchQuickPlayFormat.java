package dev.isxander.splitscreen.client.features.relaunch;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RelaunchQuickPlayFormat(String ip, Optional<String> nonce) {
    // controlify;<ip>;[nonce]
    public static final Pattern PATTERN = Pattern.compile("splitscreen;([^;]+);([a-zA-Z0-9]+)?");

    public static Either<RelaunchQuickPlayFormat, String> parse(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            String ip = matcher.group(1);
            String nonce = matcher.group(2);

            return Either.left(new RelaunchQuickPlayFormat(ip, Optional.ofNullable(nonce)));
        }

        return Either.right(string);
    }

    public static String asString(String ip, @Nullable String nonce) {
        return new RelaunchQuickPlayFormat(ip, Optional.ofNullable(nonce)).format();
    }

    public String format() {
        return "splitscreen;" + ip() + ";" + nonce().orElse("");
    }
}

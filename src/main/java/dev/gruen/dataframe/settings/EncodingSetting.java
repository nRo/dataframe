package dev.gruen.dataframe.settings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class EncodingSetting extends ColumnSetting {
    public static final EncodingSetting UTF8 = new EncodingSetting(StandardCharsets.UTF_8);

    private final Charset charset;

    public EncodingSetting(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }
}

package com.herberto.jetsetcraft.graffiti;

import java.util.Base64;

/** Compact, bounded 16x10 graffiti canvas shared by the item, packet, entity, editor, and renderer. */
public final class CustomGraffiti {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 10;
    public static final int PIXELS = WIDTH * HEIGHT;
    public static final int[] PALETTE = {
            0x00000000, 0xFF17151C, 0xFFF4EBD0, 0xFFD83A36,
            0xFFF29D38, 0xFFF0D94A, 0xFF64B85A, 0xFF2BB8A9,
            0xFF338AC7, 0xFF5657B8, 0xFF8953A6, 0xFFB85B83,
            0xFF795548, 0xFF777777, 0xFFBDBDBD, 0xFFFFFFFF
    };

    public static String encode(byte[] pixels) {
        if (pixels == null || pixels.length != PIXELS) return "";
        byte[] packed = new byte[PIXELS / 2];
        for (int i = 0; i < pixels.length; i += 2) {
            int high = Math.max(0, Math.min(15, pixels[i]));
            int low = Math.max(0, Math.min(15, pixels[i + 1]));
            packed[i / 2] = (byte) ((high << 4) | low);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(packed);
    }

    public static byte[] decode(String encoded) {
        byte[] pixels = new byte[PIXELS];
        if (encoded == null || encoded.isBlank() || encoded.length() > 128) return pixels;
        try {
            byte[] packed = Base64.getUrlDecoder().decode(encoded);
            if (packed.length != PIXELS / 2) return pixels;
            for (int i = 0; i < packed.length; i++) {
                pixels[i * 2] = (byte) ((packed[i] >>> 4) & 15);
                pixels[i * 2 + 1] = (byte) (packed[i] & 15);
            }
        } catch (IllegalArgumentException ignored) {
            return new byte[PIXELS];
        }
        return pixels;
    }

    public static String normalize(String encoded) {
        if (encoded == null || encoded.isBlank()) return "";
        String normalized = encode(decode(encoded));
        return normalized.equals(encoded) ? normalized : "";
    }

    public static boolean isValid(String encoded) {
        return !normalize(encoded).isEmpty();
    }

    private CustomGraffiti() { }
}

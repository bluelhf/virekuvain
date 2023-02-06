package blue.lhf.virekuvain.model;


import java.awt.*;

public record ColourPalette(Color primary, Color secondary, Color background) {
    public static java.awt.Color fromFX(javafx.scene.paint.Color fx) {
        return new java.awt.Color(
            (int) (fx.getRed() * 255),
            (int) (fx.getGreen() * 255),
            (int) (fx.getBlue() * 255),
            (int) (fx.getOpacity() * 255)
        );
    }

    public static String toWeb(final Color color) {
        return "#%06X%02X".formatted(color.getRGB() & 0x00FFFFFF, color.getAlpha());
    }
}

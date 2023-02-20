package blue.lhf.virekuvain.view.visualisers;

import blue.lhf.virekuvain.model.AudioSource;
import blue.lhf.virekuvain.model.VisualiserMetadata;
import blue.lhf.virekuvain.view.Visualiser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static blue.lhf.virekuvain.util.ProcessingUtils.*;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Double.isFinite;

@VisualiserMetadata(name = "Spectrogram")
public class SpectrogramVisualiser extends Visualiser {

    public static final double DBFS_MINIMUM = -120;

    protected Color toColour(final double dbfs) {
        if (!isFinite(dbfs) || dbfs < DBFS_MINIMUM) return getPalette().primary();
        final Color minimum = getPalette().primary();
        final Color maximum = getPalette().secondary();

        final double progress = Math.pow(1 - dbfs / DBFS_MINIMUM, 9);
        return new Color(
            clamp((int) (progress * maximum.getRed() + (1 - progress) * minimum.getRed()), 0, 255),
            clamp((int) (progress * maximum.getGreen() + (1 - progress) * minimum.getGreen()), 0, 255),
            clamp((int) (progress * maximum.getBlue() + (1 - progress) * minimum.getBlue()), 0, 255),
            clamp((int) (progress * maximum.getAlpha() + (1 - progress) * minimum.getAlpha()), 0, 255)
        );
    }

    @Override
    public boolean drawBackground() {
        return false;
    }

    @Override
    protected void onResize(Graphics graphics, int width, int height) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
    }

    @Override
    protected void onUpdate(Graphics graphics, int width, int height, AudioSource source) {
        graphics.copyArea(1, 0, width - 1, height, -1, 0);
        final double[] mono = averages(source.getBuffer());
        final double[] spectrum = fourierTransform(mono);
        final double[] cutoff = binsInRange(20, 15_000, spectrum, source.getFrameRate());
        double[] magnitudes = toMagnitudes(cutoff);
        magnitudes = Arrays.stream(magnitudes).map(Math::cbrt).toArray();

        final BufferedImage image = new BufferedImage(1, height, TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            final double progress = y / (double) height;
            final double relativeIndex = Math.pow(progress, 2) * magnitudes.length;
            final double magnitude = indexByDouble(magnitudes, relativeIndex);
            final double dbfs = toDBFS(magnitude);
            image.setRGB(0, height - y - 1, toColour(dbfs).getRGB());
        }


        graphics.drawImage(image, width - 1, 0, null);
    }
}

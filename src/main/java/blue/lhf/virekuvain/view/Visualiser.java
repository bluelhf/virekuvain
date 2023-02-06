package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.*;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static blue.lhf.virekuvain.util.Functional.returnNull;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public abstract class Visualiser extends Canvas implements Closeable {
    public static final PixelFormat<IntBuffer> INT_ARGB_PRE_INSTANCE = PixelFormat.getIntArgbPreInstance();

    private final Timer timer = new Timer(true);
    private final Object lock = new Object();
    private AudioSource source;
    // JavaFX pretends GraphicsContext is immediate-mode,
    // but that's a LIE, DECEIT, and it SHOULDN'T be trusted.
    //
    // We draw to a BufferedImage instead.
    private int[] pixels;
    private BufferedImage image;
    private WritableImage writableImage;
    private PixelBuffer<IntBuffer> pixelBuffer;
    private ColourPalette palette;

    protected Visualiser() {
        this(0, 0);
    }

    protected Visualiser(double width, double height) {
        super(width, height);
        setCacheHint(CacheHint.SPEED);

        /*
         * We can't set bounds or, by extension, draw things if we don't have a parent...
         * We need to set our bounds to match the parent because Canvas doesn't support
         * inheriting bounds by default.
         * */
        this.parentProperty().addListener((val, prev, parent) -> initialise(parent));
    }

    public ColourPalette getPalette() {
        return palette;
    }

    public void setSource(final AudioSource source) {
        this.source = source;
    }

    private void initialise(final Parent parent) {
        parent.layoutBoundsProperty().addListener((val, prev, next) -> buildImage(next));

        // Mouse events actually incur a pretty big performance hit... Disable them!
        parent.addEventFilter(MouseEvent.ANY, Event::consume);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (image == null) return;
                final CompletableFuture<Void> future = new CompletableFuture<>();
                update();

                Platform.runLater(() -> {
                    draw();
                    future.complete(null);
                });

                future.exceptionally(returnNull(Throwable::printStackTrace));
            }
        }, 0, (long) (1000.0 / 144.0));

    }

    private void buildImage(final Bounds bounds) {
        if (bounds.getWidth() == 0 || bounds.getHeight() == 0) return;
        synchronized (lock) {
            this.setWidth(bounds.getWidth());
            this.setHeight(bounds.getHeight());
            this.image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(), TYPE_INT_ARGB);
            this.pixels = new int[image.getWidth() * image.getHeight()];

            final IntBuffer intBuffer = IntBuffer.wrap(pixels);
            this.pixelBuffer = new PixelBuffer<>(
                image.getWidth(), image.getHeight(), intBuffer, INT_ARGB_PRE_INSTANCE);
            this.writableImage = new WritableImage(pixelBuffer);
        }
    }

    protected abstract void onUpdate(
        final Graphics graphics,
        final int width, final int height,
        final AudioSource source
    );

    public void update() {
        if (source == null) return;
        synchronized (lock) {
            final Graphics g = image.getGraphics();
            g.setColor(palette.background());
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            onUpdate(g, image.getWidth(), image.getHeight(), source);
            image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), pixels);
        }
    }

    public void draw() {
        final GraphicsContext gc = getGraphicsContext2D();
        this.pixelBuffer.updateBuffer(b -> null);
        gc.drawImage(writableImage, 0, 0);
    }


    @Override
    public void close() {
        timer.cancel();
    }

    public void setColourPalette(final ColourPalette palette) {
        this.palette = palette;
        this.getParent().setStyle(
            this.getParent().getStyle().replaceAll("-fx-background-color:[^;]+;", "")
                + "-fx-background-color: " + ColourPalette.toWeb(palette.background()) + ";");
    }
}

package blue.lhf.virekuvain.view;

import javafx.event.Event;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.*;
import javafx.scene.input.*;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.awt.image.*;
import java.nio.*;
import java.util.concurrent.*;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public abstract class FastCanvas extends Canvas {
    public static final PixelFormat<IntBuffer> INT_ARGB_PRE_INSTANCE = PixelFormat.getIntArgbPreInstance();

    private final Semaphore lock = new Semaphore(1);

    private int[] pixels;
    private BufferedImage image;
    private WritableImage writableImage;
    private PixelBuffer<IntBuffer> pixelBuffer;

    protected FastCanvas(double width, double height) {
        super(width, height);
        setCacheHint(CacheHint.SPEED);

        /*
         * We can't set bounds or, by extension, draw things if we don't have a parent...
         * We need to set our bounds to match the parent because Canvas doesn't support
         * inheriting bounds by default.
         * */
        this.parentProperty().addListener((val, prev, parent) -> {
            if (parent == null) return;
            initialise0(parent);
        });
    }

    private void initialise0(final Parent parent) {
        buildImage(parent.getLayoutBounds());
        parent.layoutBoundsProperty().addListener((val, prev, next) -> buildImage(next));

        // Mouse events actually incur a pretty big performance hit... Disable them!
        parent.addEventFilter(MouseEvent.ANY, Event::consume);
        initialise();
    }

    protected abstract void initialise();

    protected @Nullable ImageHandle open() {
        if (this.image == null) return null;
        return new ImageHandle();
    }

    private void buildImage(final Bounds bounds) {
        if (bounds.getWidth() == 0 || bounds.getHeight() == 0) return;
        lock.acquireUninterruptibly(); {
            this.setWidth(bounds.getWidth());
            this.setHeight(bounds.getHeight());
            this.image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(), TYPE_INT_ARGB);
            this.pixels = new int[image.getWidth() * image.getHeight()];

            final IntBuffer intBuffer = IntBuffer.wrap(pixels);
            this.pixelBuffer = new PixelBuffer<>(
                image.getWidth(), image.getHeight(), intBuffer, INT_ARGB_PRE_INSTANCE);
            this.writableImage = new WritableImage(pixelBuffer);
        } lock.release();
    }

    public void draw() {
        final GraphicsContext gc = getGraphicsContext2D();
        this.pixelBuffer.updateBuffer(b -> null);
        gc.drawImage(writableImage, 0, 0);
    }

    protected class ImageHandle implements AutoCloseable {
        private final Graphics graphics;

        private ImageHandle() {
            lock.acquireUninterruptibly();
            this.graphics = image.getGraphics();
        }

        public Graphics getGraphics() {
            return graphics;
        }

        public int getWidth() {
            return image.getWidth();
        }

        public int getHeight() {
            return image.getHeight();
        }

        @Override
        public void close() {
            lock.release();
            image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), pixels);
        }
    }
}

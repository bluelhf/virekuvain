package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.util.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.scene.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static blue.lhf.virekuvain.util.Functional.returnNull;

public abstract class Visualiser extends FastCanvas implements Closeable {
    private final Property<AudioSource> source = new SimpleProperty<>(this, "source");
    private final Property<ColourPalette> palette = new SimpleProperty<>(this, "palette");
    private final Timer timer = new Timer(true);

    protected Visualiser() {
        this(0, 0);
    }

    protected Visualiser(double width, double height) {
        super(width, height);
        setCacheHint(CacheHint.SPEED);

        this.palette.addListener((val, prev, next) -> {
            if (this.getParent() == null) return;
            this.getParent().setStyle(
                this.getParent().getStyle().replaceAll("-fx-background-color:[^;]+;", "")
                + "-fx-background-color: " + ColourPalette.toWeb(next.background()) + ";");
        });
    }

    public ColourPalette getPalette() {
        return palette.getValue();
    }

    @Override
    protected void initialise() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final CompletableFuture<Void> future = new CompletableFuture<>();
                Platform.runLater(() -> {
                    draw();
                    future.complete(null);
                });

                future.exceptionally(returnNull(Throwable::printStackTrace)).join();
            }
        }, 0, (long) (1000.0 / 144.0));
        // TODO(ilari): sync above period with monitor?
        //                - how do we get the monitor the application is on?
        //                - how do we get its fps?

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, (long) (1000.0 / 144.0));
    }

    protected abstract void onUpdate(
        final Graphics graphics,
        final int width, final int height,
        final AudioSource source
    );

    public void update() {
        if (source.getValue() == null) return;

        try (final ImageHandle handle = open()) {
            if (handle == null) return;

            final Graphics g = handle.getGraphics();

            if (drawBackground()) {
                g.setColor(getPalette().background());
                g.fillRect(0, 0, handle.getWidth(), handle.getHeight());
            }

            onUpdate(g, handle.getWidth(), handle.getHeight(), source.getValue());
        }
    }

    public boolean drawBackground() {
        return true;
    }

    @Override
    public void close() {
        timer.cancel();
    }

    public Property<ColourPalette> paletteProperty() {
        return this.palette;
    }

    public Property<AudioSource> sourceProperty() {
        return this.source;
    }
}

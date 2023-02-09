package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.util.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;

import java.io.*;

public class VisualiserMultiplexer extends Pane implements Closeable {
    private final Property<AudioSource> source = new SimpleProperty<>(this, "source");
    private final Property<ColourPalette> palette = new SimpleProperty<>(this, "palette");
    private final Property<Visualiser> visualiser = new SimpleProperty<>(this, "visualiser");

    public VisualiserMultiplexer() {
        super();
        visualiser.addListener((val, prev, next) -> {
            if (prev != null) prev.close();
            getChildren().remove(prev);
            getChildren().add(next);
            next.paletteProperty().bind(this.palette);
            next.sourceProperty().bind(this.source);
        });
    }

    public Property<Visualiser> visualiserProperty() {
        return visualiser;
    }

    public Visualiser getVisualiser() {
        return visualiser.getValue();
    }

    public void setVisualiser(final Visualiser newVisualiser) {
        this.visualiser.setValue(newVisualiser);
    }

    public void setColourPalette(final ColourPalette colourPalette) {
        this.palette.setValue(colourPalette);
    }
    
    public void setSource(final AudioSource source) {
        this.source.setValue(source);
    }

    @Override
    public void close() {
        if (getVisualiser() != null)
            getVisualiser().close();
    }
}

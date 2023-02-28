package blue.lhf.virekuvain.controller;

import blue.lhf.virekuvain.view.visualisers.SpectrogramVisualiser;
import blue.lhf.virekuvain.view.Visualiser;
import blue.lhf.virekuvain.view.visualisers.*;
import javafx.collections.*;

public class VisualiserList extends ObservableListBase<Class<Visualiser>> {
    // NOTE(ilari): Maybe a registry would be nicer?
    private static final Class<Visualiser>[] VISUALISERS = new Class[]{
        FFTVisualiser.class, WaveformVisualiser.class, SpectrogramVisualiser.class, VectorscopeVisualiser.class
    };

    @Override
    public Class<Visualiser> get(int index) {
        return VISUALISERS[index];
    }

    @Override
    public int size() {
        return VISUALISERS.length;
    }


}

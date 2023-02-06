package blue.lhf.virekuvain.controller;

import javafx.collections.*;

import javax.sound.sampled.*;

public class MixerList extends ObservableListBase<Mixer.Info> {
    @Override
    public Mixer.Info get(int index) {
        return AudioSystem.getMixerInfo()[index];
    }

    @Override
    public int size() {
        return AudioSystem.getMixerInfo().length;
    }
}

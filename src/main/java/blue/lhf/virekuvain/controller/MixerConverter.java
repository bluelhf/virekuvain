package blue.lhf.virekuvain.controller;

import javafx.util.*;

import javax.sound.sampled.*;

public class MixerConverter extends StringConverter<Mixer.Info> {
    @Override
    public String toString(Mixer.Info object) {
        if (object == null) return null;
        return object.getName();
    }

    @Override
    public Mixer.Info fromString(String string) {
        return null;
    }
}

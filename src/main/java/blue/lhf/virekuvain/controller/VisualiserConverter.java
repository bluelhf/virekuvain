package blue.lhf.virekuvain.controller;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.view.*;
import javafx.util.*;

public class VisualiserConverter extends StringConverter<Class<Visualiser>> {
    @Override
    public String toString(Class<Visualiser> object) {
        if (object == null) return null;
        final VisualiserMetadata metadata = object.getAnnotation(VisualiserMetadata.class);
        if (metadata != null) {
            return metadata.name();
        }

        return object.getSimpleName();
    }

    @Override
    public Class<Visualiser> fromString(String string) {
        return null;
    }
}

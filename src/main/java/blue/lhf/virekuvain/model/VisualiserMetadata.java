package blue.lhf.virekuvain.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VisualiserMetadata {
    String name();
}

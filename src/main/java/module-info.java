module blue.lhf.virekuvain {
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.controls;
    requires org.jetbrains.annotations;
    requires JTransforms;

    exports blue.lhf.virekuvain;
    exports blue.lhf.virekuvain.controller;
    opens blue.lhf.virekuvain.controller to javafx.fxml;
    exports blue.lhf.virekuvain.view;
}
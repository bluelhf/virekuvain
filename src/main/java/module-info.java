module blue.lhf.virekuvain {
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.controls;
    requires org.jetbrains.annotations;
    requires JTransforms;

    exports blue.lhf.virekuvain;
    exports blue.lhf.virekuvain.controller;
    opens blue.lhf.virekuvain.view to javafx.fxml;
    opens blue.lhf.virekuvain.view.interpolation to javafx.fxml;
    opens blue.lhf.virekuvain.view.visualisers to javafx.fxml;
    opens blue.lhf.virekuvain.controller to javafx.fxml;
}
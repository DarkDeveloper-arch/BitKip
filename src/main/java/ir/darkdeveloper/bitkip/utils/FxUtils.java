package ir.darkdeveloper.bitkip.utils;


import ir.darkdeveloper.bitkip.controllers.FXMLController;
import ir.darkdeveloper.bitkip.controllers.MainController;
import ir.darkdeveloper.bitkip.controllers.NewDownload;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ir.darkdeveloper.bitkip.BitKip.getResource;

public class FxUtils {


    public static void switchSceneToMain(Stage stage, String fxmlFilename) {
        try {
            var loader = new FXMLLoader(getResource("fxml/" + fxmlFilename));
            Parent root = loader.load();
            var scene = new Scene(root, stage.getWidth(), stage.getHeight());
            MainController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(scene);
            stage.setTitle("BitKip");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static Stage getStageFromEvent(ActionEvent e) {
        if (e.getSource() instanceof Node n
                && n.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        return null;
    }

    public static NewDownload newDownloadStage(String fxmlFilename, double minWidth, double minHeight) {
        FXMLLoader loader = null;
        Stage stage = new Stage();
        Parent root = null;
        try {
            loader = new FXMLLoader(getResource("fxml/" + fxmlFilename));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FXMLController controller = loader.getController();
        var scene = new Scene(root);
        stage.setScene(scene);
        var logoPath = getResource("images/logo.png");
        if (logoPath != null)
            stage.getIcons().add(new Image(logoPath.toExternalForm()));
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setMaxHeight(600);
        stage.setTitle("New Download");
        controller.setStage(stage);
        stage.show();
        return (NewDownload) controller;
    }

    public static <T> List<T> getAllNodes(Parent root, Class<T> tClass) {
        var nodes = new ArrayList<T>();
        addAllDescendents(root, nodes, tClass);
        return nodes;
    }

    private static <T> void addAllDescendents(Parent root, ArrayList<T> nodes, Class<T> tClass) {
        for (var node : root.getChildrenUnmodifiable()) {
            if (tClass.isInstance(node))
                nodes.add(tClass.cast(node));
            if (node instanceof Parent p)
                addAllDescendents(p, nodes, tClass);
        }
    }

}


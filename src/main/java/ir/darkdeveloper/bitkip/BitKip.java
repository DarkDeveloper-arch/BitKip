package ir.darkdeveloper.bitkip;

import ir.darkdeveloper.bitkip.config.AppConfigs;
import ir.darkdeveloper.bitkip.repo.DownloadsRepo;
import ir.darkdeveloper.bitkip.repo.QueuesRepo;
import ir.darkdeveloper.bitkip.utils.FxUtils;
import ir.darkdeveloper.bitkip.utils.IOUtils;
import ir.darkdeveloper.bitkip.utils.ResizeUtil;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ArrayList;

public class BitKip extends Application {

    @Override
    public void start(Stage stage) {
        IOUtils.createSaveLocations();
        DownloadsRepo.createTable();
        QueuesRepo.createTableAndDefaultRecords();
        AppConfigs.setQueues(QueuesRepo.getQueues());
        FxUtils.switchSceneToMain(stage, "main.fxml");
        AppConfigs.setHostServices(getHostServices());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setMinHeight(515);
        stage.setMinWidth(883);
        stage.setResizable(true);
        ResizeUtil.addResizeListener(stage);
        stage.show();
    }


    @Override
    public void stop() {
        var notObservedDms = new ArrayList<>(AppConfigs.currentDownloading);
        notObservedDms.forEach(dm -> dm.getDownloadTask().pause());
    }


    public static void main(String[] args) {
        launch();
    }

    public static URL getResource(String path) {
        return BitKip.class.getResource(path);
    }
}
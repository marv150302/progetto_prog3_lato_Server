package com.example.progettoprog3latoserver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import model.ServerModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController {
    @FXML
    private TextArea log;
    @FXML
    private Button serverSwitch;

    private final ServerModel server = new ServerModel(8080);


    public void initModel(){

        this.log.textProperty().bindBidirectional(server.getLog());
    }

    public void onSwitchServerOnButtonClick(){

        server.start();
    }

}
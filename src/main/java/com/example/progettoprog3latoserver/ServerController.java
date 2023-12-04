package com.example.progettoprog3latoserver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import model.ServerModel;

public class ServerController {
    @FXML
    private TextArea log;
    @FXML
    private Button serverSwitch;

    private final ServerModel server = new ServerModel(5056);


    public void initModel(){

        this.log.textProperty().bindBidirectional(server.getLog());
    }

    public void onSwitchServerOnButtonClick(){

        server.setDaemon(true);
        server.start();
    }

}
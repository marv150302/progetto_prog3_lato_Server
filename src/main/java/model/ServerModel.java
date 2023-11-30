package model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerModel {

    private Socket socket   = null;
    private ServerSocket server   = null;
    private DataInputStream in  =  null;

    public ServerModel(int port){

        try{

            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            while(true){

                socket = server.accept();
                System.out.println("Client accepted");

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

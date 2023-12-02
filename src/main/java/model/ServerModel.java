package model;

import javafx.beans.property.SimpleStringProperty;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class ServerModel {

    private ServerSocket server   = null;
    private DataInputStream in  =  null;
    private SimpleStringProperty log = null;
    private SimpleStringProperty serverSwitch = null;
    public int PORT;

    public void start(){

        //System.out.println("Thread is running");
        //ExecutorService service = Executors.newFixedThreadPool(10);
        try{

            server = new ServerSocket(this.PORT);
            //System.out.println("Server started");
            //System.out.println("Waiting for a client ...");
            while(!server.isClosed()){


                Socket socket = server.accept();
                // obtaining input and out streams
                //DataInputStream dis = new DataInputStream(socket.getInputStream());
                //DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                //System.out.println("Assigning new thread for this client");
                System.out.println("A new client has connected");
                Thread thread = new Thread( new ClientHandler(socket));
                thread.start();
                //Runnable client = new ClientHandler(socket, dis, dos);
                //service.execute(client);


            }


        } catch (Exception e) {this.closeServerSocket();}
    }

    public void closeServerSocket(){

        try {
            if (server!=null){
                server.close();
            }
        }catch (IOException e){

            e.printStackTrace();
        }
    }
    public ServerModel(int port){

        this.log = new SimpleStringProperty();
        this.serverSwitch = new SimpleStringProperty();
        this.PORT = port;

    }

    public SimpleStringProperty getLog(){
        return this.log;
    }

}

package model;

import javafx.beans.property.SimpleStringProperty;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerModel extends Thread{

    private ServerSocket server   = null;
    private DataInputStream in  =  null;
    private SimpleStringProperty log = null;
    private SimpleStringProperty serverSwitch = null;
    public int PORT;

    public final ServerModel model;

    public ServerModel(int port){

        this.log = new SimpleStringProperty();
        this.serverSwitch = new SimpleStringProperty();
        this.PORT = port;
        this.model = this;

    }


    public void run(){



        //System.out.println("Thread is running");
        //ExecutorService service = Executors.newFixedThreadPool(10);
        try{

            server = new ServerSocket(PORT);
            this.writeLog("Server Started");
            this.writeLog("Waiting for a client ...");
            //System.out.println("Waiting for a client ...");
            while(!server.isClosed()){


                Socket socket = server.accept();
                Thread thread = new Thread( new ClientHandler(socket, model));
                thread.start();
                //Runnable client = new ClientHandler(socket, dis, dos);
                //service.execute(client);

            }


        } catch (Exception e) {closeServerSocket();}
    }


    public void writeLog(String message){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        if (this.getLog().getValue()==null) {

            this.getLog().set(dtf.format(now) + ":  " + message);
            return;
        }
        this.getLog().set(this.getLog().getValue()+"\n"+ dtf.format(now) + ":  " + message);
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


    public SimpleStringProperty getLog(){
        return this.log;
    }

}

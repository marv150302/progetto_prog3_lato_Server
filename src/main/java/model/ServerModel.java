package model;

import javafx.beans.property.SimpleStringProperty;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerModel extends Thread{

    private Socket socket   = null;
    private ServerSocket server   = null;
    private DataInputStream in  =  null;
    private SimpleStringProperty log = null;
    private SimpleStringProperty serverSwitch = null;
    public int PORT;

    public void run(){

        System.out.println("Thread is running");
        ExecutorService service = Executors.newFixedThreadPool(10);
        try{

            server = new ServerSocket(this.PORT);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            while(true){


                socket = server.accept();
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                System.out.println("Assigning new thread for this client");

                service.execute(new ClientHandler(socket, dis, dos));

            }


        } catch (Exception e) {

            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
    private class ClientHandler extends Thread{

        private Socket socket;
        private DataInputStream dis;
        private  DataOutputStream dos;
        public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos){

            this.socket = socket;
            this.dos = dos;
            this.dis = dis;
            this.start();
        }
        public void run(){

            String received;
            String toreturn;
            while(true){

                try{

                    // Ask user what he wants
                    dos.writeUTF("What do you want?[Date | Time]..\n"+
                            "Type Exit to terminate connection.");

                    // receive the answer from client
                    received = dis.readUTF();
                    if (received.equalsIgnoreCase("exit")){

                        System.out.println("Client " + this.socket + " sends exit...");
                        System.out.println("Closing this connection.");
                        this.socket.close();
                        System.out.println("Connection closed");
                        break;
                    }
                }catch (Exception e){}
            }
        }
    }
}

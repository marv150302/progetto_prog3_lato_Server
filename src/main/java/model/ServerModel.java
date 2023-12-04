package model;

import javafx.beans.property.SimpleStringProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerModel extends Thread{

    private ServerSocket server   = null;
    private DataInputStream in  =  null;
    private SimpleStringProperty log = null;
    private SimpleStringProperty serverSwitch = null;
    public int PORT;

    public ServerModel(int port){

        this.log = new SimpleStringProperty();
        this.serverSwitch = new SimpleStringProperty();
        this.PORT = port;
        /*try{
            this.getClients();
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }

    public void run(){



        //System.out.println("Thread is running");
        //ExecutorService service = Executors.newFixedThreadPool(10);
        try{

            server = new ServerSocket(PORT);
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


        } catch (Exception e) {closeServerSocket();}
    }

    private void getClients() throws IOException, ParseException {



        String src = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/client_list.json";
        ArrayList<Client> clients = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        for (Object o : (JSONArray) jsonParser.parse(new FileReader(src))) {

            JSONObject rootObj = (JSONObject) o;
            //

            String name = (String) rootObj.get("name");
            String surname = (String) rootObj.get("surname");
            String email = (String) rootObj.get("email");
            String ID = (String) rootObj.get("ID");
            //
            clients.add(new Client(name, surname, email, ID));
        }

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

    private class Client{

        String name;
        String surname;
        String email;
        String ID;
        public Client(String name, String surname, String email, String ID){

            this.name = name;
            this.surname = surname;
            this.email = email;
            this.ID = ID;
        }


    }
}

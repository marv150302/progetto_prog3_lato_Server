package model;

import com.example.progettoprog3latoserver.Email;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private DataInputStream dis;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private DataOutputStream dos;
    public ClientHandler(Socket socket){

        try{

            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            clientHandlers.add(this);
        }catch(IOException e){

            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    public void sendMessage(String message){

            /*for (ClientHandler clientHandler: clientHandlers){

                try{


                }
            }*/
        try{
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }catch (IOException e){

            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

        try{

            if (bufferedReader!=null){

                bufferedReader.close();
            }
            if (bufferedWriter!=null){

                bufferedWriter.close();
            }
            if (socket != null){

                socket.close();
            }
        }catch (IOException e){e.printStackTrace();}
    }
    public void run(){

        String messageFromClient;

        while (socket.isConnected()){

            //System.out.println("Thread is running");
            try{

                messageFromClient = bufferedReader.readLine();

                if (messageFromClient.contains("login")){

                    System.out.println("message: " + messageFromClient);
                }

            }catch (IOException e){

                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }



        /*String received;
        String toreturn;
        while(!Thread.currentThread().isInterrupted()){

            try{

                // Ask user what he wants


                System.out.println("inside");
                // receive the answer from client
                received = dis.readUTF();
                System.out.println("received user:" + received);
                if (received.toLowerCase().contains("login")){


                    String usr_ID = received.substring(new String("login").length());
                    System.out.println("ID: " + usr_ID);
                    String user_file = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/" + usr_ID + ".json";
                    String file = new String(Files.readAllBytes(Paths.get(user_file)));
                    System.out.println("file_data"+ file);
                    dos.writeUTF(file);

                }
                if (received.equalsIgnoreCase("exit")){

                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
            }catch (Exception e){Thread.currentThread().interrupt();}
        }*/
    }

    private boolean getUserEmails(String userID) throws IOException, ParseException {

        String src = "/Users/marvel/Programming/Uni/ProgettoProg3/src/main/java/com/example/progettoprog3/MOCK_DATA.json";
        ArrayList<Email> emails = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();

        for (Object o : (JSONArray) jsonParser.parse(new FileReader(src))) {

            JSONObject rootObj = (JSONObject) o;
            //

            String ID = (String) rootObj.get("ID");
            String sender = (String) rootObj.get("sender");
            String receiver = (String) rootObj.get("receiver");
            String text = (String) rootObj.get("text");
            String object = (String) rootObj.get("object");
            String date = (String) rootObj.get("date");
            //
            emails.add(new Email(ID, sender,receiver,text, object, date));
        }
        return true;
    }

}
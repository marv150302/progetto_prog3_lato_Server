package model;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private DataInputStream dis;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private DataOutputStream dos;

    public ServerModel model;
    public ClientHandler(Socket socket, ServerModel model){

        try{

            this.socket = socket;
            this.model = model;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            clientHandlers.add(this);
        }catch(IOException e){

            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    public void sendMessage(String message){

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
    boolean once = false;
    public void run(){

        String messageFromClient;
        while (socket.isConnected()){

            try{

                messageFromClient = bufferedReader.readLine();

                if (true){

                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(messageFromClient);
                    switch ((String) json.get("action")){

                        case "login":{



                            this.username = (String) json.get("user");
                            model.writeLog(this.username + " has conncted");
                            if (this.username==null) break;
                            String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/"+this.username+".json";
                            File user_file = new File(filePathString);
                            if (!user_file.createNewFile()){

                                /*
                                 * The user already existed, so we send him back his inbox
                                 * otherwise we create a new empty json file
                                 * */
                                String usr_inbox = new String(Files.readAllBytes(Paths.get(filePathString)));
                                JSONObject json_to_send = new JSONObject();
                                json_to_send.put("action", "inbox");
                                json_to_send.put("emails", usr_inbox);
                                this.sendMessage(json_to_send.toString());
                            }
                            break;
                        }

                        case "send email":{

                            String message = (String) json.get("text");
                            String object = (String) json.get("object");
                            String receiver =(String) json.get("receiver");
                            String[] receivers = receiver.split("\\s+");
                            String failed_clients = "";
                            for (String client : receivers) {

                                ClientHandler client_to_search = searchClient(client);

                                if (client_to_search!=null){
                                    /*
                                    * if we have found the client
                                    * we proceed to send the email
                                    * */
                                    JSONObject json_to_send = new JSONObject();
                                    json_to_send.put("action","receiving email");
                                    json_to_send.put("text", message);
                                    json_to_send.put("object", object);
                                    json_to_send.put("receiver", receiver);
                                    json_to_send.put("sender", this.username);
                                    client_to_search.sendMessage(json_to_send.toString());
                                    model.writeLog(this.username + " has sent an email to: " + receiver);
                                    model.writeLog(receiver + " has received an email from: " + this.username);
                                    json_to_send = new JSONObject();
                                    json_to_send.put("action", "confirmed delivery");
                                    this.sendMessage(json_to_send.toString());
                                }else {

                                    /*
                                    * we save the wrong emails and report them to the log and the user
                                    * */
                                    failed_clients += client + " - ";
                                }
                            }
                            if (!failed_clients.isEmpty()){

                                /*
                                *
                                * we notify both the server log and the user
                                * */
                                model.writeLog(this.username + " failed to send email to: " + failed_clients);
                                JSONObject json_to_send = new JSONObject();
                                json_to_send.put("action", "error in delivery");
                                json_to_send.put("failed-emails", failed_clients);
                            }


                            break;
                        }
                    }
                }

            }catch (IOException e){

                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private ClientHandler searchClient(String user_email){

        for (ClientHandler client : clientHandlers){

            if (client.username.equals(user_email)){

                return client;
            }
        }
        return null;
    }







}
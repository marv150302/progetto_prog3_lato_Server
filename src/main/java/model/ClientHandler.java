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
    boolean once = false;
    public void run(){




        String messageFromClient;

        while (socket.isConnected()){


            //System.out.println("Thread is running");
            try{
                messageFromClient = bufferedReader.readLine();



                if (true){

                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(messageFromClient);
                    //System.out.println((String) json.get("action"));
                    switch ((String) json.get("action")){

                        case "login":{

                            this.username = (String) json.get("msg");
                            String filePathString = "/Users/marvel/Programming/Uni/progettoProg3LatoServer/src/main/java/com/example/progettoprog3latoserver/email_JSON/"+this.username+".json";
                            File user_file = new File(filePathString);
                            if (user_file.createNewFile()){

                                System.out.println("file created");
                            }else{

                                /*
                                 * The user already existed so we send him back his inbox
                                 * */
                                String usr_inbox = new String(Files.readAllBytes(Paths.get(filePathString)));
                                JSONObject json_to_send = new JSONObject();
                                json_to_send.put("action", "inbox");
                                json_to_send.put("emails", usr_inbox);
                                this.sendMessage(json_to_send.toString());

                                System.out.println("file has already been created");

                            }
                        }
                    }

                    System.out.println("message: " + messageFromClient);
                    //this.username = messageFromClient.substring(5);

                   // File f = new File(filePathString);


                    /*
                    * 1 - We will check if the user is present in our database(for now it's just a JSON file)
                    * 2 - If he is then we will simply send back his inbox
                    * 3 - Otherwise we will add him to our database
                    * */
                }

            }catch (IOException e){

                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            } catch (ParseException e) {
                throw new RuntimeException(e);
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







}
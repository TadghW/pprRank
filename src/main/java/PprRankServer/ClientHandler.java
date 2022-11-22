package main.java.pprrankserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import main.java.pprrankserver.WebpageBuilder; 

public class ClientHandler implements Runnable {

    private int threadNo;
    
    private final Socket client;

    public ClientHandler(Socket socket, int threadNo){
        this.client = socket;
        this.threadNo = threadNo;
    }

    public void run(){
        
        System.out.println("\r\nClient " + client.toString() + " connected and passed to handler on thread " + threadNo);
                
        try {
            
            //InputStreamReader reads information recieved from the client, but does so by reading one character at a time and leaving the remaining characters in the stream unprocessed
            //We can make that process drastically more efficient using BufferedReader, which takes n characters at a time from the input stream and stores them in a buffer for faster access
            //Buffer size can be set by the user.
            InputStreamReader isr = new InputStreamReader(client.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            
            //Now we can efficiently grab the information sent over by the client, we can reconstruct those recieved bytes into the single request they form
            StringBuilder request = new StringBuilder();
            
            //Using our string builder we can append these lines into a single string with whatever formatting we want
            String line = br.readLine();

            String requestSummary = line;

            //We want to stop reading when we've run out of data to read
            while(!line.isBlank()){
                request.append(line + "\r\n");
                line = br.readLine();
            }

            System.out.println("\r\nThread " + this.threadNo + " has received the following request from " + client.toString() + "\r\n\r\n--REQUEST--" + "\r\n\r\n" + requestSummary);
            
            /*
            MongoDatabase database = mongoClient.getDatabase("PPR-Listing");
            
            MongoCollection<Document> collection = database.getCollection("Headphones");
            
            Document testDoc = collection.find(eq("name", "hd600")).first();
            
            System.out.println("\r\nRetrieved document: " + testDoc.toJson());

            System.out.println("\r\nThread " + this.threadNo + " has connected to Basre Cluster");

            System.out.println("\r\nThread " + this.threadNo + " sending the generic response and sleeping");
            */

            WebpageBuilder webpageBuilder = new WebpageBuilder(mongoClient);

            //As with the InputStream the OutputStream accepts bytes and nothing else so we have to convert any response we prepare to bytes before sending them through
            OutputStream outputStream = client.getOutputStream();
            outputStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
            outputStream.write(("\r\n").getBytes());
            outputStream.write((webpageBuilder.build(requestSummary)).getBytes());
            
            //Once our response is written to the output stream we can flush it, and close it to indicate to the client that the message is over and no further responses are coming
            outputStream.flush();
            outputStream.close();

        } catch(IOException e) {
            System.out.println("ClientHandler thread " + threadNo + "has encountered an exception: " + e );
        }
    }

}
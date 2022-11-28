package main.java.pprrankserver;

import main.java.pprrankserver.DatasetPopulator;
import main.java.pprrankserver.Dataset;
import java.net.ServerSocket;
import main.java.pprrankserver.ClientHandler; 
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

class PprRankServer {

    //The first thing this application does is populate a static list with the data stored in a MongoDB cluster. It turns these into POJOs
    //to be used by WebpageBuilder objects. We do this on startup because the dataset is small enough that everything we're working with can
    //be stored in system memory, and because due to high uptime being non-critical I can just restart the application to repopulate the database

    private static DatasetPopulator datasetPopulator = new DatasetPopulator();

    protected static Dataset[] headphoneList = datasetPopulator.populate();


    private static int threadCount = 0;

    public static void main(String[] args) {

        //Sockets are a java.net feature that allows us to quickly connect our appplication to networking infrastructure
        //Check out the ClientHandler if you want to see client sockets in use.

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            
            System.out.println ("Server started..\r\nListening for messages...");

            Boolean shouldBeRunning = true; 
            while (shouldBeRunning) {
                
                //clientHandler implements runnable and so can be run in different threads. We need this class to handle communications to and from
                //the client so we pass it the Socket object generated by serverSocket.accept(), for logging purposes we also tell it what number
                //it is
                try {
                    Socket client = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(client, threadCount++);
                    new Thread(clientHandler).start();
                } catch (Exception e) {
                    System.out.println("\r\nFailed to establish connection with the client, exception: " + e + "\r\n");
                }

            }
        } catch (Exception e){
            System.out.println("\r\nFailed to launch the server, exception: " + e + "\r\n");
        }

        System.out.println("Global resource filled and active. Currently storing the following datasets: ");
        
        for(Dataset dataset : headphoneList){
            System.out.println(dataset.toString());
        }

    }

}
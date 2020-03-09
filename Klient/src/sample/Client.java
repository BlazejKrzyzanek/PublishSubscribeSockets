package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void startConnection(int port) throws IOException {
        clientSocket = new Socket(this.ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
    }

    public String recieveMessage() throws IOException {
        String resp;
        if (in.ready()) {
             resp = in.readLine();
        }
        else
            resp = "";
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    @Override
    public void run() {
        String message;
        while (true) {
            System.out.println("Odczytuje");
            try {
                message = Main.client.recieveMessage();
                System.out.println("odczytuje: " + message);
                if (message != "" ){
                    if(message.startsWith(">"))
                        Main.refreshList(message.substring(1));
                    if (message.startsWith("<")){
                        String mes = message.substring(1);
                        String [] mesSplit = mes.split(";");
                        for(int i=0; i< mesSplit.length; i+=2){
//                            System.out.println(mesSplit[i]);
//                            System.out.println(mesSplit[i+1]);
                            Main.refreshList(mesSplit[0] + ";" + mesSplit[1]);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}

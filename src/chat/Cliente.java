/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import exceptions.UserException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
/**
 *
 * @author user
 */
public class Cliente {
    
    DatagramSocket socket;
    Thread threadCliente;
    private String usuario;
    InetAddress ip;
    int porta;
    
    public Cliente(String endereco, String porta, String usuario) throws SocketException, UnknownHostException{
        socket = new DatagramSocket();
        this.ip = InetAddress.getByName(endereco);
        this.porta = Integer.parseInt(porta);
        this.usuario = usuario;
    }
    
       
    public String conectar() throws IOException, UserException{
        byte[] m = new byte[1000];
        m = ("1#"+this.getUsuario()).getBytes();
        DatagramPacket out = new DatagramPacket(m,m.length,ip,porta);
        socket.send(out);
        
        m = new byte[1000];
        DatagramPacket in = new DatagramPacket(m, m.length);
        try{
            socket.setSoTimeout(5000);
            socket.receive(in);
            String stringData = new String(in.getData()).trim();
            socket.setSoTimeout(0);
            System.out.println(stringData);
            return stringData;
        } catch (SocketTimeoutException e){
            socket.setSoTimeout(0);
            throw new UserException("Falha ao conectar com o servidor!");
        }
    }
    public String desconectar() throws IOException{
        enviarMensagem("5#");
        byte[] m = new byte[1000];
        DatagramPacket in = new DatagramPacket(m, m.length);
        socket.receive(in);
        return new String(in.getData()).trim();
    } 
    public void enviarMensagem(String msg) throws IOException{
        byte[] dados = new byte[1000];
        dados = msg.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados,dados.length,ip,porta);
        socket.send(pacote);
    }
    /**
     * @return the usuario
     */
    public String getUsuario() {
        return usuario;
    }
}

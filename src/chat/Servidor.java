/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import view.ServidorView;
import dominio.Usuario;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class Servidor extends Thread{
    public  ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
    static DatagramSocket serverSocket = null;
    private int porta;
    private ServidorView servidor;
    
    public Servidor(int porta){
        this.porta = porta;
        try {
            serverSocket = new DatagramSocket(porta);
            servidor = new ServidorView(this);
            servidor.setVisible(true);
            servidor.getLblPorta().setText(servidor.getLblPorta().getText()+" "+porta);
            servidor.getTxtLogs().append("Escutando na porta " + porta+"\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao iniciar servidor", "Atenção", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }
    public Servidor(){
    }
    public void run(){
        byte[] mensagem;
        try {
            while (true) {
                mensagem = new byte[1000];
                DatagramPacket pacote = new DatagramPacket(mensagem, mensagem.length);
                serverSocket.receive(pacote);
                lerPacote(pacote);
            }
        } catch (IOException e) {
            System.out.println("Abortando");
        } 
    }
    public void fechar(){
        serverSocket.close();
        this.interrupt();
    }
    private void lerPacote(DatagramPacket pacote) throws IOException{
        String msg = new String(pacote.getData()).trim();
        servidor.getTxtLogs().append("Servidor recebeu: "+msg+"\n");
        String[] ar = msg.split("#");
        switch (ar[0]){
            case "1" : 
                conectar(ar[1],pacote.getAddress(),pacote.getPort());
                break;
            case "3" : 
                enviarMensagem(pacote);
                break;
            case "5" : 
                desconectar(pacote.getAddress(),pacote.getPort());
                break;
            default: 
                servidor.getTxtLogs().append("###############");
                break;
        }
    }
    private void conectar(String usuario, InetAddress ip, int porta) throws IOException{
        /*if(verificarUsuario(usuario)){
            usuarios.add(new Usuario(usuario,ip,porta));
            listarUsuarios();
        } else {
            enviarPrivado(ip,Integer.toString(porta),"9#");
        }*/
        usuarios.add(new Usuario(usuario,ip,porta));
        enviarPrivado(ip, Integer.toString(porta), "1");
        atualizarTabela();
        //listarUsuarios();
    } 
    private boolean verificarUsuario(String usuario){
        return usuarios.stream().noneMatch((u) -> (u.getNome().equals(usuario)));
    }
    private void desconectar(InetAddress ip, int porta) throws IOException{
        System.out.println("asd");
        Usuario aux = null;
        for(Usuario u : usuarios){
            if(ip.equals(u.getIp()) && u.getPorta()==porta){
                aux = u;
                break;
            }
        }
        if(aux!=null){
            usuarios.remove(aux);
        }
        enviarPrivado(ip,String.valueOf(porta),"5");
        atualizarTabela();
    }
    private void enviarMensagem(DatagramPacket pacote) throws IOException {
        String msg = new String(pacote.getData()).trim();
        String[] aux = msg.split("#");
        String saida = "4#"+pacote.getAddress()+"#"+pacote.getPort()+"#"+aux[3];
        if(aux[1].equals("999.999.999.999") && aux[2].equals("99999")){
           enviarTodos(saida);
        } else {
           enviarPrivado(InetAddress.getByName(aux[1].replace("/", "")),aux[2],saida);
           //enviarPrivado(pacote.getAddress(),Integer.toString(pacote.getPort()),saida);
        }
    }
    private void enviarTodos(String msg) throws IOException{
        byte[] dados = new byte[1000];
        dados = msg.getBytes();
        for(Usuario u : usuarios){
            DatagramPacket pacote = new DatagramPacket(dados,dados.length,u.getIp(),u.getPorta());
            serverSocket.send(pacote);
            servidor.getTxtLogs().append("Servidor enviou: "+msg+"\n");
        }
    }
    private void enviarPrivado(InetAddress ip, String porta, String msg) throws UnknownHostException, IOException{
        byte[] dados = new byte[1000];
        dados = msg.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados,dados.length,ip,Integer.parseInt(porta));
        serverSocket.send(pacote);
        servidor.getTxtLogs().append("Servidor enviou: "+msg+"\n");
    }
    private void listarUsuarios() throws IOException{
        String lista = "2#";
        for(Usuario u : usuarios){
            lista += u.getIp() + "#"+u.getPorta()+"#"+u.getNome()+"#";
        }
        enviarTodos(lista);
        atualizarTabela();
    }
    private void atualizarTabela(){
        DefaultTableModel dtmUsuario = (DefaultTableModel) servidor.getJtUsuarios().getModel();
        dtmUsuario.setRowCount(0);
        for(Usuario u : usuarios){
            Object[] dados = {u.getNome(),u.getIp().toString(),Integer.toString(u.getPorta())};
            dtmUsuario.addRow(dados);
        }
    }
    public int getPorta() {
        return porta;
    }
    public void setPorta(int porta) {
        this.porta = porta;
    }
}

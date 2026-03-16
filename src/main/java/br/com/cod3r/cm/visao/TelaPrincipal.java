package br.com.cod3r.cm.visao;

import br.com.cod3r.cm.modelo.Tabuleiro;

import javax.swing.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal(){

        Tabuleiro tabuleiro = new Tabuleiro(16,30,50);
        PainelTabuleiro painelTabuleiro = new PainelTabuleiro(tabuleiro);
        add(painelTabuleiro);
        setTitle("Campo Minado");
        setSize(690,438);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);


    }


    static void main() {
        new TelaPrincipal();
    }


}

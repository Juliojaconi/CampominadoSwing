package br.com.cod3r.cm.visao;

import br.com.cod3r.cm.modelo.Tabuleiro;

import javax.swing.*;
import java.awt.*;

public class PainelTabuleiro extends JPanel {

    public PainelTabuleiro(Tabuleiro tabuleiro){
         setLayout(new GridLayout(tabuleiro.getLinhas(),
                 tabuleiro.getColunas()));

        tabuleiro.paraCada(c -> add(new BotaoCampo(c)));

        tabuleiro.registrarObservador(e -> {
            SwingUtilities.invokeLater(() -> {
            if (e.isGanhou()){
                JOptionPane.showMessageDialog(this, "Parabéns, você ganhou!");
            } else {
                JOptionPane.showMessageDialog(this, "Que pena, você perdeu!");
            }

            tabuleiro.reiniciar();
            });


        });
    }


}

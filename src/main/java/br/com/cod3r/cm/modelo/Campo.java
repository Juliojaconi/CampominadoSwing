package br.com.cod3r.cm.modelo;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Campo {


    private final int linha;

    private final int coluna;

    public boolean isMinado() {
        return minado;
    }

    public void setMinado(boolean minado) {
        this.minado = minado;
    }

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private boolean minado = false;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
     boolean aberto = false;


    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private boolean marcado = false;

    @SuppressWarnings({"FieldMayBeFinal"})

    private List<Campo> vizinhos = new ArrayList<>();

    public List<Campo> getVizinhos() {
        return vizinhos;
    }

    public void setVizinhos(List<Campo> vizinhos) {
        this.vizinhos = vizinhos;
    }

    public boolean isAberto() {
        return aberto;
    }

    public boolean isFechado() {
        return !aberto;
    }

    void setAberto(boolean aberto) {
        this.aberto = aberto;
        if (aberto){
            notificaraObservadores(CampoEvento.ABRIR);
        }
    }


    public boolean isMarcado() {
        return marcado;
    }

    public void setMarcado(boolean marcado) {
        this.marcado = marcado;
    }


    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public Campo(int linha, int coluna){
        this.linha = linha;
        this.coluna = coluna;
    }

    private List<CampoObservador> observadores = new ArrayList<>();
    //private List<BiConsumer<Campo,CampoEvento>> observadores2 = new ArrayList<>();

    public void registrarObservador(CampoObservador observador){
        observadores.add(observador);
    }

    private void notificaraObservadores(CampoEvento evento){
        observadores.stream().forEach(o -> o.eventoOcorreu(this, evento));
    }

    boolean adicionarVizinho(Campo vizinho){

        boolean linhaDiferente = this.linha != vizinho.linha;
        boolean ColunaDiferente = this.coluna != vizinho.coluna;
        boolean diagonal = linhaDiferente && ColunaDiferente;

        int deltaLinha = Math.abs(linha - vizinho.linha);
        int deltaColuna = Math.abs(coluna - vizinho.coluna);
        int deltaGeral = deltaLinha + deltaColuna;

        if (deltaGeral == 1 && !diagonal) {
            vizinhos.add(vizinho);
            return true;
        }else if (deltaGeral == 2 && diagonal) {
            vizinhos.add(vizinho);
            return true;
        }else{
            return false;
        }


        
    }

    public void alterarMarcacao(){
        if (!aberto){
            marcado = !marcado;

            if (marcado){
                notificaraObservadores(CampoEvento.MARCAR);
            }else{
                notificaraObservadores(CampoEvento.DESMARCAR);
            }
        }

    }

    public boolean abrir(){
        if (!aberto && !marcado){

            if(minado){
                notificaraObservadores(CampoEvento.EXPLODIR);
                return true;
            }

            setAberto(true);



            if (vizinhoSeguro()){
                vizinhos.forEach(v -> v.abrir());
            }

            return true;
        }else{
                return false;
        }


    }

    public boolean vizinhoSeguro() {
        return vizinhos.stream().noneMatch(v -> v.minado);
    }

    void minar(){
        minado = true;
    }

    boolean objetivoAlcancado(){
        boolean desvendado = !minado && aberto;
        boolean protegido = minado && marcado;
        return desvendado || protegido;
    }

    public long minasNaVizinhanca(){
        return vizinhos.stream().filter(v -> v.minado).count();
    }

    void reiniciar(){
        aberto = false;
        minado = false;
        marcado = false;

        notificaraObservadores(CampoEvento.REINICIAR);
    }



}

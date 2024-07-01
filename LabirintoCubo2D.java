import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class LabirintoCubo2D extends JPanel {

    private static final int CUBO_SIZE = 20; // Tamanho do cubo
    private static final int ENEMY_SIZE = 20; // Tamanho dos inimigos
    private static final int ARMA_SIZE = 10; // Tamanho da arma
    private static final int ESPADA_SIZE = 30; // Tamanho da espada de curto alcance
    private int cuboX = 50; // Posição inicial X do cubo
    private int cuboY = 50; // Posição inicial Y do cubo
    private int cuboInitX = cuboX; // Posição inicial X do cubo para reiniciar
    private int cuboInitY = cuboY; // Posição inicial Y do cubo para reiniciar

    private boolean jogoGanho = false;
    private boolean jogoPerdido = false;
    private boolean mostrarTutorial = true;
    private boolean temArma = false;
    private boolean temEspada = false;
    private long tempoInicio = System.currentTimeMillis();
    private long duracaoTutorial = 5000; // Duração do tutorial em milissegundos

    // Obstáculos (x, y, largura, altura)
    private Rectangle[] obstaculos = {
            new Rectangle(100, 100, 50, 50),
            new Rectangle(200, 150, 50, 50),
            new Rectangle(300, 200, 50, 50),
            new Rectangle(400, 250, 50, 50),
            new Rectangle(250, 300, 50, 50), // Obstáculo que reinicia o cubo
            new Rectangle(150, 350, 50, 50),  // Obstáculo que bloqueia o cubo
            new Rectangle(500, 400, 50, 50),
            new Rectangle(600, 450, 50, 50),
            new Rectangle(700, 300, 50, 50)
    };

    // Linha de chegada (x, y, largura, altura)
    private Rectangle linhaDeChegada = new Rectangle(900, 600, 50, 50);

    // Inimigos (x, y)
    private ArrayList<Rectangle> inimigos = new ArrayList<>();
    private ArrayList<Integer> direcoesInimigos = new ArrayList<>();

    // Arma (no mapa)
    private Rectangle armaNoMapa = new Rectangle(350, 350, CUBO_SIZE, CUBO_SIZE);
    private Rectangle espadaNoMapa = new Rectangle(450, 450, CUBO_SIZE, CUBO_SIZE);

    // Projétil
    private ArrayList<Rectangle> projeteis = new ArrayList<>();
    private ArrayList<Integer> direcoesProjeteis = new ArrayList<>();

    private Timer timer;

    public LabirintoCubo2D() {
        setPreferredSize(new Dimension(1000, 800));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (jogoGanho || jogoPerdido) return; // Não permitir movimento se o jogo foi ganho ou perdido

                int prevX = cuboX;
                int prevY = cuboY;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        cuboY -= 10;
                        break;
                    case KeyEvent.VK_S:
                        cuboY += 10;
                        break;
                    case KeyEvent.VK_A:
                        cuboX -= 10;
                        break;
                    case KeyEvent.VK_D:
                        cuboX += 10;
                        break;
                    case KeyEvent.VK_UP:
                        if (temArma) {
                            projeteis.add(new Rectangle(cuboX + CUBO_SIZE / 2 - ARMA_SIZE / 2, cuboY, ARMA_SIZE, ARMA_SIZE));
                            direcoesProjeteis.add(0);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (temArma) {
                            projeteis.add(new Rectangle(cuboX + CUBO_SIZE / 2 - ARMA_SIZE / 2, cuboY + CUBO_SIZE, ARMA_SIZE, ARMA_SIZE));
                            direcoesProjeteis.add(2);
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        if (temArma) {
                            projeteis.add(new Rectangle(cuboX, cuboY + CUBO_SIZE / 2 - ARMA_SIZE / 2, ARMA_SIZE, ARMA_SIZE));
                            direcoesProjeteis.add(1);
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (temArma) {
                            projeteis.add(new Rectangle(cuboX + CUBO_SIZE, cuboY + CUBO_SIZE / 2 - ARMA_SIZE / 2, ARMA_SIZE, ARMA_SIZE));
                            direcoesProjeteis.add(3);
                        }
                        break;
                    case KeyEvent.VK_E:
                        if (temEspada) {
                            usarEspada();
                        }
                        break;
                    case KeyEvent.VK_T:
                        if (jogoGanho || jogoPerdido) {
                            reiniciarJogo();
                        }
                        break;
                }

                // Verifica colisões com os obstáculos
                for (Rectangle obstaculo : obstaculos) {
                    if (new Rectangle(cuboX, cuboY, CUBO_SIZE, CUBO_SIZE).intersects(obstaculo)) {
                        if (obstaculo.equals(obstaculos[4])) { // Obstáculo que reinicia o cubo
                            cuboX = cuboInitX;
                            cuboY = cuboInitY;
                        } else { // Obstáculo que bloqueia o cubo
                            cuboX = prevX;
                            cuboY = prevY;
                        }
                    }
                }

                // Verifica colisões com os inimigos
                for (Rectangle inimigo : inimigos) {
                    if (new Rectangle(cuboX, cuboY, CUBO_SIZE, CUBO_SIZE).intersects(inimigo)) {
                        jogoPerdido = true;
                    }
                }

                // Verifica se o cubo chegou à linha de chegada
                if (new Rectangle(cuboX, cuboY, CUBO_SIZE, CUBO_SIZE).intersects(linhaDeChegada)) {
                    jogoGanho = true;
                }

                // Verifica se o cubo coletou a arma
                if (armaNoMapa != null && new Rectangle(cuboX, cuboY, CUBO_SIZE, CUBO_SIZE).intersects(armaNoMapa)) {
                    temArma = true;
                    armaNoMapa = null; // Remove a arma do mapa
                }

                // Verifica se o cubo coletou a espada
                if (espadaNoMapa != null && new Rectangle(cuboX, cuboY, CUBO_SIZE, CUBO_SIZE).intersects(espadaNoMapa)) {
                    temEspada = true;
                    espadaNoMapa = null; // Remove a espada do mapa
                }

                repaint();
            }
        });

        // Adiciona inimigos
        adicionarInimigo(150, 100);
        adicionarInimigo(300, 400);
        adicionarInimigo(500, 300);
        adicionarInimigo(700, 500);
        adicionarInimigo(800, 600);

        // Configura o timer para movimentar os inimigos
        timer = new Timer(100, e -> moverInimigos());
        timer.start();
    }

    private void adicionarInimigo(int x, int y) {
        inimigos.add(new Rectangle(x, y, ENEMY_SIZE, ENEMY_SIZE));
        direcoesInimigos.add(new Random().nextInt(4)); // Direção inicial aleatória
    }

    private void moverInimigos() {
        for (int i = 0; i < inimigos.size(); i++) {
            Rectangle inimigo = inimigos.get(i);
            int direcao = direcoesInimigos.get(i);
            switch (direcao) {
                case 0:
                    inimigo.y -= 10; // Cima
                    break;
                case 1:
                    inimigo.x -= 10; // Esquerda
                    break;
                case 2:
                    inimigo.y += 10; // Baixo
                    break;
                case 3:
                    inimigo.x += 10; // Direita
                    break;
            }
            // Verifica colisões com os obstáculos e paredes
            boolean colidiu = false;
            for (Rectangle obstaculo : obstaculos) {
                if (inimigo.intersects(obstaculo)) {
                    colidiu = true;
                    break;
                }
            }
            if (inimigo.x < 0 || inimigo.x + ENEMY_SIZE > getWidth() || inimigo.y < 0 || inimigo.y + ENEMY_SIZE > getHeight()) {
                colidiu = true;
            }
            if (colidiu) {
                direcao = new Random().nextInt(4);
                direcoesInimigos.set(i, direcao);
            }
        }
        repaint();
    }

    private void usarEspada() {
        Rectangle espada = new Rectangle(cuboX - ESPADA_SIZE / 2 + CUBO_SIZE / 2, cuboY - ESPADA_SIZE / 2 + CUBO_SIZE / 2, ESPADA_SIZE, ESPADA_SIZE);
        for (int i = 0; i < inimigos.size(); i++) {
            if (espada.intersects(inimigos.get(i))) {
                inimigos.remove(i);
                direcoesInimigos.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);

        if (jogoGanho) {
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("Você Ganhou!", 200, 300);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Pressione T para reiniciar", 250, 350);
            return;
        }

        if (jogoPerdido) {
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("Você Perdeu!", 200, 300);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Pressione T para reiniciar", 250, 350);
            return;
        }

        // Desenha o cubo
        g2d.fillRect(cuboX, cuboY, CUBO_SIZE, CUBO_SIZE);

        // Desenha os obstáculos
        g2d.setColor(Color.RED);
        for (int i = 0; i < obstaculos.length; i++) {
            g2d.fill(obstaculos[i]);
        }

        // Destacar obstáculo que reinicia o cubo
        g2d.setColor(Color.BLUE);
        g2d.fill(obstaculos[4]);

        // Desenha a linha de chegada
        g2d.setColor(Color.GREEN);
        g2d.fill(linhaDeChegada);

        // Desenha os inimigos
        g2d.setColor(Color.MAGENTA);
        for (Rectangle inimigo : inimigos) {
            g2d.fill(inimigo);
        }

        // Desenha a arma no mapa
        if (armaNoMapa != null) {
            g2d.setColor(Color.ORANGE);
            g2d.fill(armaNoMapa);
        }

        // Desenha a espada no mapa
        if (espadaNoMapa != null) {
            g2d.setColor(Color.CYAN);
            g2d.fill(espadaNoMapa);
        }

        // Desenha os projéteis
        g2d.setColor(Color.YELLOW);
        for (Rectangle projetei : projeteis) {
            g2d.fill(projetei);
        }

        // Movimento dos projéteis
        for (int i = 0; i < projeteis.size(); i++) {
            Rectangle projetei = projeteis.get(i);
            int direcao = direcoesProjeteis.get(i);
            switch (direcao) {
                case 0:
                    projetei.y -= 10; // Cima
                    break;
                case 1:
                    projetei.x -= 10; // Esquerda
                    break;
                case 2:
                    projetei.y += 10; // Baixo
                    break;
                case 3:
                    projetei.x += 10; // Direita
                    break;
            }
            // Remove projéteis que saem da tela
            if (projetei.x < 0 || projetei.x > getWidth() || projetei.y < 0 || projetei.y > getHeight()) {
                projeteis.remove(i);
                direcoesProjeteis.remove(i);
                i--;
            }
        }

        // Verifica se algum projétil colidiu com algum inimigo
        for (int i = 0; i < projeteis.size(); i++) {
            Rectangle projetei = projeteis.get(i);
            for (int j = 0; j < inimigos.size(); j++) {
                if (projetei.intersects(inimigos.get(j))) {
                    inimigos.remove(j);
                    projeteis.remove(i);
                    direcoesProjeteis.remove(i);
                    i--;
                    break;
                }
            }
        }

        // Desenha o tutorial
        if (mostrarTutorial) {
            long tempoAtual = System.currentTimeMillis();
            if (tempoAtual - tempoInicio < duracaoTutorial) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("Use W, A, S, D para mover", 50, 50);
                g2d.drawString("Use as setas para disparar", 50, 80);
                g2d.drawString("Pressione E para usar espada", 50, 110);
                g2d.drawString("Pressione T para reiniciar", 50, 140);
            } else {
                mostrarTutorial = false;
            }
        }
    }

    private void reiniciarJogo() {
        cuboX = cuboInitX;
        cuboY = cuboInitY;
        jogoGanho = false;
        jogoPerdido = false;
        mostrarTutorial = true;
        temArma = false;
        temEspada = false;
        tempoInicio = System.currentTimeMillis();
        projeteis.clear();
        direcoesProjeteis.clear();
        armaNoMapa = new Rectangle(350, 350, CUBO_SIZE, CUBO_SIZE);
        espadaNoMapa = new Rectangle(450, 450, CUBO_SIZE, CUBO_SIZE);
        inimigos.clear();
        direcoesInimigos.clear();
        adicionarInimigo(150, 100);
        adicionarInimigo(300, 400);
        adicionarInimigo(500, 300);
        adicionarInimigo(700, 500);
        adicionarInimigo(800, 600);
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Labirinto Cubo 2D");
        LabirintoCubo2D labirintoCubo = new LabirintoCubo2D();
        frame.add(labirintoCubo);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

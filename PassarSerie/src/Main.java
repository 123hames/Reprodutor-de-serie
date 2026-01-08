import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private static String baseUrl = "";
    private static int temporada = 1;
    private static int episodio = 1;
    private static String modo = "dub"; 
   
    private static int idSerie = 1;
    private static String nomeSerie = "";
    private static String estadoSerie = "Em curso";
    private static boolean emVisualizacao = false;
    
    private static final String userHome = System.getProperty("user.home");
    private static final File pastaBase = new File(userHome + File.separator + "Documents" + File.separator + "Reprodutor de serie");
    private static final File saveFile = new File(pastaBase, "serie.txt");
    private static final File configFile = new File(pastaBase, "config.txt");
    private static final File redirectFile = new File(pastaBase,"redirect.html");

    public static void main(String[] args) {
    	
        verificaFicheiros();
        boolean encontrouSerie = false;

        if (args != null && args.length > 0) {
            try {
                int idRecebidoSerie = Integer.parseInt(args[0]);
                carregarDados(idRecebidoSerie);
                encontrouSerie = true;
            } catch (NumberFormatException e) {
                System.err.println("ID inválido recebido!");
            }
        } else {
            int idAtivo = buscarIdEmVisualizacao();
            if (idAtivo != -1) {
                carregarDados(idAtivo);
                encontrouSerie = true;
            }
        }

        JFrame frame = new JFrame("Navegador de Episódios");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(12, 40, 12, 40); // Margens laterais generosas
        gbc.weightx = 1;
        gbc.weighty = 0.1;

        JLabel info = new JLabel("", SwingConstants.CENTER);
        info.setFont(new Font("Arial", Font.BOLD, 28)); // Aumentei a fonte do título
        atualizarLabel(info);

        JButton abrirAtual = new JButton("Abrir Episódio Atual");
        JButton anterior = new JButton("Ver anterior");
        JButton proximo = new JButton("Ver próximo");
        JButton voltarEP = new JButton("Voltar Episodio(sem abrir navegador)");
        JButton passarEp = new JButton("Passar Episodio(sem abrir navegador)");
        JButton definirLink = new JButton("Definir Link da Série");
        JButton inserirEpisodio = new JButton("Inserir Episódio Manualmente");

        
        JButton btnVoltarLista = new JButton("");
        btnVoltarLista.setContentAreaFilled(false);
        btnVoltarLista.setBorderPainted(false);
        btnVoltarLista.setFocusPainted(false);
        btnVoltarLista.setForeground(Color.BLUE);
        btnVoltarLista.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVoltarLista.setFont(new Font("Arial", Font.BOLD, 16)); // Fonte de voltar maior
        /*btnVoltarLista.addActionListener(e -> { 
            Listagem_series.main(new String[0]); 
            frame.dispose(); 
        });*/

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(btnVoltarLista, BorderLayout.WEST);
        topo.add(info, BorderLayout.CENTER);

        JButton[] botoes = {abrirAtual, anterior, proximo, voltarEP, passarEp, definirLink, inserirEpisodio};
        // Cores originais mantidas exatamente como pediste
        Color[] cores = {
            new Color(200, 200, 255),
            new Color(255, 100, 100),	
            new Color(100, 255, 100),
            new Color(255, 100, 100),
            new Color(100, 255, 100),
            new Color(100, 100, 255),
            new Color(255, 255, 100)
        };

        for (int i = 0; i < botoes.length; i++) {
            botoes[i].setFocusPainted(false);
            botoes[i].setBackground(cores[i]);
            botoes[i].setPreferredSize(new Dimension(200, 75)); // Altura dos botões aumentada para escala maior
            botoes[i].setFont(new Font("Arial", Font.BOLD, 20)); // Fonte dos botões aumentada
            botoes[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        // --- AÇÕES ---
        abrirAtual.addActionListener(e -> abrirNavegador());
        
        anterior.addActionListener(e -> { 
            if (episodio > 1) episodio--; 
            else if (temporada > 1) { 
                temporada--; episodio = 1; 
                while (verificarEpisodioExistente(temporada, episodio + 1)) episodio++; 
            } 
            salvarDados(); atualizarLabel(info); abrirNavegador(); 
        });

        proximo.addActionListener(e -> { 
            if (verificarEpisodioExistente(temporada, episodio + 1)) {
            	episodio++; 
            	abrirNavegador();
            }
            else if (verificarEpisodioExistente(temporada + 1, 1)) { 
            	temporada++; episodio = 1; 
            	abrirNavegador();
            } 
            else JOptionPane.showMessageDialog(frame, "Fim da série!"); 
            salvarDados();
            atualizarLabel(info); 
             
        });

        passarEp.addActionListener(e -> { 
            if (verificarEpisodioExistente(temporada, episodio + 1)) episodio++; 
            else if (verificarEpisodioExistente(temporada + 1, 1)) { temporada++; episodio = 1; } 
            salvarDados(); atualizarLabel(info); 
        });

        voltarEP.addActionListener(e -> { 
            if (episodio > 1) episodio--; 
            else if (temporada > 1) { temporada--; episodio = 1; while (verificarEpisodioExistente(temporada, episodio + 1)) episodio++; } 
            salvarDados(); atualizarLabel(info); 
        });

        definirLink.addActionListener(e -> {
            JTextField campoNome = new JTextField(nomeSerie, 20);
            JTextField campoLink = new JTextField(baseUrl, 20);
            JPanel painelInput = new JPanel();
            painelInput.setLayout(new BoxLayout(painelInput, BoxLayout.Y_AXIS));
            painelInput.add(new JLabel("Nome da Série:"));
            painelInput.add(campoNome);
            painelInput.add(Box.createVerticalStrut(10));
            painelInput.add(new JLabel("Link da Série:"));
            painelInput.add(campoLink);
            JLabel instrucao = new JLabel("(Use {t} para temporada e {e} para episódio)");
            instrucao.setFont(new Font("Arial", Font.ITALIC, 11));
            instrucao.setForeground(Color.GRAY);
            painelInput.add(instrucao);
            if (JOptionPane.showConfirmDialog(frame, painelInput, "Configurar Série", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (!campoNome.getText().trim().isEmpty() && !campoLink.getText().trim().isEmpty()) {
                    nomeSerie = campoNome.getText().trim();
                    baseUrl = campoLink.getText().trim();
                    salvarDados();
                    atualizarLabel(info);
                }
            }
        });

        inserirEpisodio.addActionListener(e -> {
            JTextField campoTemporada = new JTextField(String.valueOf(temporada), 10);
            JTextField campoEpisodio = new JTextField(String.valueOf(episodio), 10);
            JPanel painelInput = new JPanel();
            painelInput.setLayout(new BoxLayout(painelInput, BoxLayout.Y_AXIS));
            painelInput.add(new JLabel("Número de temporada:"));
            painelInput.add(campoTemporada);
            painelInput.add(Box.createVerticalStrut(10));
            painelInput.add(new JLabel("Número de episódio:"));
            painelInput.add(campoEpisodio);
            if (JOptionPane.showConfirmDialog(frame, painelInput, "Inserir Episódio", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int campoTemporadaRecebido = Integer.parseInt(campoTemporada.getText().trim());
                    int campoEpisodioRecebido = Integer.parseInt(campoEpisodio.getText().trim());
                    if(verificarEpisodioExistente(campoTemporadaRecebido,campoEpisodioRecebido)) {
                    	temporada = campoTemporadaRecebido;
                    	episodio = campoEpisodioRecebido;
                    	salvarDados();
                        atualizarLabel(info);
                    }else {
                    	JOptionPane.showMessageDialog(frame, "Não existe esse episodio");
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Insira apenas números!");
                }
            }
        });

     // --- MONTAGEM DO GRID ---
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2; 
        gbc.weighty = 0.2; 
        mainPanel.add(topo, gbc);

        // Configuração para o botão "Abrir Atual"
        gbc.gridy++; 
        gbc.weighty = 0.15;
        // Insets(topo, esquerda, baixo, direita) -> Aumentei o 'baixo' para 60
        gbc.insets = new Insets(12, 40, 60, 40); 
        mainPanel.add(abrirAtual, gbc);

        // Restaurar insets padrão para os restantes botões (baixo volta a 12)
        gbc.insets = new Insets(12, 40, 12, 40); 
        gbc.gridwidth = 1;
        
        gbc.gridy++; gbc.gridx = 0; mainPanel.add(anterior, gbc);
        gbc.gridx = 1; mainPanel.add(proximo, gbc);

        gbc.gridy++; gbc.gridx = 0; mainPanel.add(voltarEP, gbc);
        gbc.gridx = 1; mainPanel.add(passarEp, gbc);

        gbc.gridy++; gbc.gridx = 0; mainPanel.add(definirLink, gbc);
        gbc.gridx = 1; mainPanel.add(inserirEpisodio, gbc);

        // Espaçador no fundo para manter tudo no topo
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weighty = 1.0; 
        mainPanel.add(new JLabel(""), gbc);

        frame.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        frame.setVisible(true);
        
        if (!encontrouSerie) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "Sem nenhuma série em visualização.");
            });
        }
    }

    private static void atualizarLabel(JLabel label) {
        label.setText("Temporada: " + temporada + " | Episódio: " + episodio + " | Serie: " + nomeSerie);
    }

    private static void salvarDados() {
        if (!saveFile.exists()) return;
        List<String> todasAsLinhas = new ArrayList<>();
        boolean serieEncontrada = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";");
                if (Integer.parseInt(dados[0]) == idSerie) {
                    todasAsLinhas.add(idSerie + ";" + nomeSerie + ";" + baseUrl + ";" + temporada + ";" + episodio + ";" + estadoSerie + ";" + emVisualizacao);
                    serieEncontrada = true;
                } else todasAsLinhas.add(linha);
            }
        } catch (IOException e) { e.printStackTrace(); }
        if (!serieEncontrada) todasAsLinhas.add(idSerie + ";" + nomeSerie + ";" + baseUrl + ";" + temporada + ";" + episodio + ";" + estadoSerie + ";" + emVisualizacao);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            for (String l : todasAsLinhas) { writer.write(l); writer.newLine(); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void carregarDados(int idPesquisado) {
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";");
                if (Integer.parseInt(dados[0]) == idPesquisado) {
                    idSerie = idPesquisado; nomeSerie = dados[1]; baseUrl = dados[2];
                    temporada = Integer.parseInt(dados[3]); episodio = Integer.parseInt(dados[4]);
                    estadoSerie = dados[5]; emVisualizacao = Boolean.parseBoolean(dados[6]);
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static int buscarIdEmVisualizacao() {
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";");
                if (dados.length > 6 && Boolean.parseBoolean(dados[6])) return Integer.parseInt(dados[0]);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    private static void abrirNavegador() {
        if (baseUrl.isEmpty()) return;
        
        // Substituição direta das tags
        String url = baseUrl.replace("{t}", String.valueOf(temporada))
                             .replace("{e}", String.valueOf(episodio));
        
        gerarHTMLRedirect(url);
        try { 
            Desktop.getDesktop().browse(redirectFile.toURI()); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }
    private static void gerarHTMLRedirect(String url) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(redirectFile))) {
            writer.write("<html><head><meta http-equiv='refresh' content='0; url=" + url + "' /></head><body></body></html>");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static boolean verificarEpisodioExistente(int t, int e) {
        // Se o link não for dinâmico (não tiver {t}), não há o que verificar, assume-se que existe
        if (!baseUrl.contains("{t}")) return true;

        String url = baseUrl.replace("{t}", String.valueOf(t))
                             .replace("{e}", String.valueOf(e));
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(2000);
            c.connect();
            return c.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception ex) { 
            return false; 
        }
    }

    
    private static void verificaFicheiros() {
        try {
            if (!pastaBase.exists()) pastaBase.mkdirs();
            if (!saveFile.exists()) saveFile.createNewFile();
            if (!configFile.exists()) {
                try (BufferedWriter w = new BufferedWriter(new FileWriter(configFile))) { w.write("branco\n"); }
            }
            if (!redirectFile.exists()) redirectFile.createNewFile();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
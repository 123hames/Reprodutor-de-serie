import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    private static String baseUrl = "";
    private static int temporada = 1;
    private static int episodio = 1;
    private static String modo = "dub"; // pode ser "dub" ou "sub"
   
    
    //defenição das pastas e ficheiro necessarios
    private static final String userHome = System.getProperty("user.home");
    private static final File pastaBase = new File(userHome + File.separator + "Documents" + File.separator + "Reprodutor de serie");
    private static final File saveFile = new File(pastaBase, "serie.txt");
    private static final File configFile = new File(pastaBase, "config.txt");
    private static final File redirectFile = new File(pastaBase,"redirect.html");


    public static void main(String[] args) {
    	verificaFicheiros();
        carregarDados();
        

        JFrame frame = new JFrame("Navegador de Episódios");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Faz abrir sempre em tela cheia
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true); // opcional, sem barra de título

        frame.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(1200, 800));

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1;
        gbc.weighty = 0.1;

        JLabel info = new JLabel("", SwingConstants.CENTER);
        info.setFont(new Font("Arial", Font.BOLD, 24));
        atualizarLabel(info);

        JTextField inputTemporada = new JTextField(String.valueOf(temporada));
        JTextField inputEpisodio = new JTextField(String.valueOf(episodio));
        JTextField inputLink = new JTextField(baseUrl);
 
        JButton botaoFechar = new JButton("X");
        botaoFechar.setPreferredSize(new Dimension(80, 30));
        botaoFechar.setFont(new Font("Arial", Font.BOLD, 18));
        botaoFechar.setForeground(Color.RED);
        botaoFechar.setFocusPainted(false);
        botaoFechar.setContentAreaFilled(false);
        botaoFechar.setBorderPainted(false);
        botaoFechar.setOpaque(false);
        botaoFechar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botaoFechar.addActionListener(e -> System.exit(0));

        JButton botaoMinimizar = new JButton("_");
        botaoMinimizar.setPreferredSize(new Dimension(45, 18));
        botaoMinimizar.setFont(new Font("Arial", Font.BOLD, 18));
        botaoMinimizar.setForeground(Color.BLACK);
        botaoMinimizar.setFocusPainted(false);
        botaoMinimizar.setContentAreaFilled(false);
        botaoMinimizar.setBorderPainted(false);
        botaoMinimizar.setOpaque(false);
        botaoMinimizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botaoMinimizar.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        

        
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelTopo.setOpaque(false); // deixa transparente

        painelTopo.add(botaoMinimizar);
        painelTopo.add(botaoFechar);
        frame.add(painelTopo, BorderLayout.NORTH);
        
        //JButton fecharPrograma = new JButton("X");
        JButton abrirAtual = new JButton("Abrir Episódio Atual");
        JButton anterior = new JButton("Ver anterior");
        JButton proximo = new JButton("Ver próximo");
        JButton voltarEP = new JButton("Voltar Episodio(sem abrir navegador)");
        JButton passarEp = new JButton("Passar Episodio(sem abrir navegador)");
        JButton definirLink = new JButton("Definir Link da Série");
        JButton inserirEpisodio = new JButton("Inserir Episódio Manualmente");

        JButton[] botoes = {abrirAtual, anterior, proximo, voltarEP, passarEp, definirLink, inserirEpisodio};
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
            JButton btn = botoes[i];
            btn.setFocusPainted(false);
            btn.setBackground(cores[i]);
            btn.setPreferredSize(new Dimension(200, 50));
            btn.setFont(new Font("Arial", Font.PLAIN, 18));
        }

        abrirAtual.addActionListener(e -> abrirNavegador());

        anterior.addActionListener(e -> {
            if (episodio > 1) {
                episodio--;
            } else if (temporada > 1) {
                temporada--;
                int ep = 1;
                while (true) {
                	
                    if (!verificarEpisodioExistente(temporada, ep)) {
                        episodio = ep - 1;  // último episódio existente
                        if (episodio < 1) episodio = 1; // caso não encontre nenhum
                        break;
                    }
                    ep++;
                }
                
            
            }
            salvarDados();
            atualizarLabel(info);
            abrirNavegador();
        });

        Action avancarEpisodioAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (verificarEpisodioExistente(temporada, episodio + 1)) {
                    episodio++;
                } else {
                   

                    // Verifica se existe episódio 1 da próxima temporada
                    int proxTemporada = temporada + 1;
                    if (verificarEpisodioExistente(proxTemporada, 1)) {
                    	int totalEp = episodio;
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(frame, "Fim da temporada " + temporada + " com " + totalEp + " episódios")
                        );
                        temporada = proxTemporada;
                        episodio = 1;
                        
                    } else {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(frame, "Não existem mais episódios disponíveis!")
                        );
                    }
                }
                salvarDados();
                atualizarLabel(info);
                abrirNavegador();
                
            }
        };


        // Associa a ação ao botão
        proximo.addActionListener(avancarEpisodioAction);
        
        Action passarEpAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (verificarEpisodioExistente(temporada, episodio + 1)) {
                    episodio++;
                } else {

                    // Verifica se existe episódio 1 da próxima temporada
                    int proxTemporada = temporada + 1;
                    if (verificarEpisodioExistente(proxTemporada, 1)) {
                    	int totalEp = episodio;
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(frame, "Fim da temporada " + temporada + " com " + totalEp + " episódios")
                        );
                        temporada = proxTemporada;
                        episodio = 1;
                       
                    } else {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(frame, "Não existem mais episódios disponíveis!")
                        );
                    }
                }
                salvarDados();
                atualizarLabel(info);
            }
        };
        
        //açao de apenas passar ep,sem abrir no navegador
        passarEp.addActionListener(passarEpAction);
        
        
        Action voltarEPAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (episodio > 1) {
                    episodio--;
                } else if (temporada > 1) {
                    temporada--;
                    int ep = 1;
                    while (true) {
                        if (!verificarEpisodioExistente(temporada, ep)) {
                            episodio = ep - 1;  // último episódio existente
                            if (episodio < 1) episodio = 1; // caso não encontre nenhum
                            break;
                        }
                        ep++;
                    }
                }
                salvarDados();
                atualizarLabel(info);
            }
        };
        
        //açao de apenas passar ep,sem abrir no navegador
        voltarEP.addActionListener(voltarEPAction);

        // Associa a mesma ação a uma tecla (Ctrl + Seta Direita)
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(keyStroke, "avancarEpisodio");
        actionMap.put("avancarEpisodio", avancarEpisodioAction);
        

        definirLink.addActionListener(e -> {
            baseUrl = JOptionPane.showInputDialog("Cole o link base da série:");
            temporada = 1;
            episodio = 1;
            salvarDados();
            atualizarLabel(info);
            abrirNavegador();
        });

        inserirEpisodio.addActionListener(e -> {
            try {
                int temporadaInserido = Integer.parseInt(inputTemporada.getText());
                int episodioInserido = Integer.parseInt(inputEpisodio.getText());
                baseUrl = inputLink.getText();
                
                if (verificarEpisodioExistente(temporadaInserido, episodioInserido)) {
                    temporada = temporadaInserido;
                    episodio = episodioInserido;
                } else {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(frame, "Não existe este episódio inserido")
                    );
                }
                
                salvarDados();
                atualizarLabel(info);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Por favor, insira números válidos.");
            }
        });


        // Adicionando os componentes ao layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; mainPanel.add(info, gbc);
        gbc.gridy++; mainPanel.add(abrirAtual, gbc);
        gbc.gridy++; gbc.gridwidth = 1; mainPanel.add(new JLabel("Link da Série:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; mainPanel.add(inputLink, gbc);
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; mainPanel.add(new JLabel("Temporada:"), gbc);
        gbc.gridx = 1; mainPanel.add(inputTemporada, gbc);
        gbc.gridy++; gbc.gridx = 0; mainPanel.add(new JLabel("Episódio:"), gbc);
        gbc.gridx = 1; mainPanel.add(inputEpisodio, gbc);
        gbc.gridy++; gbc.gridx = 0; mainPanel.add(anterior, gbc);
        gbc.gridx = 1; mainPanel.add(proximo, gbc);
        gbc.gridy++;
        gbc.gridx = 0; mainPanel.add(voltarEP, gbc);
        gbc.gridx = 1; mainPanel.add(passarEp, gbc);
        
        gbc.gridy++; gbc.gridx = 0; mainPanel.add(definirLink, gbc);
        gbc.gridx = 1; mainPanel.add(inserirEpisodio, gbc);

        frame.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    
    private static void atualizarLabel(JLabel label) {
    	
        label.setText("Temporada: " + temporada + " | Episódio: " + episodio + " | Link base: " + baseUrl);
    }

    private static void salvarDados() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            writer.write(baseUrl + "\n" + temporada + "\n" + episodio + "\n" + modo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarDados() {
        if (!saveFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String linhaBaseUrl = reader.readLine();
            if (linhaBaseUrl != null) baseUrl = linhaBaseUrl;

            String linhaTemporada = reader.readLine();
            if (linhaTemporada != null) temporada = Integer.parseInt(linhaTemporada);

            String linhaEpisodio = reader.readLine();
            if (linhaEpisodio != null) episodio = Integer.parseInt(linhaEpisodio);

            String linhaModo = reader.readLine();
            if (linhaModo != null) modo = linhaModo;

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }


    
   
    
    

    private static void abrirNavegador() {
        if (baseUrl.isEmpty()) return;
        String fullUrl = baseUrl.replaceAll("/\\d+/\\d+/(dub|sub)", "") + "/" + temporada + "/" + episodio + "/" + modo;
        gerarHTMLRedirect(fullUrl);
        try {
            Desktop.getDesktop().browse(redirectFile.toURI()); // usa redirectFile
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void gerarHTMLRedirect(String url) {
        String htmlContent = "<html><head><meta http-equiv='refresh' content='0; url=" + url + "' /></head><body></body></html>";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(redirectFile))) { // usa redirectFile
            writer.write(htmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean verificarEpisodioExistente(int temporada, int episodio) {
        String url = baseUrl.replaceAll("/\\d+/\\d+/(dub|sub)", "") + "/" + temporada + "/" + episodio + "/" + modo;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            System.err.println("Erro ao verificar episódio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private static void verificaFicheiros() {
        try {
            if (!pastaBase.exists()) {
                if (pastaBase.mkdirs()) {
                    System.out.println("Pasta criada: " + pastaBase.getAbsolutePath());
                }
            }

            if (!saveFile.exists()) {
                saveFile.createNewFile();
                System.out.println("Arquivo criado: " + saveFile.getAbsolutePath());
            }

            if (!configFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                    writer.write("branco\n"); // tema padrão
                }
                System.out.println("Arquivo criado: " + configFile.getAbsolutePath());
            }
            
            if (!redirectFile.exists()) {
                redirectFile.createNewFile();
                System.out.println("Arquivo criado: " + redirectFile.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
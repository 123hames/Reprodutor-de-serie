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
    private static final File saveFile = new File("serie.txt");

    public static void main(String[] args) {
        carregarDados();

        JFrame frame = new JFrame("Navegador de Episódios");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setMaximumSize(new Dimension(1920, 1080));
        frame.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(1200, 800));
        mainPanel.setMaximumSize(new Dimension(1920, 1080));

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

        JButton abrirAtual = new JButton("Abrir Episódio Atual");
        JButton anterior = new JButton("Anterior");
        JButton proximo = new JButton("Próximo");
        JButton definirLink = new JButton("Definir Link da Série");
        JButton inserirEpisodio = new JButton("Inserir Episódio Manualmente");

        JButton[] botoes = {abrirAtual, anterior, proximo, definirLink, inserirEpisodio};
        Color[] cores = {
            new Color(200, 200, 255),
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
                episodio = 1;
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
                    int totalEp = episodio;
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(frame, "Fim da temporada " + temporada + " com " + totalEp + " episódios")
                    );
                    temporada++;
                    episodio = 1;
                }
                salvarDados();
                atualizarLabel(info);
                abrirNavegador();
            }
        };

        // Associa a ação ao botão
        proximo.addActionListener(avancarEpisodioAction);

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
                temporada = Integer.parseInt(inputTemporada.getText());
                episodio = Integer.parseInt(inputEpisodio.getText());
                baseUrl = inputLink.getText();
                salvarDados();
                atualizarLabel(info);
                abrirNavegador();
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
            baseUrl = reader.readLine();
            temporada = Integer.parseInt(reader.readLine());
            episodio = Integer.parseInt(reader.readLine());
            modo = reader.readLine();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static void abrirNavegador() {
        if (baseUrl.isEmpty()) return;
        String fullUrl = baseUrl.replaceAll("/\\d+/\\d+/(dub|sub)", "") + "/" + temporada + "/" + episodio + "/" + modo;
        gerarHTMLRedirect(fullUrl);
        try {
            File htmlFile = new File("redirect.html");
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void gerarHTMLRedirect(String url) {
        String htmlContent = "<html><head><meta http-equiv='refresh' content='0; url=" + url + "' /></head><body></body></html>";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("redirect.html"))) {
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
}
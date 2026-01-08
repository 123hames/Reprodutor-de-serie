import java.io.File;
import javax.swing.JOptionPane;

public class Desinstalar {

    private static final String userHome = System.getProperty("user.home");
    private static final File pastaBase = new File(userHome + File.separator + "Documents" 
                                                  + File.separator + "Reprodutor de serie");
    private static final File saveFile = new File(pastaBase, "serie.txt");
    private static final File configFile = new File(pastaBase, "config.txt");

    public static void main(String[] args) {
        // OpÃ§Ãµes personalizadas
        Object[] options = {"Sim", "Não"};
        int confirm = JOptionPane.showOptionDialog(
                null,
                "Tem certeza que deseja desinstalar o aplicativo?",
                "Desinstalar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1] 
        );

        if (confirm != JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null, "Desinstalação cancelada!");
            return;
        }

        // Tenta apagar a pasta inteira
        boolean sucesso = apagarPastaRecursivamente(pastaBase);

        if (sucesso) {
            JOptionPane.showMessageDialog(null, "Aplicativo desinstalado com sucesso!");
        } else {
            JOptionPane.showMessageDialog(null, "Erro ao desinstalar o aplicativo.");
        }
    }

    // MÃ©todo recursivo para apagar pasta e todos os arquivos dentro dela
    private static boolean apagarPastaRecursivamente(File pasta) {
        File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.isDirectory()) {
                    apagarPastaRecursivamente(arquivo);
                } else {
                    arquivo.delete();
                }
            }
        }
        return pasta.delete();
    }
}

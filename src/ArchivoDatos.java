import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class ArchivoDatos {
    private String nombreArchivo;
    public ArchivoDatos(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
    public void guardar(String tipo, String datos) {
        try {
            PrintWriter escritor = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(nombreArchivo, true), StandardCharsets.UTF_8));
            escritor.println(tipo + ";" + datos);
            escritor.close();
        } catch (IOException e) {
            System.out.println("No se pudieron guardar los datos");
        }
    }
    public List<String[]> cargar() {
        List<String[]> lineas = new ArrayList<String[]>();
        File f = new File(nombreArchivo);
        if (!f.exists()) {
            return lineas;
        }
        try {
            BufferedReader lector = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8));
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.length() > 0) {
                    lineas.add(linea.split(";", -1));
                }
            }
            lector.close();
        } catch (IOException e) {
            System.out.println("No se pudieron leer los datos");
        }
        return lineas;
    }

    public void guardarTodo(List<String> lineas) {
        try {
            PrintWriter escritor = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(nombreArchivo, false), StandardCharsets.UTF_8));
            for (int i = 0; i < lineas.size(); i++) {
                escritor.println(lineas.get(i));
            }
            escritor.close();
        } catch (IOException e) {
            System.out.println("No se pudieron guardar los datos");
        }
    }
}

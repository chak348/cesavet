public class Main {
    public static void main(String[] args) throws Exception {
        Recepcionista recepcionista = new Recepcionista("Recepcionista", "0000");
        Persistencia persistencia = new Persistencia("datos_clinica.txt");

        persistencia.cargar(recepcionista);

 

        final Recepcionista datos = recepcionista;
        final Persistencia archivo = persistencia;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> archivo.guardar(datos)));

        ServidorWeb servidor = new ServidorWeb(recepcionista, persistencia, "web");
        servidor.iniciar(Integer.getInteger("cesavet.puerto", 8080));
    }
}

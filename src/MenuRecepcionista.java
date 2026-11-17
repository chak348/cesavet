import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class MenuRecepcionista {

    private Scanner leer;
    private Recepcionista recepcionista;
    private ArchivoDatos archivo;
    private List<Veterinario> veterinarios;

    public MenuRecepcionista(Recepcionista recepcionista) {
        this.recepcionista = recepcionista;
        this.archivo = new ArchivoDatos("datos_clinica.txt");
        this.leer = new Scanner(System.in);
        this.veterinarios = new ArrayList<Veterinario>();
        cargarDatos();
    }

    private void cargarDatos() {
        List<String[]> lineas = archivo.cargar();
        for (int i = 0; i < lineas.size(); i++) {
            String[] d = lineas.get(i);
            String tipo = d[0].toUpperCase();
            try {
                if (tipo.startsWith("DUE") && d.length >= 6) {
                    Dueno dueno = new Dueno(d[1], d[2], Integer.parseInt(d[3].trim()), d[4], d[5]);
                    recepcionista.obtenerDuenos().add(dueno);
                }
                else if (tipo.equals("MASCOTA") && d.length >= 7) {
                    Dueno dueno = recepcionista.buscarDuenoPorCarnet(d[6]);
                    if (dueno != null) {
                        Mascota m = new Mascota(d[1], d[2], Integer.parseInt(d[3].trim()),
                            Double.parseDouble(d[4].trim()), d[5]);
                        m.setDueno(dueno);
                        recepcionista.obtenerMascotas().add(m);
                        dueno.getMascotas().add(m);
                    }
                }
                else if (tipo.equals("VETERINARIO") && d.length >= 3) {
                    veterinarios.add(new Veterinario(d[1], d[2]));
                }
                else if (tipo.equals("CITA") && d.length >= 7) {
                    String[] f = d[1].split("/");
                    Fecha fecha = new Fecha(Integer.parseInt(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2]));
                    Mascota mascota = recepcionista.buscarMascotaPorNombre(d[4]);
                    Veterinario vet = buscarVeterinarioPorNombre(d[5]);
                    if (mascota != null && vet != null) {
                        Cita cita = new Cita(fecha, d[2], d[3], mascota, recepcionista, vet);
                        if (d[6].equals("CANCELADA")) {
                            cita.cancelar();
                        }
                        recepcionista.obtenerCitas().add(cita);
                    }
                }
            } catch (Exception e) {
                System.out.println("Linea ignorada en el archivo: " + String.join(";", d));
            }
        }
    }

    public void iniciar() {

        int opcion = 0;

        while (opcion != 6) {

            mostrarMenu();

            System.out.print("Seleccione una opcion: ");
            opcion = leer.nextInt();
            leer.nextLine();

            if (opcion == 1) {
                registrarDueno();
            }

            else if (opcion == 2) {
                registrarMascota();
            }

            else if (opcion == 3) {
                registrarVeterinario();
            }

            else if (opcion == 4) {
                agendarCita();
            }

            else if (opcion == 5) {
                recepcionista.mostrarCitas();
            }

            else if (opcion == 6) {
                System.out.println("Saliendo del sistema");
            }

            else {
                System.out.println("Opcion incorrecta");
            }
        }
    }

    private void mostrarMenu() {

        System.out.println();
        System.out.println("       VETERINARIA CESAVET");
        System.out.println("1. Registrar dueño");
        System.out.println("2. Registrar mascota");
        System.out.println("3. Registrar veterinario");
        System.out.println("4. Agendar cita");
        System.out.println("5. Mostrar citas");
        System.out.println("6. Salir");
    }

    private void registrarDueno() {

        System.out.println();
        System.out.println("REGISTRO DE DUEÑO");

        System.out.print("Nombre: ");
        String nombre = leer.nextLine();

        System.out.print("Carnet: ");
        String carnet = leer.nextLine();

        System.out.print("Telefono: ");
        int telefono = leer.nextInt();
        leer.nextLine();

        System.out.print("Direccion: ");
        String direccion = leer.nextLine();

        System.out.print("Correo: ");
        String correo = leer.nextLine();

        Dueno dueno = new Dueno(
            nombre,
            carnet,
            telefono,
            direccion,
            correo
        );

        dueno.registrar();

        recepcionista.registrarDueno(dueno);

        if (recepcionista.buscarDuenoPorCarnet(carnet) != dueno) {
            return;
        }

        archivo.guardar(
            "DUEÑO",
            nombre + ";" +
            carnet + ";" +
            telefono + ";" +
            direccion + ";" +
            correo
        );
    }

    private void registrarMascota() {

        System.out.println();
        System.out.println("REGISTRO DE MASCOTA");
        System.out.print("Nombre: ");
        String nombre = leer.nextLine();

        System.out.print("Raza: ");
        String raza = leer.nextLine();

        System.out.print("Edad: ");
        int edad = leer.nextInt();

        System.out.print("Peso: ");
        double peso = leer.nextDouble();
        leer.nextLine();

        System.out.print("Sexo: ");
        String sexo = leer.nextLine();

        System.out.print("Carnet del dueño: ");
        String carnetDueno = leer.nextLine();

        Dueno dueno = recepcionista.buscarDuenoPorCarnet(carnetDueno);

        if (dueno == null) {
            System.out.println("El dueño no esta registrado");
            return;
        }

        Mascota mascota = new Mascota(
            nombre,
            raza,
            edad,
            peso,
            sexo
        );
        mascota.setDueno(dueno);
        mascota.registrar();

        recepcionista.registrarMascota(mascota);

        if (!recepcionista.obtenerMascotas().contains(mascota)) {
            return;
        }

        archivo.guardar(
            "MASCOTA",
            nombre + ";" +
            raza + ";" +
            edad + ";" +
            peso + ";" +
            sexo + ";" +
            carnetDueno
        );
    }

    private void registrarVeterinario() {

        System.out.println();
        System.out.println("REGISTRO DE VETERINARIO");

        System.out.print("Nombre: ");
        String nombre = leer.nextLine();

        System.out.print("Especialidad: ");
        String especialidad = leer.nextLine();

        Veterinario veterinario =
            new Veterinario(
                nombre,
                especialidad
            );

        veterinario.registrar();

        if (buscarVeterinarioPorNombre(nombre) != null) {
            System.out.println("El veterinario ya esta registrado");
            return;
        }

        veterinarios.add(veterinario);

        archivo.guardar(
            "VETERINARIO",
            nombre + ";" +
            especialidad
        );

        System.out.println(
            "Veterinario registrado correctamente"
        );
    }

    private Veterinario buscarVeterinarioPorNombre(String nombre) {

        for (int i = 0; i < veterinarios.size(); i++) {

            Veterinario veterinario =
                veterinarios.get(i);

            if (veterinario.getNombre().equalsIgnoreCase(nombre)) {
                return veterinario;
            }
        }

        return null;
    }

    private void agendarCita() {

        System.out.println();
        System.out.println("AGENDAR CITA");

        System.out.print("Nombre de mascota: ");
        String nombreMascota = leer.nextLine();

        Mascota mascota =
            recepcionista.buscarMascotaPorNombre(
                nombreMascota
            );

        if (mascota == null) {

            System.out.println(
                "La mascota no esta registrada"
            );

            return;
        }

        System.out.print("Nombre del veterinario: ");
        String nombreVeterinario = leer.nextLine();

        Veterinario veterinario =
            buscarVeterinarioPorNombre(
                nombreVeterinario
            );

        if (veterinario == null) {

            System.out.println(
                "El veterinario no esta registrado"
            );

            return;
        }

        System.out.print("Dia: ");
        int dia = leer.nextInt();

        System.out.print("Mes: ");
        int mes = leer.nextInt();

        System.out.print("Año: ");
        int año = leer.nextInt();
        leer.nextLine();

        Fecha fecha =
            new Fecha(
                dia,
                mes,
                año
            );

        System.out.print("Hora: ");
        String hora = leer.nextLine();

        System.out.print("Motivo: ");
        String motivo = leer.nextLine();

        Cita cita =
            new Cita(
                fecha,
                hora,
                motivo,
                mascota,
                recepcionista,
                veterinario
            );

        recepcionista.agendarCita(cita);

        if (!recepcionista.obtenerCitas().contains(cita)) {
            return;
        }

        archivo.guardar(
            "CITA",
            fecha.mostrarFecha() + ";" +
            hora + ";" +
            motivo + ";" +
            mascota.getNombre() + ";" +
            veterinario.getNombre() + ";" +
            cita.getEstado()
        );
    }
}
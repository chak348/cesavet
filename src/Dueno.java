import java.util.ArrayList;
import java.util.List;
public class Dueno {
    private String nombre;
    private String CI;
    private int telefono;
    private String direccion;
    private String correo;
    private List<Mascota> mascotas;
    public Dueno() {
        this.mascotas = new ArrayList<>();
    }
    public Dueno(String nombre, String carnetIdentidad, int telefono, String direccion, String correo) {
        this.nombre = nombre;
        this.CI = carnetIdentidad;
        this.telefono = telefono;
        this.direccion = direccion;
        this.correo = correo;
        this.mascotas = new ArrayList<>();
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Dueño registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(nombre, CI, telefono, direccion, correo);
    }
    private boolean validarDatos(String nombre, String carnetIdentidad,int telefono, String direccion, String correo) {
        if (!validarNombre(nombre)) {
            return false;
        }
        if (!validarCarnet(carnetIdentidad)) {
            return false;
        }
        if (!validarTelefono(telefono)) {
            return false;
        }
        if (!validarDireccion(direccion)) {
            return false;
        }
        if (!validarCorreo(correo)) {
            return false;
        }
        return true;
    }
    private boolean validarNombre(String nombre) {
        if (nombre == null || nombre.equals("") || nombre.length() > 50) {
            return false;
        }
        return true;
    }
    private boolean validarCarnet(String CI) {
        if (CI == null ||CI.equals("") ||CI.length() > 20) {
            return false;
        }
        return true;
    }
    private boolean validarTelefono(int telefono) {
        if (telefono <= 0) {
            return false;
        }
        return true;
    }
    private boolean validarDireccion(String direccion) {
        if (direccion == null ||direccion.equals("") ||direccion.length() > 100) {
            return false;
        }
        return true;
    }
    private boolean validarCorreo(String correo) {
        if (correo == null ||correo.equals("") ||correo.length() > 100) {
            return false;
        }
        return true;
    }
    public void actualizar(String nombre, String CI,int telefono, String direccion, String correo) {
        if (validarDatos(nombre, CI, telefono, direccion, correo)) {
            this.nombre = nombre;
            this.CI = CI;
            this.telefono = telefono;
            this.direccion = direccion;
            this.correo = correo;
            System.out.println("Dueño actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void eliminar() {
        if (mascotas.size() > 0) {
            System.out.println("El dueño tiene mascotas registradas.");
        } else {
            System.out.println("Dueño eliminado correctamente.");
        }
    }
    public void agregarMascota(Mascota mascota) {
        if (mascota == null) {
            System.out.println("No se pudo agregar la mascota.");
        } else {
            boolean existe = false;
            for (int i = 0; i < mascotas.size(); i++) {
                if (mascotas.get(i) == mascota) {
                    existe = true;
                }
            }
            if (existe == true) {
                System.out.println("La mascota ya está registrada con este dueño.");
            } else {
                mascotas.add(mascota);
                System.out.println("Mascota agregada al dueño correctamente.");
            }
        }
    }
    public void eliminarMascota(Mascota mascota) {
        boolean encontrada = false;
        for (int i = 0; i < mascotas.size(); i++) {
            if (mascotas.get(i) == mascota) {
                mascotas.remove(i);
                encontrada = true;
                break;
            }
        }
        if (encontrada == true) {
            System.out.println("Mascota eliminada del dueño correctamente.");
        } else {
            System.out.println("La mascota no pertenece a este dueño.");
        }
    }
    public Mascota buscarMascotaPorNombre(String nombreMascota) {
        for (int i = 0; i < mascotas.size(); i++) {
            Mascota mascota = mascotas.get(i);
            if (mascota.getNombre().equalsIgnoreCase(nombreMascota)) {
                return mascota;
            }
        }
        return null;
    }
    public List<Mascota> getMascotas() {
        return mascotas;
    }
    public String getNombre(){ 
        return nombre; 
    }
    public void setNombre(String nombre) {
        if (validarNombre(nombre)) {
            this.nombre = nombre;
        }
    }
    public String getCarnetIdentidad() {
        return CI;
    }
    public void setCarnetIdentidad(String CI) {
        if (validarCarnet(CI)) {
            this.CI = CI;
        }
    }
    public int getTelefono() {
        return telefono;
    }
    public void setTelefono(int telefono) {
        if (validarTelefono(telefono)) {
            this.telefono = telefono;
        }
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        if (validarDireccion(direccion)) {
            this.direccion = direccion;
        }
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        if (validarCorreo(correo)) {
            this.correo = correo;
        }
    }
}
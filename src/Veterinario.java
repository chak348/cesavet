import java.util.List;
import java.util.ArrayList;
public class Veterinario {
    private String nombre;
    private String especialidad;
    private boolean registrado;
    private boolean disponible = true;
    private List<HistorialClinico> historiales;
    public Veterinario(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.historiales = new ArrayList<>();
        this.registrado = false;
    }
    public void registrar() {
        if (validarDatos()) {
            registrado = true;
            System.out.println("Veterinario registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(nombre, especialidad);
    }
    private boolean validarDatos(String nombre, String especialidad) {
        if (!validarNombre(nombre)) {
            return false;
        }
        if (!validarEspecialidad(especialidad)) {
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
    private boolean validarEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.equals("") || especialidad.length() > 50) {
            return false;
        }
        return true;
    }
    public void actualizar(String nombre, String especialidad) {
        if (validarDatos(nombre, especialidad)) {
            this.nombre = nombre;
            this.especialidad = especialidad;
            System.out.println("Veterinario actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void crearHistorial(HistorialClinico historial) {
        if (historial == null) {
            System.out.println("No se pudo crear el historial");
        } else {
            boolean existe = false;
            for (int i = 0; i < historiales.size(); i++) {
                if (historiales.get(i) == historial) {
                    existe = true;
                }
            }
            if (existe == true) {
                System.out.println("El historial ya está registrado");
            } else {
                historiales.add(historial);
                System.out.println("Historial clínico creado correctamente");
            }
        }
    }
    public void actualizarHistorial(HistorialClinico historial) {
        if (historial == null) {
            System.out.println("No se pudo actualizar el historial");
        } else {
            boolean existe = false;
            for (int i = 0; i < historiales.size(); i++) {
                if (historiales.get(i) == historial) {
                    existe = true;
                }
            }
            if (existe == true) {
                System.out.println("Historial actualizado correctamente");
            } else {
                System.out.println("El historial no pertenece al veterinario");
            }
        }
    }
    public List<HistorialClinico> obtenerHistoriales() {
        return historiales;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (validarNombre(nombre)) {
            this.nombre = nombre;
        }
    }
    public String getEspecialidad() {
        return especialidad;
    }
    public void setEspecialidad(String especialidad) {
        if (validarEspecialidad(especialidad)) {
            this.especialidad = especialidad;
        }
    }
    public boolean getRegistrado() {
        return registrado;
    }
    public boolean isDisponible() {
        return disponible;
    }
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}

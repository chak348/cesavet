import java.util.List;
import java.util.ArrayList;
public class HistorialClinico {
    private Fecha fechaApertura;
    private String observacionesGenerales;
    
    private List<Consulta> consultas;
    private List<Vacuna> vacunas;
    private Mascota mascota;
    public HistorialClinico(Fecha fechaApertura, String observacionesGenerales, Mascota mascota){
        this.fechaApertura = fechaApertura;
        this.observacionesGenerales = observacionesGenerales;
        this.mascota = mascota;  
        consultas = new ArrayList<>();
        vacunas = new ArrayList<>();
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Historial clínico registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(fechaApertura, observacionesGenerales, mascota);
    }
    private boolean validarDatos(Fecha fechaApertura,String observacionesGenerales,Mascota mascota) {
        if (!validarFecha(fechaApertura)) {
            return false;
        }
        if (!validarObservaciones(observacionesGenerales)) {
            return false;
        }
        if (!validarMascota(mascota)) {
            return false;
        }
        return true;
    }
    private boolean validarFecha(Fecha fechaApertura) {
        if (fechaApertura == null) {
            return false;
        }
        return true;
    }
    private boolean validarObservaciones(String observacionesGenerales) {
        if (observacionesGenerales == null) {
            return false;
        }
        return true;
    }
    private boolean validarMascota(Mascota mascota) {
        if (mascota == null) {
            return false;
        }
        return true;
    }
    public void actualizar(Fecha fechaApertura, String observacionesGenerales, Mascota mascota) {
        if (validarDatos(fechaApertura, observacionesGenerales, mascota)) {
            this.fechaApertura = fechaApertura;
            this.observacionesGenerales = observacionesGenerales;
            this.mascota = mascota;
            System.out.println("Historial clínico actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public List<Vacuna> obtenerVacunas(){
        return vacunas;
    }
    public void agregarConsulta(Consulta consulta) {
        if (consulta == null) {
            System.out.println("No se pudo agregar la consulta.");
        } else {
            boolean existe = false;
            for (int i = 0; i < consultas.size(); i++) {
                if (consultas.get(i) == consulta) {
                    existe = true;
                }
            }
            if (existe == true) {
                System.out.println("La consulta ya está registrada.");
            } else {
                consultas.add(consulta); 
                System.out.println("Consulta agregada correctamente.");
            }
        }
    }
    public void eliminarConsulta(Consulta consulta) {
        boolean encontrada = false;
        for (int i = 0; i < consultas.size(); i++) {
            if (consultas.get(i) == consulta) {
                consultas.remove(i);
                encontrada = true;
                break;
            }
        }
        if (encontrada == true) {
            System.out.println("Consulta eliminada correctamente.");
        } else {
            System.out.println("No se encontró la consulta.");
        }
    }
    public int cantidadConsultas() {
        return consultas.size();
    }
    public List<Consulta> obtenerConsultas() {
        return consultas;
    }
    public Fecha getFechaApertura(){ 
        return fechaApertura; 
    }
    public void setFechaApertura(Fecha fechaApertura) {
        if (validarFecha(fechaApertura)) {
            this.fechaApertura = fechaApertura;
        }
    }
    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }
    public void setObservacionesGenerales(String observacionesGenerales) {
        if (validarObservaciones(observacionesGenerales)) {
            this.observacionesGenerales = observacionesGenerales;
        }
    }
    public Mascota getMascota() {
        return mascota;
    }
    public void setMascota(Mascota mascota) {
        if (validarMascota(mascota)) {
            this.mascota = mascota;
        }
    }
}
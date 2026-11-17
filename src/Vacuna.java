public class Vacuna {
    private String nombreVacuna;
    private Fecha fechaAplicacion;
    private Fecha proxAplicacion;
    private EstadoAlerta estado;
    private String observaciones = "";
    public Vacuna(String nombreVacuna, Fecha fechaAplicacion, Fecha proxAplicacion) {
        this.nombreVacuna = nombreVacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proxAplicacion = proxAplicacion;
        this.estado = EstadoAlerta.AL_DIA;
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Vacuna registrada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(nombreVacuna, fechaAplicacion, proxAplicacion);
    }
    private boolean validarDatos(String nombreVacuna, Fecha fechaAplicacion, Fecha proxAplicacion) {
        if (!validarNombreVacuna(nombreVacuna)) {
            return false;
        }
        if (!validarFechaAplicacion(fechaAplicacion)) {
            return false;
        }
        if (!validarProxAplicacion(proxAplicacion)) {
            return false;
        }
        return true;
    }
    private boolean validarNombreVacuna(String nombreVacuna) {
        if (nombreVacuna == null || nombreVacuna.equals("") || nombreVacuna.length() > 50) {
            return false;
        }
        return true;
    }
    private boolean validarFechaAplicacion(Fecha fechaAplicacion) {
        if (fechaAplicacion == null) {
            return false;
        }
        return true;
    }
    private boolean validarProxAplicacion(Fecha proxAplicacion) {
        if (proxAplicacion == null) {
            return false;
        }
        return true;
    }
    public void actualizar(String nombreVacuna, Fecha fechaAplicacion, Fecha proxAplicacion) {
        if (validarDatos(nombreVacuna, fechaAplicacion, proxAplicacion)) {
            this.nombreVacuna = nombreVacuna;
            this.fechaAplicacion = fechaAplicacion;
            this.proxAplicacion = proxAplicacion;
            System.out.println("Vacuna actualizada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void verificarEstado(Fecha fechaActual) {
        if (fechaActual == null || proxAplicacion == null) {
            return;
        }
        if (fechaActual.otraFecha(proxAplicacion)) {
            estado = EstadoAlerta.AL_DIA;
        } else {
            estado = EstadoAlerta.PENDIENTE;
        }
    }
    public String getNombreVacuna() {
        return nombreVacuna;
    }
    public void setNombreVacuna(String nombreVacuna) {
        if (validarNombreVacuna(nombreVacuna)) {
            this.nombreVacuna = nombreVacuna;
        }
    }
    public Fecha getFechaAplicacion() {
        return fechaAplicacion;
    }
    public void setFechaAplicacion(Fecha fechaAplicacion) {
        if (validarFechaAplicacion(fechaAplicacion)) {
            this.fechaAplicacion = fechaAplicacion;
        }
    }
    public Fecha getProxAplicacion() {
        return proxAplicacion;
    }
    public void setProxAplicacion(Fecha proxAplicacion) {
        if (validarProxAplicacion(proxAplicacion)) {
            this.proxAplicacion = proxAplicacion;
        }
    }
    public EstadoAlerta getEstado() {
        return estado;
    }
    public void setEstado(EstadoAlerta estado) {
        if (estado != null) {
            this.estado = estado;
        }
    }
    public String getObservaciones() {
        return observaciones;
    }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

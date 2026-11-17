public class Consulta {
    private Fecha fecha;
    private Veterinario veterinario;
    private Mascota mascota;
    private Diagnostico diagnostico;
    private Tratamiento tratamiento;
    private boolean registrado;
    private String motivo = "";
    private boolean finalizada = false;
    private String vacunasTexto = "";
    private String observaciones = "";
    private Cita cita;
    private java.util.List<ServicioRealizado> servicios = new java.util.ArrayList<>();
    public Consulta(Fecha fecha, Veterinario veterinario, Mascota mascota) {
        this.fecha = fecha;
        this.veterinario = veterinario;
        this.mascota = mascota;
        this.registrado = false;
    }
    public void registrar() {
        if (validarDatos()) {
            registrado = true;
            System.out.println("Consulta registrada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(fecha, veterinario, mascota);
    }
    private boolean validarDatos(Fecha fecha,Veterinario veterinario, Mascota mascota) {
        if (!validarFecha(fecha)) {
            return false;
        }
        if (!validarVeterinario(veterinario)) {
            return false;
        }
        if (!validarMascota(mascota)) {
            return false;
        }
        return true;
    }
    private boolean validarFecha(Fecha fecha) {
        if (fecha == null) {
            return false;
        }
        return true;
    }
    private boolean validarVeterinario(Veterinario veterinario) {
        if (veterinario == null) {
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
    private boolean validarDiagnostico(Diagnostico diagnostico) {
        if (diagnostico == null) {
            return false;
        }
        return true;
    }
    private boolean validarTratamiento(Tratamiento tratamiento) {
        if (tratamiento == null) {
            return false;
        }
        return true;
    }
    public void agregarDiagnostico(Diagnostico diagnostico) {
        if(diagnostico != null) {
            this.diagnostico = diagnostico;
            System.out.println("Diagnóstico registrado correctamente.");
        }
    }
    public void agregarTratamiento(Tratamiento tratamiento) {
        if(tratamiento != null) {
            this.tratamiento = tratamiento;
            System.out.println("Tratamiento registrado correctamente.");
        }
    }
    public void actualizar(Fecha fecha, Veterinario veterinario, Mascota mascota) {
        if (validarDatos(fecha, veterinario, mascota)) {
            this.fecha = fecha;
            this.veterinario = veterinario;
            this.mascota = mascota;
            System.out.println("Consulta actualizada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void eliminar() {
        registrado = false;
        System.out.println("Consulta eliminada correctamente");
    }
    public boolean isRegistrado() {
        return registrado;
    }
    public Fecha getFecha() {
        return fecha;
    }
    public void setFecha(Fecha fecha) {
        if (validarFecha(fecha)) {
            this.fecha = fecha;
        }
    }
    public Veterinario getVeterinario() {
        return veterinario;
    }
    public void setVeterinario(Veterinario veterinario) {
        if (validarVeterinario(veterinario)) {
            this.veterinario = veterinario;
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
    public Diagnostico getDiagnostico() {
        return diagnostico;
    }
    public Tratamiento getTratamiento() {
        return tratamiento;
    }
    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    public boolean isFinalizada() {
        return finalizada;
    }
    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }
    public String getVacunasTexto() {
        return vacunasTexto;
    }
    public void setVacunasTexto(String vacunasTexto) {
        this.vacunasTexto = vacunasTexto;
    }
    public String getObservaciones() {
        return observaciones;
    }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    public Cita getCita() {
        return cita;
    }
    public void setCita(Cita cita) {
        this.cita = cita;
    }
    public java.util.List<ServicioRealizado> getServicios() {
        return servicios;
    }
    public void setDiagnostico(Diagnostico diagnostico) {
        this.diagnostico = diagnostico;
    }
    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }
    public boolean datosClinicosCompletos() {
        return diagnostico != null && tratamiento != null
            && diagnostico.getDescripcion() != null && !diagnostico.getDescripcion().trim().equals("")
            && tratamiento.getMedicamentos() != null && !tratamiento.getMedicamentos().trim().equals("");
    }
}

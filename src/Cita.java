public class Cita{
    private Fecha fecha;
    private String hora;
    private String motivo;
    private EstadoCita estado;   
    private Recepcionista recepcionista; 
    private Mascota mascota; 
    private Veterinario veterinario;    
    public Cita(Fecha fecha, String hora, String motivo, Mascota mascota, Recepcionista recepcionista,Veterinario veterinario){
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.mascota = mascota;
        this.recepcionista = recepcionista;
        this.veterinario = veterinario;
        this.estado = EstadoCita.PROGRAMADA;
    }
    public void programar() {
        if (validarDatos()) {
            estado = EstadoCita.PROGRAMADA;
            System.out.println("Cita programada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(fecha, hora, motivo, mascota, recepcionista, veterinario);
    }
    private boolean validarDatos(Fecha fecha, String hora, String motivo, Mascota mascota, Recepcionista recepcionista, Veterinario veterinario) {
        if (!validarFecha(fecha)) {
            return false;
        }
        if (!validarHora(hora)) {
            return false;
        }
        if (!validarMotivo(motivo)) {
            return false;
        }
        if (!validarMascota(mascota)) {
            return false;
        }
        if (!validarRecepcionista(recepcionista)) {
            return false;
        }
        if (!validarVeterinario(veterinario)) {
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
    private boolean validarHora(String hora) {
        if (hora == null || hora.equals("")) {
            return false;
        }
        return true;
    }
    private boolean validarMotivo(String motivo) {
        if (motivo == null || motivo.equals("") || motivo.length() > 100) {
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
    private boolean validarRecepcionista(Recepcionista recepcionista) {
        if (recepcionista == null) {
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
    public void actualizar(Fecha fecha, String hora, String motivo,Mascota mascota, Recepcionista recepcionista, Veterinario veterinario) {
        if (validarDatos(fecha, hora, motivo, mascota, recepcionista, veterinario)) {
            this.fecha = fecha;
            this.hora = hora;
            this.motivo = motivo;
            this.mascota = mascota;
            this.recepcionista = recepcionista;
            this.veterinario = veterinario;
            System.out.println("Cita actualizada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void cancelar() {
        estado = EstadoCita.CANCELADA;
        System.out.println("La cita ha sido cancelada");
    }
    public boolean cambiarEstado(EstadoCita nuevo) {
        if (nuevo == null || estado == null) {
            return false;
        }
        if (estado == EstadoCita.ATENDIDA || estado == EstadoCita.CANCELADA) {
            return false;
        }
        estado = nuevo;
        return true;
    }
    public Fecha getFecha() {
        return fecha;
    }
    public void setFecha(Fecha fecha) {
        if (validarFecha(fecha)) {
            this.fecha = fecha;
        }
    }
    public String getHora() {
        return hora;
    }
    public void setHora(String hora) {
        if (validarHora(hora)) {
            this.hora = hora;
        }
    }
    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        if (validarMotivo(motivo)) {
            this.motivo = motivo;
        }
    }
    public EstadoCita getEstado() {
        return estado;
    }
    public Mascota getMascota() {
        return mascota;
    }
    public void setMascota(Mascota mascota) {
        if (validarMascota(mascota)) {
            this.mascota = mascota;
        }
    }
    public Recepcionista getRecepcionista() {
        return recepcionista;
    }
    public void setRecepcionista(Recepcionista recepcionista) {
        if (validarRecepcionista(recepcionista)) {
            this.recepcionista = recepcionista;
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
    
}
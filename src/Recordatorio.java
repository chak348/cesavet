public class Recordatorio {
    private String tipoRecordatorio;
    private String medioEnvio;
    private String mensaje;
    private Fecha fechaEnvio;
    private Dueno dueno;
    private Recepcionista recepcionista;
    private Mascota mascota;
    public Recordatorio(String tipoRecordatorio, String medioEnvio, Fecha fechaEnvio, Dueno dueno, Recepcionista recepcionista, Mascota mascota) {
        this.tipoRecordatorio = tipoRecordatorio;
        this.medioEnvio = medioEnvio;
        this.fechaEnvio = fechaEnvio;
        this.dueno = dueno;
        this.recepcionista = recepcionista;
        this.mascota = mascota;
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Recordatorio registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(medioEnvio, fechaEnvio, dueno, recepcionista, mascota);
    }
    private boolean validarDatos(String medioEnvio, Fecha fechaEnvio, Dueno dueno, Recepcionista recepcionista, Mascota mascota) {
        if (!validarMedioEnvio(medioEnvio)) {
            return false;
        }
        if (!validarFechaEnvio(fechaEnvio)) {
            return false;
        }
        if (!validarDueno(dueno)) {
            return false;
        }
        if (!validarRecepcionista(recepcionista)) {
            return false;
        }
        if (!validarMascota(mascota)) {
            return false;
        }
        return true;
    }
    private boolean validarMedioEnvio(String medioEnvio) {
        if (medioEnvio == null || medioEnvio.equals("")) {
            return false;
        }
        return true;
    }
    private boolean validarFechaEnvio(Fecha fechaEnvio) {
        if (fechaEnvio == null) {
            return false;
        }
        return true;
    }
    private boolean validarDueno(Dueno dueno) {
        if (dueno == null) {
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
    private boolean validarMensaje(String mensaje) {
        if (mensaje == null || mensaje.equals("")) {
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
    public void actualizar(String medioEnvio, Fecha fechaEnvio, Dueno dueno, Recepcionista recepcionista,Mascota mascota) {
        if (validarDatos(medioEnvio, fechaEnvio, dueno, recepcionista, mascota)) {
            this.medioEnvio = medioEnvio;
            this.fechaEnvio = fechaEnvio;
            this.dueno = dueno;
            this.recepcionista = recepcionista;
            this.mascota = mascota;
            System.out.println("Recordatorio actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void enviar() {
        if (validarDatos()) {
            if (!validarMensaje(mensaje)) {
                mensaje = generarMensaje();
            }
            System.out.println("RECORDATORIO veterinaria CESAVET ");
            System.out.println("Dueño: " + dueno.getNombre());
            System.out.println("Mascota: " + mascota.getNombre());
            System.out.println("Medio: " + medioEnvio);
            System.out.println("Fecha: " + fechaEnvio.mostrarFecha());
            System.out.println("Mensaje: " + mensaje);
        } else {
            System.out.println("No se pudo enviar el recordatorio");
        }
    }
    public String generarMensaje() {
        return "Recordatorio para " + mascota.getNombre()
         + " programado para la fecha "
         + fechaEnvio.mostrarFecha();
    }
    public String getMedioEnvio() {
        return medioEnvio;
    }
    public void setMedioEnvio(String medioEnvio) {
        if (validarMedioEnvio(medioEnvio)) {
            this.medioEnvio = medioEnvio;
        }
    }
    public Fecha getFechaEnvio() {
        return fechaEnvio;
    }
    public void setFechaEnvio(Fecha fechaEnvio) {
        if (validarFechaEnvio(fechaEnvio)) {
            this.fechaEnvio = fechaEnvio;
        }
    }
    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        if (validarMensaje(mensaje)) {
            this.mensaje = mensaje;
        }
    }
    public Dueno getDueno() {
        return dueno;
    }
    public void setDueno(Dueno dueno) {
        if (validarDueno(dueno)) {
            this.dueno = dueno;
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
    public Mascota getMascota() {
        return mascota;
    }
    public void setMascota(Mascota mascota) {
        if (validarMascota(mascota)) {
            this.mascota = mascota;
        }
    }
    public String getTipoRecordatorio() {
    return tipoRecordatorio;
}

public void setTipoRecordatorio(String tipoRecordatorio) {
    if (tipoRecordatorio != null && !tipoRecordatorio.trim().equals("")) {
        this.tipoRecordatorio = tipoRecordatorio.trim();
    }
}
}
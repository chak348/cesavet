import java.util.List;
import java.util.ArrayList;
public class ComprobanteDeAtencion {
    private Consulta consulta;
    private Dueno dueno;
    private List<ServicioRealizado> servicios;
    private double costoTotal;
    private String recepcionista = "";
    private Fecha fecha = Fecha.hoy();
    private String diagnosticoResumen = "";
    private String tratamientoResumen = "";
    private String observacionesResumen = "";
    public ComprobanteDeAtencion(Consulta consulta, Dueno dueno) {
        this.consulta = consulta;
        this.dueno = dueno;
        this.servicios = new ArrayList<>();
        this.costoTotal = 0;
        if (consulta != null && consulta.getDiagnostico() != null) this.diagnosticoResumen = consulta.getDiagnostico().getDescripcion();
        if (consulta != null && consulta.getTratamiento() != null) {
            Tratamiento t = consulta.getTratamiento();
            this.tratamientoResumen = t.getMedicamentos();
            if (t.getDosis() != null && !t.getDosis().trim().isEmpty()) this.tratamientoResumen += " · Dosis: " + t.getDosis();
            if (t.getDuracion() != null && !t.getDuracion().trim().isEmpty()) this.tratamientoResumen += " · Duración: " + t.getDuracion();
            if (t.getIndicaciones() != null && !t.getIndicaciones().trim().isEmpty()) this.tratamientoResumen += " · Indicaciones: " + t.getIndicaciones();
        }
        if (consulta != null) this.observacionesResumen = consulta.getObservaciones();
    }
    public void generar() {
        if (validarDatos()) {
            System.out.println("Comprobante generado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(consulta, dueno);
    }
    private boolean validarDatos(Consulta consulta, Dueno dueno) {
        if (!validarConsulta(consulta)) {
            return false;
        }
        if (!validarDueno(dueno)) {
            return false;
        }
        return true;
    }
    private boolean validarConsulta(Consulta consulta) {
        if (consulta == null) {
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
    private boolean validarServicio(ServicioRealizado servicio) {
        if (servicio == null) {
            return false;
        }
        return true;
    }
    public void agregarServicio(ServicioRealizado servicio) {
        if (validarServicio(servicio)) {
            servicios.add(servicio);
            calcularCostoTotal();
            System.out.println("Servicio agregado al comprobante");
        } else {
            System.out.println("No se pudo agregar el servicio");
        }
    }
    public void recalcularTotal() {
        calcularCostoTotal();
    }
    private void calcularCostoTotal() {
        costoTotal = 0;
        for (int i = 0; i < servicios.size(); i++) {
            costoTotal = costoTotal + servicios.get(i).getSubtotal();
        }
    }
    public void mostrarComprobante() {
        if (validarDatos()) {
            System.out.println("COMPROBANTE DE ATENCION");
            System.out.println("Veterinaria CESAVET");
            System.out.println(
                "Fecha: " +
                consulta.getFecha().mostrarFecha());
            System.out.println(
                "Mascota: " +
                consulta.getMascota().getNombre());
            System.out.println(
                "Dueño: " +
                dueno.getNombre());
            System.out.println(
                "Veterinario: " +
                consulta.getVeterinario().getNombre());
            if (consulta.getDiagnostico() != null) {
                System.out.println(
                    "Diagnóstico: " +
                    consulta.getDiagnostico().getDescripcion()
                );
            }
            if (consulta.getTratamiento() != null) {
                System.out.println(
                    "Tratamiento: " +
                    consulta.getTratamiento().getMedicamentos());
                System.out.println(
                    "Dosis: " +
                    consulta.getTratamiento().getDosis());
                System.out.println(
                    "Duración: " +
                    consulta.getTratamiento().getDuracion());
            }
            System.out.println("Servicios realizados:");
            for (int i = 0; i < servicios.size(); i++) {
                ServicioRealizado servicio = servicios.get(i);
                System.out.println(
                    "- " +
                    servicio.getNombre() +
                    " Bs. " +
                    servicio.getCostoBase());
            }
            System.out.println("TOTAL: Bs. " + costoTotal);
        } else {
            System.out.println("No se puede mostrar el comprobante");
        }
    }
    public Consulta getConsulta() {
        return consulta;
    }
    public void setConsulta(Consulta consulta) {
        if (validarConsulta(consulta)) {
            this.consulta = consulta;
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
    public double getCostoTotal() {
        return costoTotal;
    }
    public List<ServicioRealizado> obtenerServicios() {
        return servicios;
    }
    public String getRecepcionista() {
        return recepcionista;
    }
    public void setRecepcionista(String recepcionista) {
        this.recepcionista = recepcionista;
    }
    public Fecha getFecha() {
        return fecha;
    }
    public String getDiagnosticoResumen() { return diagnosticoResumen == null ? "" : diagnosticoResumen; }
    public void setDiagnosticoResumen(String valor) { diagnosticoResumen = valor == null ? "" : valor.trim(); }
    public String getTratamientoResumen() { return tratamientoResumen == null ? "" : tratamientoResumen; }
    public void setTratamientoResumen(String valor) { tratamientoResumen = valor == null ? "" : valor.trim(); }
    public String getObservacionesResumen() { return observacionesResumen == null ? "" : observacionesResumen; }
    public void setObservacionesResumen(String valor) { observacionesResumen = valor == null ? "" : valor.trim(); }
    public void setFecha(Fecha fecha) {
        this.fecha = fecha;
    }
}

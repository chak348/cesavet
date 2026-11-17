public class Diagnostico {
    private String descripcion;
    private String observaciones;
    public Diagnostico(String descripcion, String observaciones) {
        this.descripcion = descripcion;
        this.observaciones = observaciones;
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Diagnóstico registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(descripcion, observaciones);
    }
    private boolean validarDatos(String descripcion, String observaciones) {
        if (!validarDescripcion(descripcion)) {
            return false;
        }
        if (!validarObservaciones(observaciones)) {
            return false;
        }
        return true;
    }
    private boolean validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.equals("") || descripcion.length() > 200) {
            return false;
        }
        return true;
    }
    private boolean validarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.length() > 300) {
            return false;
        }
        return true;
    }
    public void actualizar(String descripcion, String observaciones) {
        if (validarDatos(descripcion, observaciones)) {
            this.descripcion = descripcion;
            this.observaciones = observaciones;
            System.out.println("Diagnóstico actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public String getDescripcion() {
        return descripcion;
    }
    public String getObservaciones() {
        return observaciones;
    }
    public void setDescripcion(String descripcion) {
        if (validarDescripcion(descripcion)) {
            this.descripcion = descripcion;
        }
    }
    public void setObservaciones(String observaciones) {
        if (validarObservaciones(observaciones)) {
            this.observaciones = observaciones;
        }
    }
}
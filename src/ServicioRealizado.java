public class ServicioRealizado {
    private String nombre;
    private String descripcion;
    private double costoBase;
    private int cantidad = 1;
    public ServicioRealizado(String nombre, String descripcion, double costoBase) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoBase = costoBase;
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Servicio registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(nombre, descripcion, costoBase);
    }
    private boolean validarDatos(String nombre, String descripcion, double costoBase) {
        if (!validarNombre(nombre)) {
            return false;
        }
        if (!validarDescripcion(descripcion)) {
            return false;
        }
        if (!validarCosto(costoBase)) {
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
    private boolean validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.equals("") || descripcion.length() > 200) {
            return false;
        }
        return true;
    }
    private boolean validarCosto(double costoBase) {
        if (costoBase < 0) {
            return false;
        }
        return true;
    }
    public void actualizar(String nombre, String descripcion, double costoBase) {
        if (validarDatos(nombre, descripcion, costoBase)) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.costoBase = costoBase;
            System.out.println("Servicio actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (validarNombre(nombre)) {
            this.nombre = nombre;
        }
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        if (validarDescripcion(descripcion)) {
            this.descripcion = descripcion;
        }
    }
    public double getCostoBase() {
        return costoBase;
    }
    public void setCostoBase(double costoBase) {
        if (validarCosto(costoBase)) {
            this.costoBase = costoBase;
        }
    }
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        if (cantidad > 0) {
            this.cantidad = cantidad;
        }
    }
    public double getSubtotal() {
        return costoBase * cantidad;
    }
}

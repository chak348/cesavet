public class Tratamiento {
    private String medicamentos;
    private String dosis;
    private String duracion;
    private String indicaciones;
    public Tratamiento(String medicamentos, String dosis, String duracion, String indicaciones) {
        this.medicamentos = medicamentos;
        this.dosis = dosis;
        this.duracion = duracion;
        this.indicaciones = indicaciones;
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Tratamiento registrado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(medicamentos, dosis, duracion, indicaciones);
    }
    private boolean validarDatos(String medicamentos, String dosis, String duracion, String indicaciones) {
        if (!validarMedicamentos(medicamentos)) {
            return false;
        }
        if (!validarDosis(dosis)) {
            return false;
        }
        if (!validarDuracion(duracion)) {
            return false;
        }
        if (!validarIndicaciones(indicaciones)) {
            return false;
        }
        return true;
    }
    private boolean validarMedicamentos(String medicamentos) {
        if (medicamentos == null || medicamentos.equals("") || medicamentos.length() > 100) {
            return false;
        }
        return true;
    }
    private boolean validarDosis(String dosis) {
        if (dosis == null || dosis.equals("") || dosis.length() > 50) {
            return false;
        }
        return true;
    }
    private boolean validarDuracion(String duracion) {
        if (duracion == null || duracion.equals("") || duracion.length() > 50) {
            return false;
        }
        return true;
    }
    private boolean validarIndicaciones(String indicaciones) {
        if (indicaciones == null || indicaciones.length() > 300) {
            return false;
        }
        return true;
    }
    public void actualizar(String medicamentos, String dosis, String duracion, String indicaciones) {
        if (validarDatos(medicamentos, dosis, duracion, indicaciones)) {
            this.medicamentos = medicamentos;
            this.dosis = dosis;
            this.duracion = duracion;
            this.indicaciones = indicaciones;
            System.out.println("Tratamiento actualizado correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public String getMedicamentos() {
        return medicamentos;
    }
    public String getDosis() {
        return dosis;
    }
    public String getDuracion() {
        return duracion;
    }
    public String getIndicaciones() {
        return indicaciones;
    }
    public void setMedicamentos(String medicamentos) {
        if (validarMedicamentos(medicamentos)) {
            this.medicamentos = medicamentos;
        }
    }
    public void setDosis(String dosis) {
        if (validarDosis(dosis)) {
            this.dosis = dosis;
        }
    }
    public void setDuracion(String duracion) {
        if (validarDuracion(duracion)) {
            this.duracion = duracion;
        }
    }
    public void setIndicaciones(String indicaciones) {
        if (validarIndicaciones(indicaciones)) {
            this.indicaciones = indicaciones;
        }
    }
}
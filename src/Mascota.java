import java.util.List;
import java.util.ArrayList;
public class Mascota {
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private String unidadEdad = "años";
    private double peso;
    private String sexo;
    private String colorExpediente = "";
    private List<RegistroExpediente> registrosExpediente = new ArrayList<>();
    public String getColorExpediente() { return colorExpediente; }
    public void setColorExpediente(String valor) { colorExpediente = valor == null ? "" : valor; }
    public List<RegistroExpediente> getRegistrosExpediente() { return registrosExpediente; }
    private Dueno dueno; 
    private HistorialClinico historialClinico;
    private Consulta[] consultas;
    private int cantidadConsultas; 
    private List<Vacuna> vacunas;
    public Mascota(String nombre, String raza, int edad, double peso, String sexo) {
        this.nombre = nombre;
        this.raza = raza;
        this.edad = edad;
        this.peso = peso;
        this.sexo = sexo;
        this.consultas = new Consulta[20];
        this.cantidadConsultas = 0;
        this.vacunas = new ArrayList<>();
    }
    public void registrar() {
        if (validarDatos() && validarDueno()) {
            System.out.println("Mascota registrada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(nombre, raza, edad, peso, sexo);
    }
    private boolean validarDatos(String nombre, String raza, int edad,double peso, String sexo) {
        if (!validarNombre(nombre)) {
            return false;
        }
        if (!validarRaza(raza)) {
            return false;
        }
        if (!validarEdad(edad)) {
            return false;
        }
        if (!validarPeso(peso)) {
            return false;
        }
        if (!validarSexo(sexo)) {
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
    private boolean validarRaza(String raza) {
        if (raza == null || raza.equals("") || raza.length() > 50) {
            return false;
        }
        return true;
    }
    private boolean validarEdad(int edad) {
        if (edad < 0) {
            return false;
        }
        return true;
    }
    private boolean validarPeso(double peso) {
        if (peso <= 0) {
            return false;
        }
        return true;
    }
    private boolean validarSexo(String sexo) {
        if (sexo == null || sexo.equals("")) {
            return false;
        }
        return true;
    }
    private boolean validarDueno() {
        if (dueno == null) {
            return false;
        }
        return true;
    }
    public void actualizar(String nombre, String raza, int edad, double peso, String sexo) {
        if (validarDatos(nombre, raza, edad, peso, sexo)) {
            this.nombre = nombre;
            this.raza = raza;
            this.edad = edad;
            this.peso = peso;
            this.sexo = sexo;
            System.out.println("Mascota actualizada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void registrarConsulta(Consulta consulta) {
        if (consulta == null) {
            System.out.println("No se puede registrar una consulta vacía");
        }else {
            consultas[cantidadConsultas] = consulta;
            cantidadConsultas++;
            System.out.println("Consulta registrada correctamente");
        }
    }
    public void eliminar() {
        if (dueno != null) {
            dueno.eliminarMascota(this);
            this.dueno = null;
            System.out.println("Mascota eliminada correctamente");
        } else {
            System.out.println("La mascota no tiene dueño registrado");
        }
    }
    public int calcularEdad(){
        return edad;
    }
    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) {
        if (validarNombre(nombre)) {
            this.nombre = nombre;
        }
    }
    public String getEspecie() {
    return especie == null ? "Sin especificar" : especie;
}
    public void setEspecie(String especie) {
        if ("Perro".equals(especie) || "Gato".equals(especie)) {
            this.especie = especie;
        }
    }
    public String getRaza() {
        return raza;
    }
    public void setRaza(String raza) {
        if (validarRaza(raza)) {
            this.raza = raza;
        }
    }
    public int getEdad() {
        return edad;
    }
    public String getUnidadEdad() {
        return unidadEdad;
    }
    public void setUnidadEdad(String unidadEdad) {
        if ("meses".equals(unidadEdad) || "años".equals(unidadEdad)) {
            this.unidadEdad = unidadEdad;
        }
    }
    public String getEdadCompleta() {
        if ("meses".equals(unidadEdad)) {
            return edad + (edad == 1 ? " mes" : " meses");
        }
        return edad + (edad == 1 ? " año" : " años");
    }
    public void setEdad(int edad) {
        if (validarEdad(edad)) {
            this.edad = edad;
        }
    }
    public double getPeso() {
        return peso;
    }
    public void setPeso(double peso) {
        if (validarPeso(peso)) {
            this.peso = peso;
        }
    }
    public String getSexo() {
        return sexo;
    }
    public void setSexo(String sexo) {
        if (validarSexo(sexo)) {
            this.sexo = sexo;
        }
    }
    public Dueno getDueno() {
        return dueno;
    }
    public void setDueno(Dueno dueno) {
        if (dueno != null) {
            this.dueno = dueno;
        }
    }
    public HistorialClinico getHistorialClinico() {
        return historialClinico;
    }
    public void setHistorialClinico(HistorialClinico historialClinico) {
        if (historialClinico != null) {
            this.historialClinico = historialClinico;
        }
    }
    public void agregarVacuna(Vacuna vacuna) {
        if (vacuna != null) {
            vacunas.add(vacuna);
            System.out.println("Vacuna agregada correctamente");
        }
    }
    public List<Vacuna> getVacunas() {
        return vacunas;
    }
}
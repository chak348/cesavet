import java.util.List;
import java.util.ArrayList;
public class Recepcionista {
    private String nombre;
    private String telefono;
    private List<Dueno> duenos;
    private List<Mascota> mascotas;
    private List<Cita> citas;
    private List<ComprobanteDeAtencion> comprobantes;
    private List<Recordatorio> recordatorios;
    private List<Veterinario> veterinarios;
    private List<String> recepcionistas;
    private List<Consulta> consultas;
    private List<ServicioRealizado> catalogo;
    public Recepcionista(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.duenos = new ArrayList<>();
        this.mascotas = new ArrayList<>();
        this.citas = new ArrayList<>();
        this.comprobantes = new ArrayList<>();
        this.recordatorios = new ArrayList<>();
        this.veterinarios = new ArrayList<>();
        this.recepcionistas = new ArrayList<>();
        this.consultas = new ArrayList<>();
        this.catalogo = new ArrayList<>();
    }
    public void registrar() {
        if (validarDatos()) {
            System.out.println("Recepcionista registrada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    private boolean validarDatos() {
        return validarDatos(nombre, telefono);
    }
    private boolean validarDatos(String nombre, String telefono) {
        if (!validarNombre(nombre)) {
            return false;
        }
        if (!validarTelefono(telefono)) {
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
    private boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.equals("")) {
            return false;
        }
        return true;
    }
    public void actualizar(String nombre, String telefono) {
        if (validarDatos(nombre, telefono)) {
            this.nombre = nombre;
            this.telefono = telefono;
            System.out.println("Recepcionista actualizada correctamente");
        } else {
            System.out.println("Debe completar correctamente todos los datos");
        }
    }
    public void registrarDueno(Dueno dueno) {
        if (dueno == null) {
            System.out.println("No se pudo registrar al dueño");
            return;
        }
        if (existeCarnet(dueno.getCarnetIdentidad())) {
            System.out.println("No se pudo registrar al dueño");
            return;
        }
        duenos.add(dueno);
        System.out.println("Dueño registrado correctamente");
    }
    private boolean existeCarnet(String carnet) {
        for (int i = 0; i < duenos.size(); i++) {
            Dueno dueno = duenos.get(i);

            if (dueno.getCarnetIdentidad().equals(carnet)) {
                return true;
            }
        }
        return false;
    }
    public void registrarMascota(Mascota mascota) {
        if (mascota == null) {
            System.out.println("No se pudo registrar la mascota");
            return;
        }
        if (mascota.getDueno() == null) {
            System.out.println("No se pudo registrar la mascota");
            return;
        }
        if (existeMascota(mascota)) {
            System.out.println("La mascota ya está registrada");
            return;
        }
        mascotas.add(mascota);
        mascota.getDueno().agregarMascota(mascota);
        System.out.println("Mascota registrada correctamente");
    }
    private boolean existeMascota(Mascota mascota) {
        for (int i = 0; i < mascotas.size(); i++) {
            if (mascotas.get(i) == mascota) {
                return true;
            }
        }
        return false;
    }
    public void agendarCita(Cita cita) {
        if (cita == null) {
            System.out.println("No se pudo agendar la cita");
            return;
        }
        if (existeCruceHorario(cita)) {
            System.out.println("Ya existe una cita programada en ese horario");
            return;
        }
        citas.add(cita);
        cita.programar();
        System.out.println("Cita agendada correctamente");
    }
    private boolean existeCruceHorario(Cita nuevaCita) {
        for (int i = 0; i < citas.size(); i++) {
            Cita cita = citas.get(i);
            if (cita.getEstado() != EstadoCita.CANCELADA) {
                boolean mismaFecha = cita.getFecha().mostrarFecha().equals(nuevaCita.getFecha().mostrarFecha());
                boolean mismaHora = cita.getHora().equals(nuevaCita.getHora());
                if (mismaFecha && mismaHora) {
                    return true;
                }
            }
        }
        return false;
    }
    public void cancelarCita(Cita cita) {
        if (cita != null && citas.contains(cita)) {
            cita.cancelar();
        } else {
            System.out.println("No se encontró la cita");
        }
    }
    public void generarComprobante(ComprobanteDeAtencion comprobante) {
        if (comprobante == null) {
            System.out.println("No se pudo generar el comprobante");
            return;
        }
        comprobantes.add(comprobante);
        comprobante.generar();
        System.out.println("Comprobante generado correctamente");
    }
    public void enviarRecordatorio(Recordatorio recordatorio) {
        if (recordatorio == null) {
            System.out.println("No se pudo enviar el recordatorio");
            return;
        }
        recordatorios.add(recordatorio);
        System.out.println("Recordatorio enviado correctamente");
    }
    public Dueno buscarDuenoPorCarnet(String carnet) {
        for (int i = 0; i < duenos.size(); i++) {
            Dueno dueno = duenos.get(i);
            if (dueno.getCarnetIdentidad().equals(carnet)) {
                return dueno;
            }
        }
        return null;
    }
    public Mascota buscarMascotaPorNombre(String nombreMascota) {
        for (int i = 0; i < mascotas.size(); i++) {
            Mascota mascota = mascotas.get(i);
            if (mascota.getNombre().equalsIgnoreCase(nombreMascota)) {
                return mascota;
            }
        }
        return null;
    }
    public void mostrarCitas() {
        if (citas.size() == 0) {
            System.out.println("No existen citas programadas");
            return;
        }
        System.out.println("CITAS REGISTRADAS");
        for (int i = 0; i < citas.size(); i++) {
            Cita cita = citas.get(i);
            System.out.println(
                cita.getFecha().mostrarFecha()
                + " - "
                + cita.getHora()
                + " - "
                + cita.getMascota().getNombre()
                + " - "
                + cita.getEstado()
            );
        }
    }
    public List<Dueno> obtenerDuenos() {
        return duenos;
    }
    public List<Mascota> obtenerMascotas() {
        return mascotas;
    }
    public List<Cita> obtenerCitas() {
        return citas;
    }
    public List<ComprobanteDeAtencion> obtenerComprobantes() {
        return comprobantes;
    }
    public List<Recordatorio> obtenerRecordatorios() {
        return recordatorios;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (validarNombre(nombre)) {
            this.nombre = nombre;
        }
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        if (validarTelefono(telefono)) {
            this.telefono = telefono;
        }
    }

    
    public List<Veterinario> obtenerVeterinarios() {
        return veterinarios;
    }
    public List<String> obtenerRecepcionistas() {
        return recepcionistas;
    }
    public List<Consulta> obtenerConsultas() {
        return consultas;
    }
    public List<ServicioRealizado> obtenerCatalogo() {
        return catalogo;
    }

 

    public static boolean vacio(String texto) {
        return texto == null || texto.trim().equals("");
    }

    private static Integer entero(String texto) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Double decimal(String texto) {
        try {
            return Double.valueOf(texto.trim().replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }

    
    public List<Veterinario> veterinariosDisponibles() {
        List<Veterinario> lista = new ArrayList<>();
        for (int i = 0; i < veterinarios.size(); i++) {
            if (veterinarios.get(i).isDisponible()) {
                lista.add(veterinarios.get(i));
            }
        }
        return lista;
    }

    
    public List<Consulta> consultasPendientesDe(Veterinario vet) {
        List<Consulta> lista = new ArrayList<>();
        for (int i = 0; i < consultas.size(); i++) {
            Consulta c = consultas.get(i);
            if (c.getVeterinario() == vet && !c.isFinalizada()) {
                lista.add(c);
            }
        }
        return lista;
    }

    public ComprobanteDeAtencion buscarComprobante(Consulta consulta) {
        for (int i = 0; i < comprobantes.size(); i++) {
            if (comprobantes.get(i).getConsulta() == consulta) {
                return comprobantes.get(i);
            }
        }
        return null;
    }
    public String registrarConMascotaYConsulta(String nombre, String carnet, String telefono,
        String direccion, String correo, String nombreMascota, String raza, String edad,
        String peso, String sexo, String motivo, Veterinario vet, String especie) {
        return registrarConMascotaYConsulta(nombre, carnet, telefono, direccion, correo,
            nombreMascota, raza, edad, peso, sexo, motivo, vet, especie, "años");
    }

    public String registrarConMascotaYConsulta(String nombre, String carnet, String telefono,
        String direccion, String correo, String nombreMascota, String raza, String edad,
        String peso, String sexo, String motivo, Veterinario vet, String especie, String unidadEdad) {
        if (vacio(carnet)) {
            return "Debe ingresar el carnet del dueño";
        }
        if (!vacio(nombre) && nombre.trim().length() > 25) return "El nombre del dueño no puede superar 25 caracteres";
        if (!vacio(direccion) && direccion.trim().length() > 30) return "La dirección no puede superar 30 caracteres";
        if (telefono == null || !telefono.trim().matches("[0-9]{8}")) return "El teléfono debe contener exactamente 8 números";
        Dueno dueno = buscarDuenoPorCarnet(carnet.trim());
        boolean duenoNuevo = (dueno == null);
        if (duenoNuevo) {
            if (vacio(nombre)) {
                return "Debe ingresar el nombre del dueño";
            }
            Integer tel = entero(telefono == null ? "" : telefono);
            if (tel == null || tel <= 0) {
                return "El teléfono debe ser un número";
            }
            dueno = new Dueno(nombre.trim(), carnet.trim(), tel,
                direccion == null ? "" : direccion.trim(), correo == null ? "" : correo.trim());
        }
        String error = registrarMascotaYConsulta(dueno, nombreMascota, raza, edad, peso,
        sexo, motivo, vet, especie, unidadEdad);
        if (error == null && duenoNuevo) {
            duenos.add(dueno);     
        }
        return error;
    }

    
    public String registrarMascotaYConsulta(Dueno dueno, String nombreMascota, String raza,
            String edad, String peso, String sexo, String motivo, Veterinario vet) {
        return registrarMascotaYConsulta(dueno, nombreMascota, raza, edad, peso, sexo, motivo, vet,
            "", "años");
    }

    public String registrarMascotaYConsulta(Dueno dueno, String nombreMascota, String raza,
            String edad, String peso, String sexo, String motivo, Veterinario vet,
            String especie, String unidadEdad) {
        if (dueno == null) {
            return "Debe elegir un dueño";
        }
        if (!vacio(nombreMascota) && nombreMascota.trim().length() > 20) return "El nombre de la mascota no puede superar 20 caracteres";
        if (!"años".equals(unidadEdad) && !"meses".equals(unidadEdad)) return "Selecciona meses o años para la edad";
        if (vacio(nombreMascota)) {
            return "Debe ingresar el nombre de la mascota";
        }
        if (vet == null || !vet.isDisponible()) {
            return "Debe elegir un veterinario disponible";
        }
        if (vacio(motivo)) {
            return "Debe ingresar el motivo de la consulta";
        }
        Mascota mascota = dueno.buscarMascotaPorNombre(nombreMascota.trim());
        if (mascota == null) {
            Integer años = entero(edad == null ? "" : edad);
            Double kilos = decimal(peso == null ? "" : peso);
            if (vacio(raza)) {
                return "Debe ingresar la raza de la mascota";
            }
            if (años == null || años < 0) {
                return "La edad debe ser un número";
            }
            if (kilos == null || kilos <= 0 || kilos > 100) {
                return "El peso debe ser mayor a 0 y como máximo 100 kg";
            }
            if (vacio(sexo)) {
                return "Debe elegir el sexo de la mascota";
            }
            if (!"Perro".equals(especie) && !"Gato".equals(especie)) {
    return "Debe elegir una especie válida";
}
            mascota = new Mascota(nombreMascota.trim(), raza.trim(), años, kilos, sexo.trim());
            mascota.setEspecie(especie);
            mascota.setUnidadEdad(unidadEdad);
            mascota.setDueno(dueno);
            dueno.getMascotas().add(mascota);
            mascotas.add(mascota);
        }
        return crearConsultaPendiente(mascota, motivo.trim(), vet, null);
    }

    
    public String registrarConsultaParaMascota(Mascota mascota, String motivo, Veterinario vet) {
        if (mascota == null) {
            return "Debe elegir una mascota";
        }
        if (vet == null || !vet.isDisponible()) {
            return "Debe elegir un veterinario disponible";
        }
        if (vacio(motivo)) {
            return "Debe ingresar el motivo de la consulta";
        }
        return crearConsultaPendiente(mascota, motivo.trim(), vet, null);
    }



    private String crearConsultaPendiente(Mascota mascota, String motivo, Veterinario vet, Cita cita) {
        Consulta consulta = new Consulta(Fecha.hoy(), vet, mascota);
        consulta.setMotivo(motivo);
        consulta.setCita(cita);
        consulta.registrar();
        consultas.add(consulta);
        if (mascota.getHistorialClinico() == null) {
            mascota.setHistorialClinico(new HistorialClinico(Fecha.hoy(), "", mascota));
        }
        mascota.getHistorialClinico().agregarConsulta(consulta);
        return null;
    }

 

    public String guardarDatosClinicos(Consulta consulta, String diagnostico, String observacionesDiag,
            String medicamentos, String dosis, String duracion, String indicaciones,
            String vacunasTexto, String observaciones) {
        if (consulta == null) {
            return "No se encontró la consulta";
        }

        consulta.setDiagnostico(new Diagnostico(diagnostico.trim(), observacionesDiag.trim()));
        consulta.setTratamiento(new Tratamiento(medicamentos.trim(), dosis.trim(), duracion.trim(), indicaciones.trim()));
        consulta.setVacunasTexto(vacunasTexto.trim());
        consulta.setObservaciones(observaciones.trim());
        return null;
    }

    public String agregarServicioAConsulta(Consulta consulta, int indiceCatalogo, String cantidad) {
        if (consulta == null) {
            return "No se encontró la consulta";
        }
        if (consulta.isFinalizada()) {
            return "La consulta ya está finalizada";
        }
        if (indiceCatalogo < 0 || indiceCatalogo >= catalogo.size()) {
            return "Debe elegir un servicio del catálogo";
        }
        Integer cant = entero(cantidad == null ? "" : cantidad);
        if (cant == null || cant <= 0) {
            return "La cantidad debe ser un número mayor a 0";
        }
        ServicioRealizado base = catalogo.get(indiceCatalogo);
        ServicioRealizado servicio = new ServicioRealizado(base.getNombre(), base.getDescripcion(), base.getCostoBase());
        servicio.setCantidad(cant);
        consulta.getServicios().add(servicio);
        return null;
    }

    public String registrarVacuna(Consulta consulta, String nombre, String aplicacion,
            String proxima, String observaciones) {
        if (consulta == null) {
            return "No se encontró la consulta";
        }
        if (consulta.isFinalizada()) {
            return "La consulta ya está finalizada";
        }
        if (vacio(nombre)) {
            return "Debe ingresar el nombre de la vacuna";
        }
        Fecha fechaAplicacion = vacio(aplicacion) ? Fecha.hoy() : Fecha.desdeTexto(aplicacion);
        Fecha fechaProxima = Fecha.desdeTexto(proxima);
        if (fechaAplicacion == null) {
            return "La fecha de aplicación no es válida";
        }
        if (fechaProxima == null) {
            return "Debe ingresar una fecha válida para la próxima dosis";
        }
        Vacuna vacuna = new Vacuna(nombre.trim(), fechaAplicacion, fechaProxima);
        vacuna.setObservaciones(observaciones == null ? "" : observaciones.trim());
        vacuna.verificarEstado(Fecha.hoy());
        consulta.getMascota().agregarVacuna(vacuna);
        return null;
    }

    public String finalizarConsulta(Consulta consulta) {
        if (consulta == null) {
            return "No se encontró la consulta";
        }
        if (consulta.isFinalizada()) {
            return "La consulta ya está finalizada";
        }
        if (!consulta.datosClinicosCompletos()) {
            return "Para finalizar debe guardar el diagnóstico y el tratamiento (medicamentos)";
        }
        consulta.setFinalizada(true);     
        if (consulta.getCita() != null) {
            consulta.getCita().cambiarEstado(EstadoCita.ATENDIDA);
        }
        return null;
    }

   

    public String generarComprobante(Consulta consulta, String recepcionista) {
        if (consulta == null) {
            return "Debe elegir una consulta";
        }
        if (!consulta.isFinalizada()) {
            return "La consulta debe estar finalizada";
        }
        if (vacio(recepcionista)) {
            return "Debe elegir un recepcionista";
        }
        if (buscarComprobante(consulta) != null) {
            return "Esta consulta ya tiene comprobante";
        }
        ComprobanteDeAtencion comprobante = new ComprobanteDeAtencion(consulta, consulta.getMascota().getDueno());
        for (int i = 0; i < consulta.getServicios().size(); i++) {
            ServicioRealizado original = consulta.getServicios().get(i);
            ServicioRealizado copia = new ServicioRealizado(original.getNombre(), original.getDescripcion(), original.getCostoBase());
            copia.setCantidad(original.getCantidad());
            comprobante.agregarServicio(copia);
        }
        comprobante.setRecepcionista(recepcionista);
        comprobante.generar();
        comprobantes.add(comprobante);
        return null;
    }

   

    public String programarCita(Mascota mascota, String fecha, String hora, String motivo, Veterinario vet) {
        if (mascota == null) {
            return "Debe elegir una mascota";
        }
        Fecha f = Fecha.desdeTexto(fecha);
        if (f == null) {
            return "Debe ingresar una fecha válida";
        }
        if (vacio(hora)) {
            return "Debe ingresar la hora";
        }
        if (vacio(motivo)) {
            return "Debe ingresar el motivo";
        }
        if (vet == null) {
            return "Debe elegir un veterinario";
        }
        for (Cita existente : citas) {
            if (existente.getEstado() == EstadoCita.PROGRAMADA
                    && existente.getFecha() != null
                    && existente.getFecha().aTexto().equals(f.aTexto())
                    && existente.getHora().trim().equals(hora.trim())) {
                return "Ya existe una cita programada en ese horario";
            }
        }
        Cita cita = new Cita(f, hora.trim(), motivo.trim(), mascota, this, vet);
        cita.programar();
        citas.add(cita);
        return null;
    }

    public String cancelarCitaProgramada(Cita cita) {
        if (cita == null || cita.getEstado() != EstadoCita.PROGRAMADA) {
            return "Solo se pueden cancelar citas programadas";
        }
        cita.cancelar();
        return null;
    }

    
    public String iniciarConsultaDeCita(Cita cita) {
        if (cita == null || cita.getEstado() != EstadoCita.PROGRAMADA) {
            return "Solo se puede iniciar la consulta de una cita programada";
        }
        for (int i = 0; i < consultas.size(); i++) {
            if (consultas.get(i).getCita() == cita) {
                return "Esta cita ya tiene una consulta";
            }
        }
        if (!cita.getVeterinario().isDisponible()) {
            return "El veterinario de esta cita no está disponible";
        }
        return crearConsultaPendiente(cita.getMascota(), cita.getMotivo(), cita.getVeterinario(), cita);
    }

    public String prepararRecordatorioDeCita(Cita cita, String medio) {
        if (cita == null || cita.getEstado() != EstadoCita.PROGRAMADA) {
            return "Solo hay recordatorio para citas programadas";
        }
        if (vacio(medio)) {
            return "Debe elegir el medio de envío";
        }
        Recordatorio r = new Recordatorio("Cita", medio, Fecha.hoy(), cita.getMascota().getDueno(), this, cita.getMascota());
        r.setMensaje("Recordatorio de cita para " + cita.getMascota().getNombre() + ": "
            + cita.getFecha().mostrarFecha() + " a las " + cita.getHora() + " - " + cita.getMotivo());
        recordatorios.add(r);
        return null;
    }

    public String prepararRecordatorioDeVacuna(Mascota mascota, Vacuna vacuna, String medio) {
        if (mascota == null || vacuna == null) {
            return "Debe elegir una vacuna";
        }
        if (vacio(medio)) {
            return "Debe elegir el medio de envío";
        }
        Recordatorio r = new Recordatorio("Vacunación", medio, Fecha.hoy(), mascota.getDueno(), this, mascota);
        r.setMensaje("Recordatorio de vacuna para " + mascota.getNombre() + ": " + vacuna.getNombreVacuna()
            + " - próxima dosis el " + vacuna.getProxAplicacion().mostrarFecha());
        recordatorios.add(r);
        return null;
    }
public String registrarRecordatorioManual(Mascota mascota, String tipo, String mensaje, String medio) {
    if (mascota == null) {
        return "Debe elegir una mascota";
    }
    if (vacio(tipo)) {
        return "Debe elegir el tipo de recordatorio";
    }
    if (vacio(mensaje)) {
        return "Debe redactar el mensaje";
    }
    if (vacio(medio)) {
        return "Debe elegir el medio de envío";
    }

    Recordatorio r = new Recordatorio(
        tipo,
        medio,
        Fecha.hoy(),
        mascota.getDueno(),
        this,
        mascota
    );

    r.setMensaje(mensaje.trim());
    recordatorios.add(r);
    return null;
}

    public String registrarVeterinario(String nombre, String especialidad) {
        if (nombre != null && nombre.trim().length() > 25) return "El nombre del personal no puede superar 25 caracteres";
        if (vacio(nombre) || vacio(especialidad)) {
            return "Debe ingresar el nombre y la especialidad";
        }
        for (int i = 0; i < veterinarios.size(); i++) {
            if (veterinarios.get(i).getNombre().equalsIgnoreCase(nombre.trim())) {
                return "El veterinario ya está registrado";
            }
        }
        Veterinario vet = new Veterinario(nombre.trim(), especialidad.trim());
        vet.registrar();
        veterinarios.add(vet);
        return null;
    }

    public String registrarRecepcionista(String nombre) {
        if (nombre != null && nombre.trim().length() > 25) return "El nombre del personal no puede superar 25 caracteres";
        if (vacio(nombre)) {
            return "Debe ingresar el nombre";
        }
        for (int i = 0; i < recepcionistas.size(); i++) {
            if (recepcionistas.get(i).equalsIgnoreCase(nombre.trim())) {
                return "El recepcionista ya está registrado";
            }
        }
        recepcionistas.add(nombre.trim());
        return null;
    }

    public String agregarServicioAlCatalogo(String nombre, String descripcion, String costo) {
        if (vacio(nombre)) {
            return "Debe ingresar el nombre del servicio";
        }
        Double c = decimal(costo == null ? "" : costo);
        if (c == null || c <= 0) {
            return "El costo debe ser un número mayor a 0";
        }
        catalogo.add(new ServicioRealizado(nombre.trim(), descripcion == null ? "" : descripcion.trim(), c));
        return null;
    }
}

import java.util.ArrayList;
import java.util.List;


public class Persistencia {

    private ArchivoDatos archivo;

    public Persistencia(String nombreArchivo) {
        this.archivo = new ArchivoDatos(nombreArchivo);
    }

   
    private String t(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(";", ",").replace("\r", " ").replace("\n", " ").trim();
    }

  
    public void guardar(Recepcionista r) {
        List<String> l = new ArrayList<String>();

        for (Dueno d : r.obtenerDuenos()) {
            l.add("DUEÑO;" + t(d.getNombre()) + ";" + t(d.getCarnetIdentidad()) + ";" + d.getTelefono()
                + ";" + t(d.getDireccion()) + ";" + t(d.getCorreo()));
        }
        for (Mascota m : r.obtenerMascotas()) {
            l.add("MASCOTA;" + t(m.getNombre()) + ";" + t(m.getRaza()) + ";" + m.getEdad() + ";" + m.getPeso()
                + ";" + t(m.getSexo()) + ";" + t(m.getDueno().getCarnetIdentidad())
                + ";" + t(m.getEspecie()) + ";" + t(m.getUnidadEdad()));
            l.add("EXPEDIENTECOLOR;" + t(m.getDueno().getCarnetIdentidad()) + ";" + t(m.getNombre()) + ";" + t(m.getColorExpediente()));
            for (RegistroExpediente e : m.getRegistrosExpediente()) {
                l.add("EXPEDIENTE;" + t(m.getDueno().getCarnetIdentidad()) + ";" + t(m.getNombre()) + ";"
                    + t(e.tipo) + ";" + t(e.fecha) + ";" + t(e.dato1) + ";" + t(e.dato2) + ";" + t(e.dato3));
            }
            for (Vacuna v : m.getVacunas()) {
                l.add("VACUNA;" + t(m.getDueno().getCarnetIdentidad()) + ";" + t(m.getNombre()) + ";"
                    + t(v.getNombreVacuna()) + ";" + v.getFechaAplicacion().aTexto() + ";"
                    + v.getProxAplicacion().aTexto() + ";" + t(v.getObservaciones()));
            }
        }
        for (Veterinario v : r.obtenerVeterinarios()) {
            l.add("VETERINARIO;" + t(v.getNombre()) + ";" + t(v.getEspecialidad()) + ";"
                + (v.isDisponible() ? "SI" : "NO"));
        }
        for (String nombre : r.obtenerRecepcionistas()) {
            l.add("RECEPCIONISTA;" + t(nombre));
        }
        for (ServicioRealizado s : r.obtenerCatalogo()) {
            l.add("SERVICIO;" + t(s.getNombre()) + ";" + t(s.getDescripcion()) + ";" + s.getCostoBase());
        }
        for (Cita c : r.obtenerCitas()) {
            l.add("CITA;" + c.getFecha().aTexto() + ";" + t(c.getHora()) + ";" + t(c.getMotivo()) + ";"
                + t(c.getMascota().getNombre()) + ";" + t(c.getVeterinario().getNombre()) + ";"
                + c.getEstado() + ";" + t(c.getMascota().getDueno().getCarnetIdentidad()));
        }
        for (int i = 0; i < r.obtenerConsultas().size(); i++) {
            Consulta c = r.obtenerConsultas().get(i);
            String diag = "", diagObs = "", med = "", dosis = "", dur = "", ind = "";
            if (c.getDiagnostico() != null) {
                diag = c.getDiagnostico().getDescripcion();
                diagObs = c.getDiagnostico().getObservaciones();
            }
            if (c.getTratamiento() != null) {
                med = c.getTratamiento().getMedicamentos();
                dosis = c.getTratamiento().getDosis();
                dur = c.getTratamiento().getDuracion();
                ind = c.getTratamiento().getIndicaciones();
            }
            int numeroCita = r.obtenerCitas().indexOf(c.getCita());
            l.add("CONSULTA;" + i + ";" + c.getFecha().aTexto() + ";"
                + t(c.getMascota().getDueno().getCarnetIdentidad()) + ";" + t(c.getMascota().getNombre()) + ";"
                + t(c.getVeterinario().getNombre()) + ";" + t(c.getMotivo()) + ";"
                + (c.isFinalizada() ? "FINALIZADA" : "PENDIENTE") + ";"
                + t(diag) + ";" + t(diagObs) + ";" + t(med) + ";" + t(dosis) + ";" + t(dur) + ";" + t(ind) + ";"
                + t(c.getVacunasTexto()) + ";" + t(c.getObservaciones()) + ";" + numeroCita);
            for (ServicioRealizado s : c.getServicios()) {
                l.add("CONSULTASERVICIO;" + i + ";" + t(s.getNombre()) + ";" + t(s.getDescripcion()) + ";"
                    + s.getCostoBase() + ";" + s.getCantidad());
            }
        }
        for (ComprobanteDeAtencion c : r.obtenerComprobantes()) {
            l.add("COMPROBANTE;" + r.obtenerConsultas().indexOf(c.getConsulta()) + ";"
                + t(c.getRecepcionista()) + ";" + c.getFecha().aTexto() + ";"
                + t(c.getDiagnosticoResumen()) + ";" + t(c.getTratamientoResumen()) + ";" + t(c.getObservacionesResumen()));
            l.add("COMPROBANTESERVICIOSVERSION;" + r.obtenerComprobantes().indexOf(c) + ";1");
            for (ServicioRealizado servicio : c.obtenerServicios()) {
                l.add("COMPROBANTESERVICIO;" + r.obtenerComprobantes().indexOf(c) + ";" + t(servicio.getNombre())
                    + ";" + t(servicio.getDescripcion()) + ";" + servicio.getCostoBase() + ";" + servicio.getCantidad());
            }
        }
        for (Recordatorio x : r.obtenerRecordatorios()) {
    l.add("RECORDATORIO;" + t(x.getMedioEnvio()) + ";" + x.getFechaEnvio().aTexto() + ";"
        + t(x.getMascota().getDueno().getCarnetIdentidad()) + ";" + t(x.getMascota().getNombre()) + ";"
        + t(x.getMensaje()) + ";" + t(x.getTipoRecordatorio()));
}
        archivo.guardarTodo(l);
    }


    public void cargar(Recepcionista r) {
        List<String[]> lineas = archivo.cargar();
      
        String[] orden = {"DUE", "MASCOTA", "VETERINARIO", "RECEPCIONISTA", "SERVICIO", "VACUNA", "CITA",
            "CONSULTA", "CONSULTASERVICIO", "COMPROBANTE", "COMPROBANTESERVICIO", "RECORDATORIO", "EXPEDIENTECOLOR", "EXPEDIENTE"};
        for (int o = 0; o < orden.length; o++) {
            for (int i = 0; i < lineas.size(); i++) {
                String[] d = lineas.get(i);
                String tipo = d[0].toUpperCase();
                if (tipo.startsWith("DUE")) {
                    tipo = "DUE";            
                }
                if (tipo.equals(orden[o])) {
                    try {
                        cargarLinea(r, tipo, d);
                    } catch (Exception e) {
                        System.out.println("Linea ignorada en el archivo: " + String.join(";", d));
                    }
                }
            }
        }

        for (int i = 0; i < r.obtenerComprobantes().size(); i++) {
            boolean guardadoComoPropio = false;
            for (String[] linea : lineas) {
                if (linea.length > 1 && ("COMPROBANTESERVICIO".equalsIgnoreCase(linea[0])
                        || "COMPROBANTESERVICIOSVERSION".equalsIgnoreCase(linea[0]))
                        && String.valueOf(i).equals(linea[1].trim())) {
                    guardadoComoPropio = true;
                    break;
                }
            }
            if (!guardadoComoPropio) {
                ComprobanteDeAtencion comp = r.obtenerComprobantes().get(i);
                for (ServicioRealizado original : comp.getConsulta().getServicios()) {
                    ServicioRealizado copia = new ServicioRealizado(original.getNombre(), original.getDescripcion(), original.getCostoBase());
                    copia.setCantidad(original.getCantidad());
                    comp.agregarServicio(copia);
                }
            }
        }
    }

    private Mascota buscarMascota(Recepcionista r, String carnet, String nombre) {
        Dueno dueno = r.buscarDuenoPorCarnet(carnet);
        if (dueno == null) {
            return null;
        }
        return dueno.buscarMascotaPorNombre(nombre);
    }

    private Veterinario buscarVeterinario(Recepcionista r, String nombre) {
        for (Veterinario v : r.obtenerVeterinarios()) {
            if (v.getNombre().equalsIgnoreCase(nombre)) {
                return v;
            }
        }
        return null;
    }

    private void cargarLinea(Recepcionista r, String tipo, String[] d) {
        if (tipo.equals("DUE")) {
            if (r.buscarDuenoPorCarnet(d[2]) == null) {
                r.obtenerDuenos().add(new Dueno(d[1], d[2], Integer.parseInt(d[3].trim()), d[4], d[5]));
            }
        } else if (tipo.equals("MASCOTA")) {
            Dueno dueno = r.buscarDuenoPorCarnet(d[6]);
            if (dueno != null && dueno.buscarMascotaPorNombre(d[1]) == null) {
                Mascota m = new Mascota(d[1], d[2], Integer.parseInt(d[3].trim()),
                    Double.parseDouble(d[4].trim()), d[5]);
                if (d.length > 7) m.setEspecie(d[7]);
                if (d.length > 8) m.setUnidadEdad(d[8]);
                m.setDueno(dueno);
                dueno.getMascotas().add(m);
                r.obtenerMascotas().add(m);
            }
        } else if (tipo.equals("VETERINARIO")) {
            Veterinario v = new Veterinario(d[1], d[2]);
            v.registrar();
            v.setDisponible(d.length < 4 || !d[3].equals("NO"));
            r.obtenerVeterinarios().add(v);
        } else if (tipo.equals("RECEPCIONISTA")) {
            r.obtenerRecepcionistas().add(d[1]);
        } else if (tipo.equals("SERVICIO")) {
            r.obtenerCatalogo().add(new ServicioRealizado(d[1], d[2], Double.parseDouble(d[3])));
        } else if (tipo.equals("EXPEDIENTECOLOR")) {
            Mascota m = buscarMascota(r, d[1], d[2]);
            if (m != null && d.length >= 4) m.setColorExpediente(d[3]);
        } else if (tipo.equals("EXPEDIENTE")) {
            Mascota m = buscarMascota(r, d[1], d[2]);
            if (m != null && d.length >= 8) m.getRegistrosExpediente().add(
                new RegistroExpediente(d[3], d[4], d[5], d[6], d[7]));
        } else if (tipo.equals("VACUNA")) {
            Mascota m = buscarMascota(r, d[1], d[2]);
            Vacuna v = new Vacuna(d[3], Fecha.desdeTexto(d[4]), Fecha.desdeTexto(d[5]));
            v.setObservaciones(d[6]);
            v.verificarEstado(Fecha.hoy());
            m.agregarVacuna(v);
        } else if (tipo.equals("CITA")) {
            Mascota m = buscarMascota(r, d[7], d[4]);
            Veterinario v = buscarVeterinario(r, d[5]);
            if (m == null || v == null) {
                throw new RuntimeException("cita sin mascota o veterinario");
            }
            Cita c = new Cita(Fecha.desdeTexto(d[1]), d[2], d[3], m, r, v);
            if (d[6].equals("CANCELADA")) {
                c.cancelar();
            } else if (d[6].equals("ATENDIDA")) {
                c.cambiarEstado(EstadoCita.ATENDIDA);
            }
            r.obtenerCitas().add(c);
        } else if (tipo.equals("CONSULTA")) {
            Mascota m = buscarMascota(r, d[3], d[4]);
            Veterinario v = buscarVeterinario(r, d[5]);
            if (m == null || v == null) {
                throw new RuntimeException("consulta sin mascota o veterinario");
            }
            Consulta c = new Consulta(Fecha.desdeTexto(d[2]), v, m);
            c.setMotivo(d[6]);
            if (!d[8].equals("") || !d[10].equals("")) {
                c.setDiagnostico(new Diagnostico(d[8], d[9]));
                c.setTratamiento(new Tratamiento(d[10], d[11], d[12], d[13]));
            }
            c.setVacunasTexto(d[14]);
            c.setObservaciones(d[15]);
            int numeroCita = Integer.parseInt(d[16].trim());
            if (numeroCita >= 0 && numeroCita < r.obtenerCitas().size()) {
                c.setCita(r.obtenerCitas().get(numeroCita));
            }
            c.setFinalizada(d[7].equals("FINALIZADA"));
            c.registrar();
            r.obtenerConsultas().add(c);
            if (m.getHistorialClinico() == null) {
                m.setHistorialClinico(new HistorialClinico(Fecha.hoy(), "", m));
            }
            m.getHistorialClinico().agregarConsulta(c);
        } else if (tipo.equals("CONSULTASERVICIO")) {
            Consulta c = r.obtenerConsultas().get(Integer.parseInt(d[1].trim()));
            ServicioRealizado s = new ServicioRealizado(d[2], d[3], Double.parseDouble(d[4]));
            s.setCantidad(Integer.parseInt(d[5].trim()));
            c.getServicios().add(s);
        } else if (tipo.equals("COMPROBANTE")) {
            Consulta c = r.obtenerConsultas().get(Integer.parseInt(d[1].trim()));
            ComprobanteDeAtencion comp = new ComprobanteDeAtencion(c, c.getMascota().getDueno());

            comp.setRecepcionista(d[2]);
            comp.setFecha(Fecha.desdeTexto(d[3]));
            if (d.length > 4) comp.setDiagnosticoResumen(d[4]);
            if (d.length > 5) comp.setTratamientoResumen(d[5]);
            if (d.length > 6) comp.setObservacionesResumen(d[6]);
            r.obtenerComprobantes().add(comp);
        } else if (tipo.equals("COMPROBANTESERVICIO")) {
            int indice = Integer.parseInt(d[1].trim());
            if (indice >= 0 && indice < r.obtenerComprobantes().size()) {
                ServicioRealizado servicio = new ServicioRealizado(d[2], d[3], Double.parseDouble(d[4]));
                servicio.setCantidad(Integer.parseInt(d[5].trim()));
                r.obtenerComprobantes().get(indice).agregarServicio(servicio);
            }
        } else if (tipo.equals("RECORDATORIO")) {
    Mascota m = buscarMascota(r, d[3], d[4]);
    String tipoRecordatorio = d.length > 6 ? d[6] : "";
    Recordatorio x = new Recordatorio(tipoRecordatorio, d[1], Fecha.desdeTexto(d[2]), m.getDueno(), r, m);
    x.setMensaje(d[5]);
    r.obtenerRecordatorios().add(x);
}
    }
}

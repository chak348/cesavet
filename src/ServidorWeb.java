import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.io.FileInputStream;


public class ServidorWeb {

    private Recepcionista r;
    private Persistencia persistencia;
    private File carpetaWeb;

    private final Map<String, String> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();
    private final Properties usuarios = new Properties();
    private final Properties fichasPersonal = new Properties();
    private final File archivoFichas = new File("personal_fichas.properties");

    public ServidorWeb(Recepcionista r, Persistencia persistencia, String carpeta) {
        this.r = r;
        this.persistencia = persistencia;
        this.carpetaWeb = new File(carpeta);
        if (archivoFichas.isFile()) {
            try (FileInputStream in = new FileInputStream(archivoFichas)) { fichasPersonal.load(in); }
            catch (IOException e) { System.err.println("No se pudo leer fichas de personal: " + e); }
        }
        File config = new File("accesos.properties");
        if (config.isFile()) {
            try (FileInputStream in = new FileInputStream(config)) { usuarios.load(in); }
            catch (IOException e) { System.err.println("No se pudo leer accesos.properties: " + e); }
        }
    }

    public void iniciar(int puerto) throws IOException {
        if (!carpetaWeb.isDirectory()) {
            System.out.println("No se encontro la carpeta \"web\". Ejecuta el programa desde la carpeta del proyecto.");
            return;
        }
      
        HttpServer servidor = HttpServer.create(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), puerto), 0);
        servidor.createContext("/", ex -> {
            try {
                atender(ex);
            } catch (Exception e) {
                e.printStackTrace();
                responder(ex, 500, "text/plain; charset=utf-8", "Error interno: " + e);
            } finally {
                ex.close();
            }
        });
        servidor.start();
        System.out.println("=======================================");
        System.out.println(" CESAVET listo. Abre en el navegador:");
        System.out.println("   http://localhost:" + puerto);
        System.out.println(" (para salir, detén el programa)");
        System.out.println("=======================================");
    }

 
    private String sesion(HttpExchange ex) {
        String cookies = ex.getRequestHeaders().getFirst("Cookie");
        if (cookies == null) return null;
        for (String c : cookies.split(";")) {
            String v = c.trim();
            if (v.startsWith("CESAVET_SESION=")) return sesiones.get(v.substring(15));
        }
        return null;
    }

    private void ir(HttpExchange ex, String url) throws IOException {
        ex.getResponseHeaders().set("Location", url);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(303, -1);
    }

    private String crearToken() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean esVeterinario(HttpExchange ex) {
        return "Veterinario".equals(sesion(ex));
    }

    private boolean esRecepcionista(HttpExchange ex) {
        return "Recepcionista".equals(sesion(ex));
    }

    private boolean accionPermitida(String rol, String accion) {
        if ("Veterinario".equals(rol)) {
            return accion.equals("cita_iniciar") || accion.equals("datos_clinicos")
                || accion.equals("servicio_consulta") || accion.equals("vacuna")
                || accion.equals("finalizar") || accion.equals("expediente_guardar")
                || accion.equals("expediente_eliminar") || accion.equals("expediente_color");
        }
        if ("Recepcionista".equals(rol)) {
            return accion.equals("registrar") || accion.equals("mascota")
                || accion.equals("consulta_nueva") || accion.equals("veterinario")
                || accion.equals("veterinario_estado") || accion.equals("recepcionista")
                || accion.equals("servicio_catalogo") || accion.equals("cita_nueva")
                || accion.equals("cita_cancelar") || accion.equals("comprobante")
                || accion.equals("recordatorio_nuevo") || accion.equals("mascota_editar")
                || accion.equals("comprobante_editar") || accion.equals("comprobante_servicio_guardar")
                || accion.equals("comprobante_servicio_eliminar");
        }
        return false;
    }

    private String adaptarPaginaRol(String html, String rol) {
        if ("Recepcionista".equals(rol)) {


            String estilo = "<style id=\"cesavet-rol-recepcionista\">"
                + ".expediente-editar-btn,.expediente-form-mini,.expediente-acciones-registro,"
                + ".mascota-modal-acciones a[href*=\"editar=1\"],"
                + ".mascota-acciones a[href*=\"editar=1\"]{display:none!important;}"
                + "</style>";
            return html.replace("</head>", estilo + "</head>");
        }
        if (!"Veterinario".equals(rol)) return html;
        if (html.contains("Registrar veterinario") || html.contains("Registrar recepcionista")) {
            html = html.replace("</head>", "<style id=\"personal-consulta\">form[action*=\"/accion/veterinario\"],form[action*=\"/accion/recepcionista\"],form[action*=\"/accion/veterinario_estado\"]{display:none!important;}</style></head>");
        }

        html = html.replaceAll("(?is)<a\\b(?=[^>]*href=\"(?:/)?comprobantes(?:\\.html)?\")[^>]*>.*?</a>", "");
        html = html.replaceAll("(?is)<a\\b(?=[^>]*href=\"(?:/)?recordatorios(?:\\.html)?\")[^>]*>.*?</a>", "");

        String soloLectura = "<style id=\"cesavet-permisos-rol\">"
            + ".ces-duenos .ces-heading>.ces-btn,.ces-duenos #ces-owner-detail a,"
            + ".ces-duenos #modalRegistro,.ces-mascotas .ces-heading>.ces-btn,"
            + ".ces-mascotas #modalRegistro,.ces-mascotas #modalEditarMascota,"
            + ".ces-mascotas #formEditarMascota,.ces-mascotas .mascota-acciones,"
            + ".ces-citas .ces-heading>.ces-btn,.ces-citas #modalRegistro{display:none!important;}"
            + "</style>";
        return html.replace("</head>", soloLectura + "</head>");
    }

    private void atender(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        Map<String, String> params = leerParametros(ex.getRequestURI().getRawQuery());

        if (ruta.equals("/salir")) {
            String cookies = ex.getRequestHeaders().getFirst("Cookie");
            if (cookies != null) for (String c : cookies.split(";")) {
                String v = c.trim();
                if (v.startsWith("CESAVET_SESION=")) sesiones.remove(v.substring(15));
            }
            ex.getResponseHeaders().add("Set-Cookie", "CESAVET_SESION=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
            ir(ex, "/login");
            return;
        }

        if (metodo.equals("POST") && ruta.equals("/ingresar")) {
            String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            params.putAll(leerParametros(cuerpo));
            String rol = params.getOrDefault("rol", "");
            String nombre = params.getOrDefault("nombre", "");
            String clave = params.getOrDefault("clave", "");
            String configurado = usuarios.getProperty(rol + ".nombre", "");
            String secreta = usuarios.getProperty(rol + ".clave", "");
            if ((rol.equals("Recepcionista") || rol.equals("Veterinario"))
                    && !configurado.isEmpty() && configurado.equals(nombre) && !secreta.isEmpty() && secreta.equals(clave)) {
                String token = crearToken();
                sesiones.put(token, rol);
                ex.getResponseHeaders().add("Set-Cookie", "CESAVET_SESION=" + token + "; Path=/; HttpOnly; SameSite=Lax");
                ir(ex, "/index");
            } else {
                ir(ex, "/login?error=1");
            }
            return;
        }

        boolean recurso = ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/img/");
        if (recurso && metodo.equals("GET")) { servirArchivo(ex, ruta); return; }
        if (ruta.equals("/login") || ruta.equals("/login.html")) {
            if (sesion(ex) != null) { ir(ex, "/index"); return; }
            String html = armarPagina("login", params, "");
            html = html.replace("__CES_LOGIN_ERROR__", params.containsKey("error") ?
                "<p class=\"ces-login-error\" role=\"alert\">Nombre o contraseña incorrectos. Inténtalo nuevamente.</p>" : "");
            responder(ex, 200, "text/html; charset=utf-8", html);
            return;
        }
        if (sesion(ex) == null) { ir(ex, "/login"); return; }

        if (metodo.equals("POST")) {
            String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            params.putAll(leerParametros(cuerpo));
            if (ruta.startsWith("/accion/")) {
                String rol = sesion(ex);
                String accion = ruta.substring("/accion/".length());
                if (!accionPermitida(rol, accion)) {
                    responder(ex, 403, "text/plain; charset=utf-8", "No tienes permisos para realizar esta acción");
                    return;
                }
                hacerAccion(ex, accion, params);
            } else responder(ex, 404, "text/plain; charset=utf-8", "No encontrado");
            return;
        }
        if (ruta.equals("/")) ruta = "/index";
        if (ruta.endsWith(".html")) ruta = ruta.substring(0, ruta.length() - 5);
        if (ruta.contains(".")) { servirArchivo(ex, ruta); return; }
        String rolActual = sesion(ex);
        String pagina = ruta.substring(1);
        if ("Veterinario".equals(rolActual)
                && (pagina.equals("comprobantes") || pagina.equals("comprobante")
                    || pagina.equals("recordatorios") || pagina.equals("servicios"))) {
            ir(ex, "/index");
            return;
        }
        if ("Recepcionista".equals(rolActual) && (pagina.equals("atencion") || pagina.equals("consulta"))) {
            ir(ex, "/historial");
            return;
        }
        String html = armarPagina(pagina, params, rolActual);
        if (html == null) responder(ex, 404, "text/html; charset=utf-8", "<h1>Página no encontrada</h1><a href=\"/index\">Volver</a>");
        else {
            String rol = sesion(ex);
            html = html.replace("Recepcionista", esc(rol));
            html = adaptarPaginaRol(html, rol);
            html = html.replace("</body>", "<script defer src=\"/js/buscador-expediente.js\"></script></body>");
            html = html.replace("</body>", "<a class=\"ces-logout\" href=\"/salir\" title=\"Cerrar sesión\">Cerrar sesión</a></body>");
            responder(ex, 200, "text/html; charset=utf-8", html);
        }
    }
    
    private void servirArchivo(HttpExchange ex, String ruta) throws IOException {
        File f = new File(carpetaWeb, ruta).getCanonicalFile();
        String tipo = tipoDeArchivo(ruta);
        boolean dentro = f.getPath().startsWith(carpetaWeb.getCanonicalPath() + File.separator);
        if (!dentro || tipo == null || !f.isFile()) {
            responder(ex, 404, "text/plain; charset=utf-8", "No encontrado");
            return;
        }
        byte[] datos = Files.readAllBytes(f.toPath());
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.sendResponseHeaders(200, datos.length);
        ex.getResponseBody().write(datos);
    }

    private String tipoDeArchivo(String ruta) {
        String x = ruta.toLowerCase();
        if (x.endsWith(".css")) return "text/css; charset=utf-8";
        if (x.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (x.endsWith(".png")) return "image/png";
        if (x.endsWith(".jpg") || x.endsWith(".jpeg")) return "image/jpeg";
        if (x.endsWith(".gif")) return "image/gif";
        if (x.endsWith(".svg")) return "image/svg+xml";
        if (x.endsWith(".webp")) return "image/webp";
        if (x.endsWith(".ico")) return "image/x-icon";
        return null;
    }

    private void responder(HttpExchange ex, int codigo, String tipo, String texto) throws IOException {
        byte[] datos = texto.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.sendResponseHeaders(codigo, datos.length);
        ex.getResponseBody().write(datos);
    }

    private void redirigir(HttpExchange ex, String volver, String mensaje, boolean ok) throws IOException {
        String separador = volver.contains("?") ? "&" : "?";
        String destino = volver + separador + "msg=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8)
            + "&ok=" + (ok ? "1" : "0");
        ex.getResponseHeaders().set("Location", destino);
        ex.sendResponseHeaders(303, -1);
    }

    
    private Map<String, String> leerParametros(String texto) {
        Map<String, String> mapa = new HashMap<String, String>();
        if (texto == null || texto.equals("")) {
            return mapa;
        }
        String[] pares = texto.split("&");
        for (int i = 0; i < pares.length; i++) {
            int igual = pares[i].indexOf('=');
            String clave = igual < 0 ? pares[i] : pares[i].substring(0, igual);
            String valor = igual < 0 ? "" : pares[i].substring(igual + 1);
            clave = URLDecoder.decode(clave, StandardCharsets.UTF_8);
            valor = URLDecoder.decode(valor, StandardCharsets.UTF_8);
            valor = valor.replace(";", ",").replace("\r", " ").replace("\n", " ").trim();
            mapa.put(clave, valor);
        }
        return mapa;
    }

   
    private void hacerAccion(HttpExchange ex, String accion, Map<String, String> p) throws IOException {
        String volver = p.getOrDefault("volver", "/");
        if (!volver.startsWith("/") || volver.startsWith("//")) {
            volver = "/";
        }
        String error = null;
        String ok = "Guardado correctamente";

        if (accion.equals("registrar")) {
    error = r.registrarConMascotaYConsulta(g(p, "nombre"), g(p, "carnet"), g(p, "telefono"),
        g(p, "direccion"), g(p, "correo"), g(p, "mascota"), g(p, "raza"), g(p, "edad"),
        g(p, "peso"), g(p, "sexo"), g(p, "motivo"), veterinario(g(p, "vet")),
        g(p, "especie"), g(p, "unidadEdad"));
            ok = "Consulta pendiente registrada para " + g(p, "mascota");
        }
        else if (accion.equals("mascota")) {
            error = r.registrarMascotaYConsulta(dueno(g(p, "dueno")), g(p, "mascota"), g(p, "raza"),
                g(p, "edad"), g(p, "peso"), g(p, "sexo"), g(p, "motivo"), veterinario(g(p, "vet")),
                g(p, "especie"), g(p, "unidadEdad"));
            ok = "Consulta pendiente registrada para " + g(p, "mascota");
        }
        else if (accion.equals("consulta_nueva")) {
            error = r.registrarConsultaParaMascota(mascota(g(p, "mascota")), g(p, "motivo"), veterinario(g(p, "vet")));
            ok = "Consulta pendiente registrada";
        }
        else if (accion.equals("veterinario")) {
            error = validarFichaPersonal(p);
            if (error == null) error = r.registrarVeterinario(g(p, "nombre"), g(p, "especialidad"));
            if (error == null || error.isEmpty()) guardarFichaPersonal("veterinario", g(p, "nombre"), p);
            ok = "Veterinario registrado";
        }
        else if (accion.equals("veterinario_estado")) {
            Veterinario v = veterinario(g(p, "id"));
            if (v == null) {
                error = "No se encontró el veterinario";
            } else {
                v.setDisponible(!v.isDisponible());
                ok = v.getNombre() + (v.isDisponible() ? " habilitado" : " deshabilitado");
            }
        }
        else if (accion.equals("recepcionista")) {
            error = validarFichaPersonal(p);
            if (error == null) error = r.registrarRecepcionista(g(p, "nombre"));
            if (error == null || error.isEmpty()) guardarFichaPersonal("recepcionista", g(p, "nombre"), p);
            ok = "Recepcionista registrado";
        }
        else if (accion.equals("servicio_catalogo")) {
            error = r.agregarServicioAlCatalogo(g(p, "nombre"), g(p, "descripcion"), g(p, "costo"));
            ok = "Servicio agregado al catálogo";
        }
        else if (accion.equals("cita_nueva")) {
            error = r.programarCita(mascota(g(p, "mascota")), g(p, "fecha"), g(p, "hora"), g(p, "motivo"),
                veterinario(g(p, "vet")));
            ok = "Cita programada";
        }
        else if (accion.equals("cita_cancelar")) {
            error = r.cancelarCitaProgramada(cita(g(p, "id")));
            ok = "Cita cancelada";
        }
        else if (accion.equals("cita_iniciar")) {
            error = r.iniciarConsultaDeCita(cita(g(p, "id")));
            ok = "Consulta pendiente creada. Continúa en Atención veterinaria";
        }
        else if (accion.equals("datos_clinicos")) {
            error = r.guardarDatosClinicos(consulta(g(p, "id")), g(p, "diagnostico"), g(p, "obs_diagnostico"),
                g(p, "medicamentos"), g(p, "dosis"), g(p, "duracion"), g(p, "indicaciones"),
                g(p, "vacunas_texto"), g(p, "observaciones"));
            ok = "Datos clínicos guardados";
        }
        else if (accion.equals("servicio_consulta")) {
            error = r.agregarServicioAConsulta(consulta(g(p, "id")), numero(g(p, "servicio")), g(p, "cantidad"));
            ok = "Servicio agregado a la consulta";
        }
        else if (accion.equals("vacuna")) {
            error = r.registrarVacuna(consulta(g(p, "id")), g(p, "nombre"), g(p, "aplicacion"),
                g(p, "proxima"), g(p, "observaciones"));
            ok = "Vacuna registrada";
        }
        else if (accion.equals("finalizar")) {
            error = r.finalizarConsulta(consulta(g(p, "id")));
            ok = "Consulta finalizada";
        }
        else if (accion.equals("expediente_color")) {
            int indice = numero(g(p, "mascota"));
            Mascota m = mascota(g(p, "mascota"));
            if (m == null) error = "No se encontró la mascota";
            else if (g(p, "color").length() > 60) error = "El color es demasiado largo";
            else { m.setColorExpediente(g(p, "color")); ok = "Color del expediente actualizado"; }
            volver = "/historial?mascota=" + indice + "&editar=1";
        }
        else if (accion.equals("expediente_guardar")) {
            int indice = numero(g(p, "mascota"));
            Mascota m = mascota(g(p, "mascota"));
            String tipo = g(p, "tipo");
            int registro = numero(g(p, "registro"));
            volver = "/historial?mascota=" + indice + "&editar=1";
            if (m == null) error = "No se encontró la mascota";
            else if (!tipo.equals("vacuna") && !tipo.equals("desparasitacion")
                    && !tipo.equals("reproductivo") && !tipo.equals("historia")) error = "Tipo de registro no válido";
            else if (g(p,"fecha").length() > 30 || g(p,"dato1").length() > 500
                    || g(p,"dato2").length() > 500 || g(p,"dato3").length() > 500) error = "El texto es demasiado largo";
            else if (g(p,"fecha").isEmpty() || g(p,"dato1").isEmpty()) error = "Completa la fecha y el dato principal";
            else {
                RegistroExpediente nuevo = new RegistroExpediente(tipo, g(p,"fecha"), g(p,"dato1"),g(p,"dato2"),g(p,"dato3"));
                if (registro >= 0 && registro < m.getRegistrosExpediente().size()
                        && m.getRegistrosExpediente().get(registro).tipo.equals(tipo)) {
                    m.getRegistrosExpediente().set(registro,nuevo);
                    ok = "Registro del expediente actualizado";
                } else if (registro == -1) {
                    m.getRegistrosExpediente().add(nuevo);
                    ok = "Nuevo registro añadido al expediente";
                } else error = "Registro no válido";
            }
        }
        else if (accion.equals("expediente_eliminar")) {
            int indice = numero(g(p, "mascota"));
            Mascota m = mascota(g(p, "mascota"));
            int registro = numero(g(p,"registro"));
            volver = "/historial?mascota=" + indice + "&editar=1";
            if (m == null || registro < 0 || registro >= m.getRegistrosExpediente().size()) error = "No se encontró el registro";
            else { m.getRegistrosExpediente().remove(registro); ok = "Registro eliminado del expediente"; }
        }
        else if (accion.equals("comprobante")) {
            Dueno d = dueno(g(p, "dueno"));
            Mascota m = mascota(g(p, "mascota"));
            Veterinario vet = veterinario(g(p, "veterinario"));
            String nombreRecepcionista = g(p, "recepcionista");
            Consulta consultaSeleccionada = null;

            if (d == null || m == null || vet == null) {
                error = "Debe seleccionar un dueño, una mascota y un veterinario válidos";
            } else if (m.getDueno() != d) {
                error = "La mascota seleccionada no pertenece al dueño elegido";
            } else if (!r.obtenerRecepcionistas().contains(nombreRecepcionista)) {
                error = "Debe seleccionar una recepcionista válida";
            } else {
                for (int i = r.obtenerConsultas().size() - 1; i >= 0; i--) {
                    Consulta c = r.obtenerConsultas().get(i);
                    if (c.getMascota() == m && c.getVeterinario() == vet
                            && c.isFinalizada()
                            && r.buscarComprobante(c) == null) {
                        consultaSeleccionada = c;
                        break;
                    }
                }
                if (consultaSeleccionada == null) {
                    error = "No existe una atención finalizada pendiente de comprobante para esta mascota y veterinario";
                } else {
                    error = r.generarComprobante(consultaSeleccionada, nombreRecepcionista);
                    if (error == null) {
                        ok = "Comprobante generado correctamente. Puedes revisar y editar sus datos.";
                        volver = "/comprobantes?editar=" + (r.obtenerComprobantes().size() - 1);
                    }
                }
            }
        }
        else if (accion.equals("comprobante_editar")) {
            int indice = numero(g(p, "idComprobante"));
            if (indice < 0 || indice >= r.obtenerComprobantes().size()) {
                error = "No se encontró el comprobante seleccionado";
            } else {
                ComprobanteDeAtencion comp = r.obtenerComprobantes().get(indice);
                comp.setDiagnosticoResumen(g(p, "diagnosticoResumen"));
                comp.setTratamientoResumen(g(p, "tratamientoResumen"));
                comp.setObservacionesResumen(g(p, "observacionesResumen"));
                volver = "/comprobantes?editar=" + indice;
                ok = "Comprobante actualizado correctamente";
            }
        }
        else if (accion.equals("comprobante_servicio_guardar")) {
            int indice = numero(g(p, "idComprobante"));
            int fila = numero(g(p, "fila"));
            volver = "/comprobantes?editar=" + indice;
            if (indice < 0 || indice >= r.obtenerComprobantes().size()) error = "Comprobante no encontrado";
            else {
                ComprobanteDeAtencion comp = r.obtenerComprobantes().get(indice);
                String nombre = g(p, "nombreServicio").trim();
                int cantidad = numero(g(p, "cantidad"));
                double precio = -1;
                try { precio = Double.parseDouble(g(p, "precio").replace(',', '.')); } catch (Exception ignored) {}
                if (nombre.isEmpty() || nombre.length() > 50 || cantidad < 1 || cantidad > 9999
                        || !Double.isFinite(precio) || precio < 0 || precio > 1000000) {
                    error = "Completa un servicio, cantidad y precio válidos";
                } else if (fila < -1 || fila >= comp.obtenerServicios().size()) {
                    error = "Fila de servicio no válida";
                } else {
                    ServicioRealizado servicio = new ServicioRealizado(nombre, "Servicio del comprobante", precio);
                    servicio.setCantidad(cantidad);
                    if (fila == -1) comp.obtenerServicios().add(servicio);
                    else comp.obtenerServicios().set(fila, servicio);
                    comp.recalcularTotal();
                    ok = "Servicios del comprobante guardados";
                }
            }
        }
        else if (accion.equals("comprobante_servicio_eliminar")) {
            int indice = numero(g(p, "idComprobante"));
            int fila = numero(g(p, "fila"));
            volver = "/comprobantes?editar=" + indice;
            if (indice < 0 || indice >= r.obtenerComprobantes().size()) error = "Comprobante no encontrado";
            else {
                ComprobanteDeAtencion comp = r.obtenerComprobantes().get(indice);
                if (fila < 0 || fila >= comp.obtenerServicios().size()) error = "Servicio no encontrado";
                else {
                    comp.obtenerServicios().remove(fila);
                    comp.recalcularTotal();
                    ok = "Servicio eliminado del comprobante";
                }
            }
        }
        else if (accion.equals("recordatorio_nuevo")) {
    Mascota m = mascota(g(p, "mascota"));
    error = r.registrarRecordatorioManual(
        m,
        g(p, "tipo"),
        g(p, "mensaje"),
        g(p, "medio")
    );
    if (error == null) ok = "Recordatorio registrado correctamente";
}
        else if (accion.equals("mascota_editar")) {

    int indice = numero(g(p, "idMascota"));

    if (indice < 0 || indice >= r.obtenerMascotas().size()) {
        error = "No se encontró la mascota seleccionada";
    } else {

        Mascota m = r.obtenerMascotas().get(indice);
        Dueno d = m.getDueno();

        String nombre = g(p, "nombre").trim();
        String especie = g(p, "especie").trim();
        String raza = g(p, "raza").trim();
        String edadTexto = g(p, "edad").trim();
        String unidadEdad = g(p, "unidadEdad").trim();
        String pesoTexto = g(p, "peso").trim();
        String sexo = g(p, "sexo").trim();

        String nombreDueno = g(p, "nombreDueno").trim();
        String carnetDueno = g(p, "carnetDueno").trim();
        String telefonoDueno = g(p, "telefonoDueno").trim();
        String direccionDueno = g(p, "direccionDueno").trim();
        String correoDueno = g(p, "correoDueno").trim();

        try {
            int edad = Integer.parseInt(edadTexto);
            double peso = Double.parseDouble(pesoTexto);
            int telefono = Integer.parseInt(telefonoDueno);

            if (d == null
                    || nombre.isEmpty() || nombre.length() > 20
                    || raza.isEmpty() || raza.length() > 50
                    || edad < 0
                    || peso <= 0 || peso > 100
                    || !telefonoDueno.matches("[0-9]{8}")
                    || !especie.equals("Perro") && !especie.equals("Gato")
                    || !sexo.equals("Macho") && !sexo.equals("Hembra")
                    || !unidadEdad.equals("años") && !unidadEdad.equals("meses")
                    || nombreDueno.isEmpty() || nombreDueno.length() > 25
                    || carnetDueno.isEmpty() || carnetDueno.length() > 20
                    || direccionDueno.length() > 30
                    || correoDueno.isEmpty() || correoDueno.length() > 100
                    || !correoDueno.contains("@")) {

                error = "Verifica los datos ingresados";

            } else {

                boolean carnetRepetido = false;

                for (Dueno existente : r.obtenerDuenos()) {
                    if (existente != d &&
                            existente.getCarnetIdentidad().equals(carnetDueno)) {
                        carnetRepetido = true;
                        break;
                    }
                }

                if (carnetRepetido) {
                    error = "El carnet ya pertenece a otro dueño";
                } else {

                    m.actualizar(nombre, raza, edad, peso, sexo);
                    m.setEspecie(especie);
                    m.setUnidadEdad(unidadEdad);

                    d.actualizar(nombreDueno, carnetDueno, telefono,
                            direccionDueno, correoDueno);

                    ok = "Mascota y dueño actualizados correctamente";
                }
            }

        } catch (NumberFormatException e) {
            error = "La edad, el peso y el teléfono deben tener valores numéricos";
        }
    }
}
        else {
            error = "Acción desconocida";
        }

        if (error == null) {
            persistencia.guardar(r);
        }
        if (error == null && accion.equals("cita_iniciar")) {
            ir(ex, volver);
        } else {
            redirigir(ex, volver, error == null ? ok : error, error == null);
        }
    }

    private String g(Map<String, String> p, String clave) {
        return p.getOrDefault(clave, "");
    }

    private int numero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private Veterinario veterinario(String i) {
        int n = numero(i);
        return (n >= 0 && n < r.obtenerVeterinarios().size()) ? r.obtenerVeterinarios().get(n) : null;
    }
    private Dueno dueno(String i) {
        int n = numero(i);
        return (n >= 0 && n < r.obtenerDuenos().size()) ? r.obtenerDuenos().get(n) : null;
    }
    private Mascota mascota(String i) {
        int n = numero(i);
        return (n >= 0 && n < r.obtenerMascotas().size()) ? r.obtenerMascotas().get(n) : null;
    }
    private Cita cita(String i) {
        int n = numero(i);
        return (n >= 0 && n < r.obtenerCitas().size()) ? r.obtenerCitas().get(n) : null;
    }
    private Consulta consulta(String i) {
        int n = numero(i);
        return (n >= 0 && n < r.obtenerConsultas().size()) ? r.obtenerConsultas().get(n) : null;
    }

   
    private String armarPagina(String nombre, Map<String, String> q, String rol) throws IOException {
        File archivo = new File(carpetaWeb, nombre + ".html");
        if (!nombre.matches("[a-z_]+") || !archivo.isFile() || nombre.equals("menu")) {
            return null;
        }
        Map<String, String> v = new HashMap<String, String>();
        v.put("hoy", java.time.LocalDate.now().toString());
        v.put("mensaje", mensajeHtml(q));

        if (nombre.equals("duenos")) {
            paginaDuenos(v);
        } else if (nombre.equals("mascotas")) {
            paginaMascotas(v);
        } else if (nombre.equals("personal")) {
            paginaPersonal(v);
        } else if (nombre.equals("citas")) {
            paginaCitas(v, rol);
        } else if (nombre.equals("consultas")) {
            paginaConsultas(v);
        } else if (nombre.equals("historial")) {
            paginaHistorial(v, rol, q);
        } else if (nombre.equals("servicios")) {
            paginaServicios(v);
        } else if (nombre.equals("comprobantes")) {
            paginaComprobantes(v, q);
        } else if (nombre.equals("comprobante")) {
            if (!paginaComprobante(v, q)) {
                return null;
            }
        } else if (nombre.equals("recordatorios")) {
            paginaRecordatorios(v);
        } else if (nombre.equals("atencion")) {
            paginaAtencion(v, q);
        } else if (nombre.equals("consulta")) {
            if (!paginaConsulta(v, q)) {
                return null;
            }
        }
        return rellenar(archivo, v);
    }

    
    private String rellenar(File archivo, Map<String, String> v) throws IOException {
        String html = new String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
        File menu = new File(carpetaWeb, "menu.html");
        v.put("menu", menu.isFile() ? new String(Files.readAllBytes(menu.toPath()), StandardCharsets.UTF_8) : "");
        for (Map.Entry<String, String> e : v.entrySet()) {
            html = html.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return html.replaceAll("\\{\\{[a-z_]+\\}\\}", "");   
    }


    private String esc(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String mensajeHtml(Map<String, String> q) {
        String msg = q.getOrDefault("msg", "");
        if (msg.equals("")) {
            return "";
        }
        String clase = q.getOrDefault("ok", "0").equals("1") ? "mensaje--ok" : "mensaje--error";
        return "<p class=\"mensaje " + clase + "\">" + esc(msg) + "</p>";
    }

    private String opcion(String valor, String texto) {
        return "<option value=\"" + esc(valor) + "\">" + esc(texto) + "</option>";
    }

    private String dinero(double valor) {
        return "Bs. " + String.format(Locale.US, "%.2f", valor);
    }

    private String tabla(String[] titulos, List<String[]> filas, String vacio) {
        if (filas.size() == 0) {
            return "<p class=\"vacio\">" + esc(vacio) + "</p>";
        }
        StringBuilder sb = new StringBuilder("<table class=\"tabla\"><thead><tr>");
        for (int i = 0; i < titulos.length; i++) {
            sb.append("<th>").append(esc(titulos[i])).append("</th>");
        }
        sb.append("</tr></thead><tbody>");
        for (int i = 0; i < filas.size(); i++) {
            sb.append("<tr>");
            for (int j = 0; j < filas.get(i).length; j++) {
                sb.append("<td>").append(filas.get(i)[j]).append("</td>");   
            }
            sb.append("</tr>");
        }
        return sb.append("</tbody></table>").toString();
    }

  
    private String boton(String accion, String id, String texto, String volver) {
        return "<form class=\"form-boton\" method=\"post\" action=\"/accion/" + accion + "\">"
            + "<input type=\"hidden\" name=\"id\" value=\"" + esc(id) + "\">"
            + "<input type=\"hidden\" name=\"volver\" value=\"" + esc(volver) + "\">"
            + "<button type=\"submit\" class=\"boton boton--chico\">" + esc(texto) + "</button></form>";
    }

    private String estado(String texto) {
        return "<span class=\"estado estado--" + texto.toLowerCase() + "\">" + esc(texto) + "</span>";
    }

    private String opcionesVeterinarios(boolean soloDisponibles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.obtenerVeterinarios().size(); i++) {
            Veterinario v = r.obtenerVeterinarios().get(i);
            if (!soloDisponibles || v.isDisponible()) {
                sb.append(opcion("" + i, v.getNombre() + " - " + v.getEspecialidad()));
            }
        }
        return sb.toString();
    }

    private String avisoSinVeterinarios() {
        if (r.veterinariosDisponibles().size() > 0) {
            return "";
        }
        return "<p class=\"mensaje mensaje--error\">No hay veterinarios disponibles. "
            + "<a href=\"/personal\">Registrar personal</a>.</p>";
    }

    private String opcionesMascotas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.obtenerMascotas().size(); i++) {
            Mascota m = r.obtenerMascotas().get(i);
            sb.append(opcion("" + i, m.getNombre() + " (dueño: " + m.getDueno().getNombre() + ")"));
        }
        return sb.toString();
    }

    

    private void paginaDuenos(Map<String, String> v) {
        v.put("veterinarios", opcionesVeterinarios(true));
        v.put("aviso_veterinarios", avisoSinVeterinarios());
        List<String[]> filas = new ArrayList<String[]>();
        for (Dueno d : r.obtenerDuenos()) {
            String nombres = "";
            for (Mascota m : d.getMascotas()) {
                nombres += (nombres.equals("") ? "" : ", ") + m.getNombre();
            }
            filas.add(new String[] {esc(d.getNombre()), esc(d.getCarnetIdentidad()), "" + d.getTelefono(),
                esc(d.getDireccion()), esc(d.getCorreo()), esc(nombres)});
        }
        v.put("tabla_duenos", tabla(new String[] {"Nombre completo", "Carnet", "Teléfono", "Dirección", "Correo", "Mascotas"},
            filas, "Todavía no hay dueños registrados."));
    }

    private void paginaMascotas(Map<String, String> v) {
        StringBuilder duenos = new StringBuilder();
        for (int i = 0; i < r.obtenerDuenos().size(); i++) {
            Dueno d = r.obtenerDuenos().get(i);
            duenos.append(opcion("" + i, d.getNombre() + " (CI " + d.getCarnetIdentidad() + ")"));
        }
        v.put("duenos_opciones", duenos.toString());
        v.put("veterinarios", opcionesVeterinarios(true));
        v.put("aviso_veterinarios", avisoSinVeterinarios());
        List<String[]> filas = new ArrayList<String[]>();

for (Mascota m : r.obtenerMascotas()) {
    filas.add(new String[] {
        esc(m.getNombre()),
        esc(m.getEspecie()),
        esc(m.getRaza()),
        esc(m.getEdadCompleta()),
        m.getPeso() + " kg",
        esc(m.getSexo()),
        esc(m.getDueno().getNombre())
    });
}

v.put("tabla_mascotas", tabla(
    new String[] {
        "Mascota",
        "Especie",
        "Raza",
        "Edad",
        "Peso",
        "Sexo",
        "Dueño"
    },
    filas,
    "Todavía no hay mascotas registradas."
));
    }

    private String validarFichaPersonal(Map<String, String> datos) {
        String nombre = g(datos, "nombre").trim();
        String telefono = g(datos, "telefono").trim();
        String direccion = g(datos, "direccion").trim();
        if (nombre.isEmpty()) return "Debe ingresar el nombre del personal";
        if (nombre.length() > 25) return "El nombre del personal no puede superar 25 caracteres";
        if (!telefono.matches("[0-9]{8}")) return "El teléfono debe contener exactamente 8 números";
        if (direccion.isEmpty()) return "Debe ingresar la dirección";
        if (direccion.length() > 30) return "La dirección no puede superar 30 caracteres";
        return null;
    }

    private String claveFicha(String cargo, String nombre, String campo) {
        return cargo + "." + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
            nombre.trim().toLowerCase(java.util.Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "." + campo;
    }

    private void guardarFichaPersonal(String cargo, String nombre, Map<String, String> datos) {
        for (String campo : new String[] {"ci", "telefono", "direccion", "correo"}) {
            fichasPersonal.setProperty(claveFicha(cargo, nombre, campo), g(datos, campo).trim());
        }
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(archivoFichas)) {
            fichasPersonal.store(out, "Fichas de personal CESAVET");
        } catch (IOException e) { System.err.println("No se pudieron guardar las fichas: " + e); }
    }

    private String datoFicha(String cargo, String nombre, String campo) {
        String dato = fichasPersonal.getProperty(claveFicha(cargo, nombre, campo), "");
        return esc(dato.isEmpty() ? "—" : dato);
    }

    private void paginaPersonal(Map<String, String> v) {
        List<String[]> filas = new ArrayList<String[]>();
        for (int i = 0; i < r.obtenerVeterinarios().size(); i++) {
            Veterinario vet = r.obtenerVeterinarios().get(i);
            filas.add(new String[] {esc(vet.getNombre()), datoFicha("veterinario", vet.getNombre(), "ci"),
                datoFicha("veterinario", vet.getNombre(), "telefono"),
                datoFicha("veterinario", vet.getNombre(), "direccion"),
                datoFicha("veterinario", vet.getNombre(), "correo"), esc(vet.getEspecialidad()),
                estado(vet.isDisponible() ? "Disponible" : "No disponible"),
                boton("veterinario_estado", "" + i, vet.isDisponible() ? "Deshabilitar" : "Habilitar", "/personal")});
        }
        v.put("tabla_veterinarios", tabla(new String[] {"Veterinario", "CI", "Teléfono", "Dirección", "Correo", "Especialidad", "Estado", ""},
            filas, "Todavía no hay veterinarios registrados."));
        List<String[]> recepcion = new ArrayList<String[]>();
        for (String nombre : r.obtenerRecepcionistas()) {
            recepcion.add(new String[] {esc(nombre), datoFicha("recepcionista", nombre, "ci"),
                datoFicha("recepcionista", nombre, "telefono"),
                datoFicha("recepcionista", nombre, "direccion"),
                datoFicha("recepcionista", nombre, "correo")});
        }
        v.put("tabla_recepcionistas", tabla(new String[] {"Recepcionista", "CI", "Teléfono", "Dirección", "Correo"}, recepcion,
            "Todavía no hay recepcionistas registrados."));
    }

    private void paginaCitas(Map<String, String> v, String rol) {
        v.put("mascotas_opciones", opcionesMascotas());
        v.put("veterinarios", opcionesVeterinarios(true));
        v.put("aviso_veterinarios", avisoSinVeterinarios());
        List<String[]> filas = new ArrayList<String[]>();
        for (int i = 0; i < r.obtenerCitas().size(); i++) {
            Cita c = r.obtenerCitas().get(i);
            String acciones = "";
            if (c.getEstado() == EstadoCita.PROGRAMADA) {
                if ("Veterinario".equals(rol)) {
                    int indiceVet = r.obtenerVeterinarios().indexOf(c.getVeterinario());
                    acciones = boton("cita_iniciar", "" + i, "Iniciar atención", "/atencion?v=" + indiceVet);
                } else {
                    acciones = boton("cita_cancelar", "" + i, "Cancelar", "/citas");
                }
            }
            filas.add(new String[] {c.getFecha().mostrarFecha(), esc(c.getHora()), esc(c.getMascota().getNombre()),
                esc(c.getMotivo()), esc(c.getVeterinario().getNombre()), estado("" + c.getEstado()), acciones});
        }
        v.put("tabla_citas", tabla(new String[] {"Fecha", "Hora", "Mascota", "Motivo", "Veterinario", "Estado", ""},
            filas, "No hay citas programadas."));
    }

    private void paginaConsultas(Map<String, String> v) {
        v.put("mascotas_opciones", opcionesMascotas());
        v.put("veterinarios", opcionesVeterinarios(true));
        v.put("aviso_veterinarios", avisoSinVeterinarios());
        List<String[]> filas = new ArrayList<String[]>();
        for (int i = 0; i < r.obtenerConsultas().size(); i++) {
            Consulta c = r.obtenerConsultas().get(i);
            filas.add(new String[] {"" + (i + 1), c.getFecha().mostrarFecha(), esc(c.getMascota().getNombre()),
                esc(c.getMascota().getDueno().getNombre()), esc(c.getVeterinario().getNombre()), esc(c.getMotivo()),
                estado(c.isFinalizada() ? "Finalizada" : "Pendiente")});
        }
        v.put("tabla_consultas", tabla(new String[] {"N°", "Fecha", "Mascota", "Dueño", "Veterinario", "Motivo", "Estado"},
            filas, "Todavía no hay consultas."));
    }

    private String normalizarBusqueda(String valor) {
        if (valor == null) return "";
        return java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private void paginaHistorial(Map<String, String> v, String rol, Map<String, String> q) {
        int indiceMascota = numero(q.getOrDefault("mascota", ""));
        Mascota seleccionada = indiceMascota >= 0 && indiceMascota < r.obtenerMascotas().size()
                ? r.obtenerMascotas().get(indiceMascota) : null;
        String busqueda = q.getOrDefault("buscar", "").trim();
        StringBuilder resultados = new StringBuilder();
        if (!busqueda.isEmpty()) {
            String termino = normalizarBusqueda(busqueda);
            int coincidencias = 0;
            for (int i = 0; i < r.obtenerMascotas().size(); i++) {
                Mascota mascota = r.obtenerMascotas().get(i);
                Dueno propietario = mascota.getDueno();
                if (normalizarBusqueda(mascota.getNombre()).contains(termino)
                        || (propietario != null && (normalizarBusqueda(propietario.getNombre()).contains(termino)
                        || normalizarBusqueda(propietario.getCarnetIdentidad()).contains(termino)))) {
                    coincidencias++;
                    resultados.append("<a class=\"expediente-resultado\" href=\"/historial?mascota=").append(i)
                        .append("\"><strong>").append(esc(mascota.getNombre())).append("</strong> — ")
                        .append(esc(propietario == null ? "" : propietario.getNombre()))
                        .append(" · CI ").append(esc(propietario == null ? "" : propietario.getCarnetIdentidad()))
                        .append("</a>");
                }
            }
            if (coincidencias == 0) resultados.append("<p>No se encontraron mascotas o propietarios con esos datos.</p>");
        }
        v.put("expediente_resultados", busqueda.isEmpty() ? "" : "<section class=\"expediente-busqueda-resultados\"><h2>Resultados de búsqueda</h2>" + resultados + "</section>");
        v.put("expediente_aviso", mensajeHtml(q));
        v.put("expediente_edicion_boton", "");
        v.put("expediente_edicion_color", "");
        v.put("expediente_form_vacuna", "");
        v.put("expediente_form_desparasitacion", "");
        v.put("expediente_form_reproductivo", "");
        v.put("expediente_form_historia", "");
        if (seleccionada == null) {
            v.put("expediente_dueno", "<p><strong>Nombre completo:</strong> —</p><p><strong>Carnet:</strong> —</p><p><strong>Teléfono:</strong> —</p><p><strong>Dirección:</strong> —</p>");
            v.put("expediente_mascota", "<p><strong>Nombre:</strong> —</p><p><strong>Especie:</strong> —</p><p><strong>Raza:</strong> —</p><p><strong>Sexo:</strong> —</p><p><strong>Color:</strong> —</p>");
            v.put("expediente_vacunas", tablaExpediente(new String[] {"Fecha", "Etiqueta vacunas", "Firma y sello", "Próx. vacuna"}, new ArrayList<String[]>(), 10));
            v.put("expediente_desparasitacion", tablaExpediente(new String[] {"Fecha", "Producto", "Próx. dosis"}, new ArrayList<String[]>(), 5));
            v.put("expediente_reproductivo", tablaExpediente(new String[] {"Anticonceptivo", "Fecha", "Producto"}, new ArrayList<String[]>(), 5));
            v.put("expediente_historia_medica", tablaExpediente(new String[] {"Fecha", "Tratamiento realizado"}, new ArrayList<String[]>(), 6));
            return;
        }
        Dueno dueno = seleccionada.getDueno();
        v.put("expediente_dueno",
            "<p><strong>Nombre completo:</strong> " + esc(dueno == null ? "" : dueno.getNombre()) + "</p>" +
            "<p><strong>Carnet:</strong> " + esc(dueno == null ? "" : dueno.getCarnetIdentidad()) + "</p>" +
            "<p><strong>Teléfono:</strong> " + (dueno == null ? "" : dueno.getTelefono()) + "</p>" +
            "<p><strong>Dirección:</strong> " + esc(dueno == null ? "" : dueno.getDireccion()) + "</p>");
        v.put("expediente_mascota",
            "<p><strong>Nombre:</strong> " + esc(seleccionada.getNombre()) + "</p>" +
            "<p><strong>Especie:</strong> " + esc(seleccionada.getEspecie()) + "</p>" +
            "<p><strong>Raza:</strong> " + esc(seleccionada.getRaza()) + "</p>" +
            "<p><strong>Sexo:</strong> " + esc(seleccionada.getSexo()) + "</p>" +
            "<p><strong>Color:</strong> " + esc(seleccionada.getColorExpediente()) + "</p>");
        boolean editar = "Veterinario".equals(rol) && "1".equals(q.get("editar"));
        v.put("expediente_edicion_boton", !"Veterinario".equals(rol) ? "" : "<a class=\"expediente-editar-btn\" href=\"/historial?mascota=" + indiceMascota
            + (editar ? "\">Terminar edición</a>" : "&editar=1\">Editar expediente</a>"));
        if (editar) {
            v.put("expediente_edicion_color", "<form class=\"expediente-form-mini\" method=\"post\" action=\"/accion/expediente_color\">"
                + "<input type=\"hidden\" name=\"mascota\" value=\"" + indiceMascota + "\">"
                + "<label>Color de la mascota <input name=\"color\" maxlength=\"60\" value=\"" + esc(seleccionada.getColorExpediente()) + "\"></label>"
                + "<button type=\"submit\">Guardar color</button></form>");
            v.put("expediente_form_vacuna", formularioExpediente(indiceMascota,"vacuna", "Vacuna / etiqueta", "Firma y sello", "Próxima vacuna", q, seleccionada));
            v.put("expediente_form_desparasitacion", formularioExpediente(indiceMascota,"desparasitacion", "Producto", "Próxima dosis", "Observaciones", q, seleccionada));
            v.put("expediente_form_reproductivo", formularioExpediente(indiceMascota,"reproductivo", "Anticonceptivo", "Producto", "Observaciones", q, seleccionada));
            v.put("expediente_form_historia", formularioExpediente(indiceMascota,"historia", "Tratamiento realizado", "Diagnóstico", "Observaciones", q, seleccionada));
        }
        List<String[]> filasVacunas = new ArrayList<String[]>();
        for (Vacuna vacuna : seleccionada.getVacunas()) {
            filasVacunas.add(new String[] {vacuna.getFechaAplicacion().mostrarFecha(), esc(vacuna.getNombreVacuna()), "", vacuna.getProxAplicacion().mostrarFecha()});
        }
        List<String[]> filasDesparasitacion = new ArrayList<String[]>();
        List<String[]> filasReproductivo = new ArrayList<String[]>();
        List<String[]> filasHistoria = new ArrayList<String[]>();
        for (Consulta consulta : r.obtenerConsultas()) {
            if (consulta.getMascota() == seleccionada) {
                Diagnostico diagnostico = consulta.getDiagnostico();
                Tratamiento tratamiento = consulta.getTratamiento();
                String detalle = "";
                if (diagnostico != null && diagnostico.getDescripcion() != null && !diagnostico.getDescripcion().trim().isEmpty()) {
                    detalle += "<strong>Diagnóstico:</strong> " + esc(diagnostico.getDescripcion()) + "<br>";
                }
                if (tratamiento != null) {
                    detalle += "<strong>Tratamiento:</strong> " + esc(tratamiento.getMedicamentos());
                    if (tratamiento.getDosis() != null && !tratamiento.getDosis().trim().isEmpty()) detalle += " · <strong>Dosis:</strong> " + esc(tratamiento.getDosis());
                    if (tratamiento.getIndicaciones() != null && !tratamiento.getIndicaciones().trim().isEmpty()) detalle += "<br>" + esc(tratamiento.getIndicaciones());
                }
                if (detalle.isEmpty()) detalle = esc(consulta.getMotivo() == null ? "" : consulta.getMotivo());
                filasHistoria.add(new String[] {consulta.getFecha().mostrarFecha(), detalle});
            }
        }
        for (int i = 0; i < seleccionada.getRegistrosExpediente().size(); i++) {
            RegistroExpediente registro = seleccionada.getRegistrosExpediente().get(i);
            String acciones = editar ? "<div class=\"expediente-acciones-registro\"><a href=\"/historial?mascota="+indiceMascota+"&editar=1&registro="+i+"&tipo="+esc(registro.tipo)+"\">Editar</a> "
                + "<form method=\"post\" action=\"/accion/expediente_eliminar\" onsubmit=\"return confirm('¿Eliminar este registro?')\">"
                + "<input type=\"hidden\" name=\"mascota\" value=\""+indiceMascota+"\"><input type=\"hidden\" name=\"registro\" value=\""+i+"\"><button type=\"submit\">Eliminar</button></form></div>" : "";
            if (registro.tipo.equals("vacuna")) {
                filasVacunas.add(new String[]{esc(registro.fecha), esc(registro.dato1)+acciones, esc(registro.dato2),esc(registro.dato3)});
            } else if (registro.tipo.equals("desparasitacion")) {
                filasDesparasitacion.add(new String[]{esc(registro.fecha),esc(registro.dato1)+acciones,esc(registro.dato2)});
            } else if (registro.tipo.equals("reproductivo")) {
                filasReproductivo.add(new String[]{esc(registro.dato1)+acciones,esc(registro.fecha),esc(registro.dato2)});
            } else if (registro.tipo.equals("historia")) {
                String detalle = esc(registro.dato1);
                if (!registro.dato2.isEmpty()) detalle += "<br><strong>Diagnóstico:</strong> " + esc(registro.dato2);
                if (!registro.dato3.isEmpty()) detalle += "<br><strong>Observaciones:</strong> " + esc(registro.dato3);
                filasHistoria.add(new String[]{esc(registro.fecha),detalle+acciones});
            }
        }
        v.put("expediente_vacunas",tablaExpediente(new String[]{"Fecha", "Etiqueta vacunas", "Firma y sello", "Próx. vacuna"},filasVacunas,Math.max(10,filasVacunas.size())));
        v.put("expediente_desparasitacion",tablaExpediente(new String[]{"Fecha", "Producto", "Próx. dosis"},filasDesparasitacion,Math.max(5,filasDesparasitacion.size())));
        v.put("expediente_reproductivo",tablaExpediente(new String[]{"Anticonceptivo", "Fecha", "Producto"},filasReproductivo,Math.max(5,filasReproductivo.size())));
        v.put("expediente_historia_medica",tablaExpediente(new String[]{"Fecha", "Tratamiento realizado"},filasHistoria,Math.max(6,filasHistoria.size())));
    }

    private String formularioExpediente(int mascotaId, String tipo, String etiqueta1, String etiqueta2,
            String etiqueta3, Map<String,String> q, Mascota mascotaActual) {
        int id = numero(q.getOrDefault("registro", ""));
        RegistroExpediente actual = id >= 0 && id < mascotaActual.getRegistrosExpediente().size()
            && tipo.equals(q.get("tipo")) && mascotaActual.getRegistrosExpediente().get(id).tipo.equals(tipo)
            ? mascotaActual.getRegistrosExpediente().get(id) : null;
        String fecha = actual == null ? java.time.LocalDate.now().toString() : actual.fecha;
        String a = actual == null ? "" : actual.dato1;
        String b = actual == null ? "" : actual.dato2;
        String c = actual == null ? "" : actual.dato3;
        return "<form class=\"expediente-form-mini\" method=\"post\" action=\"/accion/expediente_guardar\">"
            + "<input type=\"hidden\" name=\"mascota\" value=\"" + mascotaId + "\">"
            + "<input type=\"hidden\" name=\"tipo\" value=\"" + esc(tipo) + "\">"
            + "<input type=\"hidden\" name=\"registro\" value=\"" + (actual == null ? -1 : id) + "\">"
            + "<label>Fecha <input type=\"date\" name=\"fecha\" required value=\"" + esc(fecha) + "\"></label>"
            + "<label>" + esc(etiqueta1) + " <input name=\"dato1\" required maxlength=\"500\" value=\"" + esc(a) + "\"></label>"
            + "<label>" + esc(etiqueta2) + " <input name=\"dato2\" maxlength=\"500\" value=\"" + esc(b) + "\"></label>"
            + "<label>" + esc(etiqueta3) + " <input name=\"dato3\" maxlength=\"500\" value=\"" + esc(c) + "\"></label>"
            + "<button type=\"submit\">" + (actual == null ? "Añadir registro" : "Guardar cambios") + "</button></form>";
    }

    private String tablaExpediente(String[] encabezados, List<String[]> filas, int filasMinimas) {
        StringBuilder html = new StringBuilder("<table class=\"tabla-hoja\"><thead><tr>");
        for (String encabezado : encabezados) {
            html.append("<th>").append(esc(encabezado)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        int cantidad = Math.max(filasMinimas, filas.size());
        for (int i = 0; i < cantidad; i++) {
            html.append("<tr>");
            if (i < filas.size()) {
                String[] fila = filas.get(i);
                for (String celda : fila) {
                    html.append("<td>").append(celda == null ? "" : celda).append("</td>");
                }
            } else {
                for (int j = 0; j < encabezados.length; j++) html.append("<td>&nbsp;</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private void paginaServicios(Map<String, String> v) {
        List<String[]> filas = new ArrayList<String[]>();
        for (int i = 0; i < r.obtenerCatalogo().size(); i++) {
            ServicioRealizado s = r.obtenerCatalogo().get(i);
            filas.add(new String[] {"" + (i + 1), esc(s.getNombre()), esc(s.getDescripcion()), dinero(s.getCostoBase())});
        }
        v.put("tabla_servicios", tabla(new String[] {"N°", "Servicio", "Descripción", "Costo"},
            filas, "El catálogo está vacío."));
    }

    private void paginaComprobantes(Map<String, String> v, Map<String, String> q) {

        v.put("mensaje", q.getOrDefault("ok", "0").equals("1") ? mensajeHtml(q) : "");
        String avisoComp = q.getOrDefault("ok", "0").equals("1") ? "" : q.getOrDefault("msg", "");
        v.put("comp_error_modal", avisoComp.isEmpty() ? "" : "<p class=\"comp-error-inline\" role=\"alert\">" + esc(avisoComp) + "</p>");
        int seleccionado = numero(q.getOrDefault("editar", ""));
        if (seleccionado < 0 && !r.obtenerComprobantes().isEmpty()) seleccionado = r.obtenerComprobantes().size() - 1;
        ComprobanteDeAtencion comp = seleccionado >= 0 && seleccionado < r.obtenerComprobantes().size() ? r.obtenerComprobantes().get(seleccionado) : null;
        if (comp == null) {
            v.put("comp_numero", "—"); v.put("comp_fecha", "—"); v.put("comp_recepcionista", "—");
            v.put("comp_dueno", "—"); v.put("comp_carnet", "—"); v.put("comp_mascota", "—"); v.put("comp_especie_edad", "—");
            v.put("comp_veterinario", "—"); v.put("comp_diagnostico", ""); v.put("comp_tratamiento", ""); v.put("comp_observaciones", "");
            v.put("comp_motivo", "—"); v.put("comp_servicios_form", ""); v.put("comp_servicios_preview", ""); v.put("comp_subtotal", dinero(0)); v.put("comp_total", dinero(0));
            v.put("comp_formulario_edicion", "<p class=\"ayuda\">Genera un comprobante a partir de una atención finalizada para cargar sus datos.</p>");
            v.put("comp_servicios_editor", "");
        } else {
            Consulta c = comp.getConsulta(); Mascota m = c.getMascota();
            String numeroComp = String.format(Locale.US, "CP-%d-%06d", java.time.LocalDate.now().getYear(), seleccionado + 1);
            double subtotal = comp.getCostoTotal();
            v.put("comp_numero", numeroComp); v.put("comp_fecha", comp.getFecha().mostrarFecha()); v.put("comp_recepcionista", esc(comp.getRecepcionista()));
            v.put("comp_dueno", esc(comp.getDueno().getNombre())); v.put("comp_carnet", esc(comp.getDueno().getCarnetIdentidad()));
            v.put("comp_mascota", esc(m.getNombre())); v.put("comp_especie_edad", esc(m.getEspecie()) + " - " + m.getEdad() + " " + esc(m.getUnidadEdad()));
            v.put("comp_veterinario", esc(c.getVeterinario().getNombre())); v.put("comp_motivo", esc(c.getMotivo()));
            v.put("comp_diagnostico", esc(comp.getDiagnosticoResumen())); v.put("comp_tratamiento", esc(comp.getTratamientoResumen())); v.put("comp_observaciones", esc(comp.getObservacionesResumen()));
            StringBuilder filasForm = new StringBuilder(), filasPreview = new StringBuilder(); int n=1;
            for (ServicioRealizado srv : comp.obtenerServicios()) {
                filasForm.append("<tr><td>").append(n).append("</td><td>").append(esc(srv.getNombre())).append("</td><td>").append(srv.getCantidad()).append("</td><td>").append(dinero(srv.getCostoBase())).append("</td><td>").append(dinero(srv.getSubtotal())).append("</td></tr>");
                filasPreview.append("<tr><td>").append(n++).append("</td><td>").append(esc(srv.getNombre())).append("</td><td>").append(srv.getCantidad()).append("</td><td>").append(dinero(srv.getSubtotal())).append("</td></tr>");
            }
            v.put("comp_servicios_form", filasForm.toString()); v.put("comp_servicios_preview", filasPreview.toString());
            StringBuilder editor = new StringBuilder("<div class=\"comp-servicios-editor\"><h3 class=\"comp-section-title\">Editar servicios e importes</h3>");
            int filaServicio = 0;
            for (ServicioRealizado servicio : comp.obtenerServicios()) {
                editor.append("<div class=\"comp-servicio-editar-fila\"><form method=\"post\" action=\"/accion/comprobante_servicio_guardar\" class=\"comp-servicio-fila-form\">")
                    .append("<input type=\"hidden\" name=\"idComprobante\" value=\"").append(seleccionado).append("\">")
                    .append("<input type=\"hidden\" name=\"fila\" value=\"").append(filaServicio).append("\">")
                    .append("<label>Servicio<input name=\"nombreServicio\" maxlength=\"50\" required value=\"").append(esc(servicio.getNombre())).append("\"></label>")
                    .append("<label>Cantidad<input type=\"number\" name=\"cantidad\" min=\"1\" max=\"9999\" required value=\"").append(servicio.getCantidad()).append("\"></label>")
                    .append("<label>Precio (Bs.)<input type=\"number\" name=\"precio\" min=\"0\" max=\"1000000\" step=\"0.01\" required value=\"").append(servicio.getCostoBase()).append("\"></label>")
                    .append("<button class=\"ces-btn comp-light-btn\" type=\"submit\">Actualizar</button></form>")
                    .append("<form method=\"post\" action=\"/accion/comprobante_servicio_eliminar\" onsubmit=\"return confirm('¿Eliminar este servicio del comprobante?')\">")
                    .append("<input type=\"hidden\" name=\"idComprobante\" value=\"").append(seleccionado).append("\">")
                    .append("<input type=\"hidden\" name=\"fila\" value=\"").append(filaServicio++).append("\">")
                    .append("<button type=\"submit\" class=\"comp-eliminar-servicio\">Eliminar</button></form></div>");
            }
            editor.append("<form class=\"comp-servicio-fila-form comp-servicio-nuevo\" method=\"post\" action=\"/accion/comprobante_servicio_guardar\">")
                .append("<input type=\"hidden\" name=\"idComprobante\" value=\"").append(seleccionado).append("\">")
                .append("<input type=\"hidden\" name=\"fila\" value=\"-1\">")
                .append("<label>Nuevo servicio<input name=\"nombreServicio\" maxlength=\"50\" required placeholder=\"Nombre del servicio\"></label>")
                .append("<label>Cantidad<input type=\"number\" name=\"cantidad\" min=\"1\" max=\"9999\" required value=\"1\"></label>")
                .append("<label>Precio (Bs.)<input type=\"number\" name=\"precio\" min=\"0\" max=\"1000000\" step=\"0.01\" required value=\"0.00\"></label>")
                .append("<button class=\"ces-btn comp-light-btn\" type=\"submit\">Añadir servicio</button></form></div>");
            v.put("comp_servicios_editor", editor.toString());
            v.put("comp_subtotal", dinero(subtotal)); v.put("comp_total", dinero(subtotal));
            v.put("comp_formulario_edicion", "<form action=\"/accion/comprobante_editar\" method=\"post\" class=\"comp-edit-form\"><input type=\"hidden\" name=\"idComprobante\" value=\"" + seleccionado + "\"><input type=\"hidden\" name=\"volver\" value=\"/comprobantes?editar=" + seleccionado + "\"><label class=\"comp-field\"><span>Diagnóstico resumido</span><textarea name=\"diagnosticoResumen\" rows=\"3\">" + esc(comp.getDiagnosticoResumen()) + "</textarea></label><label class=\"comp-field\"><span>Tratamiento resumido</span><textarea name=\"tratamientoResumen\" rows=\"3\">" + esc(comp.getTratamientoResumen()) + "</textarea></label><label class=\"comp-field\"><span>Observaciones</span><textarea name=\"observacionesResumen\" rows=\"3\">" + esc(comp.getObservacionesResumen()) + "</textarea></label><button class=\"ces-btn comp-light-btn\" type=\"submit\">Guardar cambios</button></form>");
        }

StringBuilder opcionesDuenos = new StringBuilder();
for (int i = 0; i < r.obtenerDuenos().size(); i++) {
    Dueno d = r.obtenerDuenos().get(i);
    opcionesDuenos.append(opcion("" + i, d.getNombre()));
}
v.put("comprobante_duenos", opcionesDuenos.toString());

StringBuilder opcionesMascotas = new StringBuilder();
for (int i = 0; i < r.obtenerMascotas().size(); i++) {
    Mascota m = r.obtenerMascotas().get(i);
    int indiceDueno = -1;
    for (int j = 0; j < r.obtenerDuenos().size(); j++) {
        if (m.getDueno() == r.obtenerDuenos().get(j)) {
            indiceDueno = j;
            break;
        }
    }
    opcionesMascotas.append("<option value=\"" + i + "\" data-dueno=\"" + indiceDueno
        + "\">" + esc(m.getNombre()) + "</option>");
}
v.put("comprobante_mascotas", opcionesMascotas.toString());


StringBuilder opcionesVeterinarios = new StringBuilder();
for (int i = 0; i < r.obtenerVeterinarios().size(); i++) {
    Veterinario vet = r.obtenerVeterinarios().get(i);
    for (int j = 0; j < r.obtenerMascotas().size(); j++) {
        Mascota m = r.obtenerMascotas().get(j);
        boolean disponible = false;
        for (int k = r.obtenerConsultas().size() - 1; k >= 0; k--) {
            Consulta c = r.obtenerConsultas().get(k);
            if (c.getMascota() == m && c.getVeterinario() == vet && c.isFinalizada()
                    && r.buscarComprobante(c) == null) {
                disponible = true;
                break;
            }
        }
        if (disponible) {
            opcionesVeterinarios.append("<option value=\"" + i + "\" data-mascota=\"" + j
                + "\">" + esc(vet.getNombre()) + "</option>");
        }
    }
}
v.put("comprobante_veterinarios", opcionesVeterinarios.toString());

        StringBuilder consultas = new StringBuilder();
        for (int i = 0; i < r.obtenerConsultas().size(); i++) {
            Consulta c = r.obtenerConsultas().get(i);
            if (c.isFinalizada() && r.buscarComprobante(c) == null) {
                String sufijo = c.getServicios().size() == 0 ? " [agregar servicios en comprobante]" : "";
                consultas.append(opcion("" + i, "N° " + (i + 1) + " - " + c.getMascota().getNombre()
                    + " (" + c.getFecha().mostrarFecha() + ")" + sufijo));
            }
        }
        v.put("consultas_opciones", consultas.toString());
        

        StringBuilder recep = new StringBuilder();
        for (String nombre : r.obtenerRecepcionistas()) {
            recep.append(opcion(nombre, nombre));
        }
        v.put("recepcionistas_opciones", recep.toString());
        v.put("aviso_recepcionistas", r.obtenerRecepcionistas().size() > 0 ? ""
            : "<p class=\"mensaje mensaje--error\">No hay recepcionistas. <a href=\"/personal\">Regístralo en Personal (3)</a>.</p>");
        List<String[]> filas = new ArrayList<String[]>();
        for (int i = 0; i < r.obtenerComprobantes().size(); i++) {
            ComprobanteDeAtencion c = r.obtenerComprobantes().get(i);
            filas.add(new String[] {"" + (i + 1), c.getFecha().mostrarFecha(), esc(c.getConsulta().getMascota().getNombre()),
                esc(c.getDueno().getNombre()), esc(c.getRecepcionista()), dinero(c.getCostoTotal()),
                "<a class=\"boton boton--chico\" href=\"/comprobante?id=" + i + "\">Ver</a> "
                + "<a class=\"boton boton--chico\" href=\"/comprobantes?editar=" + i + "\">Editar</a>"});
        }
        v.put("tabla_comprobantes", tabla(new String[] {"N°", "Fecha", "Mascota", "Dueño", "Recepcionista", "Total", ""},
            filas, "Todavía no hay comprobantes."));
    }

    private boolean paginaComprobante(Map<String, String> v, Map<String, String> q) {
        int n = numero(q.getOrDefault("id", ""));
        if (n < 0 || n >= r.obtenerComprobantes().size()) {
            return false;
        }
        ComprobanteDeAtencion c = r.obtenerComprobantes().get(n);
        Consulta consulta = c.getConsulta();
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Fecha:</strong> ").append(c.getFecha().mostrarFecha()).append("</p>");
        sb.append("<p><strong>Mascota:</strong> ").append(esc(consulta.getMascota().getNombre())).append("</p>");
        sb.append("<p><strong>Dueño:</strong> ").append(esc(c.getDueno().getNombre())).append("</p>");
        sb.append("<p><strong>Veterinario:</strong> ").append(esc(consulta.getVeterinario().getNombre())).append("</p>");
        sb.append("<p><strong>Atendido por (recepción):</strong> ").append(esc(c.getRecepcionista())).append("</p>");
        sb.append("<p><strong>Diagnóstico:</strong> ").append(esc(c.getDiagnosticoResumen())).append("</p>");
        sb.append("<p><strong>Tratamiento:</strong> ").append(esc(c.getTratamientoResumen())).append("</p>");
        sb.append("<p><strong>Observaciones:</strong> ").append(esc(c.getObservacionesResumen())).append("</p>");
        List<String[]> filas = new ArrayList<String[]>();
        for (ServicioRealizado s : c.obtenerServicios()) {
            filas.add(new String[] {esc(s.getNombre()), "" + s.getCantidad(), dinero(s.getCostoBase()), dinero(s.getSubtotal())});
        }
        sb.append(tabla(new String[] {"Servicio", "Cantidad", "Costo", "Subtotal"}, filas, ""));
        sb.append("<p class=\"total\">TOTAL: ").append(dinero(c.getCostoTotal())).append("</p>");
        v.put("numero", "" + (n + 1));
        v.put("detalle", sb.toString());
        return true;
    }

    
private void paginaRecordatorios(Map<String, String> v) {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        List<java.util.Map.Entry<java.time.LocalDate, String[]>> alertasOrdenadas = new ArrayList<>();
        for (Mascota m : r.obtenerMascotas()) {
            for (Vacuna vacuna : m.getVacunas()) {
                Fecha f = vacuna.getProxAplicacion();
                if (f == null) continue;
                try {
                    java.time.LocalDate proxima = java.time.LocalDate.of(f.getAño(), f.getMes(), f.getDia());
                    long dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, proxima);
                    if (dias <= 30) {
                        String estado = dias < 0 ? "Vencida hace " + (-dias) + " días"
                            : dias == 0 ? "Vence hoy" : "Próxima en " + dias + " días";
                        alertasOrdenadas.add(new java.util.AbstractMap.SimpleEntry<>(proxima,
                            new String[] {esc(m.getNombre()),
                            esc(m.getDueno() == null ? "" : m.getDueno().getNombre()),
                            esc(vacuna.getNombreVacuna()), esc(f.mostrarFecha()), estado}));
                    }
                } catch (java.time.DateTimeException ignorada) { }
            }
        }
        alertasOrdenadas.sort(java.util.Map.Entry.comparingByKey());
        List<String[]> alertas = new ArrayList<>();
        for (java.util.Map.Entry<java.time.LocalDate, String[]> fila : alertasOrdenadas) alertas.add(fila.getValue());
        v.put("alertas_vacunacion", tabla(
            new String[] {"Mascota", "Dueño", "Vacuna", "Próxima dosis", "Estado"},
            alertas, "No hay alertas pendientes."));

        StringBuilder opcionesDuenos = new StringBuilder();
        for (int i = 0; i < r.obtenerDuenos().size(); i++) {
            Dueno d = r.obtenerDuenos().get(i);
            opcionesDuenos.append(opcion("" + i, d.getNombre()));
        }
        v.put("recordatorio_duenos", opcionesDuenos.toString());

        StringBuilder opcionesMascotas = new StringBuilder();
        for (int i = 0; i < r.obtenerMascotas().size(); i++) {
            Mascota m = r.obtenerMascotas().get(i);
            int indiceDueno = -1;
            for (int j = 0; j < r.obtenerDuenos().size(); j++) {
                if (m.getDueno() == r.obtenerDuenos().get(j)) {
                    indiceDueno = j;
                    break;
                }
            }
            opcionesMascotas.append("<option value=\"").append(i)
                .append("\" data-dueno=\"").append(indiceDueno).append("\">")
                .append(esc(m.getNombre())).append("</option>");
        }
        v.put("recordatorio_mascotas", opcionesMascotas.toString());

        List<String[]> filas = new ArrayList<String[]>();

        for (Recordatorio x : r.obtenerRecordatorios()) {
            String mascotaHtml =
                "<strong>" + esc(x.getMascota().getNombre()) + "</strong><br>"
                + "<small>" + esc(x.getMascota().getRaza()) + "</small>";
            filas.add(new String[] {
    mascotaHtml,
    esc(x.getDueno().getNombre()),
    esc(x.getTipoRecordatorio()),
    esc(x.getMensaje()),
    x.getFechaEnvio().mostrarFecha(),
    esc(x.getMedioEnvio())
});
}

v.put("tabla_recordatorios",
    tabla(
        new String[] {
            "Mascota",
            "Dueño",
            "Tipo de recordatorio",
            "Mensaje",
            "Fecha de envío",
            "Canal"
        },
        filas,
        "Todavía no hay recordatorios."
    )
);
}


    private void paginaAtencion(Map<String, String> v, Map<String, String> q) {
        int elegido = numero(q.getOrDefault("v", ""));
        StringBuilder vets = new StringBuilder();
        for (int i = 0; i < r.obtenerVeterinarios().size(); i++) {
            Veterinario vet = r.obtenerVeterinarios().get(i);
            vets.append("<option value=\"" + i + "\"" + (i == elegido ? " selected" : "") + ">"
                + esc(vet.getNombre() + " - " + vet.getEspecialidad()) + "</option>");
        }
        v.put("veterinarios", vets.toString());
        if (r.obtenerVeterinarios().size() == 0) {
            v.put("pendientes", "<p class=\"mensaje mensaje--error\">No hay veterinarios. <a href=\"/personal\">Regístralo en Personal (3)</a>.</p>");
            return;
        }
        Veterinario vet = veterinario(q.getOrDefault("v", ""));
        if (vet == null) {
            v.put("pendientes", "<p class=\"vacio\">Elige un veterinario para ver sus consultas pendientes.</p>");
            return;
        }
        List<String[]> filas = new ArrayList<String[]>();
        for (int i = 0; i < r.obtenerConsultas().size(); i++) {
            Consulta c = r.obtenerConsultas().get(i);
            if (c.getVeterinario() == vet && !c.isFinalizada()) {
                filas.add(new String[] {"" + (i + 1), c.getFecha().mostrarFecha(), esc(c.getMascota().getNombre()),
                    esc(c.getMascota().getDueno().getNombre()), esc(c.getMotivo()),
                    "<a class=\"boton boton--chico\" href=\"/consulta?id=" + i + "\">Abrir</a>"});
            }
        }
        v.put("pendientes", "<h2>Consultas pendientes de " + esc(vet.getNombre()) + "</h2>"
            + tabla(new String[] {"N°", "Fecha", "Mascota", "Dueño", "Motivo", ""}, filas,
                "Este veterinario no tiene consultas pendientes."));
    }

    private boolean paginaConsulta(Map<String, String> v, Map<String, String> q) {
        int n = numero(q.getOrDefault("id", ""));
        if (n < 0 || n >= r.obtenerConsultas().size()) {
            return false;
        }
        Consulta c = r.obtenerConsultas().get(n);
        Mascota m = c.getMascota();
        v.put("id", "" + n);
        v.put("numero", "" + (n + 1));
        v.put("volver", "/consulta?id=" + n);
        v.put("deshabilitado", c.isFinalizada() ? "disabled" : "");
        v.put("deshabilitado_clinico", "");
        v.put("estado", estado(c.isFinalizada() ? "Finalizada · editable" : "Pendiente"));
        v.put("boton_finalizar", c.isFinalizada() ? "<p class=\"ayuda\">Esta consulta ya fue finalizada. Puedes corregir los datos clínicos sin crear otra consulta.</p>" : "<button class=\"boton boton--principal\" type=\"submit\" >Finalizar consulta</button>");
        v.put("resumen", "<p><strong>Mascota:</strong> " + esc(m.getNombre()) + " (" + esc(m.getRaza()) + ", "
            + m.getEdad() + " años, " + m.getPeso() + " kg)</p>"
            + "<p><strong>Dueño:</strong> " + esc(m.getDueno().getNombre()) + "</p>"
            + "<p><strong>Veterinario:</strong> " + esc(c.getVeterinario().getNombre()) + "</p>"
            + "<p><strong>Fecha:</strong> " + c.getFecha().mostrarFecha() + "</p>"
            + "<p><strong>Motivo:</strong> " + esc(c.getMotivo()) + "</p>");
        Diagnostico d = c.getDiagnostico();
        Tratamiento t = c.getTratamiento();
        v.put("diagnostico", d == null ? "" : esc(d.getDescripcion()));
        v.put("obs_diagnostico", d == null ? "" : esc(d.getObservaciones()));
        v.put("medicamentos", t == null ? "" : esc(t.getMedicamentos()));
        v.put("dosis", t == null ? "" : esc(t.getDosis()));
        v.put("duracion", t == null ? "" : esc(t.getDuracion()));
        v.put("indicaciones", t == null ? "" : esc(t.getIndicaciones()));
        v.put("vacunas_texto", esc(c.getVacunasTexto()));
        v.put("observaciones", esc(c.getObservaciones()));

        StringBuilder servicios = new StringBuilder();
        for (int i = 0; i < r.obtenerCatalogo().size(); i++) {
            ServicioRealizado s = r.obtenerCatalogo().get(i);
            servicios.append(opcion("" + i, s.getNombre() + " - " + dinero(s.getCostoBase())));
        }
        v.put("catalogo_opciones", servicios.toString());
        List<String[]> filas = new ArrayList<String[]>();
        double total = 0;
        for (ServicioRealizado s : c.getServicios()) {
            filas.add(new String[] {esc(s.getNombre()), "" + s.getCantidad(), dinero(s.getCostoBase()), dinero(s.getSubtotal())});
            total += s.getSubtotal();
        }
        v.put("tabla_servicios", tabla(new String[] {"Servicio", "Cantidad", "Costo", "Subtotal"}, filas,
            "Todavía no se agregaron servicios.") + (filas.size() > 0 ? "<p class=\"total\">Total: " + dinero(total) + "</p>" : ""));
        List<String[]> vacunas = new ArrayList<String[]>();
        for (Vacuna vac : m.getVacunas()) {
            vacunas.add(new String[] {esc(vac.getNombreVacuna()), vac.getFechaAplicacion().mostrarFecha(),
                vac.getProxAplicacion().mostrarFecha(), esc(vac.getObservaciones())});
        }
        v.put("tabla_vacunas", tabla(new String[] {"Vacuna", "Aplicación", "Próxima dosis", "Observaciones"}, vacunas,
            "Esta mascota no tiene vacunas registradas."));
        return true;
    }
}

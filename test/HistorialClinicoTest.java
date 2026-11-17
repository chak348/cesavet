import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
public class HistorialClinicoTest {
    private Fecha fechaInicial;
    private Mascota mascotaEjemplo;
    private Veterinario veterinarioEjemplo;
    private HistorialClinico historial;
    private Consulta consultaEjemplo;
    private Diagnostico diagnosticoEjemplo;
    private Tratamiento tratamientoEjemplo;
    @Before
    public void setUp() {
        fechaInicial = new Fecha(1, 10, 2026);
        mascotaEjemplo = new Mascota("Firulais","Labrador",3, 10.5,"Macho");
        veterinarioEjemplo = new Veterinario("Carlos","Medicina General");
        historial = new HistorialClinico(fechaInicial,"Paciente sano",mascotaEjemplo);
        consultaEjemplo = new Consulta(fechaInicial, veterinarioEjemplo, mascotaEjemplo);
        diagnosticoEjemplo = new Diagnostico("Chequeo general", "Paciente estable");
        tratamientoEjemplo = new Tratamiento("Sin medicamentos", "No aplica", "No aplica", "Mantener controles periódicos");
        consultaEjemplo.agregarDiagnostico(diagnosticoEjemplo);
        consultaEjemplo.agregarTratamiento(tratamientoEjemplo);
    }
    @Test
    public void testAgregarConsulta() {
        historial.agregarConsulta(consultaEjemplo);
        assertEquals(1, historial.cantidadConsultas());
    }
    @Test
    public void testNoAgregarConsultaDuplicada() {
        historial.agregarConsulta(consultaEjemplo);
        historial.agregarConsulta(consultaEjemplo);
        assertEquals(1, historial.cantidadConsultas());
    }
    @Test
    public void testEliminarConsulta() {
        historial.agregarConsulta(consultaEjemplo);
        historial.eliminarConsulta(consultaEjemplo);
        assertEquals(0, historial.cantidadConsultas());
    }
    @Test
    public void testObtenerConsultas() {
        historial.agregarConsulta(consultaEjemplo);
        assertEquals(1, historial.obtenerConsultas().size());
        assertEquals(consultaEjemplo, historial.obtenerConsultas().get(0));
    }
    @Test
    public void testFechaApertura() {
        assertEquals(fechaInicial, historial.getFechaApertura());
    }
    @Test
    public void testMascota() {
        assertEquals(mascotaEjemplo, historial.getMascota());
    }
    @Test
    public void testObservacionesGenerales() {
        assertEquals("Paciente sano",historial.getObservacionesGenerales());
    }
    @Test
    public void testActualizarObservaciones() {
        historial.setObservacionesGenerales("Mascota en observación");
        assertEquals("Mascota en observación",historial.getObservacionesGenerales());
    }
    @Test
    public void testActualizarMascota() {
        Mascota otraMascota = new Mascota("Luna","Poodle",2,7.5,"Hembra");
        historial.setMascota(otraMascota);
        assertEquals(otraMascota, historial.getMascota());
    }
    @Test
    public void testActualizarFecha() {
        Fecha nuevaFecha = new Fecha(5, 10, 2026);
        historial.setFechaApertura(nuevaFecha);
        assertEquals(nuevaFecha, historial.getFechaApertura());
    }
}
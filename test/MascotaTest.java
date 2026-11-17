import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
public class MascotaTest {
    private Mascota mascotaValida;
    @Before
    public void setUp() {
        mascotaValida = new Mascota("Firulais", "Labrador", 3, 12.5, "Macho");
    }
    @Test
    public void testCrearMascotaCorrectamente() {
        assertEquals("Firulais", mascotaValida.getNombre());
        assertEquals("Labrador", mascotaValida.getRaza());
        assertEquals(3, mascotaValida.getEdad());
        assertEquals(12.5, mascotaValida.getPeso(), 0.001);
        assertEquals("Macho", mascotaValida.getSexo());
    }
    @Test
    public void testSetNombreInvalidoNoModifica() {
        mascotaValida.setNombre("");
        assertEquals("Firulais", mascotaValida.getNombre());
        mascotaValida.setNombre(null);
        assertEquals("Firulais", mascotaValida.getNombre());
    }
    @Test
    public void testSetEdadInvalidaNoModifica() {
        mascotaValida.setEdad(-5);
        assertEquals(3, mascotaValida.getEdad());
    }
    @Test
    public void testSetPesoInvalidoNoModifica() {
        mascotaValida.setPeso(0);
        assertEquals(12.5, mascotaValida.getPeso(), 0.001);
        mascotaValida.setPeso(-2.0);
        assertEquals(12.5, mascotaValida.getPeso(), 0.001);
    }
    @Test
    public void testActualizarDatosValidos() {
        mascotaValida.actualizar("Max", "Poodle", 4, 8.0, "Macho");
        assertEquals("Max", mascotaValida.getNombre());
        assertEquals("Poodle", mascotaValida.getRaza());
        assertEquals(4, mascotaValida.getEdad());
        assertEquals(8.0, mascotaValida.getPeso(), 0.001);
    }
    @Test
    public void testActualizarDatosInvalidosNoCambia() {
        mascotaValida.actualizar("Max", "Poodle", -1, 8.0, "Macho");
        assertEquals("Firulais", mascotaValida.getNombre());
    }
}
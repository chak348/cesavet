import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
public class DueñoTest {
    private Dueño dueñoValido;
    private Mascota mascotaEjemplo;
    @Before
    public void setUp() {
        dueñoValido = new Dueño("Carlos Pérez", "1234567 CB", 70012345, "Av. América #123", "carlos@gmail.com");
        mascotaEjemplo = new Mascota("Firulais", "Labrador", 3, 12.5, "Macho");
    }
    @Test
    public void testCrearDueñoCorrectamente() {
        assertEquals("Carlos Pérez", dueñoValido.getNombre());
        assertEquals("1234567 CB", dueñoValido.getCarnetIdentidad());
        assertEquals(70012345, dueñoValido.getTelefono());
        assertEquals("Av. América #123", dueñoValido.getDireccion());
        assertEquals("carlos@gmail.com", dueñoValido.getCorreo());
        assertEquals(0, dueñoValido.getMascotas().size());
    }
    @Test
    public void testSetTelefonoInvalidoNoModifica() {
        dueñoValido.setTelefono(-5);
        assertEquals(70012345, dueñoValido.getTelefono());
        dueñoValido.setTelefono(0);
        assertEquals(70012345, dueñoValido.getTelefono());
    }
    @Test
    public void testSetNombreInvalidoNoModifica() {
        dueñoValido.setNombre("");
        assertEquals("Carlos Pérez", dueñoValido.getNombre());
        dueñoValido.setNombre(null);
        assertEquals("Carlos Pérez", dueñoValido.getNombre());
    }
    @Test
    public void testAgregarMascotaExitosa() {
        dueñoValido.agregarMascota(mascotaEjemplo);
        assertEquals(1, dueñoValido.getMascotas().size());
        assertTrue(dueñoValido.getMascotas().contains(mascotaEjemplo));
    }
    @Test
    public void testNoPermitirMascotaDuplicada() {
        dueñoValido.agregarMascota(mascotaEjemplo);
        dueñoValido.agregarMascota(mascotaEjemplo);
        assertEquals(1, dueñoValido.getMascotas().size());
    }
    @Test
    public void testEliminarMascotaExistente() {
        dueñoValido.agregarMascota(mascotaEjemplo);
        assertEquals(1, dueñoValido.getMascotas().size());
        dueñoValido.eliminarMascota(mascotaEjemplo);
        assertEquals(0, dueñoValido.getMascotas().size());
    }
    @Test
    public void testBuscarMascotaPorNombre() {
        dueñoValido.agregarMascota(mascotaEjemplo);
        Mascota encontrada = dueñoValido.buscarMascotaPorNombre("firulais");
        assertNotNull(encontrada);
        assertEquals("Firulais", encontrada.getNombre());
        Mascota noExiste = dueñoValido.buscarMascotaPorNombre("Rex");
        assertNull(noExiste);
    }
}
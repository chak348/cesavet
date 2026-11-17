
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('input[type="search"]').forEach(input => {
    input.placeholder = 'Buscar propietario, mascota o CI...';
    input.setAttribute('aria-label', 'Buscar por nombre de propietario, mascota o CI');
    const form = input.closest('form');
    if (form) {
      form.addEventListener('submit', event => {
        event.preventDefault();
        const valor = input.value.trim();
        if (valor) window.location.href = '/historial?buscar=' + encodeURIComponent(valor);
      });
    } else {
      input.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
          event.preventDefault();
          const valor = input.value.trim();
          if (valor) window.location.href = '/historial?buscar=' + encodeURIComponent(valor);
        }
      });
    }
  });
});

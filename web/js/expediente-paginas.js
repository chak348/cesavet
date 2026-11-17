document.addEventListener('DOMContentLoaded', () => {
  const paginas = [...document.querySelectorAll('.expediente-pagina')];
  const params = new URLSearchParams(location.search);
  const tipos = {vacuna:2, desparasitacion:3, reproductivo:3, historia:4};
  let hoja = Math.max(1, Math.min(4, Number(params.get('hoja')) || tipos[params.get('tipo')] || (params.has('registro') ? 4 : 1)));

  if (params.get('editar') === '1' && !params.has('hoja')) {
    const form = document.querySelector('.expediente-form-mini input[name="registro"][value="' + params.get('registro') + '"]');
    if (form) hoja = Number(form.closest('.expediente-pagina')?.dataset.hoja) || hoja;
  }
  const mostrar = n => {
    hoja = Math.max(1, Math.min(paginas.length, n));
    paginas.forEach((pagina, i) => pagina.hidden = i !== hoja - 1);
    document.querySelectorAll('[data-hoja-numero]').forEach(boton => {
      const activo = Number(boton.dataset.hojaNumero) === hoja;
      boton.classList.toggle('activo', activo);
      boton.setAttribute('aria-current', activo ? 'page' : 'false');
    });
    document.querySelector('[data-hoja-anterior]').disabled = hoja === 1;
    document.querySelector('[data-hoja-siguiente]').disabled = hoja === paginas.length;
    const url = new URL(location.href);
    url.searchParams.set('hoja', hoja);
    history.replaceState(null, '', url);
  };
  document.querySelector('[data-hoja-anterior]').addEventListener('click', () => mostrar(hoja - 1));
  document.querySelector('[data-hoja-siguiente]').addEventListener('click', () => mostrar(hoja + 1));
  document.querySelectorAll('[data-hoja-numero]').forEach(boton => {
    boton.addEventListener('click', () => mostrar(Number(boton.dataset.hojaNumero)));
  });
  mostrar(hoja);
});

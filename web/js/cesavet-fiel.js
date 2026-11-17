
(function () {
  "use strict";
  function clean(s) {
    return (s || "").trim();
  }
  function rows(doc) {
    return [...doc.querySelectorAll("main table tbody tr")].filter(
      (x) => x.cells && x.cells.length > 1,
    );
  }
  async function getRows(page) {
    try {
      let r = await fetch("/" + page);
      if (!r.ok) return [];
      return rows(new DOMParser().parseFromString(await r.text(), "text/html"));
    } catch {
      return [];
    }
  }
  function put(id, html) {
    let x = document.getElementById(id);
    if (x) x.innerHTML = html;
  }
  function safe(text) {
    const d = document.createElement("div");
    d.textContent = text ?? "";
    return d.innerHTML;
  }
  function listed(rs, fn) {
    return rs.length
      ? rs.slice(0, 4).map(fn).join("")
      : '<p class="ces-muted">No hay registros todavía. Puedes añadirlos desde el menú.</p>';
  }
  function avatar(name) {
    let n = clean(name).toLowerCase();
    return ["luna", "mila", "rocky", "nala", "milo", "kira"].includes(n)
      ? '<img src="img/pet-' + n + '.png" alt="">'
      : '<span class="ces-avatar-fallback">🐾</span>';
  }

  async function homepage() {
    let [c, m, r] = await Promise.all([
      getRows("citas"),
      getRows("mascotas"),
      getRows("recordatorios"),
    ]);

    const mascotasPorNombre = new Map(
      m.map((tr) => {
        const a = [...tr.cells].map((x) => clean(x.textContent));
        return [a[0].toLowerCase(), a];
      }),
    );

    put(
      "datos-citas",
      listed(c, (tr) => {
        let a = [...tr.cells].map((x) => clean(x.textContent));
        let mascota = mascotasPorNombre.get(a[2].toLowerCase());
        let dueno = mascota ? mascota[6] : "";

        return `
        <div class="ces-live-row cita-inicio">
  <strong class="cita-hora">${safe(a[1])}</strong>

  <div class="cita-identidad">
    <span class="cita-patita">🐾</span>
    <strong>${safe(a[2])}</strong>
  </div>

  <small>● ${safe(dueno)}</small>

  <small class="cita-fecha">${safe(a[0])}</small>
</div>
      `;
      }),
    );

    put(
      "datos-mascotas",
      listed(m, (tr) => {
        let a = [...tr.cells].map((x) => clean(x.textContent));

        return `
        
<div class="ces-live-row mascota-inicio">
  <div class="mascota-identidad">
    <span class="mascota-patita">🐾</span>
    <div class="mascota-info-inicio">
      <strong>${safe(a[0])}</strong>
      <small>${safe(a[2])}</small>
    </div>
  </div>

  <span class="mascota-dueno">${safe(a[6])}</span>

  <span class="mascota-sexo">${safe(a[5])}</span>
</div>

      `;
      }),
    );

    put(
      "datos-recordatorios",
      listed(r, (tr) => {
        let a = [...tr.cells].map((x) => clean(x.textContent));
        let tipo = a[2] || "Recordatorio";
        let clase = tipo.toLowerCase().replaceAll(" ", "-");

        return `
        
<div class="ces-live-row recordatorio-inicio">
  <strong class="recordatorio-tipo">${safe(tipo)}</strong>
  <small class="recordatorio-fecha">${safe(a[4])}</small>
</div>

      `;
      }),
    );
  }

  async function mostrarInfoDueno(nombreDueno) {
    put(
      "ces-owner-info",
      "<h3>♟ Información del dueño</h3>" +
        '<p class="ces-muted">Cargando información...</p>',
    );

    const duenos = await getRows("duenos");

    const encontrado = duenos.find((tr) => {
      const nombre = clean(tr.cells[0]?.textContent);
      return nombre.toLowerCase() === nombreDueno.toLowerCase();
    });

    if (!encontrado) {
      put(
        "ces-owner-info",
        "<h3>♟ Información del dueño</h3>" +
          "<p>" +
          safe(nombreDueno) +
          "</p>" +
          '<p class="ces-muted">No se encontraron más datos.</p>',
      );
      return;
    }

    const d = [...encontrado.cells].map((x) => clean(x.textContent));

    put(
      "ces-owner-info",
      "<h3>♟ Información del dueño</h3>" +
        "<h2>" +
        safe(d[0]) +
        "</h2>" +
        "<hr>" +
        "<p><strong>Carnet:</strong> " +
        safe(d[1]) +
        "</p>" +
        "<p><strong>Teléfono:</strong> " +
        safe(d[2]) +
        "</p>" +
        "<p><strong>Dirección:</strong> " +
        safe(d[3]) +
        "</p>" +
        "<p><strong>Correo:</strong> " +
        safe(d[4]) +
        "</p>",
    );
  }

  async function abrirDetalleMascota(a, indiceMascota) {
    const modal = document.getElementById("modalDetalleMascota");
    const contenido = document.getElementById("contenidoDetalleMascota");
    const titulo = document.getElementById("detalleMascotaTitulo");

    if (!modal || !contenido || !titulo) return;

    titulo.textContent = "Paciente: " + clean(a[0]);

    modal.hidden = false;
    contenido.innerHTML = "<p>Cargando información...</p>";

    const duenos = await getRows("duenos");

    const dueno = duenos.find(
      (tr) =>
        clean(tr.cells[0]?.textContent).toLowerCase() ===
        clean(a[6]).toLowerCase(),
    );

    const d = dueno ? [...dueno.cells].map((x) => clean(x.textContent)) : [];

    contenido.innerHTML = `
        <section class="mascota-modal-bloque">
            <h3>🐾 Datos de la mascota</h3>

            <div class="mascota-modal-datos">
                <div><span>Especie</span><strong>${safe(a[1])}</strong></div>
                <div><span>Raza</span><strong>${safe(a[2])}</strong></div>
                <div><span>Edad</span><strong>${safe(a[3])}</strong></div>
                <div><span>Peso</span><strong>${safe(a[4])}</strong></div>
                <div><span>Sexo</span><strong>${safe(a[5])}</strong></div>
            </div>
        </section>

        <section class="mascota-modal-bloque">
            <h3>👤 Datos del dueño</h3>

            <h4>${safe(a[6])}</h4>

            <div class="mascota-modal-datos">
                <div><span>Carnet</span><strong>${safe(d[1] || "No disponible")}</strong></div>
                <div><span>Teléfono</span><strong>${safe(d[2] || "No disponible")}</strong></div>
                <div><span>Dirección</span><strong>${safe(d[3] || "No disponible")}</strong></div>
                <div><span>Correo</span><strong>${safe(d[4] || "No disponible")}</strong></div>
            </div>
        </section>

        <div class="mascota-modal-acciones">
           
<a class="ces-btn ces-btn-compact ces-btn-editar-historial"
   href="/historial?mascota=${indiceMascota}">
   👁 Ver historial clínico
</a>


            <a class="ces-btn ces-btn-compact"
               href="/historial?mascota=${indiceMascota}&editar=1">
               ✎ Editar historial clínico
            </a>
        </div>
    `;
  }

  function selection() {

    const modalMascota = document.getElementById("modalDetalleMascota");
    const botonCerrarMascota = document.getElementById("cerrarDetalleMascota");

    if (botonCerrarMascota && modalMascota) {
      botonCerrarMascota.addEventListener("click", () => {
        modalMascota.hidden = true;
      });
    }

    if (modalMascota) {
      modalMascota.addEventListener("click", (e) => {
        if (e.target === modalMascota) {
          modalMascota.hidden = true;
        }
      });
    }

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modalMascota) {
        modalMascota.hidden = true;
      }
    });
    document.addEventListener("click", (e) => {
      const tr = e.target.closest(".ces-table-scroll tbody tr");

      if (!tr) return;
      if (e.target.closest("button,a,input")) return;
      tr.parentElement
        .querySelectorAll("tr")
        .forEach((t) => t.classList.remove("ces-selected"));
      let a = [...tr.cells].map((x) => clean(x.textContent));
      const indiceMascota = [...tr.parentElement.children].indexOf(tr);

      if (document.body.classList.contains("pg-mascotas")) {
        mascotaSeleccionada = a;

        const campoId = document.getElementById("editarIdMascota");

        if (campoId) {
          campoId.value = indiceMascota;
        }
      }
      if (document.body.classList.contains("pg-mascotas")) {
        abrirDetalleMascota(a, indiceMascota);
      }
      if (document.body.classList.contains("pg-duenos"))
        put(
          "ces-selection",
          "<h3>👤 " +
            safe(a[0]) +
            "</h3><p>Documento: " +
            safe(a[1]) +
            "</p><p>Teléfono: " +
            safe(a[2]) +
            "</p><p>Dirección: " +
            safe(a[3]) +
            "</p><p>Correo: " +
            safe(a[4]) +
            "</p><p>Mascotas: " +
            safe(a[5]) +
            "</p>",
        );
      if (document.body.classList.contains("pg-citas"))
        put(
          "ces-appointment-detail",
          "<p><b>Fecha:</b> " +
            safe(a[0]) +
            "</p><p><b>Hora:</b> " +
            safe(a[1]) +
            "</p><p><b>Mascota:</b> " +
            safe(a[2]) +
            "</p><p><b>Motivo:</b> " +
            safe(a[3]) +
            "</p><p><b>Veterinario:</b> " +
            safe(a[4]) +
            "</p><p><b>Estado:</b> " +
            safe(a[5]) +
            '</p><p class="ces-muted">Las acciones de la cita están disponibles en su fila de la tabla.</p>',
        );
      if (document.body.classList.contains("pg-comprobantes"))
        put(
          "ces-pet-detail",
          '<div class="mascota-ficha">' +
            '<h3 class="mascota-nombre">🐾 ' +
            safe(a[0]) +
            "</h3>" +
            '<p class="mascota-raza">' +
            safe(a[2]) +
            "</p>" +
            '<div class="mascota-etiquetas">' +
            '<span class="etiqueta-especie">🐾 ' +
            safe(a[1]) +
            "</span>" +
            '<span class="etiqueta-sexo">⚥ ' +
            safe(a[5]) +
            "</span>" +
            '<span class="etiqueta-edad">' +
            safe(a[3]) +
            "</span>" +
            "</div>" +
            '<div class="mascota-info-resumida">' +
            "<h3>Información de la mascota</h3>" +
            "<div><span>Especie</span><strong>" +
            safe(a[1]) +
            "</strong></div>" +
            "<div><span>Raza</span><strong>" +
            safe(a[2]) +
            "</strong></div>" +
            "<div><span>Edad</span><strong>" +
            safe(a[3]) +
            "</strong></div>" +
            "<div><span>Peso</span><strong>" +
            safe(a[4]) +
            "</strong></div>" +
            "<div><span>Sexo</span><strong>" +
            safe(a[5]) +
            "</strong></div>" +
            "</div>" +
            '<div class="mascota-acciones">' +
            '<a class="ces-btn ces-btn-compact" href="/historial?mascota=' +
            indiceMascota +
            '">▦ Ver historial clínico</a>' +
            '<a class="ces-btn ces-btn-compact" href="/historial?mascota=' +
            indiceMascota +
            '&editar=1">✎ Editar historial clínico</a>' +
            "</div>" +
            "</div>",
        );
    });
  }
  async function counters() {
    let map = {
      "stat-duenos": "duenos",
      "stat-mascotas": "mascotas",
      "stat-citas": "citas",
      "stat-historial": "mascotas", // Cada mascota registrada tiene un expediente asociado; no contar filas vacías de la plantilla.
      "stat-recordatorios": "recordatorios",
    };
    await Promise.all(
      Object.entries(map).map(async ([id, page]) => {
        let el = document.getElementById(id);
        if (el) el.textContent = (await getRows(page)).length;
      }),
    );
  }
  let weekOffset = 0;

  function agenda(n) {
    weekOffset = n === 0 ? 0 : weekOffset + n;

    const inicio = new Date();
    inicio.setHours(0, 0, 0, 0);

    inicio.setDate(
      inicio.getDate() - ((inicio.getDay() + 6) % 7) + weekOffset * 7,
    );

    const contenedor = document.getElementById("ces-week");
    const fuente = document.querySelector("#ces-citas-data table tbody");

    if (!contenedor) return;

    let citas = [];

    if (fuente) {
      [...fuente.querySelectorAll("tr")].forEach((tr, indice) => {
        const c = [...tr.cells].map((x) => clean(x.textContent));

        if (c.length < 6) return;

        const partes = c[0].split("/");
        if (partes.length !== 3) return;

        const fecha = new Date(
          Number(partes[2]),
          Number(partes[1]) - 1,
          Number(partes[0]),
        );

        fecha.setHours(0, 0, 0, 0);

        if (isNaN(fecha.getTime())) return;

        citas.push({
          fecha: fecha,
          hora: c[1],
          mascota: c[2],
          motivo: c[3],
          veterinario: c[4],
          estado: c[5],
          accion: tr.cells[6] ? tr.cells[6].innerHTML.trim() : "",
          id: indice,
        });
      });
    }

    let html = "";

    const nombresDias = ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"];

    for (let i = 0; i < 7; i++) {
      const dia = new Date(inicio);
      dia.setDate(inicio.getDate() + i);

      const citasDia = citas
        .filter((c) => c.fecha.getTime() === dia.getTime())
        .sort((a, b) => a.hora.localeCompare(b.hora));

      html +=
        '<div class="ces-week-day">' +
        "<strong>" +
        nombresDias[i] +
        " " +
        dia.getDate() +
        "</strong>";

      if (citasDia.length === 0) {
        html += '<div class="ces-week-vacio">Sin citas</div>';
      } else {
        citasDia.forEach((c) => {
          const estado = c.estado.toLowerCase();

          let clase = "programada";

          if (estado.includes("atendida")) {
            clase = "atendida";
          } else if (estado.includes("cancelada")) {
            clase = "cancelada";
          } else if (estado.includes("confirmada")) {
            clase = "confirmada";
          }
          html +=
            '<button type="button" class="ces-cita-card ' +
            clase +
            '" data-fecha="' +
            safe(
              c.fecha.toLocaleDateString("es-ES", {
                weekday: "long",
                day: "numeric",
                month: "long",
                year: "numeric",
              }),
            ) +
            '" data-hora="' +
            safe(c.hora) +
            '" data-mascota="' +
            safe(c.mascota) +
            '" data-motivo="' +
            safe(c.motivo) +
            '" data-veterinario="' +
            safe(c.veterinario) +
            '" data-estado="' +
            safe(c.estado) +
            '" data-accion="' +
            safe(encodeURIComponent(c.accion)) +
            '">' +
            "<strong>" +
            safe(c.hora) +
            "</strong>" +
            "<b>" +
            safe(c.mascota) +
            "</b>" +
            "</button>";
        });
      }

      html += "</div>";
    }

    contenedor.innerHTML = html;

    contenedor.querySelectorAll(".ces-cita-card").forEach((boton) => {
      boton.addEventListener("click", async function () {
        const detalle = document.getElementById("ces-appointment-detail");
        const panel = detalle.closest(".ces-appointment-panel");

        if (panel) {
          panel.hidden = false;
        }
        if (!detalle) return;

        const mascota = this.dataset.mascota;

        const mascotas = await getRows("mascotas");

        const filaMascota = mascotas.find(
          (fila) =>
            clean(fila.cells[0]?.textContent).toLowerCase() ===
            mascota.toLowerCase(),
        );

        const dueno = filaMascota
          ? clean(filaMascota.cells[6]?.textContent)
          : "No disponible";

        detalle.innerHTML = `
    <div class="ces-detail-pet">
        <h3>${safe(mascota)}</h3>
    </div>

    <div class="ces-detail-info">

        <p>
            <span>👤</span>
            <strong>Dueño</strong>
            ${safe(dueno)}
        </p>

        <p>
            <span>📅</span>
            <strong>Fecha</strong>
            ${safe(this.dataset.fecha)}
        </p>

        <p>
            <span>🕒</span>
            <strong>Hora</strong>
            ${safe(this.dataset.hora)}
        </p>

        <p>
            <span>📋</span>
            <strong>Motivo</strong>
            ${safe(this.dataset.motivo)}
        </p>

        <p>
            <span>🩺</span>
            <strong>Veterinario</strong>
            ${safe(this.dataset.veterinario)}
        </p>

        <p>
            <span>🟢</span>
            <strong>Estado</strong>
            ${safe(this.dataset.estado)}
        </p>

    </div>
    ${this.dataset.accion ? '<div class="ces-appointment-actions">' + decodeURIComponent(this.dataset.accion) + "</div>" : ""}
`;
      });
    });
  }

  let mascotaSeleccionada = null;

  window.abrirEditarMascota = async function () {
    if (mascotaSeleccionada === null) {
      alert("Primero selecciona una mascota.");
      return;
    }

    let a = mascotaSeleccionada;

    document.getElementById("editarNombre").value = a[0];

    document.getElementById("editarEspecie").value =
      a[1] === "Sin especificar" ? "" : a[1];

    document.getElementById("editarRaza").value = a[2];

    let edadTexto = a[3];

    document.getElementById("editarEdad").value = parseInt(edadTexto, 10);

    document.getElementById("editarUnidadEdad").value = edadTexto.includes(
      "mes",
    )
      ? "meses"
      : "años";

    document.getElementById("editarPeso").value = parseFloat(
      a[4].replace(",", "."),
    );

    document.getElementById("editarSexo").value = a[5];

    const duenos = await getRows("duenos");

    const filaDueno = duenos.find(
      (tr) =>
        clean(tr.cells[0]?.textContent).toLowerCase() ===
        clean(a[6]).toLowerCase(),
    );

    if (!filaDueno) {
      alert("No se encontraron los datos del dueño.");
      return;
    }

    const d = [...filaDueno.cells].map((x) => clean(x.textContent));

    document.getElementById("editarNombreDueno").value = d[0];
    document.getElementById("editarCarnetDueno").value = d[1];
    document.getElementById("editarTelefonoDueno").value = d[2];
    document.getElementById("editarDireccionDueno").value = d[3];
    document.getElementById("editarCorreoDueno").value = d[4];

    document.getElementById("modalEditarMascota").hidden = false;
  };

  window.cerrarEditarMascota = function () {
    document.getElementById("modalEditarMascota").hidden = true;
  };

  const formularioEditar = document.getElementById("formEditarMascota");

  if (formularioEditar) {
    formularioEditar.addEventListener("submit", async function (e) {
      e.preventDefault();

      try {
        const respuesta = await fetch(formularioEditar.action, {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: new URLSearchParams(new FormData(formularioEditar)),
        });

        if (respuesta.url.includes("ok=1")) {
          cerrarEditarMascota();
          window.location.reload();
        }
      } catch (error) {

      }
    });
  }

  let mesVisible = new Date();
  mesVisible.setDate(1);

  function mostrarCalendarioMensual() {
    const contenedor = document.getElementById("ces-mini-month");
    const titulo = document.getElementById("ces-month-label");

    if (!contenedor || !titulo) return;

    const anio = mesVisible.getFullYear();
    const mes = mesVisible.getMonth();

    titulo.textContent = mesVisible.toLocaleDateString("es", {
      month: "long",
      year: "numeric",
    });

    const primerDia = new Date(anio, mes, 1);
    const diasMes = new Date(anio, mes + 1, 0).getDate();


    const hoy = new Date();
    let html = "";

    const nombresDias = ["D", "L", "M", "M", "J", "V", "S"];

    for (let i = 0; i < nombresDias.length; i++) {
      html += '<span class="ces-dia-semana">' + nombresDias[i] + "</span>";
    }
    for (let i = 0; i < espacios; i++) {
      html += "<span></span>";
    }

    for (let dia = 1; dia <= diasMes; dia++) {
      const esHoy =
        dia === hoy.getDate() &&
        mes === hoy.getMonth() &&
        anio === hoy.getFullYear();

      html += '<span class="' + (esHoy ? "today" : "") + '">' + dia + "</span>";
    }

    contenedor.innerHTML = html;
  }

  window.cambiarMes = function (direccion) {
    mesVisible.setMonth(mesVisible.getMonth() + direccion);
    mostrarCalendarioMensual();
  };

  window.cesAgenda = agenda;
  document.addEventListener("DOMContentLoaded", () => {
    const p = document.body;
    if (p.classList.contains("pg-index")) homepage();
    if (
      p.classList.contains("pg-duenos") ||
      p.classList.contains("pg-recordatorios")
    )
      counters();
    selection();
    if (document.getElementById("ces-week")) {
      agenda(0);
      let html = "";
      for (let i = 1; i <= 35; i++)
        html +=
          "<span" +
          (i === new Date().getDate() ? ' class="today"' : "") +
          ">" +
          (((i - 1) % 31) + 1) +
          "</span>";
      put("ces-mini-month", html);
    }
    let date = document.getElementById("ces-today");
    if (date) date.value = new Date().toISOString().slice(0, 10);
    document.querySelectorAll(".ces-main .visual-filter").forEach((inp) =>
      inp.addEventListener("input", () => {
        let tbl =
          inp
            .closest(".ces-card, .ces-main")
            ?.querySelector(".ces-table-scroll table") ||
          document.querySelector(".ces-table-scroll table");
        if (tbl)
          tbl
            .querySelectorAll("tbody tr")
            .forEach(
              (tr) =>
                (tr.hidden = !tr.textContent
                  .toLowerCase()
                  .includes(inp.value.toLowerCase())),
            );
      }),
    );
  });
})();

document.addEventListener("DOMContentLoaded", function () {
  const tablas = document.querySelectorAll(".ces-main .ces-table-scroll table");

  tablas.forEach(function (tabla) {
    const filas = Array.from(tabla.querySelectorAll("tbody tr"));

    const limite = document.body.classList.contains("pg-duenos") ? 7 : 8;

    if (filas.length === 0) return;

    let paginaActual = 1;

    const paginador = document.createElement("nav");
    paginador.className = "ces-paginacion";
    paginador.setAttribute("aria-label", "Páginas de la tabla");

    const contenedor = tabla.closest(".ces-table-scroll");
    contenedor.insertAdjacentElement("afterend", paginador);

    function mostrarPagina() {
      const filtradas = filas.filter(function (fila) {
        return !fila.hidden;
      });

      const totalPaginas = Math.ceil(filtradas.length / limite);

      paginaActual = Math.max(
        1,
        Math.min(paginaActual, Math.max(1, totalPaginas)),
      );

      filas.forEach(function (fila) {
        fila.style.display = "none";
      });

      filtradas
        .slice((paginaActual - 1) * limite, paginaActual * limite)
        .forEach(function (fila) {
          fila.style.display = "";
        });

      paginador.innerHTML = "";

      if (totalPaginas <= 1) return;

      function crearBoton(texto, numero, deshabilitado) {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.textContent = texto;

        if (numero === paginaActual && texto === String(numero)) {
          boton.classList.add("activo");
          boton.setAttribute("aria-current", "page");
        }

        boton.disabled = deshabilitado;

        boton.addEventListener("click", function () {
          paginaActual = numero;
          mostrarPagina();
        });

        paginador.appendChild(boton);
      }

      crearBoton("‹", paginaActual - 1, paginaActual === 1);

      for (let i = 1; i <= totalPaginas; i++) {
        crearBoton(String(i), i, false);
      }

      crearBoton("›", paginaActual + 1, paginaActual === totalPaginas);
    }

    mostrarPagina();

    document.addEventListener("input", function (evento) {
      if (evento.target.matches(".visual-filter")) {
        paginaActual = 1;
        mostrarPagina();
      }
    });

    document.addEventListener("change", function (evento) {
      if (evento.target.matches(".visual-select")) {
        paginaActual = 1;
        mostrarPagina();
      }
    });
  });
});

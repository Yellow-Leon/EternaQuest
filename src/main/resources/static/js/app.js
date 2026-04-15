/**
 * app.js — Controlador principal del SPA.
 * Orquesta State, API, WS y Views. Ningún otro módulo toca el DOM directamente.
 */
const App = (() => {

  // ── Router ─────────────────────────────────────────────────────────────
  const rutas = {
    auth:         () => Views.auth(),
    crearPersonaje: () => Views.crearPersonaje(),
    mapa:         () => Views.mapa(State.getState()),
    combate:      () => Views.combate(State.getState()),
    inventario:   () => Views.inventario(State.getState()),
    ranking:      () => Views.ranking(State.getState()),
    admin:        () => Views.admin(),
  };

  let rutaActual = null;
  let claseSeleccionada = null;

  function render() {
    const state = State.getState();
    const navbar = document.getElementById('navbar');
    const content = document.getElementById('app-content');
    const navButtons = document.getElementById('nav-buttons');

    // Overlays de invasión activa
    const invOverlay = document.getElementById('invasion-overlay');
    if (invOverlay) invOverlay.remove();
    if (state.invasion && !state.invasion.terminada) {
      document.body.insertAdjacentHTML('beforeend', Views.invasion(state.invasion));
    }

    // Navbar
    if (state.token) {
      navbar.classList.remove('hidden');
      navButtons.innerHTML = `
        <button class="nav-btn ${rutaActual === 'mapa' ? 'active' : ''}" onclick="App.irA('mapa')">Mapa</button>
        <button class="nav-btn ${rutaActual === 'inventario' ? 'active' : ''}" onclick="App.irA('inventario')">Inventario</button>
        <button class="nav-btn ${rutaActual === 'ranking' ? 'active' : ''}" onclick="App.irA('ranking')">Ranking</button>
        ${state.userRol === 'ADMINISTRADOR' ? `<button class="nav-btn ${rutaActual === 'admin' ? 'active' : ''}" onclick="App.irA('admin')">Admin</button>` : ''}
        <button class="nav-btn" onclick="App.cerrarSesion()">Salir</button>`;
    } else {
      navbar.classList.add('hidden');
    }

    content.innerHTML = rutas[rutaActual]?.() ?? '';
  }

  function irA(ruta) {
    rutaActual = ruta;
    if (ruta === 'inventario') cargarInventario();
    if (ruta === 'ranking')    cargarRanking();
    if (ruta === 'mapa')       { cargarPersonaje(); cargarZonas(); cargarMisStats(); }
    render();
  }

  // ── Auth ───────────────────────────────────────────────────────────────
  function showAuthTab(tab) {
    document.getElementById('auth-login')?.classList.toggle('hidden', tab !== 'login');
    document.getElementById('auth-register')?.classList.toggle('hidden', tab !== 'register');
    document.getElementById('tab-login')?.classList.toggle('active', tab === 'login');
    document.getElementById('tab-register')?.classList.toggle('active', tab === 'register');
  }

  async function login() {
    const email    = document.getElementById('login-email')?.value;
    const password = document.getElementById('login-pwd')?.value;
    try {
      const res = await API.auth.login({ email, password });
      localStorage.setItem('eq_token', res.token);
      State.setState({ token: res.token, userEmail: res.email, userRol: res.rol });
      conectarWS(res.token);
      if (!res.tienePersonaje) irA('crearPersonaje');
      else await iniciarSesionJuego();
    } catch (e) { toast(e.message, 'error'); }
  }

  async function register() {
    const email    = document.getElementById('reg-email')?.value;
    const password = document.getElementById('reg-pwd')?.value;
    try {
      const res = await API.auth.register({ email, password });
      localStorage.setItem('eq_token', res.token);
      State.setState({ token: res.token, userEmail: res.email, userRol: res.rol });
      conectarWS(res.token);
      irA('crearPersonaje');
    } catch (e) { toast(e.message, 'error'); }
  }

  async function cerrarSesion() {
    WS.disconnect();
    State.logout();
    rutaActual = 'auth';
    render();
  }

  // ── Personaje ──────────────────────────────────────────────────────────
  function selectClase(id) {
    claseSeleccionada = id;
    document.querySelectorAll('.class-card').forEach(c => c.classList.remove('selected'));
    document.getElementById('cls-' + id)?.classList.add('selected');
  }

  async function crearPersonaje() {
    const nombre = document.getElementById('char-name')?.value?.trim();
    if (!claseSeleccionada) return toast('Elige una clase', 'error');
    if (!nombre || nombre.length < 3) return toast('El nombre debe tener al menos 3 caracteres', 'error');
    try {
      const p = await API.personaje.crear({ nombre, clase: claseSeleccionada });
      State.setState({ personaje: p });
      toast('¡' + p.nombre + ' ha llegado a EternaQuest!', 'success');
      await iniciarSesionJuego();
    } catch (e) { toast(e.message, 'error'); }
  }

  async function cargarPersonaje() {
    try {
      const p = await API.personaje.get();
      State.setState({ personaje: p });
    } catch (e) { console.warn('cargarPersonaje:', e); }
  }

  async function cargarMisStats() {
    try {
      const s = await API.ranking.misStats();
      State.setState({ misStats: s });
    } catch (_) {}
  }

  // ── Inicio de sesión con personaje existente ───────────────────────────
  async function iniciarSesionJuego() {
    await cargarPersonaje();
    await cargarZonas();
    await cargarMisStats();
    irA('mapa');
  }

  // ── Zonas ──────────────────────────────────────────────────────────────
  async function cargarZonas() {
    try {
      const z = await API.zonas.listar();
      State.setState({ zonas: z });
    } catch (e) { console.warn('cargarZonas:', e); }
  }

  async function entrarZona(id) {
    try {
      const res = await API.zonas.entrar(id);
      State.setState({ combateZonaId: id });
      if (res.invasion) {
        // El servidor ya notificó por WS — el handler de invasión actualiza la UI
        toast('¡Peligro! Alguien acecha en esta zona...', 'error');
      } else {
        iniciarCombatePve(id);
      }
    } catch (e) { toast(e.message, 'error'); }
  }

  async function iniciarCombatePve(zonaId) {
    try {
      const estado = await API.zonas.iniciarCombate(zonaId);
      State.setState({ combateActivo: true, combateEstado: estado });
      irA('combate');
    } catch (e) { toast(e.message, 'error'); }
  }

  // ── Combate PvE ────────────────────────────────────────────────────────
  async function accionCombate(tipo, extra = {}) {
    try {
      const estado = await API.combate.accion({ accion: tipo, ...extra });
      State.setState({ combateEstado: estado });
      if (estado.combateTerminado) {
        State.setState({ combateActivo: false });
        // Refrescar personaje tras el combate
        await cargarPersonaje();
      }
      irA('combate');
      // Añadir al log sin re-renderizar entero
      const log = document.getElementById('combat-log');
      if (log) {
        const div = document.createElement('div');
        div.className = 'log-entry';
        div.textContent = estado.logMensaje;
        log.prepend(div);
      }
    } catch (e) { toast(e.message, 'error'); }
  }

  async function mostrarHabilidades() {
    const habs = State.getState().habilidades;
    const sub = document.getElementById('combat-submenu');
    if (!sub) return;
    if (habs.length === 0) {
      const h = await API.personaje.habilidades();
      State.setState({ habilidades: h });
    }
    sub.innerHTML = State.getState().habilidades.map(h =>
      `<button class="btn btn-primary btn-sm" style="margin:0.2rem"
         onclick="App.accionCombate('HABILIDAD',{habilidadId:${h.id}})">
         ${h.nombre} (${h.costeMana} mana)
       </button>`).join('');
  }

  async function mostrarObjetos() {
    const items = await API.inventario.get();
    const consumibles = items.filter(i => i.objeto.tipo === 'CONSUMIBLE');
    const sub = document.getElementById('combat-submenu');
    if (!sub) return;
    sub.innerHTML = consumibles.length === 0
      ? '<p class="text-muted" style="font-size:0.85rem">Sin consumibles</p>'
      : consumibles.map(i =>
          `<button class="btn btn-gold btn-sm" style="margin:0.2rem"
             onclick="App.accionCombate('OBJETO',{objetoId:${i.objeto.id}})">
             ${i.objeto.nombre} x${i.cantidad}
           </button>`).join('');
  }

  // ── Inventario ─────────────────────────────────────────────────────────
  async function cargarInventario() {
    try {
      const [inv, tienda] = await Promise.all([API.inventario.get(), API.inventario.tienda()]);
      State.setState({ inventario: inv, tienda });
    } catch (e) { toast(e.message, 'error'); }
  }

  function seleccionarItem(itemId, objetoId) {
    const detail = document.getElementById('item-detail');
    if (!detail) return;
    const item = State.getState().inventario.find(i => i.id === itemId);
    if (!item) return;
    const o = item.objeto;
    detail.innerHTML = `
      <div class="card" style="margin-top:0.75rem">
        <div class="flex-between mb-1">
          <span style="font-weight:700">${o.nombre}</span>
          <span class="badge badge-purple">${o.tipo}</span>
        </div>
        <p class="text-muted" style="font-size:0.85rem;margin-bottom:0.75rem">${o.efecto ?? ''}</p>
        <div style="display:flex;gap:0.5rem;flex-wrap:wrap">
          ${item.equipado
            ? `<button class="btn btn-ghost btn-sm" onclick="App.desequipar(${objetoId})">Desequipar</button>`
            : o.tipo !== 'CONSUMIBLE'
              ? `<button class="btn btn-primary btn-sm" onclick="App.equipar(${objetoId})">Equipar</button>`
              : `<button class="btn btn-success btn-sm" onclick="App.usarConsumible(${objetoId})">Usar</button>`
          }
          ${!item.equipado ? `<button class="btn btn-danger btn-sm" onclick="App.vender(${objetoId})">Vender (${Math.floor(o.precio/2)} oro)</button>` : ''}
        </div>
      </div>`;
  }

  async function equipar(id)          { try { await API.inventario.equipar(id);   await cargarInventario(); irA('inventario'); await cargarPersonaje(); } catch(e){toast(e.message,'error');} }
  async function desequipar(id)       { try { await API.inventario.desequipar(id);await cargarInventario(); irA('inventario'); await cargarPersonaje(); } catch(e){toast(e.message,'error');} }
  async function usarConsumible(id)   { try { await API.inventario.usar(id);      await cargarInventario(); irA('inventario'); } catch(e){toast(e.message,'error');} }
  async function vender(id)           { try { await API.inventario.vender(id);    await cargarInventario(); irA('inventario'); } catch(e){toast(e.message,'error');} }
  async function comprar(id)          { try { await API.inventario.comprar(id);   await cargarInventario(); irA('inventario'); toast('¡Objeto comprado!','success'); } catch(e){toast(e.message,'error');} }

  // ── Ranking ────────────────────────────────────────────────────────────
  async function cargarRanking() {
    try {
      const lista = await API.ranking.get();
      State.setState({ ranking: lista });
    } catch (e) { console.warn('ranking:', e); }
  }

  // ── Admin ──────────────────────────────────────────────────────────────
  async function adminTab(tab) {
    const el = document.getElementById('admin-content');
    if (!el) return;
    try {
      if (tab === 'usuarios') {
        const lista = await API.admin.usuarios();
        el.innerHTML = `<table class="rank-table"><thead><tr><th>ID</th><th>Email</th><th>Rol</th></tr></thead><tbody>
          ${lista.map(u => `<tr><td>${u.id}</td><td>${u.email}</td><td>${u.rol}</td></tr>`).join('')}
        </tbody></table>`;
      } else {
        el.innerHTML = `<p class="text-muted">Gestión de <strong>${tab}</strong> — formulario CRUD disponible aquí.</p>`;
      }
    } catch (e) { toast(e.message, 'error'); }
  }

  // ── WebSocket — invasión ───────────────────────────────────────────────
  function conectarWS(token) {
    WS.connect(token, () => {
      WS.on('invasion', manejarInvasion);
    });
  }

  function manejarInvasion(msg) {
    const state = State.getState();
    switch (msg.tipo) {
      case 'INVASION_INICIADA': {
        const esHost = state.userEmail !== msg.invasorNombre; // simplificado; idealmente comparar IDs
        State.setState({
          invasion: {
            sessionId:    msg.sessionId,
            esHost,
            hostNombre:   msg.hostNombre,
            invasorNombre:msg.invasorNombre,
            hostNivel:    msg.hostNivel,
            invasorNivel: msg.invasorNivel,
            vidaHost:     msg.vidaHost,
            vidaInvasor:  msg.vidaInvasor,
            logMensaje:   msg.logMensaje,
            turnoJugador: esHost,
            terminada:    false,
          }
        });
        toast(msg.logMensaje, 'error');
        render();
        break;
      }
      case 'TURNO_JUGADOR':
      case 'TURNO_INVASOR': {
        const inv = State.getState().invasion;
        if (!inv) return;
        State.setState({
          invasion: {
            ...inv,
            vidaHost:    msg.vidaHost,
            vidaInvasor: msg.vidaInvasor,
            logMensaje:  msg.logMensaje,
            turnoJugador: msg.tipo === 'TURNO_JUGADOR' ? inv.esHost : !inv.esHost,
          }
        });
        const logEl = document.getElementById('invasion-log');
        if (logEl) {
          logEl.insertAdjacentHTML('beforeend', `<div class="log-entry">${msg.logMensaje}</div>`);
          logEl.scrollTop = logEl.scrollHeight;
        }
        render();
        break;
      }
      case 'FIN_COMBATE': {
        const inv = State.getState().invasion;
        if (!inv) return;
        const gano = (msg.resultado === 'HOST_GANA' && inv.esHost) ||
                     (msg.resultado === 'INVASOR_GANA' && !inv.esHost);
        toast(gano ? '¡Has ganado la invasión!' : 'Has perdido la invasión.', gano ? 'success' : 'error');
        State.setState({ invasion: { ...inv, terminada: true } });
        setTimeout(() => {
          State.setState({ invasion: null });
          cargarPersonaje();
          irA('mapa');
        }, 2500);
        break;
      }
    }
  }

  function accionInvasion(accion) {
    const inv = State.getState().invasion;
    if (!inv || !inv.turnoJugador) return;
    WS.enviarAccionPvp(inv.sessionId, accion);
  }

  // ── Toasts ─────────────────────────────────────────────────────────────
  function toast(msg, tipo = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const div = document.createElement('div');
    div.className = `toast toast-${tipo}`;
    div.textContent = msg;
    container.appendChild(div);
    setTimeout(() => div.remove(), 4000);
  }

  // ── Bootstrap ──────────────────────────────────────────────────────────
  async function init() {
    const token = State.loadToken();
    if (token) {
      try {
        const p = await API.personaje.get();
        State.setState({ personaje: p });
        // Recuperar email del token (sin decodificar JWT: usamos el endpoint de personaje)
        const payload = JSON.parse(atob(token.split('.')[1]));
        State.setState({ userEmail: payload.sub, token });
        conectarWS(token);
        await cargarZonas();
        await cargarMisStats();
        irA('mapa');
        return;
      } catch (_) {
        // Token inválido o personaje sin crear
        State.logout();
      }
    }
    rutaActual = 'auth';
    render();
  }

  // Suscribir al estado para re-renderizar cuando cambie
  State.subscribe(() => {
    if (rutaActual !== 'auth') render();
  });

  return {
    // Públicos (llamados desde el HTML)
    irA, init, showAuthTab, login, register, cerrarSesion,
    selectClase, crearPersonaje,
    entrarZona,
    accionCombate, mostrarHabilidades, mostrarObjetos,
    seleccionarItem, equipar, desequipar, usarConsumible, vender, comprar,
    accionInvasion, adminTab,
  };
})();

// ── Parche lore: métodos adicionales ─────────────────────────────────────
App.mostrarPvpInfo = function() {
  // El PvP de invasión es pasivo (se activa al entrar en zona).
  // Este botón informa al usuario.
  App._toast('La Invasión ocurre al adentrarte en un Dominio.\nEl destino elige al invasor.', 'error');
};

// Sobrescribir seleccionarItem para usar Views.itemDetail
App.seleccionarItem = function(itemId, objetoId) {
  const detail = document.getElementById('item-detail');
  if (!detail) return;
  const item = State.getState().inventario.find(i => i.id === itemId);
  if (!item) return;
  detail.innerHTML = Views.itemDetail(item);
};

// Exponer toast públicamente
App._toast = App._toast || function(msg, tipo) {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const div = document.createElement('div');
  div.className = `toast toast-${tipo}`;
  div.textContent = msg;
  container.appendChild(div);
  setTimeout(() => div.remove(), 4000);
};

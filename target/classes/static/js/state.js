/**
 * state.js — Estado global de la aplicación.
 * Fuente única de verdad (Single Source of Truth).
 * Ningún módulo modifica el estado directamente; todo pasa por setState().
 */
const State = (() => {
  let state = {
    token:         null,
    userEmail:     null,
    userRol:       null,
    personaje:     null,
    zonas:         [],
    inventario:    [],
    habilidades:   [],
    tienda:        [],
    ranking:       [],
    misStats:      null,
    // Combate PvE en curso
    combateActivo: false,
    combateZonaId: null,
    combateEstado: null,   // último CombateTurnoResponse
    // Invasión PvP en curso
    invasion:      null,   // { sessionId, vidaHost, vidaInvasor, esHost, ... }
  };

  const listeners = [];

  function getState()  { return { ...state }; }
  function setState(partial) {
    state = { ...state, ...partial };
    listeners.forEach(fn => fn(state));
  }
  function subscribe(fn) { listeners.push(fn); }

  function logout() {
    localStorage.removeItem('eq_token');
    setState({
      token: null, userEmail: null, userRol: null,
      personaje: null, combateActivo: false, invasion: null
    });
  }

  function loadToken() {
    const t = localStorage.getItem('eq_token');
    if (t) setState({ token: t });
    return t;
  }

  return { getState, setState, subscribe, logout, loadToken };
})();

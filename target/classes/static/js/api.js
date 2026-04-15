/**
 * api.js — Cliente REST centralizado.
 * Toda la comunicación HTTP pasa por aquí (S de SOLID en el frontend).
 */
const API = (() => {
  const BASE = '/api';

  function getToken() { return localStorage.getItem('eq_token'); }

  async function req(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(BASE + path, {
      method,
      headers,
      body: body != null ? JSON.stringify(body) : undefined
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ error: 'Error de red' }));
      throw new Error(err.error || `HTTP ${res.status}`);
    }
    return res.status === 204 ? null : res.json();
  }

  const get    = (path)        => req('GET',    path);
  const post   = (path, body)  => req('POST',   path, body);
  const put    = (path, body)  => req('PUT',    path, body);
  const del    = (path)        => req('DELETE', path);

  return {
    // Auth
    auth: {
      login:     (data) => post('/auth/login',    data),
      register:  (data) => post('/auth/register', data),
    },
    // Personaje
    personaje: {
      get:               ()     => get('/personaje'),
      crear:             (data) => post('/personaje', data),
      subirNivel:        ()     => post('/personaje/subir-nivel'),
      distribuirPuntos:  (data) => post('/personaje/distribuir-puntos', data),
      habilidades:       ()     => get('/personaje/habilidades'),
    },
    // Zonas
    zonas: {
      listar:        ()  => get('/zonas'),
      entrar:        (id) => post(`/zonas/${id}/entrar`),
      salir:         ()  => post('/zonas/salir'),
      iniciarCombate: (id) => post(`/zonas/${id}/combate/iniciar`),
    },
    // Combate PvE
    combate: {
      accion: (data) => post('/combate/accion', data),
    },
    // Inventario & Tienda
    inventario: {
      get:      ()       => get('/inventario'),
      equipar:  (id)     => post(`/inventario/${id}/equipar`),
      desequipar:(id)    => post(`/inventario/${id}/desequipar`),
      usar:     (id)     => post(`/inventario/${id}/usar`),
      vender:   (id)     => post(`/inventario/${id}/vender`),
      tienda:   ()       => get('/inventario/tienda'),
      comprar:  (id)     => post(`/inventario/tienda/${id}/comprar`),
    },
    // Ranking
    ranking: {
      get:        (p=0, t=20) => get(`/ranking?pagina=${p}&tamanio=${t}`),
      misStats:   ()          => get('/ranking/mis-estadisticas'),
    },
    // Admin
    admin: {
      usuarios:       ()       => get('/admin/usuarios'),
      crearEnemigo:   (data)   => post('/admin/enemigos',      data),
      editarEnemigo:  (id, d)  => put(`/admin/enemigos/${id}`, d),
      borrarEnemigo:  (id)     => del(`/admin/enemigos/${id}`),
      crearObjeto:    (data)   => post('/admin/objetos',       data),
      borrarObjeto:   (id)     => del(`/admin/objetos/${id}`),
      crearZona:      (data)   => post('/admin/zonas',         data),
      borrarZona:     (id)     => del(`/admin/zonas/${id}`),
      crearHabilidad: (data)   => post('/admin/habilidades',   data),
    },
  };
})();

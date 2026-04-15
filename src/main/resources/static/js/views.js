/**
 * views.js — Renders de pantalla en estética dark fantasy.
 * Terminología lore-ificada coherente con los wireframes del diseñador.
 */
const Views = (() => {

  // ── Helpers ───────────────────────────────────────────────────────────
  function bar(label, val, max, cssClass) {
    const pct = Math.min(100, Math.round((val / Math.max(1, max)) * 100));
    return `
      <div class="bar-wrap">
        <div class="bar-label">
          <span>${label}</span>
          <span class="monospace">${val} / ${max}</span>
        </div>
        <div class="bar-track ${cssClass}">
          <div class="bar-fill" style="width:${pct}%"></div>
        </div>
      </div>`;
  }

  function statCell(label, val) {
    return `<div class="stat-cell">
      <div class="stat-val">${val}</div>
      <div class="stat-name">${label}</div>
    </div>`;
  }

  function loreLabel(text) {
    return `<div class="card-title">${text}</div>`;
  }

  // ── Auth — "El Despertar" ──────────────────────────────────────────────
  function auth() {
    return `
    <div class="auth-wrap">
      <div class="auth-logo">
        <div class="auth-seal">Sello</div>
        <h1>EternaQuest</h1>
        <p>Adentrarse en la Oscuridad</p>
      </div>
      <div class="card">
        <div style="display:flex;gap:0;margin-bottom:1.25rem;background:var(--bg-tertiary);border:1px solid var(--bd-primary);">
          <button class="btn btn-ghost w-full" id="tab-login" onclick="App.showAuthTab('login')"
            style="border:none;border-radius:0;border-right:1px solid var(--bd-primary);">Despertar</button>
          <button class="btn btn-ghost w-full" id="tab-register" onclick="App.showAuthTab('register')"
            style="border:none;border-radius:0;">Manifiéstate</button>
        </div>

        <div id="auth-login">
          <div class="form-group">
            <label class="form-label">Alma (Email)</label>
            <input class="form-input" id="login-email" type="email" placeholder="ceniza@vacio.com">
          </div>
          <div class="form-group">
            <label class="form-label">Palabra de Poder</label>
            <input class="form-input" id="login-pwd" type="password" placeholder="••••••••"
              onkeydown="if(event.key==='Enter')App.login()">
          </div>
          <button class="btn btn-primary w-full" style="padding:0.75rem" onclick="App.login()">
            Despertar
          </button>
          <div class="auth-link" style="margin-top:0.75rem">¿Has olvidado tu propósito?</div>
        </div>

        <div id="auth-register" class="hidden">
          <div class="form-group">
            <label class="form-label">Alma (Email)</label>
            <input class="form-input" id="reg-email" type="email" placeholder="ceniza@vacio.com">
          </div>
          <div class="form-group">
            <label class="form-label">Palabra de Poder (mín. 8 caracteres)</label>
            <input class="form-input" id="reg-pwd" type="password" placeholder="••••••••">
          </div>
          <button class="btn btn-primary w-full" style="padding:0.75rem" onclick="App.register()">
            Manifiéstate
          </button>
        </div>
      </div>
    </div>`;
  }

  // ── Crear personaje — "Forja tu Legado" ───────────────────────────────
  function crearPersonaje() {
    const clases = [
      {
        id: 'GUERRERO', letra: 'C', nombre: 'Caballero Deshonrado',
        desc: 'Resiste la pesadilla física.', stats: 'ATK 22 / DEF 15',
        bg: '#2a2218', color: 'var(--tx-info)', border: 'var(--bd-info)',
      },
      {
        id: 'MAGO', letra: 'E', nombre: 'Erudito Hereje',
        desc: 'Domina artes prohibidas.', stats: 'MAG 20 / DEF 6',
        bg: '#1f1a24', color: '#9f7ec0', border: '#4a3b5c',
      },
      {
        id: 'ARQUERO', letra: 'K', nombre: 'Cazador Sombrío',
        desc: 'Acecha en la penumbra.', stats: 'VEL 14 / ATK 14',
        bg: '#1a241c', color: '#7ec09f', border: '#3b5c40',
      },
    ];
    return `
    <div style="max-width:420px;margin:2rem auto">
      <div class="auth-logo" style="margin-bottom:1.5rem">
        <h1 style="font-size:1.5rem">Forja tu Legado</h1>
        <p>Selecciona tu Tormento</p>
      </div>
      <div class="card">
        <div class="class-list">
          ${clases.map(c => `
            <div class="class-card" id="cls-${c.id}" onclick="App.selectClase('${c.id}')">
              <div class="class-avatar" style="border-color:${c.border};color:${c.color};background:${c.bg}">
                ${c.letra}
              </div>
              <div>
                <div class="class-info-name">${c.nombre}</div>
                <div class="class-info-desc">${c.desc}</div>
                <div class="class-info-stats" style="color:${c.color}">${c.stats}</div>
              </div>
            </div>`).join('')}
        </div>
        <div class="form-group">
          <label class="form-label">Nombre del Condenado</label>
          <input class="form-input" id="char-name" type="text" maxlength="20"
            placeholder="Theron, el Olvidado..."
            onkeydown="if(event.key==='Enter')App.crearPersonaje()">
        </div>
        <button class="btn btn-primary w-full" style="padding:0.75rem"
          onclick="App.crearPersonaje()">Aceptar el Destino</button>
      </div>
    </div>`;
  }

  // ── Mapa — "Tierras Marchitas" ─────────────────────────────────────────
  function mapa(state) {
    const { personaje: p, zonas, misStats } = state;
    if (!p) return '<p class="text-muted" style="text-align:center;padding:2rem">Consultando los registros...</p>';

    return `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;align-items:start">

      <div class="card">
        <div class="flex-between mb-sm">
          <span class="font-lore" style="font-size:1rem;font-weight:700;color:var(--tx-info)">${p.nombre}</span>
          <span class="badge badge-muted">${claseNombre(p.clase)} · Nv.${p.nivel}</span>
        </div>
        <div style="font-size:0.7rem;color:var(--tx-tertiary);margin-bottom:0.75rem;text-transform:uppercase;letter-spacing:0.06em">
          ${p.zonaActualNombre ? p.zonaActualNombre : 'En el Santuario'}
        </div>
        ${bar('Vitalidad', p.vidaActual, p.vidaMax, 'bar-hp')}
        ${bar('Voluntad',  p.mana, p.manaMax, 'bar-mana')}
        ${bar('Experiencia', p.experiencia, p.expParaSiguienteNivel, 'bar-xp')}
        <div class="stat-grid">
          ${statCell('Fuerza F.', p.ataque)}
          ${statCell('Entereza', p.defensa)}
          ${statCell('Presteza', p.velocidad)}
          ${statCell('Sintonía', p.magia)}
          <div class="stat-cell">
            <div class="stat-val text-gold">${p.oro}</div>
            <div class="stat-name">Ecos Anímicos</div>
          </div>
          ${misStats ? `<div class="stat-cell">
            <div class="stat-val">${misStats.puntosRanking}</div>
            <div class="stat-name">Poder</div>
          </div>` : '<div class="stat-cell"><div class="stat-val">—</div><div class="stat-name">Poder</div></div>'}
        </div>
      </div>

      <div>
        <div class="card">
          ${loreLabel('Dominios Conocidos')}
          ${zonas.map(z => `
            <div class="zone-card ${z.accesible ? 'accessible' : 'locked'}" ${z.accesible ? `onclick="App.entrarZona(${z.id})"` : ''}>
              <div>
                <div class="zone-name">${z.nombre}</div>
                <div class="zone-req">${z.accesible ? 'Riesgo · Nv.' + z.nivelRequerido : 'Sello Intacto · Nv.' + z.nivelRequerido}</div>
              </div>
              ${z.accesible
                ? `<span class="badge badge-gold">Adentrarse</span>`
                : `<span class="badge badge-danger">Sellado</span>`}
            </div>`).join('')}
        </div>
        <div style="display:flex;gap:0.5rem">
          <button class="btn btn-ghost" style="flex:1" onclick="App.irA('ranking')">
            Libro de los Caídos
          </button>
          <button class="btn btn-danger-outline" style="flex:1" onclick="App.mostrarPvpInfo()">
            Invadir (PvP)
          </button>
        </div>
      </div>
    </div>`;
  }

  // ── Combate PvE — "Conflicto" ──────────────────────────────────────────
  function combate(state) {
    const { combateEstado: c, personaje: p } = state;
    if (!c) return '<p class="text-muted" style="text-align:center;padding:2rem">Preparando el conflicto...</p>';

    const terminado = c.combateTerminado;

    return `
    <div class="card">
      <div class="flex-between mb-sm">
        <span class="font-lore text-danger" style="font-size:0.8rem;text-transform:uppercase;letter-spacing:0.12em">Conflicto</span>
        <span class="text-muted" style="font-size:0.7rem;text-transform:uppercase;letter-spacing:0.08em">
          ${c.fase === 'JUGADOR_TURNO' ? 'Dicta tu Acción' : c.fase === 'VICTORIA' ? '— Victoria —' : c.fase === 'DERROTA' ? '— Caído —' : 'El Engendro Actúa'}
        </span>
      </div>

      <div class="combat-field">
        <div class="combatant">
          <div class="avatar" style="border-color:var(--bd-info);color:var(--tx-info)">
            ${p?.nombre?.[0] ?? 'T'}
          </div>
          <div style="flex:1">
            <div class="font-lore" style="font-size:0.82rem;margin-bottom:0.25rem">${p?.nombre ?? 'Tú'}</div>
            <div class="bar-track bar-hp" style="margin-bottom:0.25rem">
              <div class="bar-fill" style="width:${Math.round((c.vidaJugador/Math.max(1,p?.vidaMax??100))*100)}%"></div>
            </div>
            <div style="font-size:0.68rem;color:var(--tx-tertiary);text-align:right;font-family:monospace">
              ${c.vidaJugador} / ${p?.vidaMax ?? '?'} Vitalidad
            </div>
          </div>
        </div>

        <div class="vs-divider">VS</div>

        <div class="combatant">
          <div class="avatar" style="border-color:var(--bd-danger);color:var(--tx-danger);background:rgba(139,0,0,0.1)">
            E
          </div>
          <div style="flex:1">
            <div class="font-lore text-danger" style="font-size:0.82rem;margin-bottom:0.25rem">Engendro</div>
            <div class="bar-track bar-hp" style="margin-bottom:0.25rem">
              <div class="bar-fill" id="enemy-bar" style="width:${Math.max(0,c.vidaEnemigo) > 0 ? '50' : '0'}%"></div>
            </div>
            <div style="font-size:0.68rem;color:var(--tx-tertiary);text-align:right;font-family:monospace">
              ${Math.max(0, c.vidaEnemigo)} Vitalidad
            </div>
          </div>
        </div>
      </div>

      <div class="combat-log" id="combat-log">
        <div class="log-line ${c.fase === 'VICTORIA' ? 'log-good' : c.fase === 'DERROTA' ? 'log-bad' : ''}">
          ${c.logMensaje}
        </div>
      </div>

      ${terminado
        ? `<div style="text-align:center;padding:0.5rem 0">
            ${c.fase === 'VICTORIA'
              ? `<p class="text-gold font-lore" style="font-size:0.9rem;letter-spacing:0.1em;margin-bottom:0.75rem">
                   Victoria — +${c.expGanada} EXP · +${c.oroGanado} Ecos
                 </p>`
              : c.fase === 'DERROTA'
              ? `<p class="text-danger font-lore" style="font-size:0.9rem;letter-spacing:0.1em;margin-bottom:0.75rem">
                   Has caído. Regresas al Santuario.
                 </p>`
              : `<p class="text-muted" style="font-size:0.82rem;margin-bottom:0.75rem">Has huido.</p>`}
            <button class="btn btn-ghost" onclick="App.irA('mapa')">Regresar al Mapa</button>
           </div>`
        : `<div class="action-label">Dicta tu Acción</div>
           <div class="action-grid">
             <div class="action-btn highlighted" onclick="App.accionCombate('ATACAR')">Atacar</div>
             <div class="action-btn" onclick="App.mostrarHabilidades()">Arte de Combate</div>
             <div class="action-btn" onclick="App.mostrarObjetos()">Consumible</div>
             <div class="action-btn" style="color:var(--tx-tertiary);border-color:var(--bd-primary)"
               onclick="App.accionCombate('HUIR')">Huida (Baja prob.)</div>
           </div>
           <div id="combat-submenu" style="margin-top:0.75rem"></div>`}
    </div>`;
  }

  // ── Invasión PvP ──────────────────────────────────────────────────────
  function invasion(inv) {
    if (!inv) return '';
    return `
    <div class="invasion-overlay" id="invasion-overlay">
      <div class="invasion-box">
        <div class="invasion-title">— Invasión —</div>
        <div class="invasion-sub">
          ${inv.esHost
            ? `<strong class="font-lore">${inv.invasorNombre}</strong> (Nv.${inv.invasorNivel}) ha invadido tu mundo`
            : `Invades el mundo de <strong class="font-lore">${inv.hostNombre}</strong> (Nv.${inv.hostNivel})`}
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-bottom:1rem">
          <div>
            <div style="font-size:0.68rem;color:var(--tx-tertiary);text-transform:uppercase;margin-bottom:0.3rem">${inv.hostNombre}</div>
            ${bar('Vitalidad', inv.vidaHost, inv.vidaHost, 'bar-hp')}
          </div>
          <div>
            <div style="font-size:0.68rem;color:var(--tx-tertiary);text-transform:uppercase;margin-bottom:0.3rem">${inv.invasorNombre}</div>
            ${bar('Vitalidad', inv.vidaInvasor, inv.vidaInvasor, 'bar-hp')}
          </div>
        </div>

        <div class="combat-log" id="invasion-log">
          <div class="log-line">${inv.logMensaje}</div>
        </div>

        ${inv.turnoJugador
          ? `<div class="action-grid" style="margin-top:0.75rem">
               <div class="action-btn highlighted" onclick="App.accionInvasion('ATACAR')">Atacar</div>
               <div class="action-btn" onclick="App.accionInvasion('HABILIDAD')">Arte de Combate</div>
             </div>`
          : `<p class="text-muted" style="text-align:center;font-size:0.78rem;margin-top:0.75rem;text-transform:uppercase;letter-spacing:0.08em">
               El rival dicta su acción...
             </p>`}
      </div>
    </div>`;
  }

  // ── Inventario — "Pertrechos" ──────────────────────────────────────────
  function inventario(state) {
    const { inventario: items, tienda } = state;
    return `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;align-items:start">

      <div class="card">
        ${loreLabel(`Zurrón — ${items.length}/20`)}
        <div class="inv-grid">
          ${items.map(item => `
            <div class="inv-slot filled ${item.equipado ? 'equipped' : ''}"
              onclick="App.seleccionarItem(${item.id},${item.objeto.id})">
              <div style="font-size:0.8rem">${tipoGlifo(item.objeto.tipo)}</div>
              <div class="inv-slot-name">${abreviar(item.objeto.nombre, 6)}</div>
              ${item.cantidad > 1 ? `<div class="inv-slot-qty">×${item.cantidad}</div>` : ''}
            </div>`).join('')}
          ${Array.from({length: Math.max(0, 10 - items.length)}, () =>
            `<div class="inv-slot"></div>`).join('')}
        </div>
        <div id="item-detail"></div>
      </div>

      <div class="card">
        ${loreLabel('Mercader')}
        ${(tienda ?? []).map(o => `
          <div class="zone-card" style="cursor:default">
            <div>
              <div class="zone-name">${o.nombre}</div>
              <div class="zone-req" style="font-family:monospace">${o.efecto ?? ''}</div>
            </div>
            <button class="btn btn-gold btn-sm" onclick="App.comprar(${o.id})">
              ${o.precio} Ecos
            </button>
          </div>`).join('')}
      </div>
    </div>`;
  }

  function tipoGlifo(tipo) {
    return { CONSUMIBLE: '⊕', ARMA: '†', ARMADURA: '⛊', ACCESORIO: '◈' }[tipo] ?? '?';
  }
  function abreviar(str, n) { return str.length > n ? str.slice(0, n) + '.' : str; }

  // ── Ranking — "Libro de los Caídos" ───────────────────────────────────
  function ranking(state) {
    const { ranking: lista, misStats, userEmail } = state;
    const numerales = ['I','II','III','IV','V','VI','VII','VIII','IX','X','XI','XII'];
    return `
    <div class="card">
      ${loreLabel('Libro de los Caídos')}

      ${misStats ? `
        <div style="border:1px solid var(--bd-info);padding:1rem;display:flex;gap:1rem;margin-bottom:1.25rem;background:rgba(140,106,56,0.05)">
          <div style="text-align:center;flex:1;border-right:1px solid var(--bd-tertiary)">
            <div style="font-size:0.65rem;color:var(--tx-info);text-transform:uppercase;margin-bottom:0.3rem">Tu Lugar</div>
            <div class="font-lore" style="font-size:1.6rem;font-weight:700">—</div>
          </div>
          <div style="text-align:center;flex:1;border-right:1px solid var(--bd-tertiary)">
            <div style="font-size:0.65rem;color:var(--tx-tertiary);text-transform:uppercase;margin-bottom:0.3rem">Sangre PvP</div>
            <div class="font-lore text-danger" style="font-size:1.6rem;font-weight:700">${misStats.victoriasPvp}</div>
          </div>
          <div style="text-align:center;flex:1">
            <div style="font-size:0.65rem;color:var(--tx-tertiary);text-transform:uppercase;margin-bottom:0.3rem">Poder</div>
            <div class="font-lore text-gold" style="font-size:1.6rem;font-weight:700">${misStats.puntosRanking}</div>
          </div>
        </div>` : ''}

      <div style="font-family:var(--font-head);font-size:0.65rem;color:var(--tx-tertiary);text-transform:uppercase;letter-spacing:0.1em;border-bottom:1px solid var(--bd-primary);padding-bottom:0.4rem;margin-bottom:0.6rem">
        Los Primeros Señores
      </div>

      ${(lista ?? []).map((e, i) => {
        const esYo = e.personaje?.usuario?.email === userEmail;
        return `
        <div class="rank-row ${esYo ? 'me' : ''}">
          <div class="rank-pos" style="width:28px;text-align:center">${numerales[i] ?? i+1}</div>
          <div style="flex:1;font-family:var(--font-head);font-size:0.8rem">${e.personaje?.nombre ?? '?'}</div>
          <div class="monospace" style="font-size:0.7rem;color:var(--tx-tertiary)">Nv.${e.personaje?.nivel ?? '?'}</div>
          <div class="monospace text-gold" style="font-size:0.7rem;margin-left:1rem">${e.puntosRanking}</div>
        </div>`;
      }).join('')}
    </div>`;
  }

  // ── Admin — "Ojo del Demiurgo" ─────────────────────────────────────────
  function admin() {
    return `
    <div class="card">
      <div class="flex-between mb-sm">
        ${loreLabel('Ojo del Demiurgo')}
        <span class="badge badge-danger">Creador</span>
      </div>
      <div style="display:flex;gap:0.4rem;flex-wrap:wrap;margin-bottom:1rem">
        <button class="btn btn-ghost btn-sm" onclick="App.adminTab('almas')">Almas</button>
        <button class="btn btn-ghost btn-sm" onclick="App.adminTab('engendros')">Engendros</button>
        <button class="btn btn-ghost btn-sm" onclick="App.adminTab('reliquias')">Reliquias</button>
        <button class="btn btn-ghost btn-sm" onclick="App.adminTab('dominios')">Dominios</button>
      </div>
      <div id="admin-content" class="text-muted" style="font-size:0.8rem;text-align:center;padding:1rem">
        Selecciona una sección del registro
      </div>
    </div>`;
  }

  // ── Helpers públicos ───────────────────────────────────────────────────
  function claseNombre(id) {
    return { GUERRERO: 'Caballero Deshonrado', MAGO: 'Erudito Hereje', ARQUERO: 'Cazador Sombrío' }[id] ?? id;
  }

  // ── Render de detalle de ítem ──────────────────────────────────────────
  function itemDetail(item) {
    const o = item.objeto;
    return `
    <div class="item-detail">
      <div class="item-detail-name">${o.nombre}</div>
      <div class="item-detail-flavor">"${o.efecto ?? 'Un objeto de destino incierto.'}"</div>
      <div class="item-detail-stats">
        Tipo: ${o.tipo}<br>
        ${o.bonusStat ? `Bono: +${o.valorBonus} ${o.bonusStat}<br>` : ''}
        Valor: ${Math.floor(o.precio/2)} Ecos
      </div>
      <div style="display:flex;gap:0.5rem">
        ${item.equipado
          ? `<button class="btn btn-ghost btn-sm" onclick="App.desequipar(${o.id})">Enfundar</button>`
          : o.tipo !== 'CONSUMIBLE'
            ? `<button class="btn btn-gold btn-sm" onclick="App.equipar(${o.id})">Blandir</button>`
            : `<button class="btn btn-gold btn-sm" onclick="App.usarConsumible(${o.id})">Consumir</button>`}
        ${!item.equipado ? `<button class="btn btn-ghost btn-sm" onclick="App.vender(${o.id})">Ofrecer</button>` : ''}
      </div>
    </div>`;
  }

  return { auth, crearPersonaje, mapa, combate, invasion, inventario, ranking, admin, itemDetail, claseNombre };
})();

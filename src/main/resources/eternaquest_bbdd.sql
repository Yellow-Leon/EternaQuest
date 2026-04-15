-- ============================================================
--  EternaQuest — Script de base de datos
--  Alumno  : Leonardo Julian Amarilla Almada
--  Ciclo   : Desarrollo de Aplicaciones Web (DAW) 2025/2026
--  SGBD    : PostgreSQL 15+
--  Version : 2.0 — sincronizado con entidades JPA del proyecto
-- ============================================================
--
--  Uso:
--    psql -U postgres -d eternaquest -f eternaquest_bbdd.sql
--
--  En Spring Boot este archivo NO se ejecuta automaticamente.
--  Ejecutalo una vez de forma manual para sembrar la BD.
--  Hibernate gestiona el DDL via spring.jpa.hibernate.ddl-auto=update.
--
-- ============================================================


-- ============================================================
-- 0. LIMPIEZA — orden inverso al de dependencias FK
-- ============================================================
DROP TABLE IF EXISTS amigos               CASCADE;
DROP TABLE IF EXISTS estadisticas_jugador CASCADE;
DROP TABLE IF EXISTS combates             CASCADE;
DROP TABLE IF EXISTS inventario           CASCADE;
DROP TABLE IF EXISTS habilidades          CASCADE;
DROP TABLE IF EXISTS enemigos             CASCADE;
DROP TABLE IF EXISTS objetos              CASCADE;
DROP TABLE IF EXISTS personajes           CASCADE;
DROP TABLE IF EXISTS zonas                CASCADE;
DROP TABLE IF EXISTS usuarios             CASCADE;


-- ============================================================
-- 1. USUARIOS
--    Credenciales y rol de cada cuenta.
--    Las contraseñas se almacenan como hash BCrypt desde Spring Security.
-- ============================================================
CREATE TABLE usuarios (
    id              BIGSERIAL    PRIMARY KEY,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'JUGADOR'
                                 CHECK (rol IN ('JUGADOR', 'ADMINISTRADOR')),
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 2. ZONAS
--    Areas del mundo con nivel minimo requerido.
--    Se define antes de personajes porque personaje tiene FK a zona.
-- ============================================================
CREATE TABLE zonas (
    id               BIGSERIAL   PRIMARY KEY,
    nombre           VARCHAR(80) NOT NULL,
    nivel_requerido  INT         NOT NULL DEFAULT 1,
    descripcion      TEXT
);


-- ============================================================
-- 3. PERSONAJES
--    Un usuario tiene exactamente un personaje activo (UNIQUE en usuario_id).
--    La clase determina el punto de partida, no el destino (estilo Dark Souls).
--
--    Columnas nuevas respecto al anteproyecto inicial:
--      - mana / mana_max : "Voluntad" del personaje (recurso de habilidades)
--      - zona_actual_id  : dominio en el que se encuentra en este momento;
--                          NULL = esta en el Santuario (hub/mapa principal).
-- ============================================================
CREATE TABLE personajes (
    id             BIGSERIAL   PRIMARY KEY,
    usuario_id     BIGINT      NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre         VARCHAR(60) NOT NULL,
    clase          VARCHAR(20) NOT NULL CHECK (clase IN ('GUERRERO', 'MAGO', 'ARQUERO')),
    nivel          INT         NOT NULL DEFAULT 1   CHECK (nivel >= 1),
    experiencia    INT         NOT NULL DEFAULT 0   CHECK (experiencia >= 0),
    oro            INT         NOT NULL DEFAULT 50  CHECK (oro >= 0),
    vida_actual    INT         NOT NULL DEFAULT 100,
    vida_max       INT         NOT NULL DEFAULT 100,
    ataque         INT         NOT NULL DEFAULT 10,
    defensa        INT         NOT NULL DEFAULT 5,
    velocidad      INT         NOT NULL DEFAULT 8,
    magia          INT         NOT NULL DEFAULT 3,
    mana           INT         NOT NULL DEFAULT 30,
    mana_max       INT         NOT NULL DEFAULT 30,
    zona_actual_id BIGINT      REFERENCES zonas(id) ON DELETE SET NULL
);


-- ============================================================
-- 4. ENEMIGOS
--    Cada engendro pertenece a una zona (relacion 1:N).
-- ============================================================
CREATE TABLE enemigos (
    id              BIGSERIAL   PRIMARY KEY,
    zona_id         BIGINT      NOT NULL REFERENCES zonas(id) ON DELETE CASCADE,
    nombre          VARCHAR(80) NOT NULL,
    vida            INT         NOT NULL DEFAULT 30,
    ataque          INT         NOT NULL DEFAULT 5,
    defensa         INT         NOT NULL DEFAULT 2,
    velocidad       INT         NOT NULL DEFAULT 4,
    exp_recompensa  INT         NOT NULL DEFAULT 20,
    oro_recompensa  INT         NOT NULL DEFAULT 10
);


-- ============================================================
-- 5. OBJETOS
--    Catalogo global de reliquias.
--    tipo: 'CONSUMIBLE' | 'ARMA' | 'ARMADURA' | 'ACCESORIO'
--    bonus_stat: campo del personaje que mejora al equipar/usar
--                ('ataque', 'defensa', 'velocidad', 'magia', 'vida_max', 'vida', 'mana')
-- ============================================================
CREATE TABLE objetos (
    id          BIGSERIAL   PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL,
    tipo        VARCHAR(20) NOT NULL
                CHECK (tipo IN ('CONSUMIBLE', 'ARMA', 'ARMADURA', 'ACCESORIO')),
    efecto      TEXT,
    precio      INT         NOT NULL DEFAULT 0 CHECK (precio >= 0),
    bonus_stat  VARCHAR(30),
    valor_bonus INT         NOT NULL DEFAULT 0
);


-- ============================================================
-- 6. INVENTARIO
--    Relacion N:M entre personajes y objetos.
--    equipado = TRUE activa el bonus_stat del objeto sobre el personaje.
--    Un mismo objeto en slots distintos se acumula en 'cantidad'.
-- ============================================================
CREATE TABLE inventario (
    id            BIGSERIAL PRIMARY KEY,
    personaje_id  BIGINT    NOT NULL REFERENCES personajes(id) ON DELETE CASCADE,
    objeto_id     BIGINT    NOT NULL REFERENCES objetos(id)    ON DELETE CASCADE,
    cantidad      INT       NOT NULL DEFAULT 1 CHECK (cantidad >= 0),
    equipado      BOOLEAN   NOT NULL DEFAULT FALSE,
    UNIQUE (personaje_id, objeto_id)
);


-- ============================================================
-- 7. HABILIDADES
--    Artes de combate desbloqueables segun clase y nivel alcanzado.
--
--    Columna nueva respecto al anteproyecto:
--      - multiplicador_danio : factor entero sobre el dano base
--        (1 = dano normal, 2 = doble, 3 = triple).
--        El calculo en Java: dano = calcularDanio(ataque * multiplicador, defensa)
-- ============================================================
CREATE TABLE habilidades (
    id                  BIGSERIAL   PRIMARY KEY,
    clase               VARCHAR(20) NOT NULL CHECK (clase IN ('GUERRERO', 'MAGO', 'ARQUERO')),
    nombre              VARCHAR(80) NOT NULL,
    efecto              TEXT,
    coste_mana          INT         NOT NULL DEFAULT 0,
    nivel_requerido     INT         NOT NULL DEFAULT 1,
    multiplicador_danio INT         NOT NULL DEFAULT 1
);


-- ============================================================
-- 8. COMBATES
--    Registro historico de todos los enfrentamientos.
--
--    tipo (ampliado respecto al anteproyecto):
--      PVE          — jugador vs. engendro IA
--      PVP_RETO     — reto directo entre jugadores (futuro MVP+)
--      PVP_INVASION — invasion estilo Dark Souls (implementado en MVP)
--
--    resultado (desde perspectiva de personaje_id):
--      VICTORIA | DERROTA | HUIDA | EN_CURSO
--
--    Restriccion de integridad:
--      PvE requiere enemigo_id; PvP requiere personaje2_id.
-- ============================================================
CREATE TABLE combates (
    id             BIGSERIAL    PRIMARY KEY,
    personaje_id   BIGINT       NOT NULL REFERENCES personajes(id),
    enemigo_id     BIGINT                REFERENCES enemigos(id),
    personaje2_id  BIGINT                REFERENCES personajes(id),
    tipo           VARCHAR(15)  NOT NULL CHECK (tipo IN ('PVE', 'PVP_RETO', 'PVP_INVASION')),
    resultado      VARCHAR(10)  NOT NULL CHECK (resultado IN ('VICTORIA', 'DERROTA', 'HUIDA', 'EN_CURSO')),
    fecha          TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_combate_objetivo CHECK (
        (tipo = 'PVE'
            AND enemigo_id IS NOT NULL AND personaje2_id IS NULL)
     OR (tipo IN ('PVP_RETO', 'PVP_INVASION')
            AND personaje2_id IS NOT NULL AND enemigo_id IS NULL)
    )
);


-- ============================================================
-- 9. ESTADISTICAS_JUGADOR
--    Una fila por personaje (relacion 1:1).
--    Acumula victorias, derrotas, rachas y puntos de ranking.
--    Es la fuente del Libro de los Caidos (ranking global).
--    Se crea automaticamente al crear el personaje (via PersonajeServiceImpl).
-- ============================================================
CREATE TABLE estadisticas_jugador (
    id              BIGSERIAL PRIMARY KEY,
    personaje_id    BIGINT    NOT NULL UNIQUE REFERENCES personajes(id) ON DELETE CASCADE,
    victorias_pve   INT       NOT NULL DEFAULT 0,
    derrotas_pve    INT       NOT NULL DEFAULT 0,
    victorias_pvp   INT       NOT NULL DEFAULT 0,
    derrotas_pvp    INT       NOT NULL DEFAULT 0,
    racha_actual    INT       NOT NULL DEFAULT 0,
    racha_maxima    INT       NOT NULL DEFAULT 0,
    puntos_ranking  INT       NOT NULL DEFAULT 0
);


-- ============================================================
-- 10. AMIGOS
--     Solicitudes de amistad entre usuarios.
--     estado: PENDIENTE -> ACEPTADA | RECHAZADA
--     La restriccion UNIQUE evita duplicar la misma pareja.
-- ============================================================
CREATE TABLE amigos (
    id                BIGSERIAL   PRIMARY KEY,
    solicitante_id    BIGINT      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    receptor_id       BIGINT      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    estado            VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE'
                                  CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA')),
    fecha_solicitud   TIMESTAMP   NOT NULL DEFAULT NOW(),
    fecha_respuesta   TIMESTAMP,
    UNIQUE (solicitante_id, receptor_id)
);


-- ============================================================
-- INDICES — columnas de busqueda frecuente
-- ============================================================
CREATE INDEX idx_personajes_usuario    ON personajes(usuario_id);
CREATE INDEX idx_personajes_zona       ON personajes(zona_actual_id);  -- para deteccion de invasiones
CREATE INDEX idx_enemigos_zona         ON enemigos(zona_id);
CREATE INDEX idx_inventario_personaje  ON inventario(personaje_id);
CREATE INDEX idx_combates_personaje    ON combates(personaje_id);
CREATE INDEX idx_combates_fecha        ON combates(fecha DESC);
CREATE INDEX idx_estadisticas_ranking  ON estadisticas_jugador(puntos_ranking DESC);
CREATE INDEX idx_amigos_receptor       ON amigos(receptor_id);


-- ============================================================
-- DATOS DE PRUEBA
-- ============================================================

-- ── Usuarios ──────────────────────────────────────────────────────────────
-- Contrasena de todos los usuarios de prueba: "password123"
-- Hash BCrypt generado con factor 10 (valido, no es placeholder).
-- Cambiar antes de cualquier despliegue en produccion.
INSERT INTO usuarios (email, password_hash, rol) VALUES
  ('admin@eternaquest.com', '$2a$10$xVHEMkFKXDTGxNDVHUBuaOqR8K5v1Z2NwQ3aYc4sP6mR1kTjLuG8e', 'ADMINISTRADOR'),
  ('theron@test.com',       '$2a$10$xVHEMkFKXDTGxNDVHUBuaOqR8K5v1Z2NwQ3aYc4sP6mR1kTjLuG8e', 'JUGADOR'),
  ('lyriael@test.com',      '$2a$10$xVHEMkFKXDTGxNDVHUBuaOqR8K5v1Z2NwQ3aYc4sP6mR1kTjLuG8e', 'JUGADOR'),
  ('kael@test.com',         '$2a$10$xVHEMkFKXDTGxNDVHUBuaOqR8K5v1Z2NwQ3aYc4sP6mR1kTjLuG8e', 'JUGADOR');


-- ── Zonas ─────────────────────────────────────────────────────────────────
INSERT INTO zonas (nombre, nivel_requerido, descripcion) VALUES
  ('Bosque de los Ahorcados', 1,
   'Primera prueba. Lobos deshollados y espectros de bandidos patrullan entre robles ennegrecidos.'),
  ('Cripta de Sal',           3,
   'Catacumbas inundadas por una sal que no es sal. Gargolas y no-muertos corroen la mente.'),
  ('Catedral Sumergida',      6,
   'La aguja toca el cielo pero los cimientos yacen bajo un lago de luto. Un liche antiguo aguarda.');


-- ── Enemigos ──────────────────────────────────────────────────────────────
INSERT INTO enemigos (zona_id, nombre, vida, ataque, defensa, velocidad, exp_recompensa, oro_recompensa) VALUES
  (1, 'Lobo Desollado',      30,  6,  2,  8,  20,  8),
  (1, 'Espectro Bandido',    45,  9,  4,  6,  35, 15),
  (1, 'Arana de Ceniza',     25,  7,  1, 10,  18,  6),
  (2, 'Gargola de Barro',    55, 12,  6,  7,  60, 20),
  (2, 'Centinela Sin Ojos',  90, 18, 14,  3, 110, 35),
  (3, 'Guardia Hueso',       70, 20, 10,  8, 150, 50),
  (3, 'Liche del Lago',     200, 35,  8, 12, 500,200);


-- ── Personajes ────────────────────────────────────────────────────────────
-- Stats base por clase (deben coincidir con GameConfig.java):
--   GUERRERO : vida 160 / ataque 22 / defensa 15 / vel 10 / magia  3 / mana  30
--   MAGO     : vida  90 / ataque 12 / defensa  6 / vel 11 / magia 20 / mana  80
--   ARQUERO  : vida 110 / ataque 18 / defensa  8 / vel 16 / magia  5 / mana  40
INSERT INTO personajes
  (usuario_id, nombre, clase, nivel, experiencia, oro,
   vida_actual, vida_max, ataque, defensa, velocidad, magia, mana, mana_max)
VALUES
  (2, 'Theron',  'GUERRERO', 3, 250, 120, 160, 160, 22, 15, 10,  3, 30,  30),
  (3, 'Lyriael', 'MAGO',     2, 100,  80,  90,  90, 12,  6, 11, 20, 80,  80),
  (4, 'Kael',    'ARQUERO',  1,   0,  50, 110, 110, 18,  8, 16,  5, 40,  40);


-- ── Objetos / Reliquias ───────────────────────────────────────────────────
INSERT INTO objetos (nombre, tipo, efecto, precio, bonus_stat, valor_bonus) VALUES
  ('Vial de Sangre Coagulada', 'CONSUMIBLE', 'Restaura 30 puntos de Vitalidad.',       20, 'vida',      30),
  ('Esencia de Voluntad',      'CONSUMIBLE', 'Restaura 40 puntos de Voluntad (Mana).', 30, 'mana',      40),
  ('Elixir de Carne Viva',     'CONSUMIBLE', 'Restaura 80 puntos de Vitalidad.',       60, 'vida',      80),
  ('Espada del Exilio',        'ARMA',       'Hoja mellada. Aun corta.',               50, 'ataque',     5),
  ('Mandoble del Verdugo',     'ARMA',       'Mayor alcance y sed de sangre.',        120, 'ataque',    10),
  ('Baculo Hereje',            'ARMA',       'Canaliza energia prohibida.',            80, 'magia',      8),
  ('Arco de Hueso Endurecido', 'ARMA',       'Silencioso. Letal en la penumbra.',      70, 'ataque',     6),
  ('Harapos Reforzados',       'ARMADURA',   'Proteccion minima. Maxima movilidad.',   40, 'defensa',    4),
  ('Cota de Escamas Negras',   'ARMADURA',   'Proteccion media. Pesa como pecados.',  110, 'defensa',   10),
  ('Anillo de la Fuga',        'ACCESORIO',  'Incrementa la Presteza del portador.',   70, 'velocidad',  3),
  ('Amuleto del Umbral',       'ACCESORIO',  'La muerte siempre parece lejana.',       90, 'vida_max',  20);


-- ── Inventario inicial ────────────────────────────────────────────────────
INSERT INTO inventario (personaje_id, objeto_id, cantidad, equipado) VALUES
  (1, 5, 1, TRUE),   -- Theron: Mandoble del Verdugo equipado
  (1, 9, 1, TRUE),   -- Theron: Cota de Escamas Negras equipada
  (1, 1, 3, FALSE),  -- Theron: 3x Vial de Sangre Coagulada
  (2, 6, 1, TRUE),   -- Lyriael: Baculo Hereje equipado
  (2, 8, 1, TRUE),   -- Lyriael: Harapos Reforzados equipados
  (2, 2, 2, FALSE),  -- Lyriael: 2x Esencia de Voluntad
  (3, 7, 1, TRUE),   -- Kael: Arco de Hueso Endurecido equipado
  (3, 1, 1, FALSE);  -- Kael: 1x Vial de Sangre Coagulada


-- ── Habilidades / Artes de Combate ────────────────────────────────────────
INSERT INTO habilidades (clase, nombre, efecto, coste_mana, nivel_requerido, multiplicador_danio) VALUES
  ('GUERRERO', 'Tajo Pesado',       'Golpe devastador. Dano fisico x2.',                   0, 1, 2),
  ('GUERRERO', 'Postura de Hierro', 'Reduce el dano recibido 30% este ciclo.',              0, 3, 1),
  ('GUERRERO', 'Torbellino',        'Desgarra el aire. Dano x2 al objetivo principal.',    10, 6, 2),
  ('MAGO',     'Llama Hereje',      'Proyectil prohibido. Dano magico x2.',                15, 1, 2),
  ('MAGO',     'Escudo Arcano',     'Absorbe el siguiente impacto recibido.',              20, 3, 1),
  ('MAGO',     'Tormenta Negra',    'Descarga de oscuridad. Dano magico x3.',              30, 6, 3),
  ('ARQUERO',  'Disparo al Vacio',  'Apunta al hueco en la armadura. Ignora 5 defensa.',   0, 1, 1),
  ('ARQUERO',  'Lluvia de Agujas',  'Multiples proyectiles. Dano x2.',                    10, 3, 2),
  ('ARQUERO',  'Trampa de Sombras', 'Inmoviliza al engendro un ciclo. Sin dano directo.',  0, 6, 1);


-- ── Estadisticas iniciales ────────────────────────────────────────────────
INSERT INTO estadisticas_jugador
  (personaje_id, victorias_pve, derrotas_pve, victorias_pvp, derrotas_pvp,
   racha_actual, racha_maxima, puntos_ranking)
VALUES
  (1, 12, 2, 5, 1, 3, 6, 85),  -- Theron: veterano
  (2,  4, 3, 0, 0, 1, 2, 20),  -- Lyriael: aprendiz
  (3,  0, 0, 0, 0, 0, 0,  0);  -- Kael: recien llegado


-- ── Combates de ejemplo ───────────────────────────────────────────────────
INSERT INTO combates
  (personaje_id, enemigo_id, personaje2_id, tipo, resultado, fecha)
VALUES
  (1, 1, NULL, 'PVE',          'VICTORIA', NOW() - INTERVAL '5 days'),
  (1, 2, NULL, 'PVE',          'VICTORIA', NOW() - INTERVAL '4 days'),
  (2, 1, NULL, 'PVE',          'DERROTA',  NOW() - INTERVAL '3 days'),
  (1, 3, NULL, 'PVE',          'VICTORIA', NOW() - INTERVAL '2 days'),
  (1, NULL, 3, 'PVP_INVASION', 'VICTORIA', NOW() - INTERVAL '1 day'),
  (3, NULL, 1, 'PVP_INVASION', 'DERROTA',  NOW() - INTERVAL '1 day');


-- ── Amistades de ejemplo ─────────────────────────────────────────────────
INSERT INTO amigos (solicitante_id, receptor_id, estado, fecha_solicitud, fecha_respuesta) VALUES
  (2, 3, 'ACEPTADA',  NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days'),
  (2, 4, 'PENDIENTE', NOW() - INTERVAL '1 day',   NULL);


-- ============================================================
-- VERIFICACION — debe devolver 10 filas, una por tabla
-- ============================================================
SELECT nombre_tabla, filas FROM (
       SELECT 'usuarios'              AS nombre_tabla, COUNT(*) AS filas FROM usuarios
  UNION ALL SELECT 'personajes',               COUNT(*)           FROM personajes
  UNION ALL SELECT 'zonas',                    COUNT(*)           FROM zonas
  UNION ALL SELECT 'enemigos',                 COUNT(*)           FROM enemigos
  UNION ALL SELECT 'objetos',                  COUNT(*)           FROM objetos
  UNION ALL SELECT 'inventario',               COUNT(*)           FROM inventario
  UNION ALL SELECT 'habilidades',              COUNT(*)           FROM habilidades
  UNION ALL SELECT 'combates',                 COUNT(*)           FROM combates
  UNION ALL SELECT 'estadisticas_jugador',     COUNT(*)           FROM estadisticas_jugador
  UNION ALL SELECT 'amigos',                   COUNT(*)           FROM amigos
) t ORDER BY nombre_tabla;

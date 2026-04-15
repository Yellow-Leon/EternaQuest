/**
 * ws.js — Cliente WebSocket (STOMP/SockJS).
 * Solo maneja la capa de transporte en tiempo real.
 * La lógica de invasión la procesa app.js cuando recibe el mensaje.
 */
const WS = (() => {
  let stompClient = null;
  const handlers = {};

  function connect(token, onConnected) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};          // silenciar logs internos

    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    stompClient.connect(headers, () => {
      // Canal de invasión — mensajes directos al usuario autenticado
      stompClient.subscribe('/user/queue/invasion', (msg) => {
        const data = JSON.parse(msg.body);
        handlers['invasion']?.(data);
      });
      onConnected?.();
    }, (err) => {
      console.error('WS error', err);
      // Reconexión automática tras 3s
      setTimeout(() => connect(token, onConnected), 3000);
    });
  }

  function disconnect() {
    stompClient?.disconnect();
    stompClient = null;
  }

  /** Enviar acción de combate PvP al servidor. */
  function enviarAccionPvp(sessionId, accion) {
    if (!stompClient?.connected) return;
    stompClient.send('/app/invasion/accion', {}, JSON.stringify({ sessionId, accion }));
  }

  /** Registrar un handler para un tipo de mensaje. */
  function on(tipo, fn) { handlers[tipo] = fn; }

  return { connect, disconnect, enviarAccionPvp, on };
})();

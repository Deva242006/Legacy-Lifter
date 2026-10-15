import { useEffect, useRef, useCallback } from 'react';
import type { WebSocketMessage } from '../types';

const WS_BASE = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws';

type MessageHandler = (msg: WebSocketMessage) => void;

/**
 * useWebSocket – connects to the Spring Boot STOMP/raw WebSocket endpoint
 * and calls `onMessage` whenever a message arrives for the given projectId.
 *
 * Connection is automatically cleaned up when the component unmounts.
 */
export function useWebSocket(projectId: string | undefined, onMessage: MessageHandler) {
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectRef = useRef<ReturnType<typeof setTimeout>>();

  const connect = useCallback(() => {
    if (!projectId) return;

    const url = `${WS_BASE}/projects/${projectId}`;
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => {
      console.debug('[WS] connected to', url);
    };

    ws.onmessage = (event) => {
      try {
        const data: WebSocketMessage = JSON.parse(event.data);
        onMessage(data);
      } catch {
        console.warn('[WS] could not parse message', event.data);
      }
    };

    ws.onclose = (e) => {
      if (!e.wasClean) {
        // Reconnect after 3 s
        reconnectRef.current = setTimeout(connect, 3000);
      }
    };

    ws.onerror = (err) => {
      console.error('[WS] error', err);
    };
  }, [projectId, onMessage]);

  useEffect(() => {
    connect();
    return () => {
      clearTimeout(reconnectRef.current);
      wsRef.current?.close(1000, 'component unmounted');
    };
  }, [connect]);

  const send = useCallback((payload: object) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(payload));
    }
  }, []);

  return { send };
}

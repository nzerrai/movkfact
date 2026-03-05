import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const RECONNECT_DELAYS = [1000, 2000, 5000, 10000, 30000];
const WS_URL = process.env.REACT_APP_API_URL
  ? `${process.env.REACT_APP_API_URL}/ws/batch-notifications`
  : 'http://localhost:8080/ws/batch-notifications';

/**
 * Singleton WebSocket service using STOMP over SockJS.
 *
 * Features:
 * - Auto-connect on first subscribe
 * - Exponential backoff reconnection: 1s, 2s, 5s, 10s, 30s
 * - Multi-topic subscriptions (one per jobId)
 * - Connection state callbacks (onConnect, onDisconnect, onReconnecting)
 */
class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map(); // topic → STOMP subscription
    this.pendingSubscriptions = new Map(); // topic → callback (queued before connect)
    this.connected = false;
    this.reconnectAttempt = 0;
    this.onConnect = null;
    this.onDisconnect = null;
    this.onReconnecting = null;
  }

  connect() {
    if (this.client && this.connected) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 0, // we manage reconnect manually
      heartbeatIncoming: 30000,
      heartbeatOutgoing: 30000,

      onConnect: () => {
        this.connected = true;
        this.reconnectAttempt = 0;

        // Re-subscribe pending topics (after reconnect)
        this.pendingSubscriptions.forEach((callback, topic) => {
          this._doSubscribe(topic, callback);
        });
        this.pendingSubscriptions.clear();

        if (this.onConnect) this.onConnect();
      },

      onDisconnect: () => {
        this.connected = false;
        if (this.onDisconnect) this.onDisconnect();
        this._scheduleReconnect();
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame.headers['message']);
      },
    });

    this.client.activate();
  }

  disconnect() {
    this.reconnectAttempt = Infinity; // stop auto-reconnect
    if (this.client) {
      this.client.deactivate();
    }
    this.connected = false;
    this.subscriptions.clear();
    this.pendingSubscriptions.clear();
  }

  subscribeToBatch(jobId, callback) {
    const topic = `/topic/batch/${jobId}`;

    if (this.connected) {
      this._doSubscribe(topic, callback);
    } else {
      // Queue for when connection is established
      this.pendingSubscriptions.set(topic, callback);
      this.connect();
    }
  }

  unsubscribeFromBatch(jobId) {
    const topic = `/topic/batch/${jobId}`;
    const sub = this.subscriptions.get(topic);
    if (sub) {
      sub.unsubscribe();
      this.subscriptions.delete(topic);
    }
    this.pendingSubscriptions.delete(topic);
  }

  isConnected() {
    return this.connected;
  }

  // ─── Private helpers ────────────────────────────────────────────────────────

  _doSubscribe(topic, callback) {
    if (this.subscriptions.has(topic)) return; // already subscribed

    const sub = this.client.subscribe(topic, (message) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (e) {
        console.error('[WS] Failed to parse message:', e);
      }
    });
    this.subscriptions.set(topic, sub);
  }

  _scheduleReconnect() {
    if (this.reconnectAttempt >= RECONNECT_DELAYS.length) {
      console.warn('[WS] Max reconnect attempts reached.');
      return;
    }

    const delay = RECONNECT_DELAYS[this.reconnectAttempt];
    this.reconnectAttempt++;

    if (this.onReconnecting) this.onReconnecting(this.reconnectAttempt, delay);

    setTimeout(() => {
      if (!this.connected) {
        this.connect();
      }
    }, delay);
  }
}

export default new WebSocketService();

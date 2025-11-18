// Gerenciamento de WebSocket
const WebSocketManager = {
    connect() {
        const socket = new SockJS('/ws');
        GameState.stompClient = new StompJs.Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log(str),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        GameState.stompClient.onConnect = (frame) => {
            console.log('Conectado: ' + frame);
            const statusElement = document.getElementById('connectionStatus');
            if (statusElement) {
                statusElement.className = 'status-indicator connected';
            }
            
            // Assina o tópico da partida atual, se já existir
            if (GameState.currentGameId) {
                this.subscribeToGame(GameState.currentGameId);
            }
        };

        GameState.stompClient.onStompError = (frame) => {
            console.log('Erro STOMP: ' + frame.headers['message']);
            console.log('Detalhes: ' + frame.body);
            const statusElement = document.getElementById('connectionStatus');
            if (statusElement) {
                statusElement.className = 'status-indicator disconnected';
            }
        };

        GameState.stompClient.activate();
    },
    
    disconnect() {
        if (GameState.stompClient !== null) {
            GameState.stompClient.deactivate();
        }
        console.log("Desconectado");
        const statusElement = document.getElementById('connectionStatus');
        if (statusElement) {
            statusElement.className = 'status-indicator disconnected';
        }
    },
    
    subscribeToGame(gameId) {
        if (!GameState.stompClient || !GameState.stompClient.connected) return;
        
        // Cancelar assinatura anterior
        if (GameState.gameSubscription) {
            GameState.gameSubscription.unsubscribe();
            GameState.gameSubscription = null;
        }
        
        GameState.gameSubscription = GameState.stompClient.subscribe(`/topic/game/${gameId}`, (message) => {
            const data = JSON.parse(message.body);
            MessageHandler.handle(data);
        });
    },
    
    publish(destination, body) {
        if (GameState.stompClient && GameState.stompClient.connected) {
            GameState.stompClient.publish({
                destination: destination,
                body: JSON.stringify(body)
            });
            return true;
        }
        return false;
    }
};


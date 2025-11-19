// Estado global da aplicação
const GameState = {
    // WebSocket
    stompClient: null,
    gameSubscription: null,
    
    // Jogo
    currentGameId: null,
    currentPlayerId: null,
    currentTurnIndex: 0,
    currentPlayers: [],
    expectedPlayerName: null, // Nome do jogador que esta sessão espera identificar
    isJoining: false, // Flag para prevenir múltiplas chamadas de join
    
    // Perguntas
    selectedAlternative: null,
    currentQuestion: null,
    awaitingQuestion: false,
    questionShown: false,
    
    // Rotas
    gameRoutes: [],
    routeElements: new Map(),
    
    // Navios
    selectedPortForShip: null,
    
    // Vencedor (armazenado quando o jogo finaliza)
    winnerInfo: null,
    
    // Métodos auxiliares
    isMyTurn() {
        if (this.currentPlayers.length === 0 || !this.currentPlayerId) return false;
        const currentPlayerAtTurn = this.currentPlayers[this.currentTurnIndex % this.currentPlayers.length];
        if (!currentPlayerAtTurn) return false;
        // Garantir comparação correta de IDs (pode ser string ou número)
        return String(currentPlayerAtTurn.id) === String(this.currentPlayerId);
    },
    
    getCurrentPlayer() {
        if (!this.currentPlayerId) return null;
        const currentPlayerIdStr = String(this.currentPlayerId);
        return this.currentPlayers.find(p => String(p.id) === currentPlayerIdStr);
    },
    
    getCurrentPlayerAtTurn() {
        if (this.currentPlayers.length === 0) return null;
        return this.currentPlayers[this.currentTurnIndex % this.currentPlayers.length];
    }
};


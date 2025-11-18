// Ações do jogo
const GameActions = {
    createGame() {
        fetch('/api/games', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(response => response.json())
        .then(data => {
            GameState.currentGameId = data.id;
            Notifications.success('Nova partida criada! ID: ' + data.id);
            UI.updateGameStatus('Partida criada - Aguardando jogadores');
            
            const gameIdInput = document.getElementById('gameId');
            if (gameIdInput) {
                gameIdInput.value = data.id;
            }
            
            UI.updateGameControls(true, !!GameState.currentPlayerId);
            
            if (GameState.stompClient && GameState.stompClient.connected) {
                WebSocketManager.subscribeToGame(GameState.currentGameId);
            }
        })
        .catch(error => {
            console.error('Erro ao criar partida:', error);
            Notifications.error('Erro ao criar partida');
        });
    },
    
    joinGame() {
        const playerNameInput = document.getElementById('playerName');
        const gameIdInput = document.getElementById('gameId');
        
        if (!playerNameInput || !gameIdInput) return;
        
        const playerName = playerNameInput.value.trim();
        const gameId = gameIdInput.value;

        if (!playerName || !gameId) {
            Notifications.error('Preencha nome e ID da partida');
            return;
        }

        // Salvar o nome do jogador para identificação posterior
        GameState.expectedPlayerName = playerName;
        
        const success = WebSocketManager.publish('/app/game/join', {
            gameId: parseInt(gameId),
            playerName: playerName
        });
        
        GameState.currentGameId = parseInt(gameId);
        WebSocketManager.subscribeToGame(GameState.currentGameId);
        UI.updateGameControls(true, !!GameState.currentPlayerId);

        if (!success) {
            // Fallback via REST
            this.joinGameRest(gameId, playerName);
        }
    },
    
    joinGameRest(gameId, playerName) {
        // Salvar o nome do jogador para identificação posterior
        GameState.expectedPlayerName = playerName;
        
        fetch(`/api/games/${parseInt(gameId)}/join`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: playerName })
        })
        .then(r => {
            if (!r.ok) throw new Error('Falha ao entrar na partida');
            return r.json();
        })
        .then(player => {
            GameState.currentGameId = parseInt(gameId);
            GameState.currentPlayerId = player.id;
            GameState.expectedPlayerName = null; // Limpar após identificação
            UI.updateGameControls(true, true);
            Notifications.success('Você entrou na partida!');
            Routes.loadGameRoutes();
            Ships.init();
            Ships.updateAllPortsVisual();
            
            if (GameState.stompClient && GameState.stompClient.connected) {
                WebSocketManager.subscribeToGame(GameState.currentGameId);
            }
        })
        .catch(err => {
            console.error(err);
            Notifications.error('Não foi possível entrar na partida');
        });
    },
    
    nextTurn() {
        WebSocketManager.publish('/app/game/next-turn', {
            gameId: GameState.currentGameId
        });
    },
    
    restartGame() {
        WebSocketManager.publish('/app/game/restart', {
            gameId: GameState.currentGameId
        });
    },
    
    finishGame() {
        WebSocketManager.publish('/app/game/finish', {
            gameId: GameState.currentGameId
        });
    },
    
    refreshGameData() {
        if (!GameState.currentGameId) return;
        
        fetch(`/api/games/${GameState.currentGameId}`)
            .then(r => r.ok ? r.json() : null)
            .then(game => {
                if (game) {
                    // Atualizar currentTurnIndex se estiver presente
                    if (game.currentTurnIndex !== undefined) {
                        GameState.currentTurnIndex = game.currentTurnIndex;
                        console.log('Turno atualizado via refreshGameData para índice:', GameState.currentTurnIndex);
                    }
                    
                    if (Array.isArray(game.players)) {
                        GameState.currentPlayers = game.players;
                        UI.updatePlayerList(game.players);
                        
                        // Se ainda não temos currentPlayerId, tentar identificar pelo nome
                        if (!GameState.currentPlayerId) {
                            // Usar expectedPlayerName primeiro (mais confiável)
                            const expectedName = GameState.expectedPlayerName;
                            const playerNameToMatch = expectedName || 
                                (() => {
                                    const playerNameInput = document.getElementById('playerName');
                                    return playerNameInput ? playerNameInput.value.trim() : null;
                                })();
                            
                            if (playerNameToMatch) {
                                const player = game.players.find(p => 
                                    p.name && p.name.trim().toLowerCase() === playerNameToMatch.trim().toLowerCase()
                                );
                                if (player) {
                                    GameState.currentPlayerId = player.id;
                                    console.log('✅ Jogador identificado via refreshGameData:', player.name, 'ID:', player.id);
                                    if (expectedName) {
                                        GameState.expectedPlayerName = null; // Limpar após identificação
                                    }
                                    // Atualizar controles agora que o jogador foi identificado
                                    UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
                                    Ships.init();
                                    Ships.updateAllPortsVisual();
                                }
                            }
                        }
                    }
                    if (game.rotas) {
                        GameState.gameRoutes = game.rotas;
                        Routes.updateVisual();
                    }
                    UI.updateGameStatus(game.status, game.players);
                    // Garantir que os controles estejam atualizados
                    UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
                }
            })
            .catch(() => {});
    }
};

// Funções globais para chamadas do HTML
function createGame() {
    GameActions.createGame();
}

function joinGame() {
    GameActions.joinGame();
}

function nextTurn() {
    GameActions.nextTurn();
}

function restartGame() {
    GameActions.restartGame();
}

function finishGame() {
    GameActions.finishGame();
}


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
            
            // Resetar rotas para garantir que não apareçam rotas de partidas anteriores
            GameState.gameRoutes = [];
            Routes.updateVisual();
            
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
        // Prevenir múltiplas chamadas
        if (GameState.isJoining) {
            Notifications.warning('Aguarde, entrando na partida...');
            return;
        }
        
        // Se já está em um jogo, não permitir entrar novamente
        if (GameState.currentPlayerId) {
            Notifications.warning('Você já está em uma partida!');
            return;
        }
        
        const playerNameInput = document.getElementById('playerName');
        const gameIdInput = document.getElementById('gameId');
        
        if (!playerNameInput || !gameIdInput) return;
        
        const playerName = playerNameInput.value.trim();
        const gameId = gameIdInput.value;

        if (!playerName || !gameId) {
            Notifications.error('Preencha nome e ID da partida');
            return;
        }

        // Marcar que está entrando
        GameState.isJoining = true;
        
        // Salvar o nome do jogador para identificação posterior
        GameState.expectedPlayerName = playerName;
        
        const success = WebSocketManager.publish('/app/game/join', {
            gameId: parseInt(gameId),
            playerName: playerName
        });
        
        GameState.currentGameId = parseInt(gameId);
        WebSocketManager.subscribeToGame(GameState.currentGameId);
        UI.updateGameControls(true, false); // Ainda não temos playerId

        if (!success) {
            // Fallback via REST se WebSocket não está conectado
            this.joinGameRest(gameId, playerName);
        } else {
            // Se WebSocket funcionou, aguardar identificação por até 2 segundos
            // Se não identificar, usar REST como fallback
            setTimeout(() => {
                if (!GameState.currentPlayerId) {
                    console.log('WebSocket não identificou jogador, tentando REST...');
                    this.joinGameRest(gameId, playerName);
                }
            }, 2000);
        }
    },
    
    joinGameRest(gameId, playerName) {
        // Se já temos um playerId, não tentar novamente
        if (GameState.currentPlayerId) {
            console.log('Já temos playerId, não tentando REST novamente');
            return;
        }
        
        // Salvar o nome do jogador para identificação posterior
        GameState.expectedPlayerName = playerName;
        
        fetch(`/api/games/${parseInt(gameId)}/join`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: playerName })
        })
        .then(async r => {
            if (!r.ok) {
                let errorMessage = 'Falha ao entrar na partida';
                try {
                    const errorData = await r.json();
                    errorMessage = errorData.error || errorMessage;
                } catch (e) {
                    const text = await r.text();
                    errorMessage = text || errorMessage;
                }
                throw new Error(errorMessage);
            }
            return r.json();
        })
        .then(player => {
            // Verificar novamente se já temos playerId (pode ter sido identificado via WebSocket)
            if (GameState.currentPlayerId) {
                console.log('Jogador já identificado, ignorando resposta REST');
                return;
            }
            
            GameState.currentGameId = parseInt(gameId);
            GameState.currentPlayerId = player.id;
            GameState.expectedPlayerName = null; // Limpar após identificação
            GameState.isJoining = false; // Resetar flag
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
            console.error('Erro ao entrar via REST:', err);
            GameState.isJoining = false;
            GameState.expectedPlayerName = null;
            const errorMsg = err.message || 'Não foi possível entrar na partida';
            Notifications.error(errorMsg);
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
                        
                        // Se ainda não temos currentPlayerId E estamos esperando entrar, tentar identificar pelo nome esperado
                        if (!GameState.currentPlayerId && GameState.expectedPlayerName) {
                            const expectedName = GameState.expectedPlayerName;
                            const player = game.players.find(p => 
                                p.name && p.name.trim().toLowerCase() === expectedName.trim().toLowerCase()
                            );
                            if (player) {
                                GameState.currentPlayerId = player.id;
                                console.log('✅ Jogador identificado via refreshGameData:', player.name, 'ID:', player.id);
                                GameState.expectedPlayerName = null; // Limpar após identificação
                                GameState.isJoining = false; // Resetar flag
                                // Atualizar controles agora que o jogador foi identificado
                                UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
                                Ships.init();
                                Ships.updateAllPortsVisual();
                            }
                        }
                    }
                    if (game.rotas) {
                        GameState.gameRoutes = game.rotas;
                        Routes.updateVisual();
                    }
                    // Preservar winnerInfo se o jogo estiver finalizado
                    const winnerInfo = (game.status === 'FINALIZADO' && GameState.winnerInfo) ? GameState.winnerInfo : null;
                    UI.updateGameStatus(game.status, game.players, winnerInfo);
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


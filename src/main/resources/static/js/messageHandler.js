// Processamento de mensagens WebSocket
const MessageHandler = {
    handle(data) {
        console.log('Mensagem recebida:', data);

        switch (data.type) {
            case 'PLAYER_JOINED':
                this.handlePlayerJoined(data);
                break;
            
            case 'TURN_CHANGED':
                this.handleTurnChanged(data);
                break;
            
            case 'NEW_QUESTION':
                Questions.showQuestion(data.data.question);
                break;
            
            case 'QUESTION_ANSWERED':
                Questions.handleAnswer(data.data);
                break;
            
            case 'GAME_FINISHED':
                UI.updateGameStatus('Partida finalizada');
                Notifications.info('Partida finalizada!');
                UI.updateGameControls(false, false);
                break;
            
            case 'GAME_UPDATE':
                this.handleGameUpdate(data);
                break;
            
            case 'ROUTE_PURCHASED':
                Notifications.success('Rota comprada!');
                Routes.loadGameRoutes();
                GameActions.refreshGameData();
                break;
            
            case 'ERROR':
                Notifications.error(data.message);
                break;
        }
    },
    
    handlePlayerJoined(data) {
        if (!GameState.currentGameId && data.gameId) {
            GameState.currentGameId = data.gameId;
            if (GameState.stompClient && GameState.stompClient.connected) {
                WebSocketManager.subscribeToGame(GameState.currentGameId);
            }
        }
        
        // data.data agora contém o Game completo (mudamos o backend)
        const game = data.data;
        const players = game?.players || [];
        
        // Atualizar currentTurnIndex PRIMEIRO, antes de atualizar a UI
        if (game && game.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = game.currentTurnIndex;
            console.log('Turno atualizado via PLAYER_JOINED para índice:', GameState.currentTurnIndex);
        }
        
        // Atualizar lista de jogadores imediatamente se o Game foi enviado
        if (game && Array.isArray(players)) {
            GameState.currentPlayers = players;
            UI.updatePlayerList(players);
            // Atualizar status DEPOIS de ter atualizado currentTurnIndex
            UI.updateGameStatus(game.status, players);
        }
        
        // Identificar se este é o jogador desta sessão
        // O playerId está em data.playerId
        const playerId = data.playerId;
        const joinedPlayer = players.find(p => p.id === playerId);
        const playerName = joinedPlayer?.name;
        
        // Tentar identificar o jogador desta sessão
        let playerIdentified = false;
        
        // Se temos um nome esperado e ele corresponde ao player que entrou
        if (GameState.expectedPlayerName && playerName && 
            playerName.trim().toLowerCase() === GameState.expectedPlayerName.trim().toLowerCase()) {
            GameState.currentPlayerId = playerId;
            console.log('✅ Jogador identificado pelo nome esperado:', playerName, 'ID:', playerId);
            playerIdentified = true;
            GameState.expectedPlayerName = null;
        } 
        // Se ainda não temos currentPlayerId, tentar identificar pelo campo de input
        else if (!GameState.currentPlayerId) {
            const playerNameInput = document.getElementById('playerName');
            if (playerNameInput) {
                const inputName = playerNameInput.value.trim().toLowerCase();
                // Verificar se algum jogador na lista corresponde ao nome do input
                const matchingPlayer = players.find(p => 
                    p.name && p.name.trim().toLowerCase() === inputName
                );
                if (matchingPlayer) {
                    GameState.currentPlayerId = matchingPlayer.id;
                    console.log('✅ Jogador identificado pelo input:', matchingPlayer.name, 'ID:', matchingPlayer.id);
                    playerIdentified = true;
                }
                // Ou verificar se o player que entrou corresponde ao input
                else if (playerName && playerName.trim().toLowerCase() === inputName) {
                    GameState.currentPlayerId = playerId;
                    console.log('✅ Jogador identificado pelo input (joined):', playerName, 'ID:', playerId);
                    playerIdentified = true;
                }
            }
        }
        
        // Se identificamos o jogador, inicializar navios e atualizar UI
        if (playerIdentified) {
            Ships.init();
            Ships.updateAllPortsVisual();
            // Atualizar controles do jogo agora que o jogador foi identificado
            UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
            // Atualizar status novamente após identificar o jogador
            if (game && Array.isArray(players)) {
                UI.updateGameStatus(game.status, players);
            }
        }
        
        // Mostrar notificação
        if (playerName) {
            Notifications.info(`${playerName} entrou na partida!`);
        }
        
        // Garantir que os dados estejam atualizados
        if (GameState.currentGameId) {
            GameActions.refreshGameData();
        } else {
            // Se ainda não temos gameId mas temos players, atualizar controles de qualquer forma
            if (players.length > 0) {
                UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
            }
        }
    },
    
    handleTurnChanged(data) {
        if (data.data.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = data.data.currentTurnIndex;
            console.log('Turno atualizado para índice:', GameState.currentTurnIndex);
        }
        
        GameState.currentPlayers = data.data.players || [];
        UI.updateGameStatus('Turno atualizado', data.data.players);
        UI.updatePlayerList(data.data.players);
        Notifications.info('Novo turno iniciado! Todos receberam $10');
    },
    
    handleGameUpdate(data) {
        // Atualizar currentTurnIndex se estiver presente nos dados
        if (data.data && data.data.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = data.data.currentTurnIndex;
            console.log('Turno atualizado via GAME_UPDATE para índice:', GameState.currentTurnIndex);
        }
        
        GameState.currentPlayers = data.data.players || [];
        UI.updatePlayerList(data.data.players);
        UI.updateGameStatus(data.data.status, data.data.players);
        UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
        
        if (data.data.rotas) {
            GameState.gameRoutes = data.data.rotas;
            Routes.updateVisual();
        } else {
            Routes.loadGameRoutes();
        }
    }
};


// Atualizações de UI
const UI = {
    updatePlayerList(players) {
        const playerList = document.getElementById('playerList');
        if (!playerList) return;
        
        playerList.innerHTML = '';

        if (!players || players.length === 0) {
            playerList.innerHTML = '<p style="text-align: center; color: #718096; font-style: italic;">Nenhum jogador conectado</p>';
            return;
        }

        players.forEach(player => {
            const playerCard = document.createElement('div');
            playerCard.className = `player-card ${player.cor?.nome?.toLowerCase() || 'blue'}`;
            
            playerCard.innerHTML = `
                <div class="player-info">
                    <div class="player-name">${player.name}</div>
                    <div class="player-resources">
                        <i class="fas fa-coins"></i> $${player.dinheiro?.toFixed(0) || 0} | 
                        <i class="fas fa-ship"></i> ${player.naviosDisponiveis || 0} navios
                    </div>
                </div>
            `;
            
            playerList.appendChild(playerCard);
        });
    },
    
    updateGameStatus(status, players) {
        const statusDiv = document.getElementById('gameStatus');
        if (!statusDiv) return;
        
        let statusText = status || 'Nenhuma partida ativa';
        let playerInfo = '';
        
        if (players && players.length > 0) {
            const currentPlayerAtTurn = GameState.getCurrentPlayerAtTurn();
            if (currentPlayerAtTurn) {
                statusText += ` - Turno de: ${currentPlayerAtTurn.name}`;
            }
            
            // Mostrar recursos do jogador atual da sessão
            if (GameState.currentPlayerId) {
                const currentPlayer = GameState.getCurrentPlayer();
                if (currentPlayer) {
                    // Garantir comparação correta de IDs (pode ser string ou número)
                    const currentPlayerId = String(GameState.currentPlayerId);
                    const currentTurnPlayerId = currentPlayerAtTurn ? String(currentPlayerAtTurn.id) : null;
                    const isCurrentTurn = currentPlayerId === currentTurnPlayerId;
                    
                    console.log('Verificação de turno:', {
                        currentPlayerId: currentPlayerId,
                        currentTurnPlayerId: currentTurnPlayerId,
                        currentPlayerName: currentPlayer.name,
                        currentTurnPlayerName: currentPlayerAtTurn?.name,
                        currentTurnIndex: GameState.currentTurnIndex,
                        isCurrentTurn: isCurrentTurn
                    });
                    
                    playerInfo = `
                        <div style="background: #f7fafc; padding: 15px; border-radius: 8px; margin-top: 15px;">
                            <div style="font-size: 1.1rem; font-weight: 600; color: #2d3748; margin-bottom: 10px;">
                                <i class="fas fa-user"></i> ${currentPlayer.name}
                                ${isCurrentTurn ? 
                                    '<span style="background: #48bb78; color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.7rem; margin-left: 8px;"><i class="fas fa-play"></i> SEU TURNO</span>' : 
                                    '<span style="background: #e2e8f0; color: #4a5568; padding: 2px 8px; border-radius: 12px; font-size: 0.7rem; margin-left: 8px;"><i class="fas fa-clock"></i> Aguardando</span>'
                                }
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px;">
                                <div style="text-align: center;">
                                    <div style="font-size: 0.8rem; color: #718096;">Dinheiro</div>
                                    <div style="font-weight: 600; color: #38a169;">$${currentPlayer.dinheiro?.toFixed(0) || 0}</div>
                                </div>
                                <div style="text-align: center;">
                                    <div style="font-size: 0.8rem; color: #718096;">Navios</div>
                                    <div style="font-weight: 600; color: #3182ce;">${currentPlayer.naviosDisponiveis || 0}</div>
                                </div>
                                <div style="text-align: center;">
                                    <div style="font-size: 0.8rem; color: #718096;">Rotas</div>
                                    <div style="font-weight: 600; color: #805ad5;">${currentPlayer.rotasPossuidas?.length || 0}</div>
                                </div>
                            </div>
                        </div>
                    `;
                }
            }
        }
        
        statusDiv.innerHTML = `
            <div style="text-align: center;">
                <div style="font-weight: 600; color: #2d3748; margin-bottom: 5px;">
                    Status: ${statusText}
                </div>
                <div style="font-size: 0.9rem; color: #718096; margin-bottom: 10px;">
                    Partida ID: ${GameState.currentGameId || 'N/A'}
                </div>
                ${playerInfo}
            </div>
        `;
    },
    
    updateGameControls(hasGame, isPlayer) {
        const questionBtn = document.getElementById('questionBtn');
        const nextTurnBtn = document.getElementById('nextTurnBtn');
        const restartBtn = document.getElementById('restartBtn');
        const finishBtn = document.getElementById('finishBtn');
        
        if (questionBtn) questionBtn.disabled = !hasGame || !isPlayer;
        if (nextTurnBtn) nextTurnBtn.disabled = !hasGame || !isPlayer;
        if (restartBtn) restartBtn.disabled = !hasGame || !isPlayer;
        if (finishBtn) finishBtn.disabled = !hasGame || !isPlayer;
    }
};


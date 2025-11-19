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
    
    updateGameStatus(status, players, winnerInfo = null) {
        const statusDiv = document.getElementById('gameStatus');
        if (!statusDiv) return;
        
        let statusText = status || 'Nenhuma partida ativa';
        let playerInfo = '';
        let winnerDisplay = '';
        
        // Usar winnerInfo do estado se não foi passado e o jogo está finalizado
        if (!winnerInfo && status === 'FINALIZADO' && GameState.winnerInfo) {
            winnerInfo = GameState.winnerInfo;
        }
        
        // Mostrar vencedor se o jogo estiver finalizado
        if (status === 'FINALIZADO' && winnerInfo) {
            if (winnerInfo.empate) {
                // Exibir empate
                const vencedoresList = winnerInfo.vencedores?.map(v => 
                    `<div style="margin: 5px 0;">${v.nome} - ${v.pontuacao} pontos, $${v.dinheiro?.toFixed(0) || 0}</div>`
                ).join('') || winnerInfo.nome;
                
                winnerDisplay = `
                    <div style="background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); color: white; padding: 15px; border-radius: 8px; margin-bottom: 15px; text-align: center;">
                        <div style="font-size: 1.3rem; font-weight: 700; margin-bottom: 5px;">
                            <i class="fas fa-handshake"></i> Empate!
                        </div>
                        <div style="font-size: 1rem; font-weight: 600; margin-top: 10px;">
                            ${vencedoresList}
                        </div>
                        <div style="font-size: 0.9rem; opacity: 0.9; margin-top: 5px;">
                            ${winnerInfo.pontuacao} pontos cada
                        </div>
                    </div>
                `;
            } else {
                // Exibir vencedor único
                const dinheiroText = winnerInfo.dinheiro ? `, $${winnerInfo.dinheiro.toFixed(0)}` : '';
                winnerDisplay = `
                    <div style="background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); color: white; padding: 15px; border-radius: 8px; margin-bottom: 15px; text-align: center;">
                        <div style="font-size: 1.3rem; font-weight: 700; margin-bottom: 5px;">
                            <i class="fas fa-trophy"></i> Vencedor
                        </div>
                        <div style="font-size: 1.1rem; font-weight: 600;">
                            ${winnerInfo.nome}
                        </div>
                        <div style="font-size: 0.9rem; opacity: 0.9; margin-top: 5px;">
                            ${winnerInfo.pontuacao} pontos${dinheiroText}
                        </div>
                    </div>
                `;
            }
        }
        
        if (players && players.length > 0) {
            const currentPlayerAtTurn = GameState.getCurrentPlayerAtTurn();
            if (currentPlayerAtTurn && status !== 'FINALIZADO') {
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
                                    <div style="font-weight: 600; color: #805ad5;">${this.countPlayerRoutes(currentPlayer.id)}</div>
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
                ${winnerDisplay}
                ${playerInfo}
            </div>
        `;
    },
    
    updateGameControls(hasGame, isPlayer) {
        const questionBtn = document.getElementById('questionBtn');
        const buyShipsBtn = document.getElementById('buyShipsBtn');
        const nextTurnBtn = document.getElementById('nextTurnBtn');
        const restartBtn = document.getElementById('restartBtn');
        const finishBtn = document.getElementById('finishBtn');
        
        if (questionBtn) questionBtn.disabled = !hasGame || !isPlayer;
        if (buyShipsBtn) buyShipsBtn.disabled = !hasGame || !isPlayer;
        if (nextTurnBtn) nextTurnBtn.disabled = !hasGame || !isPlayer;
        if (restartBtn) restartBtn.disabled = !hasGame || !isPlayer;
        if (finishBtn) finishBtn.disabled = !hasGame || !isPlayer;
    },
    
    countPlayerRoutes(playerId) {
        // Contar rotas do jogador a partir das rotas do jogo
        // (rotasPossuidas não vem no JSON por causa do @JsonIgnore)
        if (!GameState.gameRoutes || !playerId) return 0;
        const playerIdStr = String(playerId);
        return GameState.gameRoutes.filter(route => {
            const routeOwnerId = route.dono?.id;
            return routeOwnerId && String(routeOwnerId) === playerIdStr;
        }).length;
    }
};


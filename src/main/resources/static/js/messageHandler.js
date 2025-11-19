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
                this.handleGameFinished(data);
                break;
            
            case 'GAME_UPDATE':
                this.handleGameUpdate(data);
                break;
            
            case 'ROUTE_PURCHASED':
                Notifications.success('Rota comprada!');
                Routes.loadGameRoutes().then(() => {
                    GameActions.refreshGameData();
                });
                break;
            
            case 'ERROR':
                Notifications.error(data.message);
                // Resetar flag de join em caso de erro
                if (GameState.isJoining) {
                    GameState.isJoining = false;
                    GameState.expectedPlayerName = null;
                }
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
        
        // IMPORTANTE: Só identificar se temos um nome esperado E ele corresponde EXATAMENTE ao player que entrou
        // Isso previne que outros jogadores sejam identificados incorretamente
        if (GameState.expectedPlayerName && playerName && 
            playerName.trim().toLowerCase() === GameState.expectedPlayerName.trim().toLowerCase() &&
            !GameState.currentPlayerId) { // Só identificar se ainda não temos um playerId
            GameState.currentPlayerId = playerId;
            console.log('✅ Jogador identificado pelo nome esperado:', playerName, 'ID:', playerId);
            playerIdentified = true;
            GameState.expectedPlayerName = null;
            GameState.isJoining = false; // Resetar flag de join
        }
        
        // Se identificamos o jogador, inicializar navios e atualizar UI
        if (playerIdentified) {
            Ships.init();
            Ships.updateAllPortsVisual();
            // Carregar rotas quando o jogador é identificado
            Routes.loadGameRoutes();
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
        
        // Carregar rotas se o jogo tem rotas
        if (game && game.rotas && Array.isArray(game.rotas)) {
            GameState.gameRoutes = game.rotas;
            Routes.updateVisual();
        } else if (GameState.currentGameId) {
            // Se não vieram rotas, carregar do servidor
            Routes.loadGameRoutes();
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
        const game = data.data;
        
        if (game && game.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = game.currentTurnIndex;
            console.log('Turno atualizado para índice:', GameState.currentTurnIndex);
        }
        
        const players = game?.players || [];
        GameState.currentPlayers = players;
        
        // Usar o status real do jogo, não uma string fixa
        const gameStatus = game?.status || 'EM_ANDAMENTO';
        UI.updateGameStatus(gameStatus, players);
        UI.updatePlayerList(players);
        
        // Recarregar rotas para garantir que estão atualizadas
        Routes.loadGameRoutes();
        
        Notifications.info('Novo turno iniciado! Todos receberam $10');
    },
    
    handleGameUpdate(data) {
        // Atualizar currentTurnIndex se estiver presente nos dados
        if (data.data && data.data.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = data.data.currentTurnIndex;
            console.log('Turno atualizado via GAME_UPDATE para índice:', GameState.currentTurnIndex);
        }
        
        const game = data.data;
        const players = game?.players || [];
        GameState.currentPlayers = players;
        UI.updatePlayerList(players);
        
        // Obter o status do jogo corretamente
        let gameStatus = null;
        if (game && typeof game === 'object') {
            gameStatus = game.status;
        } else if (data.data && typeof data.data === 'object') {
            gameStatus = data.data.status;
        }
        
        // Se não encontrou status, usar o status atual do estado ou padrão
        if (!gameStatus) {
            // Se o jogo está finalizado, preservar esse status
            if (GameState.winnerInfo) {
                gameStatus = 'FINALIZADO';
            } else {
                gameStatus = 'EM_ANDAMENTO'; // Status padrão
            }
        }
        
        // Preservar winnerInfo se o jogo estiver finalizado
        const winnerInfo = (gameStatus === 'FINALIZADO' && GameState.winnerInfo) ? GameState.winnerInfo : null;
        UI.updateGameStatus(gameStatus, players, winnerInfo);
        
        // Se ainda não identificamos o jogador e temos um nome esperado, tentar identificar agora
        if (!GameState.currentPlayerId && GameState.expectedPlayerName && Array.isArray(players)) {
            const expectedName = GameState.expectedPlayerName;
            const player = players.find(p => 
                p.name && p.name.trim().toLowerCase() === expectedName.trim().toLowerCase()
            );
            if (player) {
                GameState.currentPlayerId = player.id;
                console.log('✅ Jogador identificado via GAME_UPDATE:', player.name, 'ID:', player.id);
                GameState.expectedPlayerName = null;
                GameState.isJoining = false;
                Ships.init();
                Ships.updateAllPortsVisual();
                Notifications.success('Você entrou na partida!');
                // Carregar rotas quando o jogador é identificado
                Routes.loadGameRoutes();
            }
        }
        
        UI.updateGameControls(!!GameState.currentGameId, !!GameState.currentPlayerId);
        
        // Se o status é CRIADO, pode ser um restart - resetar rotas e vencedor
        if (game?.status === 'CRIADO' || data.data.status === 'CRIADO') {
            // Limpar rotas compradas visualmente
            GameState.gameRoutes = GameState.gameRoutes.map(route => {
                if (route.dono) {
                    return { ...route, dono: null };
                }
                return route;
            });
            // Limpar vencedor ao reiniciar
            GameState.winnerInfo = null;
            Routes.updateVisual();
        }
        
        // Sempre recarregar rotas para garantir sincronização
        if (data.data.rotas && Array.isArray(data.data.rotas) && data.data.rotas.length > 0) {
            GameState.gameRoutes = data.data.rotas;
            Routes.updateVisual();
        } else {
            // Se não vierem rotas no update, recarregar do servidor
            Routes.loadGameRoutes();
        }
    },
    
    handleGameFinished(data) {
        const resultado = data.data;
        let mensagem = 'Partida finalizada!';
        
        // Atualizar currentTurnIndex se estiver presente
        if (resultado?.game?.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = resultado.game.currentTurnIndex;
        } else if (resultado?.currentTurnIndex !== undefined) {
            GameState.currentTurnIndex = resultado.currentTurnIndex;
        }
        
        // Se temos informações do ganhador, exibir
        if (resultado) {
            // Verificar se resultado é um objeto com ganhador ou se é o Game diretamente
            if (resultado.ganhadorNome) {
                const ganhadorNome = resultado.ganhadorNome;
                const pontuacao = resultado.pontuacaoGanhador || 0;
                mensagem = `🏆 ${ganhadorNome} venceu com ${pontuacao} pontos!`;
                
                // Exibir pontuações de todos os jogadores
                if (resultado.pontuacoes) {
                    const pontuacoesTexto = Object.entries(resultado.pontuacoes)
                        .map(([playerId, pontos]) => {
                            const player = GameState.currentPlayers.find(p => String(p.id) === String(playerId));
                            const nome = player ? player.name : `Jogador ${playerId}`;
                            return `${nome}: ${pontos} pontos`;
                        })
                        .join('\n');
                    console.log('Pontuações finais:\n' + pontuacoesTexto);
                }
                
                // Atualizar dados do jogo se disponível
                if (resultado.game) {
                    const game = resultado.game;
                    if (game.players) {
                        GameState.currentPlayers = game.players;
                        UI.updatePlayerList(game.players);
                    }
                }
            } else if (resultado.players) {
                // Se resultado é o Game diretamente, calcular ganhador no frontend
                const game = resultado;
                const pontuacoes = {};
                game.rotas?.forEach(rota => {
                    if (rota.dono && rota.dono.id) {
                        const playerId = rota.dono.id;
                        pontuacoes[playerId] = (pontuacoes[playerId] || 0) + (rota.pontos || 0);
                    }
                });
                
                // Encontrar ganhador
                let ganhadorId = null;
                let maiorPontuacao = -1;
                Object.entries(pontuacoes).forEach(([playerId, pontos]) => {
                    if (pontos > maiorPontuacao) {
                        maiorPontuacao = pontos;
                        ganhadorId = playerId;
                    }
                });
                
                if (ganhadorId) {
                    const ganhador = game.players.find(p => String(p.id) === String(ganhadorId));
                    if (ganhador) {
                        mensagem = `🏆 ${ganhador.name} venceu com ${maiorPontuacao} pontos!`;
                    }
                }
                
                GameState.currentPlayers = game.players;
                UI.updatePlayerList(game.players);
            }
        }
        
        // Preparar informações do vencedor para exibir no status
        let winnerInfo = null;
        if (resultado && resultado.empate) {
            // Caso de empate
            if (resultado.vencedores && Array.isArray(resultado.vencedores)) {
                const vencedoresNomes = resultado.vencedores.map(v => v.nome).join(', ');
                mensagem = resultado.mensagemEmpate || `Empate! ${vencedoresNomes} empataram!`;
                winnerInfo = {
                    nome: vencedoresNomes,
                    pontuacao: resultado.vencedores[0]?.pontuacao || 0,
                    empate: true,
                    vencedores: resultado.vencedores
                };
            }
        } else if (resultado && resultado.ganhadorNome) {
            winnerInfo = {
                nome: resultado.ganhadorNome,
                pontuacao: resultado.pontuacaoGanhador || 0,
                dinheiro: resultado.dinheiroGanhador || 0
            };
        } else if (resultado && resultado.players) {
            // Calcular vencedor se não veio do backend
            const game = resultado;
            const pontuacoes = {};
            game.rotas?.forEach(rota => {
                if (rota.dono && rota.dono.id) {
                    const playerId = rota.dono.id;
                    pontuacoes[playerId] = (pontuacoes[playerId] || 0) + (rota.pontos || 0);
                }
            });
            let ganhadorId = null;
            let maiorPontuacao = -1;
            Object.entries(pontuacoes).forEach(([playerId, pontos]) => {
                if (pontos > maiorPontuacao) {
                    maiorPontuacao = pontos;
                    ganhadorId = playerId;
                }
            });
            if (ganhadorId) {
                const ganhador = game.players.find(p => String(p.id) === String(ganhadorId));
                if (ganhador) {
                    winnerInfo = {
                        nome: ganhador.name,
                        pontuacao: maiorPontuacao
                    };
                }
            }
        }
        
        // Armazenar informações do vencedor no estado para persistir
        if (winnerInfo) {
            GameState.winnerInfo = winnerInfo;
        }
        
        // Atualizar status para FINALIZADO
        UI.updateGameStatus('FINALIZADO', GameState.currentPlayers, winnerInfo);
        Notifications.success(mensagem);
        UI.updateGameControls(false, false);
        
        // Atualizar dados do jogo (mas preservar o vencedor e o status FINALIZADO)
        if (GameState.currentGameId) {
            // Usar setTimeout para garantir que o vencedor seja exibido primeiro
            setTimeout(() => {
                // Fazer refresh mas preservar o status FINALIZADO
                fetch(`/api/games/${GameState.currentGameId}`)
                    .then(r => r.ok ? r.json() : null)
                    .then(game => {
                        if (game) {
                            // Atualizar currentTurnIndex se estiver presente
                            if (game.currentTurnIndex !== undefined) {
                                GameState.currentTurnIndex = game.currentTurnIndex;
                            }
                            
                            if (Array.isArray(game.players)) {
                                GameState.currentPlayers = game.players;
                                UI.updatePlayerList(game.players);
                            }
                            
                            // Garantir que o status seja FINALIZADO
                            const finalStatus = game.status === 'FINALIZADO' ? 'FINALIZADO' : 'FINALIZADO';
                            UI.updateGameStatus(finalStatus, game.players, winnerInfo);
                        }
                    })
                    .catch(() => {});
            }, 500);
        }
    }
};


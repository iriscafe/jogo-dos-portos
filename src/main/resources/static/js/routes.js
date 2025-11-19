// Gerenciamento de rotas
const Routes = {
    selectRoute(route, fromPort, toPort) {
        if (!GameState.currentPlayerId) {
            Notifications.error('Entre em uma partida primeiro');
            return;
        }

        // Verificar se é o turno do jogador
        if (!GameState.isMyTurn()) {
            Notifications.error('Não é seu turno! Aguarde sua vez.');
            return;
        }

        // Verificar se a rota já está comprada
        const backendRoute = this.findBackendRoute(route.from, route.to);
        if (backendRoute && backendRoute.dono) {
            const ownerName = backendRoute.dono?.name || 'Outro jogador';
            Notifications.error(`Esta rota já pertence a ${ownerName}`);
            return;
        }

        // Verificar dinheiro e navios
        const currentPlayer = GameState.getCurrentPlayer();
        if (!currentPlayer) {
            Notifications.error('Erro ao buscar informações do jogador');
            return;
        }

        if (currentPlayer.dinheiro < route.cost) {
            Notifications.error(`Dinheiro insuficiente! Você tem $${currentPlayer.dinheiro.toFixed(0)}, mas precisa de $${route.cost}`);
            return;
        }

        // Verificar navios (número de navios = pontos da rota, como no Ticket to Ride)
        const naviosNecessarios = route.points;
        if (currentPlayer.naviosDisponiveis < naviosNecessarios) {
            Notifications.error(`Navios insuficientes! Você precisa de ${naviosNecessarios} navios, mas tem apenas ${currentPlayer.naviosDisponiveis}`);
            return;
        }

        // Confirmar compra (mostrar custo em dinheiro e navios)
        if (confirm(`Comprar rota ${fromPort.name} → ${toPort.name}?\nCusto: $${route.cost} e ${naviosNecessarios} navios`)) {
            this.buyRoute(route, fromPort, toPort);
        }
    },
    
    buyRoute(route, fromPort, toPort) {
        // Se não há rotas carregadas, tentar carregar primeiro
        if (!GameState.gameRoutes || GameState.gameRoutes.length === 0) {
            console.log('Rotas não carregadas, carregando...');
            this.loadGameRoutes().then(() => {
                // Tentar novamente após carregar
                this.buyRoute(route, fromPort, toPort);
            }).catch(() => {
                Notifications.error('Erro ao carregar rotas. Tente novamente.');
            });
            return;
        }
        
        const backendRoute = this.findBackendRoute(route.from, route.to);
        if (!backendRoute) {
            console.error('Rota não encontrada no backend. Frontend route:', route, 'From:', route.from, 'To:', route.to);
            console.log('Rotas disponíveis:', GameState.gameRoutes);
            console.log('Tentando recarregar rotas...');
            // Tentar recarregar rotas e tentar novamente
            this.loadGameRoutes().then(() => {
                const retryRoute = this.findBackendRoute(route.from, route.to);
                if (retryRoute) {
                    // Tentar comprar novamente
                    this.buyRoute(route, fromPort, toPort);
                } else {
                    Notifications.error('Rota não encontrada no jogo. Tente recarregar a página.');
                }
            });
            return;
        }

        if (!backendRoute.id) {
            console.error('Rota encontrada mas sem ID:', backendRoute);
            Notifications.error('Rota inválida: sem ID');
            return;
        }

        console.log('Comprando rota:', backendRoute);
        const routeData = {
            id: backendRoute.id,
            portoOrigem: backendRoute.portoOrigem || { id: route.from },
            portoDestino: backendRoute.portoDestino || { id: route.to },
            custo: route.cost,
            pontos: route.points
        };

        fetch(`/api/routes/buy?playerId=${GameState.currentPlayerId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(routeData)
        })
        .then(async response => {
            if (!response.ok) {
                let errorMessage = 'Erro ao comprar rota';
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.error || errorMessage;
                } catch (e) {
                    const text = await response.text();
                    errorMessage = text || errorMessage;
                }
                throw new Error(errorMessage);
            }
            return response.json();
        })
        .then(boughtRoute => {
            const naviosUsados = route.points;
            Notifications.success(`Rota comprada com sucesso! -$${route.cost} e ${naviosUsados} navios`);
            // Atualizar rotas e depois atualizar UI
            this.loadGameRoutes().then(() => {
                GameActions.refreshGameData();
            });
        })
        .catch(error => {
            console.error('Erro ao comprar rota:', error);
            const errorMsg = error.message.includes('Saldo insuficiente') 
                ? 'Saldo insuficiente para comprar esta rota'
                : error.message.includes('Navios insuficientes')
                ? error.message // Mostrar mensagem completa sobre navios
                : error.message.includes('já foi comprada') 
                ? 'Esta rota já foi comprada por outro jogador'
                : error.message.includes('não encontrada')
                ? 'Rota não encontrada no banco de dados'
                : error.message || 'Erro ao comprar rota. Tente novamente.';
            Notifications.error(errorMsg);
        });
    },
    
    findBackendRoute(fromId, toId) {
        // Buscar portos do frontend para obter os nomes
        const fromPort = Board.getPortById(fromId);
        const toPort = Board.getPortById(toId);
        
        if (!fromPort || !toPort) {
            console.warn('Portos não encontrados no frontend:', fromId, toId);
            return null;
        }
        
        // Se não há rotas carregadas, tentar carregar primeiro
        if (!GameState.gameRoutes || GameState.gameRoutes.length === 0) {
            console.warn('Rotas não carregadas, tentando carregar...');
            // Carregar rotas de forma síncrona não é possível, então vamos tentar buscar diretamente
            // Mas primeiro vamos logar o que temos
            console.log('GameState.gameRoutes está vazio. Tentando recarregar...');
            this.loadGameRoutes();
            // Retornar null e deixar o erro ser tratado acima
            return null;
        }
        
        const foundRoute = GameState.gameRoutes.find(r => {
            // Tentar por ID primeiro
            const fromIdBackend = r.portoOrigem?.id || r.portoOrigemId;
            const toIdBackend = r.portoDestino?.id || r.portoDestinoId;
            const matchById = (fromIdBackend === fromId && toIdBackend === toId) || 
                             (fromIdBackend === toId && toIdBackend === fromId);
            
            if (matchById) return true;
            
            // Se não encontrou por ID, tentar por nome da cidade
            const fromCityBackend = r.portoOrigem?.cidade;
            const toCityBackend = r.portoDestino?.cidade;
            const matchByName = (fromCityBackend === fromPort.name && toCityBackend === toPort.name) ||
                               (fromCityBackend === toPort.name && toCityBackend === fromPort.name);
            
            return matchByName;
        });
        
        if (!foundRoute) {
            console.error('Rota não encontrada. From:', fromId, 'To:', toId);
            console.log('Portos frontend:', fromPort.name, toPort.name);
            console.log('Rotas disponíveis:', GameState.gameRoutes.map(r => ({
                id: r.id,
                from: r.portoOrigem?.id || r.portoOrigemId,
                to: r.portoDestino?.id || r.portoDestinoId,
                fromCity: r.portoOrigem?.cidade,
                toCity: r.portoDestino?.cidade
            })));
        }
        
        return foundRoute;
    },
    
    loadGameRoutes() {
        if (!GameState.currentGameId) return Promise.resolve();
        
        return fetch(`/api/games/${GameState.currentGameId}`)
            .then(r => r.ok ? r.json() : null)
            .then(game => {
                if (game && game.rotas) {
                    GameState.gameRoutes = game.rotas;
                    this.updateVisual();
                    // Atualizar UI para refletir nova contagem de rotas
                    if (GameState.currentPlayers && GameState.currentPlayers.length > 0) {
                        UI.updateGameStatus(game.status, GameState.currentPlayers);
                    }
                }
                return game;
            })
            .catch(err => {
                console.error('Erro ao carregar rotas:', err);
                return null;
            });
    },
    
    updateVisual() {
        GameState.routeElements.forEach((routeData, routeKey) => {
            const routeElement = routeData.element;
            const route = routeData.route;
            const [fromId, toId] = routeKey.split('-').map(Number);
            
            const backendRoute = this.findBackendRoute(fromId, toId);

            if (backendRoute && backendRoute.dono) {
                // Rota comprada - mostrar com cor do dono e navios
                const ownerColor = backendRoute.dono?.cor?.nome?.toLowerCase() || 'blue';
                routeElement.className = `route ${ownerColor} owned`;
                routeElement.style.opacity = '0.8';
                routeElement.style.cursor = 'default';
                
                // Mostrar navios na rota (como trens no Ticket to Ride)
                const naviosNaRota = route.points; // Número de navios = pontos
                this.showShipsOnRoute(routeElement, naviosNaRota, ownerColor);
                
                const ownerName = backendRoute.dono?.name || 'Jogador';
                const fromPort = Board.getPortById(fromId);
                const toPort = Board.getPortById(toId);
                const fromPortName = backendRoute.portoOrigem?.cidade || fromPort?.name || fromId;
                const toPortName = backendRoute.portoDestino?.cidade || toPort?.name || toId;
                routeElement.title = `Rota: ${fromPortName} → ${toPortName}\nPertence a: ${ownerName} | Navios: ${naviosNaRota} | Pontos: ${route.points}`;
            } else {
                // Remover navios se a rota não está comprada
                this.removeShipsFromRoute(routeElement);
                // Rota disponível - restaurar cor original
                const originalColor = routeElement.dataset.originalColor || route.color;
                routeElement.className = `route ${originalColor}`;
                routeElement.classList.remove('owned');
                routeElement.style.opacity = '1';
                routeElement.style.cursor = 'pointer';
                
                // Restaurar título original
                const fromPort = Board.getPortById(fromId);
                const toPort = Board.getPortById(toId);
                if (fromPort && toPort) {
                    routeElement.title = `Rota: ${fromPort.name} → ${toPort.name}\nCusto: $${route.cost} | Navios necessários: ${route.points} | Pontos: ${route.points}`;
                }
            }
        });
    },
    
    showShipsOnRoute(routeElement, shipCount, ownerColor) {
        // Remover navios anteriores
        this.removeShipsFromRoute(routeElement);
        
        // Adicionar indicadores de navios ao longo da rota
        for (let i = 0; i < shipCount; i++) {
            const shipIndicator = document.createElement('div');
            shipIndicator.className = 'ship-on-route';
            shipIndicator.dataset.shipIndex = i;
            
            // Posicionar navios ao longo da rota (distribuídos)
            const position = (i + 1) / (shipCount + 1); // 0.25, 0.5, 0.75, etc.
            shipIndicator.style.left = `${position * 100}%`;
            shipIndicator.style.top = '50%';
            shipIndicator.style.transform = 'translate(-50%, -50%)';
            
            // Cor do navio baseada na cor do dono
            const colorMap = {
                'azul': '#3182ce',
                'blue': '#3182ce',
                'vermelho': '#e53e3e',
                'red': '#e53e3e',
                'verde': '#38a169',
                'green': '#38a169',
                'amarelo': '#d69e2e',
                'yellow': '#d69e2e',
                'roxo': '#805ad5',
                'purple': '#805ad5'
            };
            shipIndicator.style.backgroundColor = colorMap[ownerColor] || colorMap.blue;
            shipIndicator.style.borderColor = colorMap[ownerColor] || colorMap.blue;
            
            routeElement.appendChild(shipIndicator);
        }
    },
    
    removeShipsFromRoute(routeElement) {
        const existingShips = routeElement.querySelectorAll('.ship-on-route');
        existingShips.forEach(ship => ship.remove());
    }
};


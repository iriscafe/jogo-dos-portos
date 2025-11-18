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

        // Verificar dinheiro
        const currentPlayer = GameState.getCurrentPlayer();
        if (!currentPlayer) {
            Notifications.error('Erro ao buscar informações do jogador');
            return;
        }

        if (currentPlayer.dinheiro < route.cost) {
            Notifications.error(`Dinheiro insuficiente! Você tem $${currentPlayer.dinheiro.toFixed(0)}, mas precisa de $${route.cost}`);
            return;
        }

        // Confirmar compra
        if (confirm(`Comprar rota ${fromPort.name} → ${toPort.name} por $${route.cost}?`)) {
            this.buyRoute(route, fromPort, toPort);
        }
    },
    
    buyRoute(route, fromPort, toPort) {
        const backendRoute = this.findBackendRoute(route.from, route.to);
        if (!backendRoute) {
            Notifications.error('Rota não encontrada no jogo');
            return;
        }

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
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Erro ao comprar rota');
                });
            }
            return response.json();
        })
        .then(boughtRoute => {
            Notifications.success(`Rota comprada com sucesso! -$${route.cost}`);
            this.loadGameRoutes();
            GameActions.refreshGameData();
        })
        .catch(error => {
            console.error('Erro ao comprar rota:', error);
            const errorMsg = error.message.includes('Saldo insuficiente') 
                ? 'Saldo insuficiente para comprar esta rota'
                : error.message.includes('já pertence') 
                ? 'Esta rota já foi comprada por outro jogador'
                : 'Erro ao comprar rota. Tente novamente.';
            Notifications.error(errorMsg);
        });
    },
    
    findBackendRoute(fromId, toId) {
        return GameState.gameRoutes.find(r => {
            const fromIdBackend = r.portoOrigem?.id || r.portoOrigemId;
            const toIdBackend = r.portoDestino?.id || r.portoDestinoId;
            return (fromIdBackend === fromId && toIdBackend === toId) || 
                   (fromIdBackend === toId && toIdBackend === fromId);
        });
    },
    
    loadGameRoutes() {
        if (!GameState.currentGameId) return;
        
        fetch(`/api/games/${GameState.currentGameId}`)
            .then(r => r.ok ? r.json() : null)
            .then(game => {
                if (game && game.rotas) {
                    GameState.gameRoutes = game.rotas;
                    this.updateVisual();
                }
            })
            .catch(err => console.error('Erro ao carregar rotas:', err));
    },
    
    updateVisual() {
        GameState.routeElements.forEach((routeData, routeKey) => {
            const routeElement = routeData.element;
            const route = routeData.route;
            const [fromId, toId] = routeKey.split('-').map(Number);
            
            const backendRoute = this.findBackendRoute(fromId, toId);

            if (backendRoute && backendRoute.dono) {
                // Rota comprada - mostrar com cor do dono
                const ownerColor = backendRoute.dono?.cor?.nome?.toLowerCase() || 'blue';
                routeElement.className = `route ${ownerColor} owned`;
                routeElement.style.opacity = '0.8';
                routeElement.style.cursor = 'default';
                
                const ownerName = backendRoute.dono?.name || 'Jogador';
                const fromPort = Board.getPortById(fromId);
                const toPort = Board.getPortById(toId);
                const fromPortName = backendRoute.portoOrigem?.cidade || fromPort?.name || fromId;
                const toPortName = backendRoute.portoDestino?.cidade || toPort?.name || toId;
                routeElement.title = `Rota: ${fromPortName} → ${toPortName}\nPertence a: ${ownerName} | Pontos: ${route.points}`;
            } else {
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
                    routeElement.title = `Rota: ${fromPort.name} → ${toPort.name}\nCusto: $${route.cost} | Pontos: ${route.points}`;
                }
            }
        });
    }
};


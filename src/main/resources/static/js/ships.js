// Gerenciamento de navios e movimentação
const Ships = {
    selectedPort: null,
    shipsInPorts: new Map(), // Mapeia portId -> { playerId: count }
    
    init() {
        // Inicializar navios nos portos (todos começam no porto 1)
        if (GameState.currentPlayerId) {
            this.shipsInPorts.set(1, { [GameState.currentPlayerId]: 1 });
        }
    },
    
    selectPort(port) {
        // Navios agora funcionam como trens no Ticket to Ride - são colocados nas rotas, não movidos entre portos
        // Esta funcionalidade foi desabilitada
        Notifications.info('Navios são colocados automaticamente nas rotas quando você as compra');
    },
    
    placeShip(port) {
        const currentPlayer = GameState.getCurrentPlayer();
        if (!currentPlayer || currentPlayer.naviosDisponiveis <= 0) {
            Notifications.error('Você não tem navios disponíveis!');
            return;
        }

        // Colocar navio no porto
        const portShips = this.shipsInPorts.get(port.id) || {};
        portShips[GameState.currentPlayerId] = (portShips[GameState.currentPlayerId] || 0) + 1;
        this.shipsInPorts.set(port.id, portShips);

        // Atualizar visual
        this.updatePortVisual(port.id);
        
        // Simular redução de navios disponíveis (front-end apenas)
        Notifications.success(`Navio colocado em ${port.name}`);
    },
    
    moveShip(fromPort, toPort) {
        const currentPlayer = GameState.getCurrentPlayer();
        if (!currentPlayer) return;

        // Verificar se há navio no porto de origem
        const shipsAtOrigin = this.getShipsAtPort(fromPort.id, GameState.currentPlayerId);
        if (shipsAtOrigin === 0) {
            Board.highlightPort(fromPort.id, false);
            this.selectedPort = null;
            Notifications.error('Você não tem navios neste porto!');
            return;
        }

        // Verificar se há rota comprada entre os portos
        const hasRoute = this.hasOwnedRoute(fromPort.id, toPort.id);
        if (!hasRoute) {
            Board.highlightPort(fromPort.id, false);
            this.selectedPort = null;
            Notifications.error(`Você precisa comprar a rota entre ${fromPort.name} e ${toPort.name} primeiro!`);
            return;
        }

        // Mover navio
        const fromPortShips = this.shipsInPorts.get(fromPort.id) || {};
        fromPortShips[GameState.currentPlayerId] = Math.max(0, (fromPortShips[GameState.currentPlayerId] || 0) - 1);
        this.shipsInPorts.set(fromPort.id, fromPortShips);

        const toPortShips = this.shipsInPorts.get(toPort.id) || {};
        toPortShips[GameState.currentPlayerId] = (toPortShips[GameState.currentPlayerId] || 0) + 1;
        this.shipsInPorts.set(toPort.id, toPortShips);

        // Atualizar visual
        this.updatePortVisual(fromPort.id);
        this.updatePortVisual(toPort.id);
        
        // Limpar seleção
        Board.highlightPort(fromPort.id, false);
        this.selectedPort = null;

        Notifications.success(`Navio movido de ${fromPort.name} para ${toPort.name}!`);
    },
    
    hasOwnedRoute(fromPortId, toPortId) {
        const currentPlayer = GameState.getCurrentPlayer();
        if (!currentPlayer) return false;

        // Verificar se o jogador possui uma rota entre os portos
        return GameState.gameRoutes.some(route => {
            const fromId = route.portoOrigem?.id || route.portoOrigemId;
            const toId = route.portoDestino?.id || route.portoDestinoId;
            const routeOwnerId = route.dono?.id;
            
            return routeOwnerId === GameState.currentPlayerId && 
                   ((fromId === fromPortId && toId === toPortId) || 
                    (fromId === toPortId && toId === fromPortId));
        });
    },
    
    getShipsAtPort(portId, playerId) {
        const portShips = this.shipsInPorts.get(portId) || {};
        return portShips[playerId] || 0;
    },
    
    updatePortVisual(portId) {
        const portElement = document.querySelector(`.port[data-port-id="${portId}"]`);
        if (!portElement) return;

        const currentPlayer = GameState.getCurrentPlayer();
        if (!currentPlayer) return;

        const shipCount = this.getShipsAtPort(portId, GameState.currentPlayerId);
        
        // Remover indicadores de navio anteriores
        const existingIndicator = portElement.querySelector('.ship-indicator');
        if (existingIndicator) {
            existingIndicator.remove();
        }

        // Adicionar indicador se houver navios
        if (shipCount > 0) {
            const indicator = document.createElement('div');
            indicator.className = 'ship-indicator';
            indicator.textContent = shipCount;
            indicator.style.background = this.getPlayerColor(currentPlayer);
            portElement.appendChild(indicator);
        }
    },
    
    getPlayerColor(player) {
        const colorMap = {
            'blue': '#3182ce',
            'red': '#e53e3e',
            'green': '#38a169',
            'yellow': '#d69e2e',
            'purple': '#805ad5'
        };
        const colorName = player.cor?.nome?.toLowerCase() || 'blue';
        return colorMap[colorName] || colorMap.blue;
    },
    
    updateAllPortsVisual() {
        Board.ports.forEach(port => {
            this.updatePortVisual(port.id);
        });
    },
    
    reset() {
        this.selectedPort = null;
        this.shipsInPorts.clear();
        Board.clearAllHighlights();
    }
};


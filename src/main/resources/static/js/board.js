// Inicialização e gerenciamento do tabuleiro
const Board = {
    ports: [
        { id: 1, name: 'Roterdã', x: 50, y: 100, color: 'blue' },
        { id: 2, name: 'Hamburgo', x: 150, y: 80, color: 'blue' },
        { id: 3, name: 'Antuérpia', x: 200, y: 120, color: 'blue' },
        { id: 4, name: 'Lisboa', x: 100, y: 200, color: 'blue' },
        { id: 5, name: 'Barcelona', x: 180, y: 180, color: 'red' },
        { id: 6, name: 'Marselha', x: 250, y: 160, color: 'red' },
        { id: 7, name: 'Gênova', x: 300, y: 140, color: 'red' },
        { id: 8, name: 'Nápoles', x: 320, y: 200, color: 'green' },
        { id: 9, name: 'Pireu', x: 350, y: 250, color: 'green' },
        { id: 10, name: 'Istambul', x: 400, y: 220, color: 'green' }
    ],
    
    routes: [
        { from: 1, to: 2, color: 'blue', cost: 15, points: 3 },
        { from: 2, to: 3, color: 'blue', cost: 12, points: 2 },
        { from: 3, to: 4, color: 'blue', cost: 20, points: 4 },
        { from: 4, to: 5, color: 'red', cost: 18, points: 3 },
        { from: 5, to: 6, color: 'red', cost: 15, points: 2 },
        { from: 6, to: 7, color: 'red', cost: 12, points: 2 },
        { from: 7, to: 8, color: 'green', cost: 10, points: 2 },
        { from: 8, to: 9, color: 'green', cost: 25, points: 5 },
        { from: 9, to: 10, color: 'green', cost: 30, points: 6 },
        { from: 1, to: 4, color: 'yellow', cost: 35, points: 7 },
        { from: 2, to: 5, color: 'yellow', cost: 28, points: 5 },
        { from: 3, to: 6, color: 'yellow', cost: 25, points: 4 },
        { from: 4, to: 7, color: 'purple', cost: 22, points: 4 },
        { from: 5, to: 8, color: 'purple', cost: 20, points: 3 },
        { from: 6, to: 9, color: 'purple', cost: 18, points: 3 }
    ],
    
    initialize() {
        const board = document.getElementById('boardContent');
        if (!board) return;
        
        this.createPorts(board);
        this.createRoutes(board);
    },
    
    createPorts(board) {
        this.ports.forEach(port => {
            const portElement = document.createElement('div');
            portElement.className = `port ${port.color}`;
            portElement.dataset.portId = port.id;
            portElement.style.left = `${port.x}px`;
            portElement.style.top = `${port.y}px`;
            portElement.title = port.name;
            portElement.textContent = port.id;
            portElement.onclick = () => Ships.selectPort(port);
            board.appendChild(portElement);
        });
    },
    
    createRoutes(board) {
        this.routes.forEach(route => {
            const fromPort = this.ports.find(p => p.id === route.from);
            const toPort = this.ports.find(p => p.id === route.to);
            
            if (fromPort && toPort) {
                const routeElement = document.createElement('div');
                routeElement.className = `route ${route.color}`;
                routeElement.dataset.routeFrom = route.from;
                routeElement.dataset.routeTo = route.to;
                routeElement.dataset.originalColor = route.color;
                
                const angle = Math.atan2(toPort.y - fromPort.y, toPort.x - fromPort.x);
                const length = Math.sqrt(Math.pow(toPort.x - fromPort.x, 2) + Math.pow(toPort.y - fromPort.y, 2));
                
                routeElement.style.left = `${fromPort.x + 20}px`;
                routeElement.style.top = `${fromPort.y + 18}px`;
                routeElement.style.width = `${length - 40}px`;
                routeElement.style.transform = `rotate(${angle}rad)`;
                routeElement.style.transformOrigin = '0 50%';
                
                routeElement.title = `Rota: ${fromPort.name} → ${toPort.name}\nCusto: $${route.cost} | Pontos: ${route.points}`;
                routeElement.onclick = () => Routes.selectRoute(route, fromPort, toPort);
                
                // Armazenar referência para atualização posterior
                const routeKey = `${route.from}-${route.to}`;
                GameState.routeElements.set(routeKey, { element: routeElement, route: route });
                
                board.appendChild(routeElement);
            }
        });
    },
    
    highlightPort(portId, highlight) {
        const portElement = document.querySelector(`.port[data-port-id="${portId}"]`);
        if (!portElement) return;
        
        if (highlight) {
            portElement.classList.add('selected');
        } else {
            portElement.classList.remove('selected');
        }
    },
    
    clearAllHighlights() {
        document.querySelectorAll('.port').forEach(port => {
            port.classList.remove('selected');
        });
    },
    
    getPortById(id) {
        return this.ports.find(p => p.id === id);
    },
    
    getRouteByPorts(fromId, toId) {
        return this.routes.find(r => 
            (r.from === fromId && r.to === toId) || 
            (r.from === toId && r.to === fromId)
        );
    }
};


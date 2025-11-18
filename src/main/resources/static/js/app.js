// Arquivo principal - inicialização da aplicação
window.onload = function() {
    WebSocketManager.connect();
    Board.initialize();
};

window.onbeforeunload = function() {
    WebSocketManager.disconnect();
};

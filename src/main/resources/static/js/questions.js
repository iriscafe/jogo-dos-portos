// Gerenciamento de perguntas
const Questions = {
    showQuestion(question) {
        GameState.questionShown = true;
        GameState.awaitingQuestion = false;
        GameState.currentQuestion = question;
        
        const questionText = document.getElementById('questionText');
        if (questionText) {
            questionText.textContent = question.enunciado;
        }
        
        const alternativesDiv = document.getElementById('alternatives');
        if (!alternativesDiv) return;
        
        alternativesDiv.innerHTML = '';

        question.alternativas.forEach((alt) => {
            const altElement = document.createElement('div');
            altElement.className = 'alternative';
            altElement.innerHTML = `
                <div class="alternative-letter">${alt.letra}</div>
                <div>${alt.texto}</div>
            `;
            altElement.onclick = () => this.selectAlternative(altElement, alt);
            alternativesDiv.appendChild(altElement);
        });

        const modal = document.getElementById('questionModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    },
    
    selectAlternative(element, alternative) {
        // Remover seleção anterior
        document.querySelectorAll('.alternative').forEach(el => {
            el.classList.remove('selected');
        });
        
        // Selecionar nova alternativa
        element.classList.add('selected');
        GameState.selectedAlternative = alternative;
        
        const submitBtn = document.getElementById('submitAnswerBtn');
        if (submitBtn) {
            submitBtn.disabled = false;
        }
    },
    
    submitAnswer() {
        if (!GameState.selectedAlternative || !GameState.currentQuestion || !GameState.currentPlayerId) {
            Notifications.error('Selecione uma alternativa');
            return;
        }

        const success = WebSocketManager.publish('/app/game/answer-question', {
            gameId: GameState.currentGameId,
            playerId: GameState.currentPlayerId,
            questionId: GameState.currentQuestion.id,
            alternativeId: GameState.selectedAlternative.id
        });

        if (!success) {
            // Fallback REST
            this.submitAnswerRest();
        }

        this.closeModal();
    },
    
    submitAnswerRest() {
        fetch('/api/questions/answer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playerId: GameState.currentPlayerId,
                questionId: GameState.currentQuestion.id,
                alternativeId: GameState.selectedAlternative.id
            })
        })
        .then(r => r.json())
        .then(res => {
            if (res.correct) {
                Notifications.success('Resposta correta! +$20');
            } else {
                Notifications.error('Resposta incorreta! -$5');
            }
            GameActions.refreshGameData();
        })
        .catch(() => Notifications.error('Erro ao enviar resposta'));
    },
    
    closeModal() {
        const modal = document.getElementById('questionModal');
        if (modal) {
            modal.style.display = 'none';
        }
        GameState.selectedAlternative = null;
        GameState.currentQuestion = null;
        
        const submitBtn = document.getElementById('submitAnswerBtn');
        if (submitBtn) {
            submitBtn.disabled = true;
        }
    },
    
    handleAnswer(data) {
        if (data.correct) {
            Notifications.success('Resposta correta! +$20');
        } else {
            Notifications.error('Resposta incorreta! -$5');
        }
    },
    
    getRandomQuestion() {
        GameState.questionShown = false;
        GameState.awaitingQuestion = true;
        
        const success = WebSocketManager.publish('/app/game/get-random-question', {
            gameId: GameState.currentGameId
        });
        
        if (success) {
            // Fallback por timeout se WS não entregar
            setTimeout(() => {
                if (!GameState.questionShown) {
                    this.getRandomQuestionRest();
                }
            }, 800);
        } else {
            this.getRandomQuestionRest();
        }
    },
    
    getRandomQuestionRest() {
        fetch('/api/questions/random')
            .then(r => r.status === 204 ? null : r.json())
            .then(q => {
                if (!q) {
                    Notifications.error('Sem perguntas disponíveis');
                    return;
                }
                this.showQuestion(q);
            })
            .catch(() => Notifications.error('Erro ao obter pergunta'))
            .finally(() => { GameState.awaitingQuestion = false; });
    }
};

// Funções globais para chamadas do HTML
function submitAnswer() {
    Questions.submitAnswer();
}

function closeQuestionModal() {
    Questions.closeModal();
}

function getRandomQuestion() {
    Questions.getRandomQuestion();
}


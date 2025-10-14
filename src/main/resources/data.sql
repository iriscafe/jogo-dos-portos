-- Inserir cores (ignorar se já existirem)
INSERT IGNORE INTO colors (nome) VALUES 
('Azul'),
('Vermelho'),
('Verde'),
('Amarelo'),
('Roxo');

-- Inserir portos (ignorar se já existirem)
INSERT IGNORE INTO ports (cidade) VALUES 
('Roterdã'),
('Hamburgo'),
('Antuérpia'),
('Lisboa'),
('Barcelona'),
('Marselha'),
('Gênova'),
('Nápoles'),
('Pireu'),
('Istambul');

-- Inserir banco de perguntas (ignorar se já existir)
INSERT IGNORE INTO question_banks (pontos, valor, gabarito) VALUES 
(10, 50.0, 'Banco de perguntas gerais sobre portos e navegação');

-- Inserir perguntas (com resposta temporária)
INSERT IGNORE INTO questions (enunciado, cor_id, banco_perguntas_id, resposta_correta_id) VALUES 
('Qual e a capital do Brasil?', 1, 1, 1),
('Qual o maior oceano do planeta?', 2, 1, 1),
('Qual pais possui o porto de Roterda?', 3, 1, 1),
('Qual e a moeda oficial da Alemanha?', 4, 1, 1),
('Qual cidade e conhecida como "Cidade Luz"?', 5, 1, 1);

-- Inserir alternativas para todas as perguntas (ignorar se já existirem)
INSERT IGNORE INTO alternatives (texto, letra, question_id) VALUES 
-- Pergunta 1: Capital do Brasil
('Sao Paulo', 'A', 1),
('Brasilia', 'B', 1),
('Rio de Janeiro', 'C', 1),
('Salvador', 'D', 1),

-- Pergunta 2: Maior oceano
('Atlantico', 'A', 2),
('Pacifico', 'B', 2),
('Indico', 'C', 2),
('Artico', 'D', 2),

-- Pergunta 3: Porto de Roterda
('Alemanha', 'A', 3),
('Belgica', 'B', 3),
('Paises Baixos', 'C', 3),
('Dinamarca', 'D', 3),

-- Pergunta 4: Moeda da Alemanha
('Marco', 'A', 4),
('Euro', 'B', 4),
('Libra', 'C', 4),
('Dólar', 'D', 4),

-- Pergunta 5: Cidade Luz
('Londres', 'A', 5),
('Paris', 'B', 5),
('Roma', 'C', 5),
('Madrid', 'D', 5);

-- Atualizar as perguntas com as respostas corretas
UPDATE questions SET resposta_correta_id = 2 WHERE id = 1 AND resposta_correta_id IS NULL;  -- Brasília
UPDATE questions SET resposta_correta_id = 6 WHERE id = 2 AND resposta_correta_id IS NULL;  -- Pacífico
UPDATE questions SET resposta_correta_id = 10 WHERE id = 3 AND resposta_correta_id IS NULL; -- Países Baixos
UPDATE questions SET resposta_correta_id = 14 WHERE id = 4 AND resposta_correta_id IS NULL; -- Euro
UPDATE questions SET resposta_correta_id = 18 WHERE id = 5 AND resposta_correta_id IS NULL; -- Paris

-- Inserir rotas entre portos (ignorar se já existirem)
INSERT IGNORE INTO routes (porto_origem_id, porto_destino_id, custo, pontos, cor_id) VALUES 
-- Rotas Azuis (cor_id = 1)
(1, 2, 15.0, 3, 1),  -- Roterdã -> Hamburgo
(2, 3, 12.0, 2, 1),  -- Hamburgo -> Antuérpia
(3, 4, 20.0, 4, 1),  -- Antuérpia -> Lisboa

-- Rotas Vermelhas (cor_id = 2)
(4, 5, 18.0, 3, 2),  -- Lisboa -> Barcelona
(5, 6, 15.0, 2, 2),  -- Barcelona -> Marselha
(6, 7, 12.0, 2, 2),  -- Marselha -> Gênova

-- Rotas Verdes (cor_id = 3)
(7, 8, 10.0, 2, 3),  -- Gênova -> Nápoles
(8, 9, 25.0, 5, 3),  -- Nápoles -> Pireu
(9, 10, 30.0, 6, 3), -- Pireu -> Istambul

-- Rotas Amarelas (cor_id = 4)
(1, 4, 35.0, 7, 4),  -- Roterdã -> Lisboa
(2, 5, 28.0, 5, 4),  -- Hamburgo -> Barcelona
(3, 6, 25.0, 4, 4),  -- Antuérpia -> Marselha

-- Rotas Roxas (cor_id = 5)
(4, 7, 22.0, 4, 5),  -- Lisboa -> Gênova
(5, 8, 20.0, 3, 5),  -- Barcelona -> Nápoles
(6, 9, 18.0, 3, 5);  -- Marselha -> Pireu
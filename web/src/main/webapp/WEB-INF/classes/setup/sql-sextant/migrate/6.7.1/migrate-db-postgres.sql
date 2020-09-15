
UPDATE metadata
    SET data = replace(data, 'Type de jeux de donnée - ODATIS', 'Type de jeux de donnée ODATIS')
    WHERE data LIKE '%Type de jeux de donnée - ODATIS%';

UPDATE metadata
    SET data = replace(data, 'Thèmatiques - ODATIS', 'Thèmatiques ODATIS')
    WHERE data LIKE '%Thèmatiques - ODATIS%';

UPDATE metadata
    SET data = replace(data, 'Variables Odatis', 'Variables ODATIS')
    WHERE data LIKE '%Variables Odatis%';

UPDATE metadata
    SET data = replace(data, 'Centre de données - ODATIS', 'Centre de données ODATIS')
    WHERE data LIKE '%Centre de données - ODATIS%';

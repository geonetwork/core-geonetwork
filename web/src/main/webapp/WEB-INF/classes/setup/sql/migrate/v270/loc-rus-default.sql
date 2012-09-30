-- Take care to table ID (related to other loc files)
delete from CategoriesDes where iddes <14;
INSERT INTO CategoriesDes VALUES (1,'ru','Карты и графика');
INSERT INTO CategoriesDes VALUES (2,'ru','Наборы данных');
INSERT INTO CategoriesDes VALUES (3,'ru','Интерактивные ресурсы');
INSERT INTO CategoriesDes VALUES (4,'ru','Компьютерные программы');
INSERT INTO CategoriesDes VALUES (5,'ru','Практические ситуации');
INSERT INTO CategoriesDes VALUES (6,'ru','Материалы конференций');
INSERT INTO CategoriesDes VALUES (7,'ru','Фотографии');
INSERT INTO CategoriesDes VALUES (8,'ru','Аудио/Видео');
INSERT INTO CategoriesDes VALUES (9,'ru','Каталоги/справочники');
INSERT INTO CategoriesDes VALUES (10,'ru','Другие ресурсы');
INSERT INTO CategoriesDes VALUES (11,'rus','Z3950 Servers');
INSERT INTO CategoriesDes VALUES (12,'rus','Registers');
INSERT INTO CategoriesDes VALUES (13,'rus','Физические образцы');

INSERT INTO StatusValuesDes VALUES (0,'rus','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'rus','Draft');
INSERT INTO StatusValuesDes VALUES (2,'rus','Approved');
INSERT INTO StatusValuesDes VALUES (3,'rus','Retired');
INSERT INTO StatusValuesDes VALUES (4,'rus','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'rus','Rejected');


-- ======================================================================
-- === Table: Categories
-- ======================================================================

INSERT INTO Categories (id, name) VALUES (1,'maps');
INSERT INTO Categories (id, name) VALUES (2,'datasets');
INSERT INTO Categories (id, name) VALUES (3,'interactiveResources');
INSERT INTO Categories (id, name) VALUES (4,'applications');
INSERT INTO Categories (id, name) VALUES (5,'caseStudies');
INSERT INTO Categories (id, name) VALUES (6,'proceedings');
INSERT INTO Categories (id, name) VALUES (7,'photo');
INSERT INTO Categories (id, name) VALUES (8,'audioVideo');
INSERT INTO Categories (id, name) VALUES (9,'directories');
INSERT INTO Categories (id, name) VALUES (10,'otherResources');
INSERT INTO Categories (id, name) VALUES (12,'registers');
INSERT INTO Categories (id, name) VALUES (13,'physicalSamples');

-- ======================================================================
-- === Table: Groups
-- ======================================================================

INSERT INTO Groups (id, name, description, email, referrer) VALUES (-1,'GUEST','self-registered users',NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (0,'intranet',NULL,NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (1,'all',NULL,NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (2,'sample',NULL,NULL,NULL);

-- ======================================================================
-- === Table: IsoLanguages
-- ======================================================================

INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (1,'aar', 'aa');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (2,'abk', 'ab');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (3,'ace', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (4,'ach', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (5,'ada', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (6,'ady', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (7,'afa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (8,'afh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (9,'afr', 'af');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (10,'ain', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (11,'aka', 'ak');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (12,'akk', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (13,'alb', 'sq');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (14,'ale', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (15,'alg', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (16,'alt', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (17,'amh', 'am');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (18,'ang', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (19,'anp', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (20,'apa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (21,'ara', 'ar');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (22,'arc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (23,'arg', 'an');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (24,'arm', 'hy');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (25,'arn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (26,'arp', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (27,'art', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (28,'arw', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (29,'asm', 'as');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (30,'ast', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (31,'ath', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (32,'aus', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (33,'ava', 'av');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (34,'ave', 'ae');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (35,'awa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (36,'aym', 'ay');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (37,'aze', 'az');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (38,'bad', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (39,'bai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (40,'bak', 'ba');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (41,'bal', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (42,'bam', 'bm');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (43,'ban', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (44,'baq', 'eu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (45,'bas', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (46,'bat', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (47,'bej', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (48,'bel', 'be');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (49,'bem', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (50,'ben', 'bn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (51,'ber', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (52,'bho', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (53,'bih', 'bh');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (54,'bik', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (55,'bin', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES (56,'bis', 'bi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (57,'bla', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (58,'bnt', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (59,'bos', 'bs');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (60,'bra', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (61,'bre', 'br');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (62,'btk', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (63,'bua', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (64,'bug', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (65,'bul', 'bg');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (66,'bur', 'my');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (67,'byn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (68,'cad', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (69,'cai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (70,'car', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (71,'cat', 'ca');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (72,'cau', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (73,'ceb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (74,'cel', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (75,'cha', 'ch');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (76,'chb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (77,'che', 'ce');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (78,'chg', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (79,'chi', 'zh');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (80,'chk', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (81,'chm', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (82,'chn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (83,'cho', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (84,'chp', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (85,'chr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (86,'chu', 'cu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (87,'chv', 'cv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (88,'chy', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (89,'cmc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (90,'cop', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (91,'cor', 'kw');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (92,'cos', 'co');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (93,'cpe', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (94,'cpf', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (95,'cpp', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (96,'cre', 'cr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (97,'crh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (98,'crp', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (99,'csb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (100,'cus', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (101,'cze', 'cs');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (102,'dak', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (103,'dan', 'da');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (104,'dar', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (105,'day', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (106,'del', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (107,'den', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (108,'dgr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (109,'din', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (110,'div', 'dv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (111,'doi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (112,'dra', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (113,'dsb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (114,'dua', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (115,'dum', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (116,'dut', 'nl');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (117,'dyu', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (118,'dzo', 'dz');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (119,'efi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (120,'egy', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (121,'eka', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (122,'elx', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (123,'eng', 'en');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (124,'enm', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (125,'epo', 'eo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (126,'est', 'et');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (127,'ewe', 'ee');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (128,'ewo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (129,'fan', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (130,'fao', 'fo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (131,'fat', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (132,'fij', 'fj');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (133,'fil', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (134,'fin', 'fi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (135,'fiu', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (136,'fon', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (137,'fre', 'fr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (138,'frm', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (139,'fro', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (140,'frr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (141,'frs', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (142,'fry', 'fy');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (143,'ful', 'ff');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (144,'fur', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (145,'gaa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (146,'gay', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (147,'gba', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (148,'gem', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (149,'geo', 'ka');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (150,'ger', 'de');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (151,'gez', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (152,'gil', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (153,'gla', 'gd');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (154,'gle', 'ga');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (155,'glg', 'gl');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (156,'glv', 'gv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (157,'gmh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (158,'goh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (159,'gon', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (160,'gor', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (161,'got', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (162,'grb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (163,'grc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (164,'gre', 'el');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (165,'grn', 'gn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (166,'gsw', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (167,'guj', 'gu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (168,'gwi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (169,'hai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (170,'hat', 'ht');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (171,'hau', 'ha');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (172,'haw', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (173,'heb', 'he');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (174,'her', 'hz');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (175,'hil', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (176,'him', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (177,'hin', 'hi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (178,'hit', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (179,'hmn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (180,'hmo', 'ho');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (181,'hsb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (182,'hun', 'hu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (183,'hup', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (184,'iba', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (185,'ibo', 'ig');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (186,'ice', 'is');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (187,'ido', 'io');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (188,'iii', 'ii');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (189,'ijo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (190,'iku', 'iu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (191,'ile', 'ie');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (192,'ilo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (193,'ina', 'ia');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (194,'inc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (195,'ind', 'id');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (196,'ine', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (197,'inh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (198,'ipk', 'ik');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (199,'ira', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (200,'iro', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (201,'ita', 'it');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (202,'jav', 'jv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (203,'jbo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (204,'jpn', 'ja');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (205,'jpr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (206,'jrb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (207,'kaa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (208,'kab', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (209,'kac', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (210,'kal', 'kl');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (211,'kam', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (212,'kan', 'kn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (213,'kar', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (214,'kas', 'ks');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (215,'kau', 'kr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (216,'kaw', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (217,'kaz', 'kk');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (218,'kbd', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (219,'kha', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (220,'khi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (221,'khm', 'km');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (222,'kho', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (223,'kik', 'ki');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (224,'kin', 'rw');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (225,'kir', 'ky');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (226,'kmb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (227,'kok', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (228,'kom', 'kv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (229,'kon', 'kg');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (230,'kor', 'ko');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (231,'kos', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (232,'kpe', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (233,'krc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (234,'krl', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (235,'kro', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (236,'kru', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (237,'kua', 'kj');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (238,'kum', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (239,'kur', 'ku');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (240,'kut', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (241,'lad', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (242,'lah', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (243,'lam', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (244,'lao', 'lo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (245,'lat', 'la');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (246,'lav', 'lv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (247,'lez', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (248,'lim', 'li');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (249,'lin', 'ln');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (250,'lit', 'lt');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (251,'lol', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (252,'loz', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (253,'ltz', 'lb');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (254,'lua', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (255,'lub', 'lu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (256,'lug', 'lg');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (257,'lui', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (258,'lun', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (259,'luo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (260,'lus', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (261,'mac', 'mk');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (262,'mad', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (263,'mag', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (264,'mah', 'mh');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (265,'mai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (266,'mak', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (267,'mal', 'ml');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (268,'man', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (269,'mao', 'mi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (270,'map', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (271,'mar', 'mr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (272,'mas', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (273,'may', 'ms');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (274,'mdf', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (275,'mdr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (276,'men', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (277,'mga', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (278,'mic', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (279,'min', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (280,'mis', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (281,'mkh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (282,'mlg', 'mg');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (283,'mlt', 'mt');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (284,'mnc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (285,'mni', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (286,'mno', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (287,'moh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (288,'mol', 'ml');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (289,'mon', 'mn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (290,'mos', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (291,'mul', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (292,'mun', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (293,'mus', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (294,'mwl', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (295,'mwr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (296,'myn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (297,'myv', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (298,'nah', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (299,'nai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (300,'nap', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (301,'nau', 'na');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (302,'nav', 'nv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (303,'nbl', 'nr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (304,'nde', 'nd');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (305,'ndo', 'ng');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (306,'nds', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (307,'nep', 'ne');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (308,'new', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (309,'nia', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (310,'nic', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (311,'niu', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (312,'nno', 'nn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (313,'nob', 'nb');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (314,'nog', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (315,'non', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (316,'nor', 'no');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (317,'nso', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (318,'nub', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (319,'nwc', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (320,'nya', 'ny');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (321,'nym', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (322,'nyn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (323,'nyo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (324,'nzi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (325,'oci', 'oc');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (326,'oji', 'oj');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (327,'ori', 'or');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (328,'orm', 'om');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (329,'osa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (330,'oss', 'os');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (331,'ota', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (332,'oto', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (333,'paa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (334,'pag', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (335,'pal', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (336,'pam', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (337,'pan', 'pa');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (338,'pap', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (339,'pau', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (340,'peo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (341,'per', 'fa');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (342,'phi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (343,'phn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (344,'pli', 'pi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (345,'pol', 'pl');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (346,'pon', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (347,'por', 'pt');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (348,'pra', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (349,'pro', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (350,'pus', 'ps');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (351,'qaa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (352,'que', 'qu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (353,'raj', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (354,'rap', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (355,'rar', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (356,'roa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (357,'roh', 'rm');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (358,'rom', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (359,'rum', 'ro');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (360,'run', 'rn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (361,'rup', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (362,'rus', 'ru');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (363,'sad', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (364,'sag', 'sg');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (365,'sah', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (366,'sai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (367,'sal', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (368,'sam', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (369,'san', 'sa');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (370,'sas', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (371,'sat', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (372,'srp', 'sr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (373,'scn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (374,'sco', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (375,'hrv', 'hr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (376,'sel', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (377,'sem', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (378,'sga', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (379,'sgn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (380,'shn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (381,'sid', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (382,'sin', 'si');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (383,'sio', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (384,'sit', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (385,'sla', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (386,'slo', 'sk');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (387,'slv', 'sl');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (388,'sma', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (389,'sme', 'se');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (390,'smi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (391,'smj', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (392,'smn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (393,'smo', 'sm');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (394,'sms', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (395,'sna', 'sn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (396,'snd', 'sd');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (397,'snk', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (398,'sog', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (399,'som', 'so');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (400,'son', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (401,'sot', 'st');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (402,'spa', 'es');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (403,'srd', 'sc');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (404,'srn', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (405,'srr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (406,'ssa', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (407,'ssw', 'ss');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (408,'suk', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (409,'sun', 'su');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (410,'sus', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (411,'sux', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (412,'swa', 'sw');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (413,'swe', 'sv');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (414,'syr', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (415,'tah', 'ty');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (416,'tai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (417,'tam', 'ta');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (418,'tat', 'tt');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (419,'tel', 'te');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (420,'tem', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (421,'ter', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (422,'tet', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (423,'tgk', 'tg');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (424,'tgl', 'tl');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (425,'tha', 'th');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (426,'tib', 'bo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (427,'tig', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (428,'tir', 'ti');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (429,'tiv', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (430,'tkl', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (431,'tlh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (432,'tli', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (433,'tmh', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (434,'tog', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (435,'ton', 'to');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (436,'tpi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (437,'tsi', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (438,'tsn', 'tn');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (439,'tso', 'ts');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (440,'tuk', 'tk');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (441,'tum', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (442,'tup', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (443,'tur', 'tr');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (444,'tut', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (445,'tvl', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (446,'twi', 'tw');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (447,'tyv', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (448,'udm', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (449,'uga', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (450,'uig', 'ug');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (451,'ukr', 'uk');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (452,'umb', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (453,'und', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (454,'urd', 'ur');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (455,'uzb', 'uz');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (456,'vai', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (457,'ven', 've');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (458,'vie', 'vi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (459,'vol', 'vo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (460,'vot', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (461,'wak', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (462,'wal', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (463,'war', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (464,'was', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (465,'wel', 'cy');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (466,'wen', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (467,'wln', 'wa');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (468,'wol', 'wo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (469,'xal', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (470,'xho', 'xh');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (471,'yao', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (472,'yap', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (473,'yid', 'yi');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (474,'yor', 'yo');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (475,'ypk', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (476,'zap', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (477,'zen', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (478,'zha', 'za');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (479,'znd', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (480,'zul', 'zu');
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (481,'zun', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (482,'zxx', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (483,'nqo', NULL);
INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (484,'zza', NULL);

-- ======================================================================
-- === Table: IsoLanguages (id, code, shortcode)
-- ======================================================================


-- ======================================================================
-- === Table: StatusValues
-- ======================================================================

-- Workflow
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (1,'draft','y', 1, 'workflow', 'recordUserAuthor');
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (2,'approved','y', 3, 'workflow', 'recordUserAuthor');
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (3,'retired','y', 5, 'workflow', 'recordUserAuthor');
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (4,'submitted','y', 2, 'workflow', 'recordProfileReviewer');
-- Deprecated, kept for retro-compatibility
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (5,'rejected','y', 4, 'workflow', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (0,'unknown','y', 0, 'workflow', null);

-- Task
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (100,'doiCreationTask','n', 100, 'task', 'statusUserOwner');

-- Event
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (50,'recordcreated','y', 50, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (51,'recordupdated','y', 51, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (52,'attachmentadded','y', 52, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (53,'attachmentdeleted','y', 53, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (54,'recordownerchange','y', 54, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (55,'recordgroupownerchange','y', 55, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (56,'recordprivilegeschange','y', 56, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (57,'recordcategorychange','y', 57, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (58,'recordvalidationtriggered','y', 58, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (59,'recordstatuschange','y', 59, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (60,'recordprocessingchange','y', 60, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (61,'recorddeleted','y', 61, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (62,'recordimported','y', 62, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (63,'recordrestored','y', 63, 'event', null);

-- ======================================================================
-- === Table: StatusValuesDes
-- ======================================================================

-- ======================================================================
-- === Table: Operations
-- ======================================================================

INSERT INTO Operations (id, name) VALUES  (0,'view');
INSERT INTO Operations (id, name) VALUES  (1,'download');
INSERT INTO Operations (id, name) VALUES  (2,'editing');
INSERT INTO Operations (id, name) VALUES  (3,'notify');
INSERT INTO Operations (id, name) VALUES  (5,'dynamic');
INSERT INTO Operations (id, name) VALUES  (6,'featured');


-- ======================================================================
-- === Table: Settings
-- ======================================================================

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/name', 'My GeoNetwork catalogue', 0, 110, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/siteId', '', 0, 120, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/organization', 'My organization', 0, 130, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/platform/version', '4.4.5', 0, 150, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/platform/subVersion', 'SNAPSHOT', 0, 160, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/svnUuid', '', 0, 170, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/host', 'localhost', 0, 210, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/protocol', 'http', 0, 220, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/port', '8080', 1, 230, 'n');
INSERT INTO settings (name, value, datatype, position, internal) VALUES ('system/server/log','log4j2.xml', 0, 250, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/timeZone', '', 0, 260, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/intranet/network', '127.0.0.1', 0, 310, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/intranet/netmask', '255.0.0.0', 0, 320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/use', 'false', 2, 510, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/host', NULL, 0, 520, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/port', NULL, 1, 530, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/username', NULL, 0, 540, 'y');
INSERT INTO Settings (name, value, datatype, position, internal, encrypted) VALUES ('system/proxy/password', NULL, 0, 550, 'y', 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/ignorehostlist', NULL, 0, 560, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/cors/allowedHosts', '*', 0, 561, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/documentation/url', 'https://docs.geonetwork-opensource.org/{{version}}/{{lang}}', 0, 570, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/email', 'root@localhost', 0, 610, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/host', '', 0, 630, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/port', '25', 1, 640, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/username', '', 0, 642, 'y');
INSERT INTO Settings (name, value, datatype, position, internal, encrypted) VALUES ('system/feedback/mailServer/password', '', 0, 643, 'y', 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ssl', 'false', 2, 641, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ignoreSslCertificateErrors', 'false', 2, 645, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/selectionmanager/maxrecords', '1000', 1, 910, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/enable', 'true', 2, 1210, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/enabledWhenIndexing', 'true', 2, 1211, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/capabilityRecordUuid', '-1', 0, 1220, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/metadataPublic', 'false', 2, 1310, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/transactionUpdateCreateXPath', 'true', 2, 1320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/enable', 'false', 2, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/enable', 'false', 2, 1911, 'n');
INSERT INTO Settings (name, value, datatype, position, internal, editable) VALUES ('system/userFeedback/lastNotificationDate', '', 0, 1912, 'y', 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/metadata/enable', 'false', 2, 1913, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/clickablehyperlinks/enable', 'true', 2, 2010, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/localrating/enable', 'advanced', 0, 2110, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/localrating/notificationLevel', 'catalogueAdministrator', 0, 2111, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/localrating/notificationGroups', '', 0, 2112, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/enable', 'false', 2, 2310, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/localXlinkEnable', 'true', 2, 2311, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/ignore', 'operatesOn,featureCatalogueCitation,Anchor,source,parentIdentifier', 0, 2312, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/referencedDeletionAllowed', 'true', 2, 2313, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/hidewithheldelements/enableLogging', 'false', 2, 2320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/autofixing/enable', 'true', 2, 2410, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/searchStats/enable', 'false', 2, 2510, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/mdmode', '1', 0, 7010, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/tokentimeout', '3600', 1, 7020, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/cachesize', '60', 1, 7030, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/maxrecords', '10', 1, 7040, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/enable', 'false', 2, 7210, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atom', 'disabled', 0, 7230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomSchedule', '0 0 0 * * ?', 0, 7240, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomProtocol', 'INSPIRE-ATOM', 0, 7250, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/enableEditing', 'false', 2, 9010, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/enablePrivilegesManagement', 'false', 2, 9010, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/disabledHarvesterTypes', '', 0, 9011, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/recipient', NULL, 0, 9020, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/template', '', 0, 9021, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/templateError', 'There was an error on the harvesting: $$errorMsg$$', 0, 9022, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/templateWarning', '', 0, 9023, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/subject', '[$$harvesterType$$] $$harvesterName$$ finished harvesting', 0, 9024, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/enabled', 'false', 2, 9025, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level1', 'false', 2, 9026, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level2', 'false', 2, 9027, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level3', 'false', 2, 9028, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/users/identicon', 'gravatar:mp', 0, 9110, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/prefergrouplogo', 'true', 2, 9111, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/allThesaurus', 'false', 2, 9160, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/thesaurusNamespace', 'https://registry.geonetwork-opensource.org/{{type}}/{{filename}}', 0, 9161, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/vcs/enable', 'false', 2, 9161, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/validation/removeSchemaLocation', 'false', 2, 9170, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/history/enabled', 'false', 2, 9171, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/generateUuid', 'true', 2, 9100, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/preferredGroup', '', 1, 9105, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/preferredTemplate', '', 0, 9106, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/usergrouponly', 'false', 2, 9180, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publicationbyrevieweringroupowneronly', 'true', 2, 9181, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publication/notificationLevel', '', 0, 9182, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publication/notificationGroups', '', 0, 9183, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/threadedindexing/maxthreads', '1', 1, 9210, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/url', '', 0, 7211, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/urlquery', '', 0, 7212, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/nodeid', '', 0, 7213, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/apikey', '', 0, 7214, 'y');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/provider', '', 0, 7301, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/serviceUrl', '', 0, 7302, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/apiKey', '', 0, 7303, 'y');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'osm', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:3857', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/useGeodesicExtents', 'false', 2, 9591, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/sitemapLinkUrl', NULL, 0, 9165, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/sitemapDoiFirst', 'false', 2, 9166, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/dynamicAppLinkUrl', NULL, 0, 9167, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/resourceIdentifierPrefix', 'http://localhost:8080/geonetwork/srv/resources', 0, 10001, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/import/restrict', '', 0, 11000, 'y');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/enable', 'false', 2, 100002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/draftWhenInGroup', '', 0, 100003, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowSubmitApproveInvalidMd', 'true', 2, 100004, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowPublishNonApprovedMd', 'true', 2, 100005, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowPublishInvalidMd', 'true', 2, 100006, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/automaticUnpublishInvalidMd', 'false', 2, 100007, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/forceValidationOnMdSave', 'false', 2, 100008, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/backuparchive/enable', 'false', 2, 12000, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/link/excludedUrlPattern', '', 0, 12010, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/import/userprofile', 'Editor', 0, 12001, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/batchediting/accesslevel', 'Editor', 0, 12020, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/history/accesslevel', 'Editor', 0, 12021, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/delete/profilePublishedMetadata', 'Editor', 0, 12011, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/profilePublishMetadata', 'Reviewer', 0, 12021, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/profileUnpublishMetadata', 'Reviewer', 0, 12022, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/coverPdf', '', 0, 12500, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/introPdf', '', 0, 12501, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/tocPage', 'false', 2, 12502, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/headerLeft', '{siteInfo}', 0, 12503, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/headerRight', '', 0, 12504, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/footerLeft', '', 0, 12505, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/footerRight', '{date}', 0, 12506, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/pdfName', 'metadata_{datetime}.pdf', 0, 12507, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/pdfReport/headerLogoFileName', '', 0, 12508, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/csvReport/csvName', 'metadata_{datetime}.csv', 0, 12607, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/ui/defaultView', 'default', 0, 10100, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/enable', 'false', 2, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/publickey', '', 0, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/secretkey', '', 0, 1910, 'y');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doienabled', 'false', 2, 100191, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doiurl', '', 0, 100192, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doiusername', '', 0, 100193, 'n');
INSERT INTO Settings (name, value, datatype, position, internal, encrypted) VALUES ('system/publication/doi/doipassword', '', 0, 100194, 'y', 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doikey', '', 0, 110095, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doilandingpagetemplate', 'http://localhost:8080/geonetwork/srv/resources/records/{{uuid}}', 0, 100195, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipublicurl', '', 0, 100196, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipattern', '{{uuid}}', 0, 100197, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/minLength', '6', 1, 12000, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/maxLength', '20', 1, 12001, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/usePattern', 'true', 2, 12002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal, editable) VALUES ('system/security/passwordEnforcement/pattern', '^((?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*(_|[^\w])).*)$', 0, 12003, 'n', 'n');

-- WARNING: Security / Add this settings only if you need to allow admin
-- users to be able to reset user password. If you have mail server configured
-- user can reset password directly. If not, then you may want to add that settings
-- if you don't have access to the database.
-- INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/password/allowAdminReset', 'false', 2, 12004, 'n');

INSERT INTO HarvesterSettings (id, parentid, name, value) VALUES  (1,NULL,'harvesting',NULL);

-- ======================================================================
-- === Table: Users
-- ======================================================================

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype, isenabled) VALUES  (0,'nobody','','nobody','nobody',4,'','','','', 'n');
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (0, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (0, 0);

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype, isenabled) VALUES  (1,'admin','46e44386069f7cf0d4f2a420b9a2383a612f316e2024b0fe84052b0b96c479a23e8a0be8b90fb8c2','admin','admin',0,'','','','', 'y');
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (1, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (1, 1);


-- ======================================================================
-- === Table: MetadataURNTemplates
-- ======================================================================

INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (0, 'Custom URN', ' ', 'y');
INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (1, 'Autogenerated URN', ' ', 'y');

INSERT INTO Selections (id, name, isWatchable) VALUES (0, 'PreferredList', 'n');
INSERT INTO Selections (id, name, isWatchable) VALUES (1, 'WatchList', 'y');


INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (-1, 'Average', 'y');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (0, 'Completeness', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (1, 'Discoverability', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (2, 'Readability', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (3, 'DataQuality', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (4, 'ServiceQuality', 'n');

.. _iso19139_glossary:
.. include:: ../../../substitutions.txt
    
Glossaire des champs de métadonnées (ISO19139)
==============================================
                        
.. _iso19139-gmd-CI-Address:
        

**Adresse** 
                  
- *Code :* gmd:CI_Address
                
- *Description :* Type de données pour la localisation de l'organisation ou la personne individuelle responsable
        
- *Information complémentaire :* Type de données avec indication d'adresse.
        
        
                        
.. _iso19139-gmd-address:
        
                        
**Adresse** 
                  
- *Code :* gmd:address
                
- *Description :* Adresse physique et électronique à laquelle la personne ou l'organisation responsable peut être contactée
        
- *Information complémentaire :* Adresse postale ou électronique d'un premier niveau de contact (par exemple un secrétariat). Ces informations sont du type CI_Address et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-deliveryPoint:
        
                        
**Adresse** 
                  
- *Code :* gmd:deliveryPoint
                
- *Description :* Adresse de l'emplacement (comme décrit dans ISO 11180, Annexe A)
        
- *Information complémentaire :* Nom de la rue
        
        
                        
.. _iso19139-gmd-linkage:
        
                        
**Adresse Internet** 
                  
- *Code :* gmd:linkage
                
- *Description :* URL ou indication semblable d'une adresse Internet pour un accès on-line , par exemple http://www.isotc211.org
        
- *Information complémentaire :* Lien Internet, par exemple www.cosig.ch.
        
        
                        
.. _iso19139-gmd-onlineResource:
        
                        
**Adresse Internet** 
                  
- *Code :* gmd:onlineResource
                
- *Description :* Information on-line qui peut être utilisée pour contacter la personne ou l'organisation responsable
        
- *Information complémentaire :* Information en ligne, par exemple l'adresse Internet de l'organisation.
        
        
                        
.. _iso19139-gmd-electronicMailAddress:
        
                        
**Adresse e-mail** 
                  
- *Code :* gmd:electronicMailAddress
                
- *Description :* Adresse du courrier électronique de l'organisation ou de la personne individuelle responsable
        
- *Information complémentaire :* Adresse de courrier électronique de l'organisation ou de la personne responsable
        
        
                        
.. _iso19139-gmd-illuminationAzimuthAngle:
        
                        
**Angle azimutal** 
                  
- *Code :* gmd:illuminationAzimuthAngle
                
- *Description :* Angle d'azimut mesuré en degré dans le sens des aiguilles d'une montre entre le nord géographique et la ligne optique au moment de la prise de vue. Pour les images scannées, le pixel central prend le rôle de la ligne optique
        
- *Information complémentaire :* Azimut d'éclairage mesuré en degrés sexagésimaux dans le sens horaire à compter du nord géographique au moment de la prise de vues. Pour les images scannées, la référence est le pixel central.
        
        
                        
.. _iso19139-gmd-illuminationElevationAngle:
        
                        
**Angle d'élévation** 
                  
- *Code :* gmd:illuminationElevationAngle
                
- *Description :* Angle d'élévation mesuré en degré dans le sens des aiguilles d'une montre entre a. l'intersection de la ligne optique et le plan cible et b. la surface terrestre. Pour les images scannées, le pixel central prend le rôle de la ligne optique
        
- *Information complémentaire :* Angle vertical d'éclairage mesuré en degrés sexagésimaux dans le sens horaire à compter du plan cible à l'intersection de la ligne de visée optique avec la surface terrestre. Pour les images scannées, la référence est le pixel central.
        
        
                        
.. _iso19139-gmd-attributes:
        
                        
**Attributs** 
                  
- *Code :* gmd:attributes
                
- *Description :* Attributs sur lesquels l'information s'applique
        
- *Information complémentaire :* Attributs auxquels les informations se rapportent.
        
        
                        
.. _iso19139-gmd-authority:
        
                        
**Autorité** 
                  
- *Code :* gmd:authority
                
- *Description :* Personne ou service responsable pour la maintenance du domaine de valeurs
        
- *Information complémentaire :* Personne ou organisation responsable de cet espace nominal, par exemple un service. Ces informations sont du type CI_Citation et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-other:
        
                        
**Autre** 
                  
- *Code :* gmd:other
                
- *Description :* Type d'information qui ne se laisse classer dans aucune des autres possibilités de choix
        
- *Information complémentaire :* Description des informations ne se laissant classer dans aucune des autres possibilités de choix.
        
        
                        
.. _iso19139-gmd-userDefinedMaintenanceFrequency:
        
                        
**Autre fréquence de mise à jour** 
                  
- *Code :* gmd:userDefinedMaintenanceFrequency
                
- *Description :* Rythmes de mise à jour autres que ceux définis
        
- *Information complémentaire :* Rythme de mise à jour défini par l'utilisateur, si l'option "Définie par l'utilisateur" a été sélectionnée dans la liste de la "Fréquence d'entretien et de mise à jour".
        
        
                        
.. _iso19139-gmd-locale:
        
                        
**Autre langue** 
                  
- *Code :* gmd:locale
                
- *Description :* Utiliser cette section pour définir la liste des langues utilisées dans cette métadonnée.
        
        
        
                        
.. _iso19139-gmd-otherConstraints:
        
                        
**Autres contraintes** 
                  
- *Code :* gmd:otherConstraints
                
- *Description :* Autres restrictions et prérequis légaux pour accéder et utiliser les ressources où de métadonnées
        
- *Information complémentaire :* Autres restrictions et conditions préalables de nature juridique concernant l'accès et l'utilisation de la ressource ou des métadonnées. Ce champ doit être complété dès lors que l'un des deux champs précédents (accessConstraints, useConstraints) porte la mention "Autres restrictions".
        
        
                        
.. _iso19139-gmd-otherCitationDetails:
        
                        
**Autres informations de référence** 
                  
- *Code :* gmd:otherCitationDetails
                
- *Description :* Autre information utilisée pour compléter les informations de référence qui ne sont pas prévues ailleurs
        
- *Information complémentaire :* Autre information requise pour une description complète de la source, non saisie ou impossible à saisir dans un autre attribut.
        
        
                        
.. _iso19139-gmd-MD-Band:
        
                        
**Bande** 
                  
- *Code :* gmd:MD_Band
                
- *Description :* Classe pour l'étendue des longueurs d'ondes utilisées dans le spectre électromagnétique
        
- *Information complémentaire :* Classe précisant le domaine des longueurs d'onde utilisées dans le spectre électromagnétique.
        
        
                        
.. _iso19139-gmd-bitsPerValue:
        
                        
**Bits par pixel** 
                  
- *Code :* gmd:bitsPerValue
                
- *Description :* Nombre maximum de bits significatifs dans la représentation non-comprimée de la valeur dans chaque bande de chaque pixel
        
- *Information complémentaire :* Nombre maximal de bits significatifs dans la représentation non comprimée de la valeur dans chaque gamme et dans chaque pixel.
        
        
                        
.. _iso19139-gmd-EX-GeographicBoundingBox:
        
                        
**Boite géographique** 
                  
- *Code :* gmd:EX_GeographicBoundingBox
                
- *Description :* Type de données pour la description de la position géographique du jeu de données. Il s'agit ici d'une référence approximative de telle sorte qu''il n''est pas nécessaire de spécifier le système de coordonnées
        
- *Information complémentaire :* Type de données destiné à la description de la position géographique du jeu de données. Il s'agit ici de la définition d'une enveloppe sommaire (délimitation en latitude et en longitude). Des informations supplémentaires peuvent être trouvées sous EX_Extent et EX_GeographicExtent.
        
        
                        
.. _iso19139-gml-previousEdge:
        
                        
**Bord précédent** 
                  
- *Code :* gml:previousEdge
                
- *Description :* Bord précédent
        
        
        
                        
.. _iso19139-gml-nextEdge:
        
                        
**Bord suivant** 
                  
- *Code :* gml:nextEdge
                
- *Description :* Bord suivant
        
        
        
                        
.. _iso19139-gmd-purpose:
        
                        
**But** 
                  
- *Code :* gmd:purpose
                
- *Description :* Résumé des intentions pour lesquelles les ressources ont été développées
        
- *Information complémentaire :* Motif(s) de la création de ce jeu de données.
        
        
                        
.. _iso19139-gmd-rationale:
        
                        
**But du processus** 
                  
- *Code :* gmd:rationale
                
- *Description :* Exigences ou buts pour une étape de processus
        
- *Information complémentaire :* Motif de l'étape de traitement ou but poursuivi.
        
        
                        
.. _iso19139-gmd-verticalCRS:
        
                        
**CRS vertical** 
                  
- *Code :* gmd:verticalCRS
                
- *Description :* Information sur l'origine depuis laquelle les altitudes maximale et minimale ont été mesurées
        
        
        
                        
.. _iso19139-frame:
        
                        
**Cadre** 
                  
- *Code :* frame
                
- *Description :* Frame attribute provides a URI reference that identifies a description of the reference system
        
        
        
                        
.. _iso19139-gml-LinearRing:
        
                        
**Cercle** 
                  
- *Code :* gml:LinearRing
                
- *Description :* Cercle
        
        
        
                        
.. _iso19139-gmd-characterEncoding:
        
                        
**CharacterEncoding** 
                  
- *Code :* gmd:characterEncoding
                
- *Description :* CharacterEncoding
        
        
        
                        
.. _iso19139-codegmd-MD-Identifier:
        
                        
**Code** (cf. `gmd:MD_Identifier <#iso19139-gmd-md-identifier>`_)
                  
- *Code :* code
                
- *Description :* Valeur alphanumérique pour l'identification d'une occurrence dans le domaine de valeurs
        
- *Information complémentaire :* Code alphanumérique de l'identifiant. Ces informations sont du type PT_FreeText et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-code:
        
                        
**Code** 
                  
- *Code :* gmd:code
                
- *Description :* Valeur alphanumérique pour l'identification d'une occurrence dans le domaine de valeurs
        
- *Information complémentaire :* Code alphanumérique de l'identifiant. Ces informations sont du type PT_FreeText et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-domainCode:
        
                        
**Code** 
                  
- *Code :* gmd:domainCode
                
- *Description :* Trois codes digitaux attribués à l'élément étendu
        
- *Information complémentaire :* Code à trois chiffres affecté à l'élément étendu.
        
        
                        
.. _iso19139-gmd-LanguageCode:
        
                        
**Code ISO de la langue** 
                  
- *Code :* gmd:LanguageCode
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-MD-PixelOrientationCode:
        
                        
**Code de l’orientation du pixel** 
                  
- *Code :* gmd:MD_PixelOrientationCode
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-MD-ScopeCode:
        
                        
**Code du sujet** 
                  
- *Code :* gmd:MD_ScopeCode
                
- *Description :* 
        
        
        
                        
.. _iso19139-code:
        
                        
**Code du système** 
                  
- *Code :* code
                
- *Description :* Code. Par exemple, le code epsg.
        
        
        
                        
.. _iso19139-gmd-MD-ObligationCode:
        
                        
**Code d’obligation** 
                  
- *Code :* gmd:MD_ObligationCode
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-postalCode:
        
                        
**Code postal** 
                  
- *Code :* gmd:postalCode
                
- *Description :* Code postale ou autre code pour l'emplacement
        
- *Information complémentaire :* Code postal
        
        
                        
.. _iso19139-gmd-imageQualityCode:
        
                        
**Code sur la qualité de l'image** 
                  
- *Code :* gmd:imageQualityCode
                
- *Description :* Connaissances qui spécifient la qualité de l'image
        
- *Information complémentaire :* Identification spécifiant la qualité de l'image
        
        
                        
.. _iso19139-gmd-processingLevelCode:
        
                        
**Code sur les niveau de processus** 
                  
- *Code :* gmd:processingLevelCode
                
- *Description :* Code du distributeur d'image qui identifie les niveaux de processus radiométrique et géométrique appliqués
        
- *Information complémentaire :* Identification du distributeur de l'image indiquant le niveau de traitement radiométrique et géométrique appliqué.
        
        
                        
.. _iso19139-gmd-DQ-ConceptualConsistency:
        
                        
**Cohérence conceptuelle** 
                  
- *Code :* gmd:DQ_ConceptualConsistency
                
- *Description :* Classe pour l'adhésion aux règles du schéma conceptuel
        
- *Information complémentaire :* Classe permettant la description du respect des règles du schéma conceptuel.
      Exemple : Une parcelle n’a pas de propriétaire.
        
        
                        
.. _iso19139-gmd-DQ-DomainConsistency:
        
                        
**Cohérence du domaine de valeur** 
                  
- *Code :* gmd:DQ_DomainConsistency
                
- *Description :* Classe pour l'adhésion des valeurs aux domaines de valeurs
        
- *Information complémentaire :* Classe permettant la description du respect des valeurs des domaines de valeurs.
      Exemple : Un attribut a une valeur non renseignée dans la nomenclature, La charte graphique n’est pas respectée.
        
        
                        
.. _iso19139-gmd-DQ-FormatConsistency:
        
                        
**Cohérence du format** 
                  
- *Code :* gmd:DQ_FormatConsistency
                
- *Description :* Degré de conformité de l’échange de données avec le format spécifié.
        
- *Information complémentaire :* Classe permettant la description du niveau d'accord auquel le jeu de données décrit dans le domaine de qualité des données est mémorisé en regard de la structure de données physique.
      Exemple : France / Le lot EDIGEO n’est pas conforme au standard PCI, Les couleurs Autocad sont renseignées sur l’entité et non sur le layer, Un objet bâtiments est stocké dans la couche parcelle.
        
        
                        
.. _iso19139-gmd-DQ-TemporalConsistency:
        
                        
**Cohérence temporelle** 
                  
- *Code :* gmd:DQ_TemporalConsistency
                
- *Description :* Classe pour la description de la justesse d'événements, ou séquences, ordonnés, dans le cas ou ils sont donnés
        
- *Information complémentaire :* Exemple : La date renseignée d’approbation d’une procédure est antérieure à celle de la demande.
        
        
                        
.. _iso19139-gmd-DQ-TopologicalConsistency:
        
                        
**Cohérence topologique** 
                  
- *Code :* gmd:DQ_TopologicalConsistency
                
- *Description :* Classe pour la description de la justesse des caractéristiques topologiques définies explicitement du jeu de donnée définit par le domaine
        
- *Information complémentaire :* Classe permettant de décrire l'exactitude des caractéristiques topologiques définies explicitement, conformément au champ d'application défini pour le jeu de données.
      Exemple : La zone de plan d'occupation du sol (POS) ne passe pas sur tous les sommets des parcelles.
        
        
                        
.. _iso19139-gmd-cornerPoints:
        
                        
**Coins du raster** 
                  
- *Code :* gmd:cornerPoints
                
- *Description :* Relation du système de coordonnées raster au système terrestre définie par les cellules des quatre coins du raster et par les coordonnées correspondantes du système de référence spatial
        
- *Information complémentaire :* Lien du système de coordonnées de la trame avec le système terrestre défini par les cellules des quatre coins de la trame et les coordonnées correspondantes dans le système de référence spatial.
        
        
                        
.. _iso19139-srv-DCP:
        
                        
**Communication** 
                  
- *Code :* srv:DCP
                
- *Description :* Plateforme de communication (DCP) sur laquelle l’opération a été implémentée
        
        
        
                        
.. _iso19139-gmd-composedOf:
        
                        
**ComposedOf** 
                  
- *Code :* gmd:composedOf
                
- *Description :* ComposedOf
        
        
        
                        
.. _iso19139-gmd-condition:
        
                        
**Condition** 
                  
- *Code :* gmd:condition
                
- *Description :* Condition sous laquelle l'élément étendu est obligatoire
        
- *Information complémentaire :* Condition sous laquelle l'élément est obligatoire.
        
        
                        
.. _iso19139-gmd-imagingCondition:
        
                        
**Conditions d'image** 
                  
- *Code :* gmd:imagingCondition
                
- *Description :* Conditions affectant l'image
        
- *Information complémentaire :* Contraintes auxquelles l'image est soumise.
        
        
                        
.. _iso19139-gmd-complianceCode:
        
                        
**Conformité à ISO 19110** 
                  
- *Code :* gmd:complianceCode
                
- *Description :* Indication de la conformité du catalogue d'objets avec l'ISO 19110
        
- *Information complémentaire :* Indication de la conformité du catalogue d'objets mentionné avec la norme ISO 19110
        
        
                        
.. _iso19139-gmd-CI-Contact:
        
                        
**Contact** 
                  
- *Code :* gmd:CI_Contact
                
- *Description :* Type de données avec l'information utilisée pour permettre le contact avec la personne et/ou l'organisation responsable
        
- *Information complémentaire :* Type de données intégrant des informations telles qu''un numéro de téléphone, de télécopie, des heures d'ouverture ou d'autres indications, toujours en rapport avec la personne ou le service désigné dans CI_ResponsibleParty.
        
        
                        
.. _iso19139-gmd-contactgmd-MD-Metadata:
        
                        
**Contact** (cf. `gmd:MD_Metadata <#iso19139-gmd-md-metadata>`_)
                  
- *Code :* gmd:contact
                
- *Description :* Organisme responsable pour les informations que contiennent les métadonnées.
        
- *Information complémentaire :* Rôle (propriétaire, prestataire, gestionnaire, etc.) de la personne ou du service compétent pouvant être sélectionné dans la liste présentée ici. Cet attribut renvoie à la classe CI_ResponsibleParty dans laquelle les informations relatives au service ou à la personne concernée sont gérées. Cet attribut est du type CI_ResponsibleParty et est géré dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-contact:
        
                        
**Contact** 
                  
- *Code :* gmd:contact
                
- *Description :* Organisme responsable pour les informations que contiennent les métadonnées.
        
- *Information complémentaire :* Rôle (propriétaire, prestataire, gestionnaire, etc.) de la personne ou du service compétent pouvant être sélectionné dans la liste présentée ici. Cet attribut renvoie à la classe CI_ResponsibleParty dans laquelle les informations relatives au service ou à la personne concernée sont gérées. Cet attribut est du type CI_ResponsibleParty et est géré dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-distributorContact:
        
                        
**Contact** 
                  
- *Code :* gmd:distributorContact
                
- *Description :* Services depuis lesquels la ressource peut être obtenue. Cette liste n''a pas besoin d'être exhaustive
        
- *Information complémentaire :* Personne ou organisation compétente auprès de laquelle le jeu de données peut être obtenu. Une seule information est permise. La référence est du type de données CI_ResponsibleParty et est gérée dans la classe du même nom.
        
        
                        
.. _iso19139-srv-serviceContact:
        
                        
**Contact** 
                  
- *Code :* srv:serviceContact
                
- *Description :* Informations permettant contacter le fournisseur du service
        
        
        
                        
.. _iso19139-gmd-userContactInfo:
        
                        
**Contact concernant la ressource** 
                  
- *Code :* gmd:userContactInfo
                
- *Description :* Identification des personnes et organisations, et des modes de communication avec celles-ci, utilisant les ressources
        
- *Information complémentaire :* Identification de la personne (ou des personnes) et de l'organisation (ou des organisations) utilisant la ou les ressources et mode de communication avec elle(s). Cette personne ou ce service endosse un rôle bien spécifique (propriétaire, prestataire, gestionnaire, etc.) pouvant être sélectionné dans la liste présentée. Ces informations sont du type CI_Citation et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-contactgmd-MD-MaintenanceInformation:
        
                        
**Contact pour la mise à jour** (cf. `gmd:MD_MaintenanceInformation <#iso19139-gmd-md-maintenanceinformation>`_)
                  
- *Code :* gmd:contact
                
- *Description :* Indications concernant la personne ou l'organisation qui est responsable de la mis à jour des métadonnées
        
- *Information complémentaire :* Informations concernant la personne ou l'organisation responsable de la mise à jour des données. Ces informations sont du type CI_ResponsibleParty et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-MD-CoverageDescription:
        
                        
**Contenu du raster** 
                  
- *Code :* gmd:MD_CoverageDescription
                
- *Description :* CLasse pour l'information sur le contenu de la cellule de données raster
        
- *Information complémentaire :* Des contenus physiques et thématiques d'une trame peuvent être décrits dans la classe MD_CoverageDescription. Si l'on se limite au contenu, les informations générales relatives aux attributs obligatoires attributeDescription et contentType peuvent convenir, la description de l'attribut dans attributeDescription devant toutefois s'effectuer via la valeur mesurée. Cf. MD_ContentInformation pour plus d'informations.
        
        
                        
.. _iso19139-srv-restrictions:
        
                        
**Contraintes** 
                  
- *Code :* srv:restrictions
                
- *Description :* Contraintes légales et de sécurité sur l’accès au service et sur la distribution de la donnée via le service
        
        
        
                        
.. _iso19139-gmd-accessConstraints:
        
                        
**Contraintes d'accès** 
                  
- *Code :* gmd:accessConstraints
                
- *Description :* Contraintes d'accès appliquées pour assurer la protection de la propriété privée et intellectuelle, et autres restrictions spéciales ou limitations pour obtenir la ressource où de métadonnées
        
- *Information complémentaire :* Restrictions d'accès relatives à la garantie de la propriété privée ou intellectuelle et restrictions de toutes natures visant à la conservation de la ressource ou des métadonnées. Elles peuvent être sélectionnées parmi les éléments suivants : droit d'auteur, brevet, brevet en voie de délivrance, marque, licence, propriété intellectuelle, diffusion limitée, autres restrictions.
        
        
                        
.. _iso19139-gmd-useConstraints:
        
                        
**Contraintes d'utilisation** 
                  
- *Code :* gmd:useConstraints
                
- *Description :* Contraintes appliquées pour assurer la protection des sphères privées et intellectuelles, et autres restrictions spéciales ou limitations ou mises en garde pour utiliser les ressources où de métadonnées
        
- *Information complémentaire :* Restrictions d'utilisation à fondement juridique destinées à garantir la sphère privée, la propriété intellectuelle ou d'autres domaines similaires tels que les conditions d'octroi de licence. Elles peuvent être sélectionnées parmi les éléments suivants : droit d'auteur, brevet, brevet en voie de délivrance, marque, licence, propriété intellectuelle, diffusion limitée, autres restrictions.
        
        
                        
.. _iso19139-gmd-MD-SecurityConstraints:
        
                        
**Contraintes de sécurité** 
                  
- *Code :* gmd:MD_SecurityConstraints
                
- *Description :* Classe avec les restrictions de manipulation imposées sur les ressources où de métadonnées pour la sécurité nationale ou des situations de sécurité similaires
        
- *Information complémentaire :* Classe contenant des informations relatives aux restrictions de sécurité liées à des questions de sécurité de portée nationale ou assimilée (exemple : secret, confidentialité, etc.). Cette classe est une représentation de la classe MD_Constraints. Cf. MD_Constraints pour d'autres informations.
        
        
                        
.. _iso19139-gmd-MD-LegalConstraints:
        
                        
**Contraintes légales** 
                  
- *Code :* gmd:MD_LegalConstraints
                
- *Description :* Classe pour les restrictions et conditions préalables légales pour accéder et utiliser les ressources où de métadonnées
        
- *Information complémentaire :* Classe contenant des informations relatives aux restrictions juridiques s'appliquant à la ressource, au jeu de métadonnées ou à leur utilisation. Cette classe est une représentation de la classe MD_Constraints. Cf. MD_Constraints pour de plus amples informations.
        
        
                        
.. _iso19139-gmd-resourceConstraints:
        
                        
**Contraintes sur la ressource** 
                  
- *Code :* gmd:resourceConstraints
                
- *Description :* Informations sur les contraites concernant les ressources
        
- *Information complémentaire :* Informations relatives aux restrictions s'appliquant aux ressources. Ces informations sont gérées dans la classe MD_Constraints.
        
        
                        
.. _iso19139-gmd-metadataConstraints:
        
                        
**Contraintes sur les métadonnées** 
                  
- *Code :* gmd:metadataConstraints
                
- *Description :* Contraintes sur l’accès et l’utilisation des métadonnées
        
        
        
                        
.. _iso19139-gmd-metadataConstraintsgmd-MD-Metadata:
        
                        
**Contraites sur les métadonnées** (cf. `gmd:MD_Metadata <#iso19139-gmd-md-metadata>`_)
                  
- *Code :* gmd:metadataConstraints
                
- *Description :* Contraintes sur l'accès et l'utilisation des métadonnées
        
- *Information complémentaire :* Restrictions d'accès et d'utilisation des métadonnées (exemple : copyright, conditions d'octroi de licence, etc.). Ces informations sont gérées dans la classe MD_Constraints.
        
        
                        
.. _iso19139-gml-coordinates:
        
                        
**Coordonnées** 
                  
- *Code :* gml:coordinates
                
- *Description :* Coordonnées
        
        
        
                        
.. _iso19139-gmd-DS-Association:
        
                        
**DS_Association** 
                  
- *Code :* gmd:DS_Association
                
- *Description :* DS_Association
        
        
        
                        
.. _iso19139-gmd-DS-DataSet:
        
                        
**DS_DataSet** 
                  
- *Code :* gmd:DS_DataSet
                
- *Description :* DS_DataSet
        
        
        
                        
.. _iso19139-gmd-DS-Initiative:
        
                        
**DS_Initiative** 
                  
- *Code :* gmd:DS_Initiative
                
- *Description :* DS_Initiative
        
        
        
                        
.. _iso19139-gmd-DS-OtherAggregate:
        
                        
**DS_OtherAggregate** 
                  
- *Code :* gmd:DS_OtherAggregate
                
- *Description :* DS_OtherAggregate
        
        
        
                        
.. _iso19139-gmd-DS-Platform:
        
                        
**DS_Platform** 
                  
- *Code :* gmd:DS_Platform
                
- *Description :* DS_Platform
        
        
        
                        
.. _iso19139-gmd-DS-ProductionSeries:
        
                        
**DS_ProductionSeries** 
                  
- *Code :* gmd:DS_ProductionSeries
                
- *Description :* DS_ProductionSeries
        
        
        
                        
.. _iso19139-gmd-DS-Sensor:
        
                        
**DS_Sensor** 
                  
- *Code :* gmd:DS_Sensor
                
- *Description :* DS_Sensor
        
        
        
                        
.. _iso19139-gmd-DS-Series:
        
                        
**DS_Series** 
                  
- *Code :* gmd:DS_Series
                
- *Description :* DS_Series
        
        
        
                        
.. _iso19139-gmd-DS-StereoMate:
        
                        
**DS_StereoMate** 
                  
- *Code :* gmd:DS_StereoMate
                
- *Description :* DS_StereoMate
        
        
        
                        
.. _iso19139-gco-Date:
        
                        
**Date** 
                  
- *Code :* gco:Date
                
- *Description :* Type de données pour des dates de références et des événements avec lesquels la date est en relation
        
- *Information complémentaire :* Type de données intégrant la date de référence et le type de référence de la source citée.
        
        
                        
.. _iso19139-gmd-CI-Date:
        
                        
**Date** 
                  
- *Code :* gmd:CI_Date
                
- *Description :* Type de données pour des dates de références et des événements avec lesquels la date est en relation
        
- *Information complémentaire :* Type de données intégrant la date de référence et le type de référence de la source citée.
        
        
                        
.. _iso19139-gmd-dategmd-CI-Citation:
        
                        
**Date** (cf. `gmd:CI_Citation <#iso19139-gmd-ci-citation>`_)
                  
- *Code :* gmd:date
                
- *Description :* Date de référence pour la ressource en question
        
- *Information complémentaire :* Date de référence indiquée sous forme de date (jj.mm.aaaa) et de type de date (création, publication, traitement). Ces informations sont du type CI_Date et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-dategmd-CI-Date:
        
                        
**Date** (cf. `gmd:CI_Date <#iso19139-gmd-ci-date>`_)
                  
- *Code :* gmd:date
                
- *Description :* Date de référence pour la ressource en question
        
- *Information complémentaire :* Date de référence (jj.mm.aaaa)
        
        
                        
.. _iso19139-gco-Dategmd-CI-Citation:
        
                        
**Date d'édition** (cf. `gmd:CI_Citation <#iso19139-gmd-ci-citation>`_)
                  
- *Code :* gco:Date
                
- *Description :* Date de l'édition
        
- *Information complémentaire :* Date de la version / de l'édition (jj.mm.aaaa).
        
        
                        
.. _iso19139-gmd-editionDate:
        
                        
**Date d'édition** 
                  
- *Code :* gmd:editionDate
                
- *Description :* Date de l'édition
        
- *Information complémentaire :* Date de la version / de l'édition (jj.mm.aaaa).
        
        
                        
.. _iso19139-gmd-dateStamp:
        
                        
**Date de création** 
                  
- *Code :* gmd:dateStamp
                
- *Description :* Date de création des métadonnées
        
- *Information complémentaire :* Date de création des métadonnées. Elle est automatiquement attribuée par l'application.
        
        
                        
.. _iso19139-gml-beginPosition:
        
                        
**Date de début** 
                  
- *Code :* gml:beginPosition
                
- *Description :* Date de début de validité (AAAA-MM-JJTHH:mm:ss)
        
        
        
                        
.. _iso19139-gml-endPosition:
        
                        
**Date de fin** 
                  
- *Code :* gml:endPosition
                
- *Description :* Date de fin de validité (AAAA-MM-JJTHH:mm:ss)
        
        
        
                        
.. _iso19139-gmd-dategmd-MD-MaintenanceInformation:
        
                        
**Date de la prochaine mise à jour** (cf. `gmd:MD_MaintenanceInformation <#iso19139-gmd-md-maintenanceinformation>`_)
                  
- *Code :* gmd:date
                
- *Description :* Date de la prochaine mise à jour de la ressource
        
- *Information complémentaire :* Date de la prochaine mise à jour (jj.mm.aaaa).
        
        
                        
.. _iso19139-gmd-dateOfNextUpdate:
        
                        
**Date de la prochaine mise à jour** 
                  
- *Code :* gmd:dateOfNextUpdate
                
- *Description :* Date de la prochaine mise à jour de la ressource
        
- *Information complémentaire :* Date de la prochaine mise à jour (jj.mm.aaaa).
        
        
                        
.. _iso19139-gmd-density:
        
                        
**Densité** 
                  
- *Code :* gmd:density
                
- *Description :* Densité d'écriture sur le medium
        
- *Information complémentaire :* Densité d'écriture sur le support concerné.
        
        
                        
.. _iso19139-srv-dependsOn:
        
                        
**Depends On** 
                  
- *Code :* srv:dependsOn
                
- *Description :* List of operations that must be completed immediately before current operation is invoked, structured as a list for capturing alternate predecessor paths and sets for capturing parallel predecessor paths
        
        
        
                        
.. _iso19139-gmd-describes:
        
                        
**Describes** 
                  
- *Code :* gmd:describes
                
- *Description :* Describes
        
        
        
                        
.. _iso19139-gmd-measureDescription:
        
                        
**Descripion du test** 
                  
- *Code :* gmd:measureDescription
                
- *Description :* Description des tests
        
- *Information complémentaire :* Description de la mesure (test).
        
        
                        
.. _iso19139-gmd-descriptionLI-Source:
        
                        
**Description** 
                  
- *Code :* gmd:description
                
- *Description :* Description détaillée de l'état des données sources
        
- *Information complémentaire :* Description des données source.
        
        
                        
.. _iso19139-gmd-descriptiongmd-EX-Extent:
        
                        
**Description** (cf. `gmd:EX_Extent <#iso19139-gmd-ex-extent>`_)
                  
- *Code :* gmd:description
                
- *Description :* Etendue spatiale et temporelle pour l'objet en question
        
- *Information complémentaire :* Description sous forme textuelle de l'extension spatiale et temporelle de l'objet considéré.
        
        
                        
.. _iso19139-gmd-descriptiongmd-CI-OnlineResource:
        
                        
**Description** (cf. `gmd:CI_OnlineResource <#iso19139-gmd-ci-onlineresource>`_)
                  
- *Code :* gmd:description
                
- *Description :* Texte descriptif détaillé sur ce que la ressource en ligne est/fait
        
- *Information complémentaire :* Description détaillée de ce que propose la source en ligne.
        
        
                        
.. _iso19139-srv-description:
        
                        
**Description** 
                  
- *Code :* srv:description
                
- *Description :* Description du rôle du paramètre
        
        
        
                        
.. _iso19139-gmd-attributeDescription:
        
                        
**Description de l'attribut** 
                  
- *Code :* gmd:attributeDescription
                
- *Description :* Description de l'attribut décrit par la valeur mesurée
        
- *Information complémentaire :* Description de l'attribut décrit par la valeur mesurée.
        
        
                        
.. _iso19139-gmd-environmentDescription:
        
                        
**Description de l'environnement de travail** 
                  
- *Code :* gmd:environmentDescription
                
- *Description :* Description de l'environnement de travail dans lequel le jeu de données a été créé, incluant des choses telles que logiciel, système d'exploitation, nom de fichier et taille du jeu de données
        
- *Information complémentaire :* Description de l'environnement de travail dans lequel le jeu de données est créé, incluant des éléments tels que le logiciel utilisé, le système d'exploitation, le nom et la taille du fichier.
        
        
                        
.. _iso19139-gmd-fileDescription:
        
                        
**Description de l'illustration** 
                  
- *Code :* gmd:fileDescription
                
- *Description :* Description textuelle de l'illustration du jeu de données
        
- *Information complémentaire :* Description de la représentation figurative du jeu de données. Elle indique ce qui est présenté, le degré d'adaptation avec le jeu de données sélectionné, etc.
        
        
                        
.. _iso19139-gmd-MD-ImageDescription:
        
                        
**Description de l'image** 
                  
- *Code :* gmd:MD_ImageDescription
                
- *Description :* Classe avec l'information sur l'utilisation d'une image
        
- *Information complémentaire :* Classe contenant les informations relatives aux possibilités d'utilisation d'une image.
        
        
                        
.. _iso19139-gmd-descriptor:
        
                        
**Description de l'étendue de valeur** 
                  
- *Code :* gmd:descriptor
                
- *Description :* Description de l'étendue de la valeur mesurée sur une cellule
        
- *Information complémentaire :* Description du domaine de valeurs mesurées dans une cellule.
        
        
                        
.. _iso19139-gmd-transformationDimensionDescription:
        
                        
**Description de l'étendue géographique** 
                  
- *Code :* gmd:transformationDimensionDescription
                
- *Description :* Description général de la transformation
        
- *Information complémentaire :* Description générale de la transformation.
        
        
                        
.. _iso19139-gmd-descriptionLI-ProcessStep:
        
                        
**Description de l'événement**
                  
- *Code :* gmd:description
                
- *Description :* Description de l'événement, incluant les paramètres ou tolérances y relatifs
        
- *Information complémentaire :* Description du processus (étape de traitement) pouvant inclure la description de paramètres ou de tolérances.
        
        
                        
.. _iso19139-gmd-evaluationMethodDescription:
        
                        
**Description de la méthode d'évaluation** 
                  
- *Code :* gmd:evaluationMethodDescription
                
- *Description :* Description des méthodes d'évaluation
        
- *Information complémentaire :* Description de la méthode d'appréciation.
        
        
                        
.. _iso19139-srv-operationDescription:
        
                        
**Description de l’opération** 
                  
- *Code :* srv:operationDescription
                
- *Description :* Description de l’opération sous forme de texte libre
        
        
        
                        
.. _iso19139-gmd-handlingDescription:
        
                        
**Description de manipulation** 
                  
- *Code :* gmd:handlingDescription
                
- *Description :* Information complémentaire sur les restrictions au sujet de la manipulation des ressources où de métadonnées
        
- *Information complémentaire :* Description de la manière dont la restriction est à appliquer, des cas dans lesquels elle doit l'être et des exceptions recensées.
        
        
                        
.. _iso19139-gmd-orientationParameterDescription:
        
                        
**Description des paramètres d'orientation** 
                  
- *Code :* gmd:orientationParameterDescription
                
- *Description :* Description des paramètres utilisés pour décrire l'orientation des senseurs
        
- *Information complémentaire :* Description des paramètres utilisés pour l'orientation du capteur.
        
        
                        
.. _iso19139-gmd-checkPointDescription:
        
                        
**Description des points de contrôle** 
                  
- *Code :* gmd:checkPointDescription
                
- *Description :* Description des points de contrôle utilisés pour tester la précision des données raster géoréférencées
        
- *Information complémentaire :* Description des points de contrôle utilisés pour tester la précision du géoréférencement de la trame.
        
        
                        
.. _iso19139-gmd-MD-FeatureCatalogueDescription:
        
                        
**Description du catalogue d'objet** 
                  
- *Code :* gmd:MD_FeatureCatalogueDescription
                
- *Description :* Classe pour l'information qui définit le catalogue d'objets ou le modèle de données
        
- *Information complémentaire :* Classe destinée aux informations identifiant le catalogue d'objets ou le modèle de données utilisé. Dans cette classe, le catalogue d'objets mis en application est spécifié sans entrer dans les détails de son contenu. Le catalogue d'objets auquel il est renvoyé par l'intermédiaire de l'attribut featureCatalogueCitation contient la description des propriétés des objets figurant dans le jeu de données. Dans la plupart des cas, le catalogue d'objets consiste en une liste exhaustive enregistrée qui n''est pas définie en fonction du jeu de données spécifique auquel le jeu de métadonnées se rapporte. La norme ne prévoit pas non plus que les objets définis dans le catalogue soient listés. Il est uniquement indiqué si le jeu de données contient effectivement des objets dont les propriétés coïncident avec celles des objets du catalogue. Vous trouverez d'autres informations sous MD_ContentInformation.
        
        
                        
.. _iso19139-gmd-MD-ScopeDescription:
        
                        
**Description du domaine** 
                  
- *Code :* gmd:MD_ScopeDescription
                
- *Description :* Description de la classe d'information concernée par les informations
        
- *Information complémentaire :* Description du domaine auquel se rapporte une information. Ces indications sont utilisées pour les informations de qualité (DQ_Quality) et les informations de mise à jour (MD_Maintenance) lorsque les caractéristiques ne sont pas homogènes sur l'ensemble du jeu de données décrit. l'un au moins de ces attributs doit être saisi. Exemple de la MO, jeu de données décrit : "Lot". La couche des biens-fonds est mise à jour en permanence, la couche de la nomenclature n''est quant à elle actualisée qu''au besoin. Si la mise à jour de la couche des biens-fonds est décrite, l'option "Biens-fonds" est entrée pour les propriétés. Les informations de qualité sont gérées sur le même modèle. Vous trouverez d'autres informations sous MD_MaintenanceInformation et DQ_DataQuality.
        
        
                        
.. _iso19139-gmd-updateScopeDescription:
        
                        
**Description du domaine de mise à jour** 
                  
- *Code :* gmd:updateScopeDescription
                
- *Description :* Information supplémentaire sur le domaine ou l'étendue de la mise à jour
        
- *Information complémentaire :* Informations supplémentaires relatives au domaine ou à l'étendue de la mise à jour. Ces données supplémentaires sont gérées dans la classe MD_ScopeDescription. La couche de la MO concernée par la mise à jour est par exemple précisée ici.
        
        
                        
.. _iso19139-gmd-levelDescription:
        
                        
**Description du niveau** 
                  
- *Code :* gmd:levelDescription
                
- *Description :* Description détaillée sur le niveau des données spécifiées par l'attribut scope (79) du domaine d'applicabilité
        
- *Information complémentaire :* Description détaillée du domaine des données. Ces données sont gérées dans la classe MD_ScopeDescription.
        
        
                        
.. _iso19139-gmd-EX-GeographicDescription:
        
                        
**Description géographique** 
                  
- *Code :* gmd:EX_GeographicDescription
                
- *Description :* Type de données pour la description de la surface géographique en utilisant des identifiants
        
- *Information complémentaire :* Type de données destiné à la description de l'extension géographique au moyen d'identifiants, par exemple une commune issue d'une liste. Des informations supplémentaires peuvent être trouvées sous EX_Extent et EX_GeographicExtent.
        
        
                        
.. _iso19139-gmd-MD-Dimension:
        
                        
**Dimension** 
                  
- *Code :* gmd:MD_Dimension
                
- *Description :* Classe contenant les propriétés des axes
        
- *Information complémentaire :* Cette classe contient les propriétés des axes requises pour la définition de données tramées. Ces informations sont utilisées dans la classe MD_GridSpatialRepresentation. d'autres informations peuvent y être trouvées.
        
        
                        
.. _iso19139-gmd-dimension:
        
                        
**Dimension** 
                  
- *Code :* gmd:dimension
                
- *Description :* Informations sur les dimensions des cellules
        
- *Information complémentaire :* Informations concernant les dimensions des valeurs mesurées dans les cellules.
        
        
                        
.. _iso19139-gmd-MD-RangeDimension:
        
                        
**Dimension de la cellule** 
                  
- *Code :* gmd:MD_RangeDimension
                
- *Description :* Classe pour l'information sur la dimension de la valeur mesurée dans chaque cellule
        
- *Information complémentaire :* Classe destinée aux informations relatives aux domaines de valeurs de chacune des dimensions des valeurs mesurées dans les cellules.
        
        
                        
.. _iso19139-gmd-numberOfDimensions:
        
                        
**Dimensions** 
                  
- *Code :* gmd:numberOfDimensions
                
- *Description :* Nombre d'axes spatio-temporels indépendants (nombre de dimensions)
        
        
        
                        
.. _iso19139-srv-direction:
        
                        
**Direction** 
                  
- *Code :* srv:direction
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-orientationParameterAvailability:
        
                        
**Disponibililté des paramètres d'orientation** 
                  
- *Code :* gmd:orientationParameterAvailability
                
- *Description :* Indication si oui ou non des paramètres d'orientation sont disponibles
        
- *Information complémentaire :* Indication de l'existence ou de l'absence de paramètres d'orientation pour le géoréférencement.
        
        
                        
.. _iso19139-gmd-cameraCalibrationInformationAvailability:
        
                        
**Disponibilité de la calibration de la caméra** 
                  
- *Code :* gmd:cameraCalibrationInformationAvailability
                
- *Description :* Indication si oui ou non les constantes pour les corrections de la calibration de la caméra sont à disposition
        
- *Information complémentaire :* Indication de la présence éventuelle de constantes pouvant être utilisées pour les corrections d'étalonnage de chambre.
        
        
                        
.. _iso19139-gmd-radiometricCalibrationDataAvailability:
        
                        
**Disponibilité de la calibration radiométrique** 
                  
- *Code :* gmd:radiometricCalibrationDataAvailability
                
- *Description :* Indication si oui ou non l'information sur la calibration radiométrique pour générer le produit standard corrigé radiométriquement est à disposition
        
- *Information complémentaire :* Indication de la présence éventuelle d'informations relatives à l'étalonnage radiométrique utilisé pour générer le produit standard corrigé au plan radiométrique.
        
        
                        
.. _iso19139-gmd-transformationParameterAvailability:
        
                        
**Disponibilité des paramètres de transformation** 
                  
- *Code :* gmd:transformationParameterAvailability
                
- *Description :* Indication si oui ou non des paramètres de transformation existent
        
- *Information complémentaire :* Indication de l'existence ou de l'absence de paramètres de transformation pour un géoréférencement de la trame.
        
        
                        
.. _iso19139-gmd-checkPointAvailability:
        
                        
**Disponibilité des points de contrôle** 
                  
- *Code :* gmd:checkPointAvailability
                
- *Description :* Indication si oui ou non des points de contrôle sont disponibles pour tester la précision des données raster géoréférencées
        
- *Information complémentaire :* Indication de l'existence ou de l'absence de points de contrôle pour tester la précision du géoréférencement de la trame.
        
        
                        
.. _iso19139-gmd-controlPointAvailability:
        
                        
**Disponibilité des points de contrôle** 
                  
- *Code :* gmd:controlPointAvailability
                
- *Description :* Indication si oui ou non des points de contrôle existent
        
- *Information complémentaire :* Indication de l'existence ou de l'absence de points d'appui pour le géoréférencement.
        
        
                        
.. _iso19139-gmd-filmDistortionInformationAvailability:
        
                        
**Disponibilité du protocole de calibration** 
                  
- *Code :* gmd:filmDistortionInformationAvailability
                
- *Description :* Indication si oui ou non le protocole de calibration est à disposition
        
- *Information complémentaire :* Indication de la présence éventuelle de valeurs d'étalonnage d'un réseau.
        
        
                        
.. _iso19139-gco-DateTimegmd-MD-StandardOrderProcess:
        
                        
**Disponibilité planifiée des données** (cf. `gmd:MD_StandardOrderProcess <#iso19139-gmd-md-standardorderprocess>`_)
                  
- *Code :* gco:DateTime
                
- *Description :* Date et heure à laquelle les donnée seront à disposition
        
- *Information complémentaire :* Date et heure à laquelle les données seront disponibles.
        
        
                        
.. _iso19139-gmd-plannedAvailableDateTime:
        
                        
**Disponibilité planifiée des données** 
                  
- *Code :* gmd:plannedAvailableDateTime
                
- *Description :* Date et heure à laquelle les donnée seront à disposition
        
- *Information complémentaire :* Date et heure à laquelle les données seront disponibles.
        
        
                        
.. _iso19139-gmd-lensDistortionInformationAvailability:
        
                        
**Disponibilité sur la distortion des lentilles** 
                  
- *Code :* gmd:lensDistortionInformationAvailability
                
- *Description :* Indication si oui ou non des informations sur la corrections de fautes dues aux lentilles sont à disposition
        
- *Information complémentaire :* Indication de la présence éventuelle de valeurs de correction de la distorsion de l'objectif ayant servi à la prise de vue.
        
        
                        
.. _iso19139-gmd-distance:
        
                        
**Distance au sol** 
                  
- *Code :* gmd:distance
                
- *Description :* Distance de référence, mesurée au sol
        
- *Information complémentaire :* Résolution au sol
        
- Liste de suggestions :

        
   - 10 (0.10)

        
   - 25 (0.25)

        
   - 50 (0.50)

        
   - 1 (1)

        
   - 30 (30)

        
   - 100 (100)

        
        
                        
.. _iso19139-gmd-MD-Distributor:
        
                        
**Distributeur** 
                  
- *Code :* gmd:MD_Distributor
                
- *Description :* Classe avec l'information sur le distributeur
        
- *Information complémentaire :* Classe contenant les informations relatives au distributeur des données (nom, rôle, adresse, etc.). d'autres informations peuvent être trouvées sous MD_Distribution.
        
        
                        
.. _iso19139-gmd-distributor:
        
                        
**Distributeur** 
                  
- *Code :* gmd:distributor
                
- *Description :* Informations sur le distributeur et sur la façon d'acquérir les ressources
        
- *Information complémentaire :* Informations relatives au distributeur.
        
        
                        
.. _iso19139-gmd-MD-Distribution:
        
                        
**Distribution** 
                  
- *Code :* gmd:MD_Distribution
                
- *Description :* Classe avec l'information sur le distributeur de données et sur les possibilités d'obtenir les ressources
        
- *Information complémentaire :* Classe contenant des informations relatives au distributeur des données de même qu''aux possibilités d'obtention du jeu de données. Cette classe recèle des indications sur le lieu de délivrance des données ainsi que sur la forme de leur obtention. MD_Distribution est une agrégation des informations concernant le transfert de données numériques (MD_DigitalTransferOptions) et des informations relatives au format des données (MD_Format).
        
        
                        
.. _iso19139-gmd-scope:
        
                        
**Domaine concernant la qualité** 
                  
- *Code :* gmd:scope
                
- *Description :* Les données spécifiques auxquelles sont appliquées les informations de qualité des données
        
- *Information complémentaire :* Indications relatives au domaine (exemple : attribut, jeu de données, série, projet, etc.) auquel se rapportent les informations concernant la qualité des données. Ces informations sont du type DQ_Scope et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-domainOfValidity:
        
                        
**Domaine d'applicabliité** 
                  
- *Code :* gmd:domainOfValidity
                
- *Description :* Domaine de validité pour le système de référence
        
- *Information complémentaire :* Domaine de validité du système de référence géographique.
        
        
                        
.. _iso19139-gmd-DQ-Scope:
        
                        
**Domaine d'appliquabilité** 
                  
- *Code :* gmd:DQ_Scope
                
- *Description :* Description de l'étendue des charactéristiques des données pour lesquelles des informations de qualité sont rapportées
        
- *Information complémentaire :* Description du domaine du jeu de données pour lequel des informations de qualité ont été saisies. d'autres indications peuvent être trouvées sous DQ_Dataquality.
        
        
                        
.. _iso19139-gmd-updateScope:
        
                        
**Domaine de la mise à jour** 
                  
- *Code :* gmd:updateScope
                
- *Description :* Domaine d'applicabilité des données sur lequel une mise à jour est appliquée
        
- *Information complémentaire :* Domaine des données concerné par la mise à jour. La catégorie à laquelle l'information se rapporte peut être indiquée dans la liste de codes (exemple : attributs, objets géométriques, jeu de données, etc.).
        
        
                        
.. _iso19139-gmd-DQ-CompletenessCommission:
        
                        
**Données excédentaires** 
                  
- *Code :* gmd:DQ_CompletenessCommission
                
- *Description :* Classe pour la description des données excédentaires présentes dans le jeu de donnée définit par le domaine d'applicabilité
        
- *Information complémentaire :* Classe destinée à la description des données surabondantes présentes dans le jeu de données, conformément au champ d'application défini. 
      Exemple : Un bâtiment a été reporté deux fois, Un cours d’eau a été représenté alors qu’il n’aurait pas dû être pris en compte.
        
        
                        
.. _iso19139-gmd-DQ-CompletenessOmission:
        
                        
**Données manquantes** 
                  
- *Code :* gmd:DQ_CompletenessOmission
                
- *Description :* Classe pour la description des données manquantes du jeu de donnée définit par le domaine d'applicabilité
        
- *Information complémentaire :* Classe destinée à la description des données manquantes dans le jeu de données, conformément au champ d'application défini.
      Exemple : il manque un bâtiment.
        
        
                        
.. _iso19139-gml-duration:
        
                        
**Durée** 
                  
- *Code :* gml:duration
                
- *Description :* Durée
        
        
        
                        
.. _iso19139-gml-start:
        
                        
**Début** 
                  
- *Code :* gml:start
                
- *Description :* Début
        
        
        
                        
.. _iso19139-gml-begin:
        
                        
**Début** 
                  
- *Code :* gml:begin
                
- *Description :* Début
        
        
        
                        
.. _iso19139-gmd-definition:
        
                        
**Définition** 
                  
- *Code :* gmd:definition
                
- *Description :* Définition de l'élément étendu
        
        
        
                        
.. _iso19139-gmd-denominator:
        
                        
**Dénominateur** 
                  
- *Code :* gmd:denominator
                
- *Description :* Le dénominateur de l'échelle (le chiffre en dessous de la barre de fraction)
        
- *Information complémentaire :* La valeur se trouvant sous la barre de fraction. Il s'agit ici de l'échelle : dans le cas d'une carte à l'échelle du 1:25000, seul le terme "25000" est entré. Cette valeur peut également représenter une indication de précision dans le cas d'un jeu de données vectorielles. Exemple : des limites saisies à une échelle de 1:25'000 et présentant ce niveau de précision.
        
- Liste de suggestions :

        
   - 1:5 000 (5000)

        
   - 1:10 000 (10000)

        
   - 1:25 000 (25000)

        
   - 1:50 000 (50000)

        
   - 1:100 000 (100000)

        
   - 1:200 000 (200000)

        
   - 1:300 000 (300000)

        
   - 1:500 000 (500000)

        
   - 1:1 000 000 (1000000)

        
        
                        
.. _iso19139-gmd-scaleDenominator:
        
                        
**Dénominateur de l'échelle** 
                  
- *Code :* gmd:scaleDenominator
                
- *Description :* Dénominateur de l'échelle de la carte source
        
- *Information complémentaire :* Facteur d'échelle (dénominateur) de la carte source. Ces informations sont du type MD_RepresentativeFraction et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-dataSetURI:
        
                        
**Désignation de la donnée (URI)** 
                  
- *Code :* gmd:dataSetURI
                
- *Description :* Uniformed Resource Identifier (URI) du jeu de données, que les métadonnées décrivent
        
- *Information complémentaire :* Identifiant URI (Uniformed Resource Identifier) du jeu de données auquel les métadonnées renvoient. Une adresse URL est indiquée ici, par exemple www.cosig.ch.
        
        
                        
.. _iso19139-gml-TimeEdge:
        
                        
**Ecart** 
                  
- *Code :* gml:TimeEdge
                
- *Description :* Ecart de temps
        
        
        
                        
.. _iso19139-gmd-MD-RepresentativeFraction:
        
                        
**Echelle** 
                  
- *Code :* gmd:MD_RepresentativeFraction
                
- *Description :* Classe : dérivé de ISO 19103 l'échelle, ou MD_RepresentativeFraction.denominator = 1 / Scale.measure et Scale.targetUnits = Scale.sourceUnits
        
- *Information complémentaire :* Classe comportant des informations relatives à l'échelle. Seul le dénominateur de l'échelle est saisi dans cette classe. l'échelle est déduite de la norme ISO 19103. Les informations contenues dans cette classe sont requises pour la description de la résolution géométrique du jeu de données dans la classe MD_Resolution, pour l'attribut d'équivalence d'échelle ("equivalentScale"). Cf. également sous MD_Resolution.
        
        
                        
.. _iso19139-gmd-equivalentScale:
        
                        
**Echelle comparative** 
                  
- *Code :* gmd:equivalentScale
                
- *Description :* Degré de détail exprimé avec l'échelle d'un graphique ou carte papier comparable
        
- *Information complémentaire :* Degré de spécification exprimé au moyen de l'échelle d'une carte ou d'un graphique analogique comparable. 
      Cette échelle peut également indiquer la précision de la saisie dans le cas d'un jeu de données vectorielles. 
      Cette information est du type MD_RepresentativeFraction et est gérée dans la classe du même nom où de plus amples renseignements peuvent être obtenus.
    
        
        
                        
.. _iso19139-gmd-edition:
        
                        
**Edition** 
                  
- *Code :* gmd:edition
                
- *Description :* Version de la ressource en question
        
- *Information complémentaire :* Version/édition de la source mentionnée
        
        
                        
.. _iso19139-gml-idgmd-MD-CRS:
        
                        
**Ellipsoïde**
                  
- *Code :* gml:id
                
- *Description :* Identification de l'ellipsoïde utilisée
        
- *Information complémentaire :* Identification de l'ellipsoïde utilisé.
        
        
                        
.. _iso19139-gml-complex:
        
                        
**Elément complexe** 
                  
- *Code :* gml:complex
                
- *Description :* Elément complexe
        
        
        
                        
.. _iso19139-gmd-geographicElement:
        
                        
**Elément géographique** 
                  
- *Code :* gmd:geographicElement
                
- *Description :* Informations sur l'etendue géographique
        
- *Information complémentaire :* Informations concernant l'extension géographique. Ces informations sont gérées dans la classe EX_GeographicExtent.
        
        
                        
.. _iso19139-gmd-temporalElement:
        
                        
**Elément temporel** 
                  
- *Code :* gmd:temporalElement
                
- *Description :* Informations sur l'étendue temporelle
        
- *Information complémentaire :* 
      L’étendue temporelle définit la période de temps couverte par le contenu de la ressource. Cette période peut être
      exprimée de l’une des manières suivantes : une date déterminée,
      un intervalle de dates exprimé par la date de début et la date de fin de l’intervalle,
      un mélange de dates et d’intervalles.
      
      
      Informations relatives à l'extension temporelle. Elles sont gérées dans la classe EX_TemporalExtent.
        
        
                        
.. _iso19139-gmd-verticalElement:
        
                        
**Elément vertical** 
                  
- *Code :* gmd:verticalElement
                
- *Description :* Informations sur l'étendue verticale
        
- *Information complémentaire :* Informations concernant l'extension verticale. Elles sont gérées dans la classe EX_VerticalExtent.
        
        
                        
.. _iso19139-gmd-parentEntity:
        
                        
**Entité parent** 
                  
- *Code :* gmd:parentEntity
                
- *Description :* Nom des entités de métadonnées sous lesquelles l'élément étendu de métadonnée pourrait apparaître. Les noms devraient être des éléments de métadonnées standards ou d'autres éléments de métadonnées étendus
        
- *Information complémentaire :* Nom de la ou des classes de métadonnées dans lesquelles apparaît cet élément de métadonnées étendu. Le nom peut être celui d'une classe standard ou d'une classe étendue.
        
        
                        
.. _iso19139-gmd-eastBoundLongitude:
        
                        
**Est** 
                  
- *Code :* gmd:eastBoundLongitude
                
- *Description :* Coordonnée la plus à l'est de la limite de l'étendue du jeu de données, exprimée en longitude avec des degrés décimaux (EST positif)
        
- *Information complémentaire :* Limite est de l'extension du jeu de données, exprimée en longitude géographique (degrés décimaux) comptée positivement vers l'est.
        
        
                        
.. _iso19139-gmd-sourceStep:
        
                        
**Etape du processus** 
                  
- *Code :* gmd:sourceStep
                
- *Description :* Informations sur une étape du processus de création des données
        
- *Information complémentaire :* Informations relatives aux étapes de traitement requises par la génération et l'actualisation des données source, avec indication des dates correspondantes. Les différentes étapes conduisant au jeu de données définitif sont mentionnées ici (les données source sont décrites). Exemple : restitution photogrammétrique de clichés aériens, vérification et complètement sur le terrain puis établissement de la carte. Ces étapes peuvent être saisies dans la classe LI_ProcessStep (texte, date, source, personne responsable du traitement).
        
        
                        
.. _iso19139-gmd-status:
        
                        
**Etat** 
                  
- *Code :* gmd:status
                
- *Description :* Etat (de travail) des ressources
        
- *Information complémentaire :* Etat de traitement du jeu de données. Sélection de l'une des options suivantes : complet, archive historique, obsolète, en cours, en projet, nécessaire, à l'étude.
        
        
                        
.. _iso19139-gmd-EX-Extent:
        
                        
**Etendue** 
                  
- *Code :* gmd:EX_Extent
                
- *Description :* Type de données pour l'information sur l'étendue horizontale, verticale et temporelle du jeu de données
        
- *Information complémentaire :* Type de données contenant des informations relatives à l'extension horizontale, verticale et temporelle du jeu de données. Les types de données de cette classe contiennent des éléments de métadonnées décrivant l'extension spatiale et temporelle des données. EX_Extent est une agrégation des classes EX_GeographicExtent (description de l'extension géographique), EX_TemporalExtent (extension temporelle des données) et EX_VerticalExtent (extension verticale des données). l'extension géographique est spécifiée plus avant par une délimitation au moyen d'un polygone (EX_BoundingPolygon) comme par un rectangle de délimitation géographique (EX_GeographicBoundingBox) et une description textuelle (EX_GeographicDescription). Pour EX_Extent comme pour CI_Citation, il s'agit d'un regroupement de classes pouvant être appelées par plusieurs attributs de la norme.
        
        
                        
.. _iso19139-gmd-extentgmd-MD-DataIdentification:
        
                        
**Etendue** (cf. `gmd:MD_DataIdentification <#iso19139-gmd-md-dataidentification>`_)
                  
- *Code :* gmd:extent
                
- *Description :* Information complémentaire sur les étendues spatiales et temporelles du jeu de données, incluant le polygone de délimitation et les dimensions verticales et temporelles
        
- *Information complémentaire :* Informations supplémentaires concernant l'extension spatiale et temporelle des données, incluant le polygone de délimitation, les altitudes et la durée de validité. Ces informations sont du type EX_Extent et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-extentgmd-DQ-Scope:
        
                        
**Etendue** (cf. `gmd:DQ_Scope <#iso19139-gmd-dq-scope>`_)
                  
- *Code :* gmd:extent
                
- *Description :* Information sur les domaines horizontaux, verticaux et temporels des données spécifiées par l'attribut scope (79) du domaine d'applicabilité
        
- *Information complémentaire :* Informations relatives à l'extension horizontale, verticale et temporelle des données conformément au domaine de validité défini. Ces informations sont du type EX_Extent et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-extentgmd-EX-TemporalExtent:
        
                        
**Etendue** (cf. `gmd:EX_TemporalExtent <#iso19139-gmd-ex-temporalextent>`_)
                  
- *Code :* gmd:extent
                
- *Description :* Date et temps pour le contenu du jeu de donnée
        
- *Information complémentaire :* Date et heure du domaine de validité du jeu de données (texte).
        
        
                        
.. _iso19139-gmd-EX-BoundingPolygon:
        
                        
**Etendue du polygone** 
                  
- *Code :* gmd:EX_BoundingPolygon
                
- *Description :* Type de données pour la description d'une surface fermée, exprimée par un ensemble de paires de coordonnées (x, y), qui englobe le jeu de données. Le premier et le dernier points sont identiques
        
- *Information complémentaire :* Type de données destiné à la description d'une surface fermée (polygone) définie par un ensemble de paires de coordonnées (x, y) et englobant le jeu de données. Le premier et le dernier point sont identiques. Des informations supplémentaires peuvent être trouvées sous EX_Extent et EX_GeographicExtent.
        
        
                        
.. _iso19139-gmd-spatialExtent:
        
                        
**Etendue spatiale** 
                  
- *Code :* gmd:spatialExtent
                
- *Description :* Information sur l'étendue spatiale de la composition spatio-temporelle de l'étendue
        
- *Information complémentaire :* Informations relatives à l'extension spatiale de la classe composée EX_SpatialTemporalExtent. Ces informations sont gérées dans la classe EX_Extent.
        
        
                        
.. _iso19139-gmd-EX-SpatialTemporalExtent:
        
                        
**Etendue spatio-temporelle** 
                  
- *Code :* gmd:EX_SpatialTemporalExtent
                
- *Description :* Type de données pour la description de l'étendue en respectant les limites date/heure et spatiales
        
- *Information complémentaire :* Type de données destiné à la description de l'extension dans le respect des limites spatiales et temporelles. Cette classe est une représentation de la classe EX_TemporalExtent. l'extension géographique est saisie en plus de l'indication de la validité temporelle.
        
        
                        
.. _iso19139-gmd-EX-TemporalExtent:
        
                        
**Etendue temporelle** 
                  
- *Code :* gmd:EX_TemporalExtent
                
- *Description :* Type de données pour la description de la période de temps couverte par le contenu du jeu de donnée
        
- *Information complémentaire :* La validité temporelle du jeu de données est définie dans cette classe. Cette classe connaît la représentation EX_SpatialTemporalExtent. Des informations supplémentaires peuvent être trouvées sous EX_Extent.
        
        
                        
.. _iso19139-gmd-EX-VerticalExtent:
        
                        
**Etendue verticale** 
                  
- *Code :* gmd:EX_VerticalExtent
                
- *Description :* Type de données pour la description de l'étendue verticale du jeu de données
        
- *Information complémentaire :* Type de données permettant la description de la troisième dimension (axe Z) avec ses altitudes minimale et maximale ainsi que l'unité de mesure utilisée. Vous trouverez des informations supplémentaires sous EX_Extent.
        
        
                        
.. _iso19139-gmd-sourceExtent:
        
                        
**Etendues des données sources** 
                  
- *Code :* gmd:sourceExtent
                
- *Description :* Information sur les étendues spatiales, verticales et temporelles des données sources
        
- *Information complémentaire :* Informations concernant l'extension spatiale, verticale et temporelle des données source. Ces informations sont du type EX_Extent et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-processStep:
        
                        
**Evénement** 
                  
- *Code :* gmd:processStep
                
- *Description :* Informations sur des événements dans la vie du jeu de données
        
- *Information complémentaire :* Informations concernant les étapes de traitement requises par la génération et l'actualisation d'un jeu de données avec indication des dates correspondantes. Les différentes étapes conduisant au jeu de données définitif sont mentionnées ici. Exemple : restitution photogrammétrique de clichés aériens, vérification et complètement sur le terrain puis établissement de la carte. Ces étapes peuvent être saisies dans la classe LI_ProcessStep (texte, date, source et personne en charge du traitement).
        
        
                        
.. _iso19139-gmd-LI-ProcessStep:
        
                        
**Evénement dans le processus** 
                  
- *Code :* gmd:LI_ProcessStep
                
- *Description :* Information sur un événement du processus de création ou de transformation, y inclus le processus de la maintenance des données
        
- *Information complémentaire :* Informations relatives à une étape du processus de création ou de transformation des données incluant leur processus d'actualisation. Les différentes étapes conduisant au jeu de données définitif sont mentionnées ici. Exemple : restitution photogrammétrique de clichés aériens, vérification et complètement sur le terrain puis établissement de la carte. Ces étapes peuvent être saisies via un texte, une date, une source et l'identification de la personne ayant réalisé le traitement. Des informations complémentaires peuvent être trouvées sous DQ_Dataquality.
        
        
                        
.. _iso19139-gmd-extentTypeCode:
        
                        
**Exclusion** 
                  
- *Code :* gmd:extentTypeCode
                
- *Description :* Indication si le polygone de délimitation recouvre une surface recouverte par des données ou une surface ne comportant pas de données
        
- *Information complémentaire :* Indication du fait de savoir si le polygone de délimitation définit une zone recouverte ou exempte de données du jeu de données.
        
        
                        
.. _iso19139-gmd-explanation:
        
                        
**Explication** 
                  
- *Code :* gmd:explanation
                
- *Description :* Explication de la signification de conformance pour ces résultats
        
- *Information complémentaire :* Explication de la signification de la concordance pour ce résultat.
        
        
                        
.. _iso19139-gmd-userNote:
        
                        
**Explications sur les restrictions** 
                  
- *Code :* gmd:userNote
                
- *Description :* Explications sur l'application des contraintes légales, ou d'autres restrictions et conditions préalables légales, pour obtenir et utiliser les ressources où de métadonnées
        
- *Information complémentaire :* Explication plus détaillée de la restriction.
        
        
                        
.. _iso19139-gml-extent:
        
                        
**Extension** 
                  
- *Code :* gml:extent
                
- *Description :* Extension
        
        
        
                        
.. _iso19139-srv-extent:
        
                        
**Extension** 
                  
- *Code :* srv:extent
                
- *Description :* Extension géographique/temporelle du service
        
        
        
                        
.. _iso19139-factor:
        
                        
**Facteur** 
                  
- *Code :* factor
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-scaleFactor:
        
                        
**Facteur d'échelle** 
                  
- *Code :* gmd:scaleFactor
                
- *Description :* Facteur d'échelle appliqué à la valeur de la cellule
        
- *Information complémentaire :* Facteur d'échelle appliqué à la valeur de la cellule.
        
        
                        
.. _iso19139-gmd-featureAttribute:
        
                        
**FeatureAttribute** 
                  
- *Code :* gmd:featureAttribute
                
- *Description :* FeatureAttribute
        
        
        
                        
.. _iso19139-gmd-featureType:
        
                        
**FeatureType** 
                  
- *Code :* gmd:featureType
                
- *Description :* FeatureType
        
        
        
                        
.. _iso19139-gmd-softwareDevelopmentFile:
        
                        
**Fichier de développement** 
                  
- *Code :* gmd:softwareDevelopmentFile
                
- *Description :* Schéma d'application entièrement donné dans un fichier de développement software
        
- *Information complémentaire :* Schéma d'application complet sous forme de fichier de développement logiciel.
        
        
                        
.. _iso19139-gmd-graphicsFile:
        
                        
**Fichier graphique** 
                  
- *Code :* gmd:graphicsFile
                
- *Description :* Schéma d'application entièrement donné dans un graphique
        
- *Information complémentaire :* Représentation graphique du schéma d'application complet.
        
        
                        
.. _iso19139-gml-end:
        
                        
**Fin** 
                  
- *Code :* gml:end
                
- *Description :* Fin
        
        
        
                        
.. _iso19139-gmd-function:
        
                        
**Fonction** 
                  
- *Code :* gmd:function
                
- *Description :* Code pour une fonction accomplie par la ressource on-line
        
- *Information complémentaire :* Rôle de la source en ligne, sélection dans la liste suivante : téléchargement, information, accès hors ligne, commande ou recherche.
        
        
                        
.. _iso19139-gmd-CI-RoleCode:
        
                        
**Fonction** 
                  
- *Code :* gmd:CI_RoleCode
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-MD-Format:
        
                        
**Format** 
                  
- *Code :* gmd:MD_Format
                
- *Description :* Classe avec la description du format informatique avec lequel la représentation du jeu de donnée peut être enregistrée et transférée, sous la forme d'un enregistrement de données, d'un fichier, d'un message, d'un support de stockage ou d'un canal de transmission
        
- *Information complémentaire :* Classe contenant la description du format de fichier dans lequel le jeu de données peut être stocké et transféré sur un support de données, dans un fichier, via un courrier électronique, un périphérique de stockage ou un canal de transmission.
        
        
                        
.. _iso19139-gmd-distributionFormat:
        
                        
**Format de distribution** 
                  
- *Code :* gmd:distributionFormat
                
- *Description :* Description du format de distribution
        
- *Information complémentaire :* Description du format de distribution. Ces informations sont gérées dans la classe MD_Format.
        
        
                        
.. _iso19139-gmd-formatDistributor:
        
                        
**Format de distribution** 
                  
- *Code :* gmd:formatDistributor
                
- *Description :* Informations sur le format de distribution
        
- *Information complémentaire :* Informations relatives au distributeur, aux coûts et aux modalités de commande. Elles sont gérées dans la classe MD_Distributor.
        
        
                        
.. _iso19139-gmd-fileType:
        
                        
**Format de l'illustration** 
                  
- *Code :* gmd:fileType
                
- *Description :* Format dans lequel l'illustration est enregistrée. Exemple : CGM, EPS, GIF, JPEG, PBM, PS, TIFF, XWD
        
- *Information complémentaire :* Format dans lequel la représentation est enregistrée, ex : CGM, EPS, GIF, JPEG, PBM, PS, TIFF, XWD
        
        
                        
.. _iso19139-gmd-resourceFormat:
        
                        
**Format de la ressource** 
                  
- *Code :* gmd:resourceFormat
                
- *Description :* Description du format de la ressource
        
- *Information complémentaire :* Description du format de la ressource. Le nom et la version du format sont entrés ici. Ces informations sont gérées dans la classe MD_Format.
        
        
                        
.. _iso19139-gmd-distributorFormat:
        
                        
**Format du distributeur** 
                  
- *Code :* gmd:distributorFormat
                
- *Description :* Informations sur le format utilisé par le distributeur
        
- *Information complémentaire :* Informations relatives au format utilisé par le distributeur (nom et version du format, par exemple TIFF, version 6.0). Ces informations sont gérées dans la classe MD_Format.
        
        
                        
.. _iso19139-gmd-softwareDevelopmentFileFormat:
        
                        
**Format du fichier** 
                  
- *Code :* gmd:softwareDevelopmentFileFormat
                
- *Description :* Format, dépendant du software, utilisé pour le fichier (dépendant du software) du schéma d'application
        
- *Information complémentaire :* Format lié à un logiciel et utilisé pour la description du schéma d'application dans un fichier de développement logiciel.
        
        
                        
.. _iso19139-gmd-mediumFormat:
        
                        
**Format du média** 
                  
- *Code :* gmd:mediumFormat
                
- *Description :* Méthode utilisée pour écrire dans le média
        
- *Information complémentaire :* Format d'écriture utilisé pour le support considéré. La sélection peut s'effectuer parmi les formats suivants : cpio, tar, highSierra, iso9660, iso9660Rockridge, iso9660AppleHFS.
        
        
                        
.. _iso19139-gmd-presentationForm:
        
                        
**Forme de la présentation** 
                  
- *Code :* gmd:presentationForm
                
- *Description :* Mode dans lequel la ressource est représentée
        
- *Information complémentaire :* Forme sous laquelle la source est disponible. Exemple : document numérique ou analogique, image, carte, modèle, etc. (sélection dans une liste).
        
        
                        
.. _iso19139-gmd-maintenanceAndUpdateFrequency:
        
                        
**Fréquence de mise à jour** 
                  
- *Code :* gmd:maintenanceAndUpdateFrequency
                
- *Description :* Fréquence avec laquelle des changements et des ajouts sont fait à la ressource après que la ressource initiale ait été complétée
        
- *Information complémentaire :* Fréquence à laquelle des changements et des ajouts sont apportés à la ressource. La valeur concernée est à sélectionner dans la liste suivante : en permanence, quotidienne, hebdomadaire, bimensuelle, mensuelle, trimestrielle, semestrielle, annuelle, au besoin, irrégulière, non prévue, inconnue, définie par l'utilisateur.
        
        
                        
.. _iso19139-gmd-MD-BrowseGraphic:
        
                        
**Graphique** 
                  
- *Code :* gmd:MD_BrowseGraphic
                
- *Description :* Classe pour la description d'un graphique qui contient une illustration du jeu de données
        
- *Information complémentaire :* Classe destinée à la description d'un graphique contenant une représentation du jeu de données (une légende du graphique devrait être incluse). Il s'agit généralement d'un jeu de données d'exemple, valant pour des jeux de données de même nature (des cartes nationales par exemple).
        
        
                        
.. _iso19139-gmd-textGroup:
        
                        
**Groupe** 
                  
- *Code :* gmd:textGroup
                
- *Description :* Informations sur les éléments nécessaire pour utiliser le multilinguisme
        
- *Information complémentaire :* Informations relatives à la définition de l'élément de texte en différentes langues (langue, contenu, etc.). Ces informations sont du type PT_Group et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-LI-Lineage:
        
                        
**Généalogie, provenance** 
                  
- *Code :* gmd:LI_Lineage
                
- *Description :* Classe contenant l'information sur les événements ou les données sources utilisées pour la construction des données
        
- *Information complémentaire :* Classe contenant des informations relatives à la provenance et au processus de génération du jeu de données. 
      Ces indications font partie intégrante des informations sur la qualité. 
      Des informations complémentaires peuvent être trouvées sous DQ_Dataquality.
      
      
      L’historique d’une série de données et son cycle de vie, depuis sa collecte et son acquisition jusqu’à sa
      forme actuelle, en passant par sa compilation et sa dérivation, conformément à la norme EN ISO 19101.      
      
      
      Example : X% lampadaires issus de restitution photogrammétrique, X% lampadaires issus levers
        
        
                        
.. _iso19139-gmd-statement:
        
                        
**Généralités sur la provenance** 
                  
- *Code :* gmd:statement
                
- *Description :* Explication générale sur les connaissances du producteur de données au sujet de la filiation du jeu de données
        
- *Information complémentaire :* Explication générale de la filiation du jeu de données fournie par le créateur des données. Dans le cas de données de la MO, il peut s'agir de l'indication des bases (MO93/MD93). Pour des données tramées, il peut s'agir de l'indication du fait qu''elles dérivent de prises de vues aériennes.
        
        
                        
.. _iso19139-gmd-has:
        
                        
**Has** 
                  
- *Code :* gmd:has
                
- *Description :* Has
        
        
        
                        
.. _iso19139-gmd-hoursOfService:
        
                        
**Heures de service** 
                  
- *Code :* gmd:hoursOfService
                
- *Description :* Période de temps (incluant aussi le fuseau horaire) pendant laquelle la personne ou l'organisation responsable peut être contactée
        
- *Information complémentaire :* Heures d'ouverture, indications fournies sous forme de texte libre, par exemple : "08h00 - 11h45h et 13h30 - 17h00" ou "De 08h00 à 11h45 et de 13h30 à 17h00"
        
        
                        
.. _iso19139-gmd-ISBN:
        
                        
**ISBN** 
                  
- *Code :* gmd:ISBN
                
- *Description :* Numéro international normalisé d'un livre (ISBN)
        
        
        
                        
.. _iso19139-gmd-ISSN:
        
                        
**ISSN** 
                  
- *Code :* gmd:ISSN
                
- *Description :* Numéro international normalisé d'une publication en série (ISSN)
        
- *Information complémentaire :* Numéro international normalisé d'une série de publications (ISSN)
        
        
                        
.. _iso19139-gmd-MD-Identifier:
        
                        
**Identifiant** 
                  
- *Code :* gmd:MD_Identifier
                
- *Description :* Classe contenant une valeur codée unique à l'intérieur d'un domaine de valeurs
        
- *Information complémentaire :* Cette classe contient un identifiant unique au sein d'un espace nominal. Il peut s'agir d'une description géographique (exemple : une liste de communes) ou une indication de sources (exemple : désignation d'un thésaurus). Dans la représentation RS_Identifier, l'identifiant est spécifiquement utilisé pour des systèmes de référence. MD_Identifier avec la représentation RS_Identifier peut être appelé par plusieurs attributs de la norme.
        
        
                        
.. _iso19139-gml-id:
        
                        
**Identifiant** 
                  
- *Code :* gml:id
                
- *Description :* 
        
        
        
                        
.. _iso19139-id:
        
                        
**Identifiant** 
                  
- *Code :* id
                
- *Description :* 
        
        
        
                        
.. _iso19139-srv-identifier:
        
                        
**Identifiant** 
                  
- *Code :* srv:identifier
                
- *Description :* Identifiant de la resource sur laquelle l’opération porte
        
        
        
                        
.. _iso19139-srv-operatesOn:
        
                        
**Identifiant de la donnée associée** 
                  
- *Code :* srv:operatesOn
                
- *Description :* Information sur la ou les données associées au service
        
        
        
                        
.. _iso19139-gmd-fileIdentifier:
        
                        
**Identifiant du fichier** 
                  
- *Code :* gmd:fileIdentifier
                
- *Description :* Identifiant unique pour ce fichier de métadonnées
        
- *Information complémentaire :* Identifiant unique pour ce fichier de métadonnées. Il correspond à un et un seul nom de fichier.
        
        
                        
.. _iso19139-gmd-parentIdentifier:
        
                        
**Identifiant du parent** 
                  
- *Code :* gmd:parentIdentifier
                
- *Description :* Identifiant du fichier de métadonnées parent.
        
- *Information complémentaire :* Nom unique du fichier de métadonnées parent ou origine. Il peut s'agir d'un modèle prédéfini ou de données de rang supérieur (dans le cas par exemple d'une carte nationale au 1:25''000, le parent peut être la série de toutes les cartes au 1:25''000).
        
        
                        
.. _iso19139-gmd-RS-Identifier:
        
                        
**Identifiant du système de référence** 
                  
- *Code :* gmd:RS_Identifier
                
- *Description :* Classe pour l'identifiant utilisé pour les systèmes de référence
        
- *Information complémentaire :* Classe réservée aux identifiants de systèmes de référence. Cette classe est une représentation de MD_Identifier pour l'identification d'un système de référence par des attributs supplémentaires. Cf. également sous MD_Identifier.
        
        
                        
.. _iso19139-gmd-geographicIdentifier:
        
                        
**Identifiant géographique** 
                  
- *Code :* gmd:geographicIdentifier
                
- *Description :* Identifiant utilisé pour représenter une surface géographique
        
- *Information complémentaire :* Identifiant servant à la définition sans équivoque d'une zone géographique. Le code d'identification (MD_Identifier.code) correspond par exemple au numéro communal à 4 chiffres de l'OFS s'il s'agit du territoire d'une commune. Ces informations sont du type MD_Identifier et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-identifier:
        
                        
**Identificateur de provenance** 
                  
- *Code :* gmd:identifier
                
- *Description :* Identificateur de l'indication de provenance
        
- *Information complémentaire :* Identificateur de l'indication de provenance. La classe MD_Identifier permet d'affecter une indication de provenance à un registre existant.
        
        
                        
.. _iso19139-gmd-aggregateDataSetIdentifier:
        
                        
**Identificateur du jeu de données aggrégé** 
                  
- *Code :* gmd:aggregateDataSetIdentifier
                
- *Description :* Informations d'identification sur le jeu de données rassemblé
        
- *Information complémentaire :* Informations d'identification des jeux de données de rang inférieur. Identification sans équivoque d'un objet au sein d'un espace nominal et indication du service responsable de ce nom et de son actualisation. Ces informations sont du type MD_Identifier et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-MD-DataIdentification:
        
                        
**Identification des données** 
                  
- *Code :* gmd:MD_DataIdentification
                
- *Description :* Classe avec l'information utile pour identifier un jeu de données
        
- *Information complémentaire :* Classe contenant des informations de base utilisées pour l'identification sans équivoque du ou des jeux de données. Il s'agit de la description du jeu de données concret. La classe MD_DataIdentification est la représentation de MD_Identification pour les données. Elle intègre des informations relatives à la caractérisation spatiale et temporelle des données, au jeu de caractères et à la langue utilisés, de même que d'autres informations descriptives. Une extension spatiale minimale des données est à indiquer par l'intermédiaire de l'option "geographicBox" (rectangle de délimitation géographique), de l'option "geographicDescription" (description textuelle de l'extension) ou des deux simultanément. Il est en outre possible de restreindre l'extension par le biais de l'attribut "extent", aussi bien au niveau spatial (via un polygone) que temporel. La norme prévoit une liste internationale de 19 thèmes (MD_TopicCategoryCode) pour la classification thématique des données, gérée par l'intermédiaire de l'attribut "topicCategory". Une recherche standardisée par thèmes est de la sorte possible au plan international.
        
        
                        
.. _iso19139-gmd-MD-ServiceIdentification:
        
                        
**Identification des services** 
                  
- *Code :* gmd:MD_ServiceIdentification
                
- *Description :* Classe pour l'identification des prestations, disponibles auprès un fournisseur de services, via un ensemble d'interfaces définissant un comportement (cf. ISO 19119 pour obtenir plus d'information).
        
- *Information complémentaire :* Classe destinée à l'identification des services qu''un prestataire propose à l'utilisateur et dont le contenu et l'étendue sont définis par un ensemble d'informations. Dans cette classe, les domaines dans lesquels le prestataire met ses services à la disposition de l'utilisateur peuvent être saisis. l'attribut de type de service (serviceType) définit le nom du service géomatique proposé et l'attribut des propriétés du type de service (ServiceTypProperty) permet la description des caractéristiques qui lui sont associées. Ces deux attributs font appel à l'un des types de données définis dans la norme 19118. On suppose que les services géomatiques concernés sont standardisés et répertoriés au sein d'une liste.
        
        
                        
.. _iso19139-srv-SV-ServiceIdentification:
        
                        
**Identification du service (ISO 19119)** 
                  
- *Code :* srv:SV_ServiceIdentification
                
- *Description :* Identification du service (ISO 19119)
        
        
        
                        
.. _iso19139-gmd-measureIdentification:
        
                        
**Identification du test** 
                  
- *Code :* gmd:measureIdentification
                
- *Description :* Code identifiant une procédure standard enregistrée
        
- *Information complémentaire :* Identification d'une procédure normalisée enregistrée
        
        
                        
.. _iso19139-gmd-includedWithDataset:
        
                        
**Inclus dans le jeu de données** 
                  
- *Code :* gmd:includedWithDataset
                
- *Description :* Indications si oui ou non le catalogue d'objets est inclus dans le jeu de données
        
- *Information complémentaire :* Indication de la présence ou de l'absence du catalogue d'objets dans le jeu de données.
        
        
                        
.. _iso19139-gmd-administrativeArea:
        
                        
**Incorporation administrative** 
                  
- *Code :* gmd:administrativeArea
                
- *Description :* Canton ou département de l'emplacement
        
- *Information complémentaire :* Canton
        
        
                        
.. _iso19139-gmd-triangulationIndicator:
        
                        
**Indicateur de la triangulation** 
                  
- *Code :* gmd:triangulationIndicator
                
- *Description :* Indication si oui ou non la triangulation a été effectuée sur l'image
        
- *Information complémentaire :* Indication de l'éventuelle exécution d'une triangulation sur l'image.
        
        
                        
.. _iso19139-gmd-issueIdentification:
        
                        
**Information d'édition** 
                  
- *Code :* gmd:issueIdentification
                
- *Description :* Information identifiant l'édition des séries
        
- *Information complémentaire :* Informations concernant l'édition ou le numéro d'édition de la série.
        
        
                        
.. _iso19139-gmd-identificationInfo:
        
                        
**Information de l'identification** 
                  
- *Code :* gmd:identificationInfo
                
- *Description :* Informations de base sur les ressources concernées par les métadonnées
        
- *Information complémentaire :* Informations de base concernant la ressource (voire les ressources) ou le jeu de données auquel se rapportent les métadonnées. Ces informations sont gérées dans la classe MD_IdentificationInformation.
        
        
                        
.. _iso19139-gmd-MD-MaintenanceInformation:
        
                        
**Information de maintenance** 
                  
- *Code :* gmd:MD_MaintenanceInformation
                
- *Description :* Classe sur la raison, l'étendue et la fréquence des mises à jour.
        
- *Information complémentaire :* Les informations concernant l'étendue, la fréquence et la date de mise à jour des données sont contenues dans la classe MD_MaintenanceInformation. Cette classe recèle des attributs renseignant sur la fréquence et l'étendue de la mise à jour et de la réactualisation des données du jeu. Seule l'indication de la fréquence est impérative et doit être sélectionnée dans la liste MD_MaintenanceFrequencyCode. l'étendue de la mise à jour, les attributs qu''elle concerne et les descriptions associées sont des informations qu''il est possible d'indiquer via les attributs "updateScope" et "updateScopeDescription". Il n''est pas prévu d'indiquer l'extension spatiale de la mise à jour. Si seules des parties d'un jeu de données sont mises à jour ou si toutes ses parties ne sont pas mises à jour simultanément, alors les parties concernées par la description de la mise à jour peuvent être précisées via "+updateScopeDescription" dans la classe MD_ScopeDescription.
        
        
                        
.. _iso19139-gmd-CI-Citation:
        
                        
**Information de référence** 
                  
- *Code :* gmd:CI_Citation
                
- *Description :* Type de données pour la description standardisée des informations de références de la ressource
        
- *Information complémentaire :* Type de données destiné à une description unifiée des sources (renvoi standardisé aux sources). Ce type de données permet une indication standardisée des sources (CI_Citation). Il contient également des types de données pour la description des services en charge de données et de métadonnées (CI_ResponsibleParty). La description du service compétent peut intégrer le nom de l'organisation comme celui de la personne responsable au sein de cette organisation. Il est également impératif de décrire sa fonction (son rôle). CI_Contact recèle des informations sur le mode de communication avec le service compétent. CI_Citation contient les principaux attributs permettant l'identification d'un jeu de données ou d'une source. Parmi ceux-ci on peut citer le titre, sa forme abrégée, l'édition ou la date. Le type de données CI_Citation est alors appelé lorsque l'identification complète d'une information supplémentaire d'une source de données est à fournir. Des renvois sont effectués à partir de ce type de données vers chacun des autres types de données du groupe ?Citation?. CI_Citation est un regroupement de classes pouvant être appelées par plusieurs attributs de la norme.
        
        
                        
.. _iso19139-gmd-MD-ExtendedElementInformation:
        
                        
**Information sur l'extension d'un élément** 
                  
- *Code :* gmd:MD_ExtendedElementInformation
                
- *Description :* Classe pour des éléments de métadonnées nouveaux, qu''on ne trouve pas dans ISO 19115, utilisés pour décrire des données géographiques
        
- *Information complémentaire :* Classe réservée à de nouveaux éléments de métadonnées requis pour la description des données géographiques mais absents de la norme ISO19115.
        
        
                        
.. _iso19139-gmd-metadataExtensionInfo:
        
                        
**Information sur l'extension des métadonnées** 
                  
- *Code :* gmd:metadataExtensionInfo
                
- *Description :* Informations décrivant l'extension des métadonnées
        
- *Information complémentaire :* Informations décrivant des extensions de métadonnées
        
        
                        
.. _iso19139-gmd-transformationDimensionMapping:
        
                        
**Information sur l'étendue géographique** 
                  
- *Code :* gmd:transformationDimensionMapping
                
- *Description :* Information sur l'étendue géographique définie par l'étendue du raster
        
- *Information complémentaire :* Règle de représentation spatiale de la trame.
        
        
                        
.. _iso19139-gmd-spatialRepresentationInfo:
        
                        
**Information sur la représentation spatiale** 
                  
- *Code :* gmd:spatialRepresentationInfo
                
- *Description :* Représentation digitale de l'information spatiale dans le jeu de données
        
- *Information complémentaire :* Informations sur la manière dont les représentations spatiales sont définies. Une distinction est étable entre les données vectorielles et les données tramées. Dans le cas de données vectorielles, les indications concernent le type géométrique, la topologie, etc., tandis qu''elles se rapportent au nombre de pixels, à l'ordre de succession des axes, aux paramètres de géoréférencement, etc. dans le cas de données tramées. Ces informations sont gérées dans la classe MD_SpatialRepresentation.
        
        
                        
.. _iso19139-gmd-contentInfo:
        
                        
**Information sur le contenu** 
                  
- *Code :* gmd:contentInfo
                
- *Description :* Informations sur le catalogue d'objet et sur les descriptions de la couverture et des charactéristiques raster
        
- *Information complémentaire :* Description du contenu du jeu de données. Renvoi au catalogue d'objets, au modèle de données ou à la description des données. Le contenu de ces catalogues et de ces descriptions ne fait toutefois pas partie des métadonnées. Ces informations sont gérées dans la classe MD_ContentInformation.
        
        
                        
.. _iso19139-gmd-MD-ApplicationSchemaInformation:
        
                        
**Information sur le schéma d'application** 
                  
- *Code :* gmd:MD_ApplicationSchemaInformation
                
- *Description :* Classe avec l'information sur le schéma d'application utilisé pour construire le jeu de donnée
        
- *Information complémentaire :* Dans MD_ApplicationSchemaInformation, il est possible d'indiquer les conditions marginales sous lesquelles les données peuvent être utilisées pour une application spécifique. Exemple : supposons qu''un jeu de données relatif à des clairières ait été créé par des forestiers ; la description comprise dans MD_ApplicationSchemaInformation contiendrait alors des informations sur la manière dont les clairières sont à représenter en sylviculture comme sur les aspects sous lesquels leurs limites sont à définir et à interpréter. Si le même jeu de données avait été généré par des botanistes, les clairières auraient été considérées sous des aspects bien différents. d'autres signes conventionnels auraient par ailleurs été utilisés. De telles informations peuvent exercer une forte influence sur l'utilisation ultérieure comme sur le champ d'application. Les indications entrées sous MD_ApplicationSchemaInformation ne sont associées à aucune restriction d'utilisation puisque c''est déjà le cas sous MD_Constraints. Les conditions marginales sous lesquelles le jeu de données a été saisi puis est à traiter dans l'application correspondante sont entrées ici. Il peut même s'agir de plusieurs options différentes. La description d'un schéma d'application doit au moins comprendre le nom, le langage de modélisation et la langue utilisée. Le fait de savoir si le schéma de données est transmis sous forme d'un graphique, d'un fichier ASCII ou d'un fichier spécifique à un environnement logiciel donné est quant à lui facultatif. Si les noms des objets spatiaux définis par l'intermédiaire du schéma d'application sont à indiquer, alors la classe MD_SpatialAttributeSupplement renvoyant à la classe MD_FeatureTypeList peut servir à cela.
        
        
                        
.. _iso19139-gmd-referenceSystemInfo:
        
                        
**Information sur le système de référence** 
                  
- *Code :* gmd:referenceSystemInfo
                
- *Description :* Description des références spatiale et temporelle utilisées dans le jeu de données
        
- *Information complémentaire :* Description des systèmes de référence spatiale et temporelle utilisés dans le jeu de données. Ces informations sont gérées dans la classe MD_ReferenceSystem.
        
        
                        
.. _iso19139-gmd-aggregationInfo:
        
                        
**Information sur les agrégations** 
                  
- *Code :* gmd:aggregationInfo
                
- *Description :* Met à dispositionles les informations sur le jeu de données rassemblé
        
- *Information complémentaire :* Informations concernant le jeu de données de rang supérieur et les relations qu''il entretient avec le jeu de données décrit. Ces informations sont du type MD_AggregateInformation et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-contactInfo:
        
                        
**Informations de contact** 
                  
- *Code :* gmd:contactInfo
                
- *Description :* Heures de service du service responsable
        
- *Information complémentaire :* Adresse du service responsable. Ces informations sont du type CI_Contact et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-citationgmd-MD-Identification:
        
                        
**Informations de référence**
                  
- *Code :* gmd:citation
                
- *Description :* Informations de référence sur les ressources
        
- *Information complémentaire :* Indication des sources du jeu de données décrit. Le nom ou le titre du fichier du jeu de données de même qu''une date du type adéquat (création, publication, traitement) sont gérés ici. Cet attribut est du type CI_Citation et est géré dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-supplementalInformation:
        
                        
**Informations supplémentaires** 
                  
- *Code :* gmd:supplementalInformation
                
- *Description :* Toute autre information descriptive sur le jeu de données
        
- *Information complémentaire :* Informations descriptives supplémentaires relatives au jeu de données présentant un intérêt général ou plus spécifiquement lié à l'utilisation, au traitement, etc. du jeu de données.
        
        
                        
.. _iso19139-gmd-distributionInfo:
        
                        
**Informations sur la distribution** 
                  
- *Code :* gmd:distributionInfo
                
- *Description :* Informations sur le distributeur et sur la façon d'acquérir les ressources
        
- *Information complémentaire :* Informations relatives au distributeur et au mode d'acquisition de la ou des ressources. Indications concernant le lieu et la forme d'obtention des données. Ces informations sont gérées dans la classe MD_Distribution.
        
        
                        
.. _iso19139-gmd-dataQualityInfo:
        
                        
**Informations sur la qualité des données** 
                  
- *Code :* gmd:dataQualityInfo
                
- *Description :* Estimation de la qualité des ressources
        
- *Information complémentaire :* Estimation de la qualité du jeu de données. Ces informations sont gérées dans la classe DQ_DataQuality.
        
        
                        
.. _iso19139-gmd-portrayalCatalogueInfo:
        
                        
**Informations sur la réprésentation** 
                  
- *Code :* gmd:portrayalCatalogueInfo
                
- *Description :* Informations sur le catalogue de règles concernant la représentation des ressources
        
- *Information complémentaire :* Informations concernant le catalogue de règles établi pour la représentation de ressources.
        
        
                        
.. _iso19139-gmd-applicationSchemaInfo:
        
                        
**Informations sur le schéma conceptuel** 
                  
- *Code :* gmd:applicationSchemaInfo
                
- *Description :* Informations sur le schéma conceptuel du jeu de données
        
- *Information complémentaire :* Informations relatives au schéma conceptuel du jeu de données
        
        
                        
.. _iso19139-gmd-MD-AggregateInformation:
        
                        
**Informations sur les aggregations** 
                  
- *Code :* gmd:MD_AggregateInformation
                
- *Description :* Informations du jeu de données rassemblé
        
- *Information complémentaire :* Informations relatives aux jeux de données de rang inférieur telles que le nom, l'identification, le genre de relation, le motif de la saisie. Exemple : lorsqu''un lot de la MO est décrit, il est renvoyé ici aux couches existantes de la MO pour ce lot (biens-fonds, couverture du sol, etc.). Ces indications décrivent la hiérarchie de rang inférieur, au contraire de l'attribut MD_Metadata.parentIdentifier décrivant lui le jeu de données de rang supérieur.
        
        
                        
.. _iso19139-gmd-MD-MetadataExtensionInformation:
        
                        
**Informations sur les extensions de métadonnées** 
                  
- *Code :* gmd:MD_MetadataExtensionInformation
                
- *Description :* Classe pour les informations décrivant les extensions du modèle de métadonnées
        
- *Information complémentaire :* Classe contenant des informations décrivant les extensions des métadonnées.
        
        
                        
.. _iso19139-gml-timePosition:
        
                        
**Instant** 
                  
- *Code :* gml:timePosition
                
- *Description :* Instant
        
        
        
                        
.. _iso19139-gml-TimeInstant:
        
                        
**Instant** 
                  
- *Code :* gml:TimeInstant
                
- *Description :* Date ponctuelle
        
        
        
                        
.. _iso19139-gmd-orderingInstructions:
        
                        
**Instructions de commande** 
                  
- *Code :* gmd:orderingInstructions
                
- *Description :* Instructions générales, conditions et services offerts par le distributeur
        
- *Information complémentaire :* Instructions générales, délais et services offerts par le distributeur
        
        
                        
.. _iso19139-gmd-contactInstructions:
        
                        
**Instructions pour le contact** 
                  
- *Code :* gmd:contactInstructions
                
- *Description :* Instructions supplémentaires sur quand et comment contacter la personne ou l'organisation responsable
        
- *Information complémentaire :* Informations supplémentaires pour la prise de contact.
        
        
                        
.. _iso19139-gml-timeInterval:
        
                        
**Intervalle de temps** 
                  
- *Code :* gml:timeInterval
                
- *Description :* Intervalle de temps
        
        
        
                        
.. _iso19139-gmd-characterSetgmd-MD-Metadata:
        
                        
**Jeu de caractère** (cf. `gmd:MD_Metadata <#iso19139-gmd-md-metadata>`_)
                  
- *Code :* gmd:characterSet
                
- *Description :* Nom complet du standard de code de caractères utilisé pour le jeu de métadonnées
        
- *Information complémentaire :* Nom complet du code de caractères normalisé utilisé pour le fichier de métadonnées. Le paramètre par défaut est "utf8". Les fichiers de texte contiennent normalement des valeurs d'octets représentant un sous-ensemble de valeurs de caractères via un codage (8859_1, ISO Latin-1), un format de transfert (Unicode-Transfer-Format UTF8) ou tout autre moyen.
        
        
                        
.. _iso19139-gmd-characterSetgmd-MD-DataIdentification:
        
                        
**Jeu de caractère** (cf. `gmd:MD_DataIdentification <#iso19139-gmd-md-dataidentification>`_)
                  
- *Code :* gmd:characterSet
                
- *Description :* Nom entier du standard de code de caractères utilisé pour le jeu de données
        
- *Information complémentaire :* Nom complet du code de caractères normalisé utilisé pour le fichier de métadonnées. Le paramètre par défaut est "utf8". Les fichiers de texte contiennent normalement des valeurs d'octets représentant un sous-ensemble de valeurs de caractères via un codage (8859_1, ISO Latin-1), un format de transfert (Unicode-Transfer-Format UTF8) ou tout autre moyen.
        
        
                        
.. _iso19139-gmd-characterSet:
        
                        
**Jeu de caractère** 
                  
- *Code :* gmd:characterSet
                
- *Description :* Nom complet du standard de code de caractères utilisé pour le jeu de métadonnées
        
- *Information complémentaire :* Nom complet du code de caractères normalisé utilisé pour le fichier de métadonnées. Le paramètre par défaut est "utf8". Les fichiers de texte contiennent normalement des valeurs d'octets représentant un sous-ensemble de valeurs de caractères via un codage (8859_1, ISO Latin-1), un format de transfert (Unicode-Transfer-Format UTF8) ou tout autre moyen.
        
        
                        
.. _iso19139-gmd-dataset:
        
                        
**Jeu de données** 
                  
- *Code :* gmd:dataset
                
- *Description :* Jeu de données sur lequel l'information s'applique
        
- *Information complémentaire :* Jeu de données auquel les informations se rapportent.
        
        
                        
.. _iso19139-gmd-constraintLanguage:
        
                        
**Langage utilisée** 
                  
- *Code :* gmd:constraintLanguage
                
- *Description :* Langage formelle utilisée dans le schéma d'application
        
- *Information complémentaire :* Langage formel utilisé pour le schéma d'application.
        
        
                        
.. _iso19139-gmd-languageCode:
        
                        
**LanguageCode** 
                  
- *Code :* gmd:languageCode
                
- *Description :* LanguageCode
        
        
        
                        
.. _iso19139-gmd-languagegmd-MD-Metadata:
        
                        
**Langue** (cf. `gmd:MD_Metadata <#iso19139-gmd-md-metadata>`_)
                  
- *Code :* gmd:language
                
- *Description :* Langue utilisée pour documenter les métadonnées
        
- *Information complémentaire :* Langue utilisée pour la documentation des métadonnées. La sélection s'opère dans la liste des langues ISO. Exemple : "fr" pour le français, "de" pour l'allemand, "en" pour l'anglais, "it" pour l'italien, "rm" pour le romanche, ...
        
        
                        
.. _iso19139-gmd-languagegmd-MD-DataIdentification:
        
                        
**Langue** (cf. `gmd:MD_DataIdentification <#iso19139-gmd-md-dataidentification>`_)
                  
- *Code :* gmd:language
                
- *Description :* Langue utilisée pour le jeu de données
        
- *Information complémentaire :* Langue utilisée pour la documentation des données. La sélection s'opère dans la liste des langues ISO. Exemple : "fr" pour le français, "de" pour l'allemand, "en" pour l'anglais, "it" pour l'italien, "rm" pour le romanche, ...
        
        
                        
.. _iso19139-gmd-languagegmd-MD-FeatureCatalogueDescription:
        
                        
**Langue** (cf. `gmd:MD_FeatureCatalogueDescription <#iso19139-gmd-md-featurecataloguedescription>`_)
                  
- *Code :* gmd:language
                
- *Description :* Langues utilisées dans le catalogue
        
- *Information complémentaire :* Langue utilisée dans le catalogue d'objets. La sélection s'opère dans la liste des langues ISO. Exemple : "fr" pour le français, "de" pour l'allemand, "en" pour l'anglais, "it" pour l'italien, "rm" pour le romanche, ...
        
        
                        
.. _iso19139-gmd-PT-Locale:
        
                        
**Langue** 
                  
- *Code :* gmd:PT_Locale
                
- *Description :* Langue
        
        
        
                        
.. _iso19139-gmd-schemaLanguage:
        
                        
**Langue du schéma** 
                  
- *Code :* gmd:schemaLanguage
                
- *Description :* Identification de la langue de schéma utilisée
        
- *Information complémentaire :* Identification de la langue utilisée pour le schéma d'application.
        
        
                        
.. _iso19139-gmx-Anchor:
        
                        
**Libellé** 
                  
- *Code :* gmx:Anchor
                
- *Description :* description
        
        
        
                        
.. _iso19139-gml-LineString:
        
                        
**Ligne** 
                  
- *Code :* gml:LineString
                
- *Description :* Ligne
        
        
        
                        
.. _iso19139-gmd-useLimitation:
        
                        
**Limitation d'utilisation** 
                  
- *Code :* gmd:useLimitation
                
- *Description :* Limitation d'utilisation de la ressource où de métadonnées. Exemple: "ne pas utiliser pour la navigation"
        
- *Information complémentaire :* Restriction d'utilisation de la ressource ou des métadonnées. Exemple: "ne pas utiliser pour la navigation"
        
        
                        
.. _iso19139-gmd-userDeterminedLimitations:
        
                        
**Limitation des applications** 
                  
- *Code :* gmd:userDeterminedLimitations
                
- *Description :* Applications déterminées par l'utilisateur pour lesquelles les ressources et/ou série de ressource ne sont pas adéquates
        
- *Information complémentaire :* Applications indiquées par l'utilisateur pour lesquelles la ressource et/ou la série de ressources est inadéquate.
        
        
                        
.. _iso19139-gml-exterior:
        
                        
**Limite extérieure** 
                  
- *Code :* gml:exterior
                
- *Description :* Limite extérieure
        
        
        
                        
.. _iso19139-gml-interior:
        
                        
**Limite intérieure** 
                  
- *Code :* gml:interior
                
- *Description :* Limite intérieure
        
        
        
                        
.. _iso19139-gmd-featureTypes:
        
                        
**Liste des types d’entité** 
                  
- *Code :* gmd:featureTypes
                
- *Description :* Provides information about the list of feature types with the same spatial representation
        
        
        
                        
.. _iso19139-gmd-pointInPixel:
        
                        
**Location du pixel** 
                  
- *Code :* gmd:pointInPixel
                
- *Description :* Point d'un pixel correspondant à la location terrestre du pixel
        
- *Information complémentaire :* Point d'un pixel correspondant à la localisation du pixel dans le système terrestre.
        
        
                        
.. _iso19139-gmd-maximumOccurrence:
        
                        
**Maximum d’occurrence** 
                  
- *Code :* gmd:maximumOccurrence
                
- *Description :* Nombre maximum d’occurrences du nouvel élément de métadonnée
        
        
        
                        
.. _iso19139-gmd-resourceMaintenance:
        
                        
**Mise à jour de la ressource** 
                  
- *Code :* gmd:resourceMaintenance
                
- *Description :* Informations sur la fréquence de mise à jour des ressources, ainsi que de leur étendue
        
- *Information complémentaire :* Informations concernant l'étendue et la date de mise à jour de la ressource. Si la mise à jour ne concerne pas la totalité du jeu de données, les options "updateScope" et "updateScopeDescription" permettent la description de la mise à jour individualisée de chacune des parties du jeu. Exemple : les biens-fonds de la MO sont actualisés annuellement, les nomenclatures ne l'étant qu''au besoin. Ces informations sont gérées dans la classe MD_MaintenanceInformation.
        
        
                        
.. _iso19139-gmd-metadataMaintenance:
        
                        
**Mise à jour des métadonnées** 
                  
- *Code :* gmd:metadataMaintenance
                
- *Description :* Informations sur la fréquence de mise à jour des métadonnées, ainsi que de leur étendue
        
- *Information complémentaire :* Informations concernant la fréquence, l'étendue, la date et la validité des mises à jour. Ces informations sont gérées dans la classe MD_MaintenanceInformation.
        
        
                        
.. _iso19139-gmd-keyword:
        
                        
**Mot Clé** 
                  
- *Code :* gmd:keyword
                
- *Description :* Mots, notions ou phrases courants utilisés pour décrire le sujet
        
- *Information complémentaire :* Mots clés du jeu de données par l'intermédiaire desquels il peut être caractérisé et défini. Ces termes sont également utilisés en tant qu''arguments de recherche. Ces informations sont du type PT_FreeText et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-MD-Keywords:
        
                        
**Mots clés** 
                  
- *Code :* gmd:MD_Keywords
                
- *Description :* Classe pour les mots clés, leur type et leur source de référence
        
- *Information complémentaire :* Classe réservée aux mots clés, à leurs types (c.-à-d. la catégorie dont ils sont issus), de même qu''à leur référence ou à leur provenance. Les mots clés sont utilisés comme arguments de recherche.
        
        
                        
.. _iso19139-gmd-descriptiveKeywords:
        
                        
**Mots clés descriptifs** 
                  
- *Code :* gmd:descriptiveKeywords
                
- *Description :* Catégorie, type et source de référence des mots clés
        
- *Information complémentaire :* 
      En Europe, si la ressource est une série de données géographiques ou un ensemble de séries de données géographiques, il
      convient de fournir au moins un mot clé du thésaurus multilingue de l’environnement (GEMET, General Environ­mental Multi-lingual Thesaurus) 
      décrivant le thème dont relèvent les données géographiques, conformément aux
      définitions des annexes I, II ou III de la directive 2007/2/CE.
            
      
      Catégorie du mot clé, décrivant si ce dernier concerne la discipline, le lieu, la couche, l'intervalle de temps, ou le thème. Ces informations sont gérées dans la classe MD_Keywords.
        
        
                        
.. _iso19139-srv-keywords:
        
                        
**Mots-clés** 
                  
- *Code :* srv:keywords
                
- *Description :* Mots-clés décrivant le service
        
        
        
                        
.. _iso19139-gmd-MD-Medium:
        
                        
**Média** 
                  
- *Code :* gmd:MD_Medium
                
- *Description :* Classe avec l'information sur les médias sur lesquels les données pouvent être distribuée
        
- *Information complémentaire :* Classe contenant des informations relatives au support sur lequel les données peuvent être obtenues. Il s'agit d'un support hors ligne. d'autres informations peuvent être trouvées sous MD_Distribution.
        
        
                        
.. _iso19139-gmd-MD-Metadata:
        
                        
**Métadonnées** 
                  
- *Code :* gmd:MD_Metadata
                
- *Description :* Classe qui définit les métadonnées concernant des ressources
        
- *Information complémentaire :* Classe définissant les métadonnées d'une ou de plusieurs ressources. Les métadonnées peuvent se rapporter à des jeux de données entiers mais également à des objets géométriques, des attributs, des types d'objets géométriques et d'attributs ou à des agrégations de données, de séries de données ou de niveaux hiérarchiques ("hierarchyLevel"). Les relations de dépendance hiérarchique entre jeux de métadonnées peuvent être indiquées par la filiation ("parentIdentifier"). La norme ISO prévoit une relation monovalente entre jeux de métadonnées dans ce cadre, une seule filiation pouvant être indiquée et non plusieurs. La classe MD_Metadata présente également un attribut de contact avec le service compétent pour d'autres informations relatives aux métadonnées.
        
        
                        
.. _iso19139-gmd-evaluationMethodType:
        
                        
**Méthode d'évaluation** 
                  
- *Code :* gmd:evaluationMethodType
                
- *Description :* Type de méthodes utilisées pour évaluer la qualité du jeu de donnée
        
- *Information complémentaire :* Méthode utilisée pour apprécier la qualité du jeu de données.
        
        
                        
.. _iso19139-gmd-errorStatistic:
        
                        
**Méthode satistique** 
                  
- *Code :* gmd:errorStatistic
                
- *Description :* Méthode statistique utilisée pour déterminer la valeur
        
- *Information complémentaire :* Méthode statistique utilisée pour la détermination de la valeur.
        
        
                        
.. _iso19139-gmd-level:
        
                        
**Niveau** 
                  
- *Code :* gmd:level
                
- *Description :* Niveau hiérarchique des données spécifiées par l'attribut scope (79) du domaine d'applicabilité
        
- *Information complémentaire :* Domaine auquel s'appliquent les informations de qualité. La catégorie à laquelle se rapporte cette information peut être indiquée dans la liste de codes (exemple : attributs, objets géométriques, jeu de données, etc.).
        
        
                        
.. _iso19139-gmd-hierarchyLevel:
        
                        
**Niveau hiérarchique** 
                  
- *Code :* gmd:hierarchyLevel
                
- *Description :* Domaine auquel les métadonnées s'appliquent (voir l'annexe C pour plus d'information au sujet des niveaux de hiérarchie des métadonnées)
        
- *Information complémentaire :* Domaine auquel les métadonnées se rapportent. La catégorie d'informations à laquelle l'entité se réfère peut être indiquée dans la liste des codes (exemple : attributs, objets géométriques, jeu de données, etc.). "Jeu de données" est le paramètre par défaut.
        
        
                        
.. _iso19139-gmd-topologyLevel:
        
                        
**Niveau topologie** 
                  
- *Code :* gmd:topologyLevel
                
- *Description :* Code qui identifie le degré de complexité des relations spatiales
        
- *Information complémentaire :* Code indiquant les caractéristiques topologiques présentes dans le jeu de données telles que la géométrie sans topologie, les lignes, les lignes planes fermées, les surfaces, les corps, les surfaces tridimensionnelles, etc.
        
        
                        
.. _iso19139-gml-TimeNode:
        
                        
**Noeud** 
                  
- *Code :* gml:TimeNode
                
- *Description :* Noeud
        
        
        
                        
.. _iso19139-gmd-namegmd-MD-Medium:
        
                        
**Nom** (cf. `gmd:MD_Medium <#iso19139-gmd-md-medium>`_)
                  
- *Code :* gmd:name
                
- *Description :* Nom du média sur lequel les données pouvent être obtenues
        
- *Information complémentaire :* Type de support sur lequel les données peuvent être obtenues, par exemple CD-ROM, DVD, DVD-ROM, disquette, etc. (sélection dans une liste).
        
        
                        
.. _iso19139-gmd-namegmd-MD-ExtendedElementInformation:
        
                        
**Nom** (cf. `gmd:MD_ExtendedElementInformation <#iso19139-gmd-md-extendedelementinformation>`_)
                  
- *Code :* gmd:name
                
- *Description :* Nom de l'élément de métadonnée étendu
        
- *Information complémentaire :* Nom de l'élément de métadonnées étendu.
        
        
                        
.. _iso19139-gmd-namegmd-MD-ApplicationSchemaInformation:
        
                        
**Nom** (cf. `gmd:MD_ApplicationSchemaInformation <#iso19139-gmd-md-applicationschemainformation>`_)
                  
- *Code :* gmd:name
                
- *Description :* Nom du schéma d'application utilisé
        
- *Information complémentaire :* Nom du schéma d'application utilisé.
        
        
                        
.. _iso19139-gmd-namegmd-CI-OnlineResource:
        
                        
**Nom** (cf. `gmd:CI_OnlineResource <#iso19139-gmd-ci-onlineresource>`_)
                  
- *Code :* gmd:name
                
- *Description :* Nom de la ressource en ligne
        
- *Information complémentaire :* Nom de la source en ligne.
        
        
                        
.. _iso19139-gmd-namegmd-CI-Series:
        
                        
**Nom** (cf. `gmd:CI_Series <#iso19139-gmd-ci-series>`_)
                  
- *Code :* gmd:name
                
- *Description :* Nom des séries ou du jeu de données global desquels le jeu de donnée est une partie
        
- *Information complémentaire :* Nom de la série ou du jeu de données composé dont émane le jeu de données.
        
        
                        
.. _iso19139-gmd-name:
        
                        
**Nom** 
                  
- *Code :* gmd:name
                
- *Description :* Name of the series, or aggregate dataset, of which the dataset is a part
        
        
        
                        
.. _iso19139-srv-name:
        
                        
**Nom** 
                  
- *Code :* srv:name
                
- *Description :* Nom utilisé par le service pour le paramètre
        
        
        
                        
.. _iso19139-gco-aName:
        
                        
**Nom** 
                  
- *Code :* gco:aName
                
- *Description :* 
        
        
        
                        
.. _iso19139-gco-ScopedName:
        
                        
**Nom** 
                  
- *Code :* gco:ScopedName
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-shortName:
        
                        
**Nom court** 
                  
- *Code :* gmd:shortName
                
- *Description :* Nom court utilisé lors d'une implémentation telle que par exemple XML, SGML ou autres
        
- *Information complémentaire :* Forme abrégée du nom utilisée lors d'une implémentation dans XML ou SGML. Remarque : d'autres méthodes d'implémentation peuvent être employées.
        
        
                        
.. _iso19139-codeSpace:
        
                        
**Nom de l'identifiant** 
                  
- *Code :* codeSpace
                
- *Description :* Nom ou identification de la personne ou de l'organisation responsable pour le domaine de valeurs
        
- *Information complémentaire :* Informations sur la personne ou l'organisation en charge de l'espace nominal ou de l'identifiant.
        
        
                        
.. _iso19139-gmd-codeSpace:
        
                        
**Nom de l'identifiant** 
                  
- *Code :* gmd:codeSpace
                
- *Description :* Nom ou identification de la personne ou de l'organisation responsable pour la domaine de valeurs
        
- *Information complémentaire :* Informations sur la personne ou l'organisation en charge de l'espace nominal ou de l'identifiant.
        
        
                        
.. _iso19139-gmd-fileName:
        
                        
**Nom de l'illustration** 
                  
- *Code :* gmd:fileName
                
- *Description :* Nom du fichier qui contient le graphique contenant une illustration du jeu de données
        
- *Information complémentaire :* Nom du fichier contenant une représentation figurative du jeu de données.
        
        
                        
.. _iso19139-calendarEraName:
        
                        
**Nom de l'ère** 
                  
- *Code :* calendarEraName
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-individualName:
        
                        
**Nom de la personne** 
                  
- *Code :* gmd:individualName
                
- *Description :* Nom de la personne responsable. Prénom, nom et titre sont séparés par un signe de délimitation
        
- *Information complémentaire :* Nom de la personne responsable. Des séparateurs (virgules) figurent entre le prénom, le nom et le titre.
        
        
                        
.. _iso19139-srv-invocationName:
        
                        
**Nom de l’appel** 
                  
- *Code :* srv:invocationName
                
- *Description :* Nom de l’appel
        
        
        
                        
.. _iso19139-srv-operationName:
        
                        
**Nom de l’opération** 
                  
- *Code :* srv:operationName
                
- *Description :* Nom de l’opération
        
        
        
                        
.. _iso19139-gmx-FileName:
        
                        
**Nom du fichier** 
                  
- *Code :* gmx:FileName
                
- *Description :* Nom du fichier et URL.
        
        
        
                        
.. _iso19139-gmd-namegmd-MD-Format:
        
                        
**Nom du format** (cf. `gmd:MD_Format <#iso19139-gmd-md-format>`_)
                  
- *Code :* gmd:name
                
- *Description :* Nom des formats de transfert des données
        
- *Information complémentaire :* Nom du format de transfert de données, par exemple TIFF, ZIP, etc.
        
        
                        
.. _iso19139-srv-providerName:
        
                        
**Nom du fournisseur** 
                  
- *Code :* srv:providerName
                
- *Description :* Nom du fournisseur du service
        
        
        
                        
.. _iso19139-gmd-aggregateDataSetName:
        
                        
**Nom du jeu de données aggrégé** 
                  
- *Code :* gmd:aggregateDataSetName
                
- *Description :* Informations de référence sur le jeu de données rassemblé
        
- *Information complémentaire :* Nom et autres indications de sources des jeux de données de rang inférieur. Ces informations sont du type CI_Citation et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-hierarchyLevelName:
        
                        
**Nom du niveau de hiérarchie** 
                  
- *Code :* gmd:hierarchyLevelName
                
- *Description :* Nom du niveau de hiérarchie pour lequel les métadonnées sont produites
        
- *Information complémentaire :* Nom du niveau hiérarchique auquel les métadonnées se rapportent. Il peut par exemple s'agir du nom d'une série.
        
        
                        
.. _iso19139-gmd-metadataStandardName:
        
                        
**Nom du standard de métadonnées** 
                  
- *Code :* gmd:metadataStandardName
                
- *Description :* Nom du standard (incluant le nom du profil) de métadonnées utilisé
        
- *Information complémentaire :* Nom de la norme sur les métadonnées utilisée, profil inclus (exemple : GM03Core, GM03Profil).
        
        
                        
.. _iso19139-gmd-namegmd-RS-ReferenceSystem:
        
                        
**Nom du système de référence**
                  
- *Code :* gmd:name
                
- *Description :* Nom du système de référence utilisé
        
- *Information complémentaire :* Nom du système de référence utilisé.
        
        
                        
.. _iso19139-gmd-referenceSystemIdentifier:
        
                        
**Nom du système de référence** 
                  
- *Code :* gmd:referenceSystemIdentifier
                
- *Description :* Nom du système de référence spatiale, par lequel sont définis la projection, l'ellipsoïde et le datum géodésique utilisés
        
- *Information complémentaire :* Nom du système de référence spatial englobant la définition de la projection, de l'ellipsoïde et du datum géodésique utilisés. Ces informations sont du type RS_Identifier et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gco-Measuregmd-DQ-Element:
        
                        
**Nom du test**
                  
- *Code :* gco:Measure
                
- *Description :* Nom des tests qui ont été appliqués aux données
        
- *Information complémentaire :* Nom du test appliqué aux données.
        
        
                        
.. _iso19139-gmd-nameOfMeasure:
        
                        
**Nom du test** 
                  
- *Code :* gmd:nameOfMeasure
                
- *Description :* Nom des tests qui ont été appliqués aux données
        
- *Information complémentaire :* Nom du test appliqué aux données.
        
        
                        
.. _iso19139-gmd-thesaurusName:
        
                        
**Nom du thésaurus** 
                  
- *Code :* gmd:thesaurusName
                
- *Description :* Nom du thésaurus formellement enregistré ou d'une source d'autorité reconnue de mots clés
        
- *Information complémentaire :* Nom d'un thésaurus enregistré sous forme d'une banque de données ou d'une source de mots clés similaire faisant autorité. Ces informations sont du type CI_Citation et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gco-aNamegco-TypeName:
        
                        
**Nom du type** (cf. `gco:TypeName <#iso19139-gco-typename>`_)
                  
- *Code :* gco:aName
                
- *Description :* 
        
        
- Liste de suggestions :

        
   - BOOLEAN (BOOLEAN )

        
   - BYTE (BYTE)

        
   - CHARACTER (CHARACTER)

        
   - DATE (DATE)

        
   - DATETIME (DATETIME)

        
   - DOUBLE (DOUBLE)

        
   - FLOAT (FLOAT)

        
   - INTEGER (INTEGER)

        
   - NUMERIC (NUMERIC)

        
   - REAL (REAL)

        
   - SERIAL (SERIAL)

        
   - VARCHAR (VARCHAR)

        
   - TEXT (TEXT)

        
        
                        
.. _iso19139-gco-TypeName:
        
                        
**Nom du type** 
                  
- *Code :* gco:TypeName
                
- *Description :* 
        
        
        
                        
.. _iso19139-gco-LocalName:
        
                        
**Nom local** 
                  
- *Code :* gco:LocalName
                
- *Description :* 
        
        
        
                        
.. _iso19139-gco-localName:
        
                        
**Nom local** 
                  
- *Code :* gco:localName
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-geometricObjectCount:
        
                        
**Nombre d'objets géométriques** 
                  
- *Code :* gmd:geometricObjectCount
                
- *Description :* Nombre total d'objets de type point ou vecteur intervenant dans le jeu de données
        
- *Information complémentaire :* Nombre de points ou d'objets vectoriels présents dans le fichier.
        
        
                        
.. _iso19139-gmd-sequenceIdentifier:
        
                        
**Nombre de bandes de longueur d'onde** 
                  
- *Code :* gmd:sequenceIdentifier
                
- *Description :* Nombre qui identifie de façon unique le nombre de bandes de longueurs d'ondes sur lesquelles un senseur travaille
        
- *Information complémentaire :* Entier identifiant sans équivoque le nombre de gammes de longueurs d'onde utilisées par un capteur.
        
        
                        
.. _iso19139-gmd-dimensionSize:
        
                        
**Nombre de pixel** 
                  
- *Code :* gmd:dimensionSize
                
- *Description :* Nombre d'éléments le long de cet axe
        
- *Information complémentaire :* Nombre de cellules le long de cet axe
        
        
                        
.. _iso19139-gmd-volumes:
        
                        
**Nombre de sujets** 
                  
- *Code :* gmd:volumes
                
- *Description :* Nombre de sujets identifiés dans le média
        
- *Information complémentaire :* Nombre d'exemplaires de supports. Exemple : s'il s'agit de CD-ROM et si un disque est insuffisant pour le stockage, le nombre total de disques requis pour l'enregistrement du jeu de données complet est à indiquer ici.
        
        
                        
.. _iso19139-gmd-toneGradation:
        
                        
**Nombre de tons de gris** 
                  
- *Code :* gmd:toneGradation
                
- *Description :* Nombre de valeurs numériques discrètes dans les données raster
        
- *Information complémentaire :* Nombre de valeurs numériques discrètes dans les données tramées.
        
        
                        
.. _iso19139-gmd-dimensionName:
        
                        
**Noms des axes** 
                  
- *Code :* gmd:dimensionName
                
- *Description :* Nom de l'axe
        
        
        
                        
.. _iso19139-gmd-northBoundLatitude:
        
                        
**Nord** 
                  
- *Code :* gmd:northBoundLatitude
                
- *Description :* Coordonnée la plus au nord de la limite de l'étendue du jeu de données, exprimée en latitude avec des degrés décimaux (NORD positif)
        
- *Information complémentaire :* Limite nord de l'extension du jeu de données, exprimée en latitude géographique (degrés décimaux) comptée positivement vers le nord.
        
        
                        
.. _iso19139-gmd-extendedElementInformation:
        
                        
**Nouvel élément** 
                  
- *Code :* gmd:extendedElementInformation
                
- *Description :* Informations relatives à un nouvel élément de métadonnées requis pour la description des données géographiques, mais absent de la norme ISO19115.
        
        
        
                        
.. _iso19139-gmd-facsimile:
        
                        
**Numéro de fax** 
                  
- *Code :* gmd:facsimile
                
- *Description :* Numéro de fax de la personne ou organisation responsable
        
- *Information complémentaire :* Numéro de télécopieur.
        
        
                        
.. _iso19139-gmd-voice:
        
                        
**Numéro de téléphone** 
                  
- *Code :* gmd:voice
                
- *Description :* Numéro de téléphone de la personne ou organisation responsable
        
- *Information complémentaire :* Numéro de téléphone.
        
        
                        
.. _iso19139-gmd-amendmentNumber:
        
                        
**Numéro de version du format** 
                  
- *Code :* gmd:amendmentNumber
                
- *Description :* Numéro d'amélioration du format
        
- *Information complémentaire :* Numéro de la modification apportée au format.
        
        
                        
.. _iso19139-gmd-features:
        
                        
**Objets Géométriques** 
                  
- *Code :* gmd:features
                
- *Description :* Objets sur lesquels l'information s'applique
        
- *Information complémentaire :* Propriétés auxquelles les informations se rapportent.
        
        
                        
.. _iso19139-gmd-MD-GeometricObjects:
        
                        
**Objets géométriques** 
                  
- *Code :* gmd:MD_GeometricObjects
                
- *Description :* Classe pour le nombre d'objets utilisés par le jeu de données. Ils sont listés en fonction du type d'objet géométrique
        
- *Information complémentaire :* Classe destinée au type et au nombre d'objets utilisés dans le jeu de données. Les objets sont ordonnés selon leur type géométrique. Cette information définit l'objet géométrique décrit dans la classe MD_VectorSpatialRepresentation.
        
        
                        
.. _iso19139-gmd-geometricObjects:
        
                        
**Objets géométriques** 
                  
- *Code :* gmd:geometricObjects
                
- *Description :* Information sur les objets géométriques utilisés dans le jeu de données
        
- *Information complémentaire :* Informations concernant les objets géométriques utilisés dans le jeu de données. Ces informations sont du type MD_GeometricObjects et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-obligation:
        
                        
**Obligation** 
                  
- *Code :* gmd:obligation
                
- *Description :* Obligation de l'élément étendu
        
- *Information complémentaire :* Niveau d'obligation associé à l'élément étendu (obligatoire, optionnel ou obligatoire sous certaines conditions).
        
        
                        
.. _iso19139-gmd-attributeInstances:
        
                        
**Occurences d'attributs** 
                  
- *Code :* gmd:attributeInstances
                
- *Description :* Occurrences d'attributs sur lesquels l'information s'applique
        
- *Information complémentaire :* Occurrences d'attributs auxquelles les informations se rapportent.
        
        
                        
.. _iso19139-gmd-featureInstances:
        
                        
**Occurences d'objets** 
                  
- *Code :* gmd:featureInstances
                
- *Description :* Occurrences d'objets sur lesquels l'information s'applique
        
- *Information complémentaire :* Occurrences de propriétés auxquelles les informations se rapportent.
        
        
                        
.. _iso19139-gmd-offset:
        
                        
**Offset** 
                  
- *Code :* gmd:offset
                
- *Description :* Valeur physique correspondant à la valeur zéro d'une cellule
        
- *Information complémentaire :* Valeur physique correspondant à la valeur zéro d'une cellule.
        
        
                        
.. _iso19139-srv-SV-OperationMetadata:
        
                        
**Operations** 
                  
- *Code :* srv:SV_OperationMetadata
                
- *Description :* Information sur les opérations
        
        
        
                        
.. _iso19139-srv-optionality:
        
                        
**Optionalité** 
                  
- *Code :* srv:optionality
                
- *Description :* Indication sur l’optionalité du paramètre
        
        
        
                        
.. _iso19139-gmd-transferOptions:
        
                        
**Options de transfert** 
                  
- *Code :* gmd:transferOptions
                
- *Description :* Informations sur la façon de se procurer les données chez le distributeur
        
- *Information complémentaire :* Informations relatives au mode d'obtention des données auprès du distributeur. Ces informations sont gérées dans la classe MD_DigitalTransferOptions.
        
        
                        
.. _iso19139-gmd-distributorTransferOptions:
        
                        
**Options de transfert du distributeur** 
                  
- *Code :* gmd:distributorTransferOptions
                
- *Description :* Informations concernant la technique et le média utilisés par le distributeur
        
- *Information complémentaire :* Informations concernant le mode et le support de diffusion utilisés par le distributeur, par exemple la taille du fichier, la manière de l'obtenir, etc. Ces informations sont gérées dans la classe MD_DigitalTransferOptions.
        
        
                        
.. _iso19139-gmd-MD-DigitalTransferOptions:
        
                        
**Options de transfert numérique** 
                  
- *Code :* gmd:MD_DigitalTransferOptions
                
- *Description :* Classe avec les possibilités techniques et les médias avec lesquels une ressource peut être obtenue par un distributeur
        
- *Information complémentaire :* Classe contenant les possibilités techniques et les supports envisageables pour l'obtention d'une ressource auprès d'un distributeur. Cf. MD_Distribution pour des informations complémentaires.
        
        
                        
.. _iso19139-srv-containsOperations:
        
                        
**Opérations** 
                  
- *Code :* srv:containsOperations
                
- *Description :* Information sur les opérations
        
        
        
                        
.. _iso19139-gmd-organisationName:
        
                        
**Organisation** 
                  
- *Code :* gmd:organisationName
                
- *Description :* Nom de l'organisation responsable
        
- *Information complémentaire :* Nom de l'organisation responsable, s'il s'agit d'une personne isolée ou nom de l'organisation au sein de laquelle cette personne est employée. Ces informations sont du type PT_FreeText et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-westBoundLongitude:
        
                        
**Ouest** 
                  
- *Code :* gmd:westBoundLongitude
                
- *Description :* Coordonnée la plus à l'ouest de la limite de l'étendue du jeu de données, exprimée en longitude avec des degrés décimaux (EST positif)
        
- *Information complémentaire :* Limite ouest de l'extension du jeu de données, exprimée en longitude géographique (degrés décimaux) comptée positivement vers l'est.
        
        
                        
.. _iso19139-gmd-PT-LocaleContainer:
        
                        
**PT_LocaleContainer** 
                  
- *Code :* gmd:PT_LocaleContainer
                
- *Description :* PT_LocaleContainer
        
        
        
                        
.. _iso19139-gmd-page:
        
                        
**Page** 
                  
- *Code :* gmd:page
                
- *Description :* Détails sur quelles pages de la publication l'article a été publié
        
- *Information complémentaire :* Indication détaillée des numéros de pages de l'article dans la publication concernée ou du jeu de données dans la série considérée.
        
        
                        
.. _iso19139-srv-SV-Parameter:
        
                        
**Paramètre** 
                  
- *Code :* srv:SV_Parameter
                
- *Description :* 
        
        
        
                        
.. _iso19139-srv-parameters:
        
                        
**Paramètres** 
                  
- *Code :* srv:parameters
                
- *Description :* Paramètres requis par l’interface
        
        
        
                        
.. _iso19139-gmd-georeferencedParameters:
        
                        
**Paramètres de georéférence** 
                  
- *Code :* gmd:georeferencedParameters
                
- *Description :* Indication sur les données de géoréférencement du raster
        
- *Information complémentaire :* Valeurs numériques contenant les données de géoréférencement de la trame.
        
        
                        
.. _iso19139-gmd-partOf:
        
                        
**PartOf** 
                  
- *Code :* gmd:partOf
                
- *Description :* PartOf
        
        
        
                        
.. _iso19139-gmd-country:
        
                        
**Pays** 
                  
- *Code :* gmd:country
                
- *Description :* Pays dans la langue duquel l'URL libre est écrit
        
- *Information complémentaire :* Pays dans la langue duquel l'URL libre est écrit, la sélection s'opère dans la liste des pays ISO.
        
        
                        
.. _iso19139-gmd-countrygmd-MD-Legislation:
        
                        
**Pays**
                  
- *Code :* gmd:country
                
- *Description :* Pays d'où provient la loi
        
- *Information complémentaire :* Pays dans lequel la loi a été promulguée, sélection dans la liste ISO des pays.
        
        
                        
.. _iso19139-gmd-countryPT-Group:
        
                        
**Pays**
                  
- *Code :* gmd:country
                
- *Description :* Pays de la langue dans le lequel le texte libre est écrit
        
- *Information complémentaire :* Pays dans la langue duquel le texte libre est rédigé. Sélection dans la liste ISO des pays.
        
        
                        
.. _iso19139-gmd-processor:
        
                        
**Personnes responsables du processus** 
                  
- *Code :* gmd:processor
                
- *Description :* Identification des personnes et organisations associées avec l'étape de processus, ainsi que des moyens de communications à utiliser pour entrer en contact avec elles
        
- *Information complémentaire :* Identification de la personne (voire des personnes) ou de l'organisation (voire des organisations) en charge du jeu de données décrit et mode de communication avec elle(s). Cette personne ou ce service endosse un rôle bien spécifique (propriétaire, prestataire, gestionnaire, etc.) pouvant être sélectionné dans la liste proposée ici. Ces informations sont du type CI_ResponsibleParty et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-srv-DCPList:
        
                        
**Plateforme de communication** 
                  
- *Code :* srv:DCPList
                
- *Description :* 
        
        
        
                        
.. _iso19139-gml-Point:
        
                        
**Point** 
                  
- *Code :* gml:Point
                
- *Description :* Point
        
        
        
                        
.. _iso19139-gmd-centerPoint:
        
                        
**Point central** 
                  
- *Code :* gmd:centerPoint
                
- *Description :* Relation du système de coordonnées raster au système de référence spatiale définie par la cellule du centre du raster et par les coordonnées correspondantes du système de référence spatiale
        
- *Information complémentaire :* Lien du système de coordonnées de la trame avec le système terrestre défini par la cellule centrale de la trame et les coordonnées correspondantes dans le système de référence spatial.
        
        
                        
.. _iso19139-srv-connectPoint:
        
                        
**Point de connection** 
                  
- *Code :* srv:connectPoint
                
- *Description :* Point de connection pour accéder à l’interface
        
        
        
                        
.. _iso19139-gmd-pointOfContact:
        
                        
**Point de contact** 
                  
- *Code :* gmd:pointOfContact
                
- *Description :* Identification, et mode de communication avec, des personnes ou des organisations associées aux ressources
        
- *Information complémentaire :* Identification de la personne (voire des personnes) ou de l'organisation (voire des organisations) responsable du jeu de données décrit et mode de communication avec elle. Cette personne ou ce service endosse un rôle (propriétaire, prestataire, gestionnaire, etc.) bien spécifique pouvant être sélectionné dans la liste proposée. Les données correspondantes de la personne ou du service sont gérées dans la classe CI_ResponsibleParty. Ce rôle peut également servir à l'affectation d'un jeu de données à une commune. Exemple : le rôle de "propriétaire" permet ici d'affecter un lot de la MO à la commune correspondante.
        
        
                        
.. _iso19139-gmd-polygon:
        
                        
**Polygone** 
                  
- *Code :* gmd:polygon
                
- *Description :* Liste de points définissant le polygone de délimitation
        
- *Information complémentaire :* Série de points définissant le polygone de délimitation. Les positions sont exprimées en latitude et longitude géographique. Il s'agit d'un objet géométrique.
        
        
                        
.. _iso19139-gml-Polygon:
        
                        
**Polygone** 
                  
- *Code :* gml:Polygon
                
- *Description :* Polygone
        
        
        
                        
.. _iso19139-gmd-positionName:
        
                        
**Position** 
                  
- *Code :* gmd:positionName
                
- *Description :* Rôle ou position de la personne responsable
        
- *Information complémentaire :* Fonction ou position de la personne responsable.
        
        
                        
.. _iso19139-gml-position:
        
                        
**Position** 
                  
- *Code :* gml:position
                
- *Description :* Position
        
        
        
                        
.. _iso19139-indeterminatePosition:
        
                        
**Position indéterminée** 
                  
- *Code :* indeterminatePosition
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-cloudCoverPercentage:
        
                        
**Pourcentage de la couverture nuageuse** 
                  
- *Code :* gmd:cloudCoverPercentage
                
- *Description :* Surface du jeu de données recouverte par les nuages, exprimée en pourcentage de la couverture spatiale
        
- *Information complémentaire :* Surface du jeu de données images assombrie par des nuages (en pourcentage de la surface totale).
        
        
                        
.. _iso19139-gmd-MD-StandardOrderProcess:
        
                        
**Processus de commande standard** 
                  
- *Code :* gmd:MD_StandardOrderProcess
                
- *Description :* Classe pour la description des instructions et des modes usuels d'obtention de la ressource, comprenant également les informations sur les coûts
        
- *Information complémentaire :* Classe contenant des informations relatives aux émoluments et à la commande. d'autres informations peuvent être trouvées sous MD_Distribution.
        
        
                        
.. _iso19139-gmd-distributionOrderProcess:
        
                        
**Processus de distribution et de commande** 
                  
- *Code :* gmd:distributionOrderProcess
                
- *Description :* Informations sur comment les données peuvent êtres commandées, ainsi que sur leur coûts et sur les formatlités de commandes
        
- *Information complémentaire :* Informations relatives au mode de commande des données, à leur coût ainsi qu''à d'autres instructions de commande. Ces informations sont gérées dans la classe MD_StandardOrderProcess.
        
        
                        
.. _iso19139-gmd-evaluationProcedure:
        
                        
**Procédure d'évaluation** 
                  
- *Code :* gmd:evaluationProcedure
                
- *Description :* Référence à l'information sur la procédure
        
- *Information complémentaire :* Renvoi à la description de la procédure.
        
        
                        
.. _iso19139-gmd-applicationProfile:
        
                        
**Profil d'application** 
                  
- *Code :* gmd:applicationProfile
                
- *Description :* Nom d'un profil d'application qui peut être utilisé avec les ressources en ligne
        
- *Information complémentaire :* Nom d'un profil d'application pouvant être utilisé pour la source en ligne.
        
        
                        
.. _iso19139-gmd-propertyType:
        
                        
**PropertyType** 
                  
- *Code :* gmd:propertyType
                
- *Description :* PropertyType
        
        
        
                        
.. _iso19139-gmd-axisDimensionProperties:
        
                        
**Propriétés des axes** 
                  
- *Code :* gmd:axisDimensionProperties
                
- *Description :* Information sur les propriétés des axes spatio-temporels
        
- *Information complémentaire :* Informations relatives aux propriétés des axes spatio-temporels telles que le nom, la dimension et la résolution. Ces informations sont du type MD_Dimension et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-srv-accessProperties:
        
                        
**Propriétés d’accès** 
                  
- *Code :* srv:accessProperties
                
- *Description :* Information sur la disponibilité du service (prix, instructions de commande...)
        
        
        
                        
.. _iso19139-gmd-protocol:
        
                        
**Protocole** 
                  
- *Code :* gmd:protocol
                
- *Description :* Protocole de la connexion à utiliser
        
- *Information complémentaire :* Protocole de connexion utilisé, par exemple FTP.
        
        
                        
.. _iso19139-gmd-DQ-AbsoluteExternalPositionalAccuracy:
        
                        
**Précision absolue de la position** 
                  
- *Code :* gmd:DQ_AbsoluteExternalPositionalAccuracy
                
- *Description :* Classe pour la description de la concordance des coordonnées mesurées avec les valeurs vraies ou acceptées comme telles
        
- *Information complémentaire :* Classe destinée à décrire la précision des coordonnées effectives par rapport aux coordonnées théoriques.
      Exemple : Des points GPS sont à plus de 10 m de leur localisation acceptée comme
      vraie, Lampadaires issus de la restitution : 40 cm et Lampadaires issus des levers :
      5 cm.
        
        
                        
.. _iso19139-gmd-DQ-ThematicClassificationCorrectness:
        
                        
**Précision de la classification thématique** 
                  
- *Code :* gmd:DQ_ThematicClassificationCorrectness
                
- *Description :* Classe pour la description de la comparaison de classes ou leurs attributs assignés aux objets avec un discours universel
        
- *Information complémentaire :* Classe permettant la description de la comparaison de classes ou d'attributs assignés aux objets avec un large éventail de possibilités.
      Exemple : Un poteau a été affecté à la classe « poteau téléphone ».
        
        
                        
.. _iso19139-gmd-DQ-AccuracyOfATimeMeasurement:
        
                        
**Précision de la mesure du temps** 
                  
- *Code :* gmd:DQ_AccuracyOfATimeMeasurement
                
- *Description :* Classe pour la description de la justesse de la référence temporelle d'un élément (rapport d'erreur d'une mesure de temps)
        
- *Information complémentaire :* Exemple : La date renseignée pour une procédure administrative est imprécise.
        
        
                        
.. _iso19139-gmd-DQ-GriddedDataPositionalAccuracy:
        
                        
**Précision de la position raster** 
                  
- *Code :* gmd:DQ_GriddedDataPositionalAccuracy
                
- *Description :* Classe pour la description de la concordance des valeurs de position dans la grille avec les valeurs vraies ou acceptées comme telles
        
- *Information complémentaire :* Classe permettant la description de la précision en position dans le réseau, en comparaison des valeurs théoriques.
        
        
                        
.. _iso19139-gmd-DQ-RelativeInternalPositionalAccuracy:
        
                        
**Précision de la position relative** 
                  
- *Code :* gmd:DQ_RelativeInternalPositionalAccuracy
                
- *Description :* Classe pour la description de la concordance des positions relatives des objets du domaine d'applicabilité avec les positions relatives respectives vraies ou admises comme telles
        
- *Information complémentaire :* Classe destinée à la description de la précision de position relative par rapport à une position relative acceptée ou à une position théorique.
      Exemple : Le bâtiment est de l’autre côté de la route.
        
        
                        
.. _iso19139-gmd-DQ-NonQuantitativeAttributeAccuracy:
        
                        
**Précision des attributs non quantitatifs** 
                  
- *Code :* gmd:DQ_NonQuantitativeAttributeAccuracy
                
- *Description :* Classe pour la description de la précision des attributs non-quantitatifs
        
- *Information complémentaire :* Classe destinée à la description de la précision d'attributs non quantitatifs.
      Exemple : La commune du client est fausse.
        
        
                        
.. _iso19139-gmd-DQ-QuantitativeAttributeAccuracy:
        
                        
**Précision des attributs quantitatifs** 
                  
- *Code :* gmd:DQ_QuantitativeAttributeAccuracy
                
- *Description :* Classe pour la description de la précision des attributs quantitatifs
        
- *Information complémentaire :* Classe destinée à la description de la précision d'attributs quantitatifs.
      Exemple : La surface renseignée de la parcelle est inférieure de 20% à la réalité.
        
        
                        
.. _iso19139-gml-TimePeriod:
        
                        
**Période** 
                  
- *Code :* gml:TimePeriod
                
- *Description :* Période de temps (début/fin)
        
        
        
                        
.. _iso19139-gts-TM-PeriodDuration:
        
                        
**Période** 
                  
- *Code :* gts:TM_PeriodDuration
                
- *Description :* 
      Le type durée permet de définir un interval de temps.
      
      Le format est le suivant "PnYnMnDTnHnMnS" :
      
      * P Période (Obligatoire)
      * nY : nombre d'années
      * nM : nombre de mois
      * nD : nombre de jours
      * T début de la section temps (Obligatoire, si définition d'un des éléments suivants)
      * nH : nombre d'heures
      * nM : nombre de minutes
      * nS : nombre de secondes      
    
        
        
        
                        
.. _iso19139-gco-DateTimegmd-MD-Usage:
        
                        
**Période de l'utilisation** (cf. `gmd:MD_Usage <#iso19139-gmd-md-usage>`_)
                  
- *Code :* gco:DateTime
                
- *Description :* Date et heure de la première utilisation ou de la période d'utilisation de la ressource et/ou de la série de ressource
        
- *Information complémentaire :* Date et heure de la première utilisation ou de la période d'utilisation de la ressource et/ou de la série de ressources.
        
        
                        
.. _iso19139-gmd-usageDateTime:
        
                        
**Période de l'utilisation** 
                  
- *Code :* gmd:usageDateTime
                
- *Description :* Date et heure de la première utilisation ou de la période d'utilisation de la ressource et/ou de la série de ressource
        
- *Information complémentaire :* Date et heure de la première utilisation ou de la période d'utilisation de la ressource et/ou de la série de ressources.
        
        
                        
.. _iso19139-gmd-dateTimegmd-DQ-Element:
        
                        
**Période de test**
                  
- *Code :* gmd:dateTime
                
- *Description :* Date ou période pendant laquelle une mesure de qualité des données a été appliquée
        
- *Information complémentaire :* Date à laquelle ou période durant laquelle la qualité des données a été déterminée.
        
        
                        
.. _iso19139-gmd-dateTimegmd-LI-ProcessStep:
        
                        
**Période du processus** (cf. `gmd:LI_ProcessStep <#iso19139-gmd-li-processstep>`_)
                  
- *Code :* gmd:dateTime
                
- *Description :* Date et heure, ou période, à laquelle l'étape de processus s'est réalisée
        
- *Information complémentaire :* Date à laquelle ou période au sein de laquelle le processus de traitement a été effectué. Indication en format horaire jj.mm.aaaa/ hh.mm.ss
        
        
                        
.. _iso19139-gmd-lineage:
        
                        
**Qualité de la provenance** 
                  
- *Code :* gmd:lineage
                
- *Description :* Informations de qualité concernant la provenance des données
        
- *Information complémentaire :* Informations relatives à la provenance, à la filiation ou au processus de génération. Elles sont gérées dans la classe LI_Lineage. "+lineage" est obligatoire si "scope.DQ_Scope.level" = "Jeu de données".
        
        
                        
.. _iso19139-gmd-DQ-DataQuality:
        
                        
**Qualité des données** 
                  
- *Code :* gmd:DQ_DataQuality
                
- *Description :* Classe avec l'information sur la qualité pour les données spécifiées par un domaine de qualité des données
        
- *Information complémentaire :* Classe comportant des informations relatives à la qualité du jeu de données. La qualité des données est exprimée par les éléments de métadonnées des classes DQ_DataQuality, LI_Lineage et DQ_Legislation. l'étendue des indications de qualité est définie et décrite dans la classe du champ d'application (DQ_Scope). La filiation ou la provenance des données est décrite dans la classe LI_Lineage qui est une agrégation des deux classes LI_Source (indications concernant les données source) et LI_ProcessStep (informations relatives aux étapes de traitement). Les différentes étapes de traitement peuvent à leur tour présenter une relation avec les données source. "+lineage" est obligatoire si "scope.DQ_Scope.level" = "Jeu de données". 
      Les prescriptions de qualité applicables au jeu de données à décrire figurent dans la classe DQ_Legislation.
    
    
    La totalité des caractéristiques d’un produit qui lui confèrent l’aptitude à satisfaire des besoins exprimés ou
    implicites, conformément à la norme EN ISO 19101.    
    
        
        
                        
.. _iso19139-gmd-report:
        
                        
**Qualité quantitative** 
                  
- *Code :* gmd:report
                
- *Description :* Information de qualité quantitative pour les données concernée par le domaine
        
- *Information complémentaire :* Information de qualité de nature quantitative portant sur les données.
        
        
                        
.. _iso19139-gmd-compressionGenerationQuantity:
        
                        
**Quantité de compressions d'image** 
                  
- *Code :* gmd:compressionGenerationQuantity
                
- *Description :* Nombre de cycles de compressions (avec pertes) appliqués à l'image
        
- *Information complémentaire :* Nombre de cycles de compression (avec pertes) appliqués à l'image
        
        
                        
.. _iso19139-radix:
        
                        
**Radical** 
                  
- *Code :* radix
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-rationalegmd-MD-ExtendedElementInformation:
        
                        
**Raison** (cf. `gmd:MD_ExtendedElementInformation <#iso19139-gmd-md-extendedelementinformation>`_)
                  
- *Code :* gmd:rationale
                
- *Description :* Raison de la création de l'élément étendu
        
- *Information complémentaire :* Motif de l'extension de la norme par l'élément considéré.
        
        
                        
.. _iso19139-gmd-MD-Georectified:
        
                        
**Raster géorectifié** 
                  
- *Code :* gmd:MD_Georectified
                
- *Description :* Classe pour la description du raster qui est aligné sur un système de coordonnées géographiques défini par un système à référence spatial, de telle sorte que chaque cellule puisse être localisée géographiquement avec les coordonnées raster ou l'origine du raster
        
- *Information complémentaire :* Classe réservée à des trames dont les cellules sont disposées de façon régulière dans un système de coordonnées en projection ou dans un système de coordonnées géographiques défini dans le système de référence spatial, de telle sorte que chacune des cellules puisse être localisée par les coordonnées, l'origine, l'extension et l'orientation de la trame. La classe MD_Georectified permet la description de la rectification géographique de la trame. Le lien de la trame redressée avec le système de coordonnées spatial ne peut toutefois être défini que par l'intermédiaire des quatre coins de la trame. La description de l'extension spatiale de la trame de même que l'indication de points de contrôle pour la vérification de la précision du géoréférencement sont optionnels.
        
        
                        
.. _iso19139-gmd-MD-Georeferenceable:
        
                        
**Raster géoréférencable** 
                  
- *Code :* gmd:MD_Georeferenceable
                
- *Description :* Classe pour un raster, dont les cellules sont disposées irrégulièrement en fonction de différents systèmes de projections, de telle manière qu''ils ne sont pas localisables avec les propriétés du raster mais avec les informations de localisation disponible
        
- *Information complémentaire :* Classe réservée à une trame dont les cellules sont disposées de façon irrégulière dans un système de coordonnées quelconque (en projection / géographique), de sorte qu''elles ne peuvent être localisées qu''à l'aide d'informations de localisation fournies et non au moyen des propriétés de la trame. La classe MD_Georeferenceable contient la description des informations concernant le géoréférencement. Il est établi une distinction dans ce cadre entre un géoréférencement par des points d'appui et par des paramètres de transformation. l'attribut "parameterCitation" permet, via le type de données CI_Citation, de fournir d'autres informations encore concernant la provenance et le service compétent ayant transmis les informations de géoréférencement.
        
        
                        
.. _iso19139-gmd-credit:
        
                        
**Reconnaissance** 
                  
- *Code :* gmd:credit
                
- *Description :* Reconnaissance de ceux qui ont contribués à ces ressources
        
- *Information complémentaire :* Reconnaissance ou confirmation des intervenants ayant apporté leur contribution à cette ressource.
        
        
                        
.. _iso19139-gml-relatedTime:
        
                        
**Relation de temps** 
                  
- *Code :* gml:relatedTime
                
- *Description :* Définit la relation entre un temps donné et l’objet
        
        
        
                        
.. _iso19139-gmd-maintenanceNote:
        
                        
**Remarque sur la mise à jour** 
                  
- *Code :* gmd:maintenanceNote
                
- *Description :* Informations ou remarques en ce qui concerne les besoins spécifiques concernant la maintenance des ressources
        
- *Information complémentaire :* Informations ou remarques concernant la prise en compte de besoins spécifiques lors de la mise à jour des ressources.
        
        
                        
.. _iso19139-gmd-MD-GridSpatialRepresentation:
        
                        
**Représentation spatiale du raster** 
                  
- *Code :* gmd:MD_GridSpatialRepresentation
                
- *Description :* Classe contenant l'information sur les objets spatiaux de type raster du jeu de données
        
- *Information complémentaire :* Cette classe contient des informations relatives à l'extension spatiale dans le cas d'un jeu de données tramées. Les valeurs entrées concernent les axes, les cellules, etc. Cette classe est une représentation de MD_SpatialRepresentation. Vous trouverez d'autres informations sous MD_SpatialRepresentation.
        
        
                        
.. _iso19139-gmd-MD-VectorSpatialRepresentation:
        
                        
**Représentation spatiale du vecteur** 
                  
- *Code :* gmd:MD_VectorSpatialRepresentation
                
- *Description :* Classe qui contient l'information sur les objets géographiques de type vecteur du jeu de données
        
- *Information complémentaire :* Cette classe contient des informations concernant l'extension spatiale lorsqu''un jeu de données vectorielles est concerné. Les valeurs entrées sont des informations relatives à la topologie et aux objets géométriques. Cette classe est une représentation de MD_SpatialRepresentation. Vous trouverez d'autres informations sous MD_SpatialRepresentation.
        
        
                        
.. _iso19139-gmd-CI-ResponsibleParty:
        
                        
**Responsable** 
                  
- *Code :* gmd:CI_ResponsibleParty
                
- *Description :* Type de données pour l'identification des personnes et organisations, ainsi que pour la description des modes de communication avec, associées avec le jeu de données
        
- *Information complémentaire :* Type de données destiné à l'identification de personnes et/ou d'organisations en relation avec le jeu de données (en tant que responsable, en charge du traitement, propriétaire, etc.), intégrant par ailleurs d'autres informations telles que le numéro de téléphone, l'adresse (postale, messagerie électronique) permettant d'entrer en contact avec ces personnes et/ou organisations. Les trois premiers attributs (individualName, organisationName, positionName) de ce type de données permettent de savoir s'il s'agit de la description d'une personne, d'un service ou de la domiciliation d'une personne précédemment définie. Une indication au moins est obligatoire. La liste de sélection CI_RoleCode spécifie alors la nature de la responsabilité endossée par le service désigné. Cf. CI_Citation pour d'autres informations.
        
        
                        
.. _iso19139-gmd-citedResponsibleParty:
        
                        
**Responsable** 
                  
- *Code :* gmd:citedResponsibleParty
                
- *Description :* Information sur le nom et la position d'une personne individuelle ou d'une organisation responsable pour la ressource
        
- *Information complémentaire :* Informations concernant le nom et la domiciliation de la personne ou de l'organisation responsable de la source citée. Ces informations sont du type CI_ResponsibleParty et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-srv-coupledResource:
        
                        
**Ressource couplée** 
                  
- *Code :* srv:coupledResource
                
- *Description :* 
      Si la ressource est un service de données géographiques, cet élément de métadonnées identifie, le cas échéant, la
      série ou les séries de données géographiques cibles du service grâce à leurs identificateurs de ressource uniques
      (Unique Resource Identifiers, URI).      
    
        
        
        
                        
.. _iso19139-srv-SV-CoupledResource:
        
                        
**Ressource couplée** 
                  
- *Code :* srv:SV_CoupledResource
                
- *Description :* Information sur la ressource couplée
        
        
        
                        
.. _iso19139-gmd-CI-OnlineResource:
        
                        
**Ressource en ligne** 
                  
- *Code :* gmd:CI_OnlineResource
                
- *Description :* Type de données pour l'information sur les sources en ligne, grâce auxquelles les éléments de métadonnées étendus sur le jeu de données, la spécification ou le profil peuvent être obtenus
        
- *Information complémentaire :* Type de données contenant des informations relatives à la possibilité et dans l'affirmative, à la manière d'accéder en ligne au jeu de données.
        
        
                        
.. _iso19139-gmd-extensionOnLineResource:
        
                        
**Ressource en line sur les extensions** 
                  
- *Code :* gmd:extensionOnLineResource
                
- *Description :* Information sur les sources en ligne décrivant le profil d'un domaine d'application spécifique ainsi que les extensions du modèle. Information sur tous les nouveaux éléments de métadonnées.
        
- *Information complémentaire :* Informations relatives à des sources en ligne contenant le profil de la communauté d'utilisateurs de même que des éléments de métadonnées étendus. Informations concernant tous les nouveaux éléments de métadonnées.
        
        
                        
.. _iso19139-gmd-mediumNote:
        
                        
**Restriction média** 
                  
- *Code :* gmd:mediumNote
                
- *Description :* Description d'autres restrictions ou exigences pour l'utilisation du média
        
- *Information complémentaire :* Descriptions complémentaires ou informations importantes concernant le support telles que des restrictions, des exigences, etc.
        
        
                        
.. _iso19139-gmd-MD-Constraints:
        
                        
**Restrictions** 
                  
- *Code :* gmd:MD_Constraints
                
- *Description :* Classe pour les restrictions sur l'accès et l'utilisation d'une ressource ou de métadonnées
        
- *Information complémentaire :* Classe recelant des informations relatives aux restrictions d'accès et d'utilisation de la ressource ou du jeu de métadonnées. Le paquet contenant la description des restrictions d'accès et d'utilisation intègre la classe MD_Constraints avec un attribut fournissant une description générale des restrictions et les deux sous-classes MD_LegalConstraints (informations sur les restrictions d'accès et d'utilisation dues au respect de droits d'auteurs) et MD_SecurityConstraints (informations sur les restrictions liées à des questions de sécurité de portée nationale ou assimilée telles que la confidentialité ou le secret). La classification des restrictions et des niveaux de sécurité s'effectue via des listes de sélection (MD_RestrictionCode et MD_ClassificationCode).
        
        
                        
.. _iso19139-gmd-classification:
        
                        
**Restrictions de manipulation** 
                  
- *Code :* gmd:classification
                
- *Description :* Noms des restrictions de manipulation sur les ressources où de métadonnées
        
- *Information complémentaire :* Type de restriction. Sélection dans la liste suivante : non classé, diffusion restreinte, confidentiel, secret, top secret.
        
        
                        
.. _iso19139-gmd-rule:
        
                        
**Règle de relation** 
                  
- *Code :* gmd:rule
                
- *Description :* Spécifications comment l'élément étendu est en relation avec d'autres éléments et entités existants
        
- *Information complémentaire :* Spécification de la manière dont l'élément étendu est en relation avec d'autres classes et éléments existants.
        
        
                        
.. _iso19139-gmd-portrayalCatalogueCitation:
        
                        
**Référence bibliographique** 
                  
- *Code :* gmd:portrayalCatalogueCitation
                
- *Description :* Référence bibliographique du catalogue de présentation utilisé
        
- *Information complémentaire :* Référence bibliographique au catalogue de représentation cité
        
        
                        
.. _iso19139-gmd-sourceCitation:
        
                        
**Référence des données sources** 
                  
- *Code :* gmd:sourceCitation
                
- *Description :* Référence recommandée pour les données sources
        
- *Information complémentaire :* Indication des sources du jeu de données source décrit. Le nom / titre du fichier de données source de même qu''une date du type correspondant (création, publication, traitement complémentaire) sont gérés ici. Des informations supplémentaires peuvent être indiquées au besoin. Elles sont du type CI_Citation et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-featureCatalogueCitation:
        
                        
**Référence du catalogue d'objet** 
                  
- *Code :* gmd:featureCatalogueCitation
                
- *Description :* Référence bibliographique complète à un ou plusieurs catalogues d'objets externes
        
- *Information complémentaire :* Référence bibliographique complète vers un ou plusieurs catalogues d'objets externes. La référence est du type de données CI_Citation et est gérée dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-MD-PortrayalCatalogueReference:
        
                        
**Référence du catalogue de présentation** 
                  
- *Code :* gmd:MD_PortrayalCatalogueReference
                
- *Description :* Classe avec l'information pour l'identification du catalogue de présentation utilisé
        
- *Information complémentaire :* La classe MD_PortrayalCatalogueReference comprend uniquement l'attribut portrayalCatalogueCitation. Le catalogue utilisé pour la description de la représentation des objets peut être identifié ici via le type de données CI_Citation. La classe MD_PortrayalCatalogueReference permet uniquement la description des conditions marginales de la représentation "cartographique" sans tenir compte de leur mise en oeuvre.
        
        
                        
.. _iso19139-gmd-parameterCitation:
        
                        
**Référence sur les paramètres** 
                  
- *Code :* gmd:parameterCitation
                
- *Description :* Référence bibliographique sur les paramètres
        
- *Information complémentaire :* Titre et date (citation) avec la description des paramètres
        
        
                        
.. _iso19139-gmd-peakResponse:
        
                        
**Réponse maxmale** 
                  
- *Code :* gmd:peakResponse
                
- *Description :* Longueur d'onde à laquelle la réponse est la plus haute
        
- *Information complémentaire :* Longueur d'onde à laquelle l'intensité de la réponse est la plus forte.
        
        
                        
.. _iso19139-srv-repeatability:
        
                        
**Répétabilité** 
                  
- *Code :* srv:repeatability
                
- *Description :* Indication sur la répétabilité du paramètre
        
        
        
                        
.. _iso19139-gmd-MD-Resolution:
        
                        
**Résolution** 
                  
- *Code :* gmd:MD_Resolution
                
- *Description :* Classe avec le degré de détail exprimé avec un facteur d'échelle ou une distance au sol
        
- *Information complémentaire :* Dans la classe MD_Resolution, le degré de spécification est indiqué en référence à l'objet. Cela signifie que la résolution d'un objet est à indiquer ici dans le cas d'une trame (exemple : la résolution spatiale d'une orthophoto de 50cm). Outre la distance au sol (distance), il est également possible de saisir une échelle pour indiquer le degré de spécification, éventualité à envisager dans le cas de données vectorielles ou tramées. l'échelle est précisée en recourant à la classe MD_RepresentativeFraction.
        
        
                        
.. _iso19139-gmd-resolution:
        
                        
**Résolution** 
                  
- *Code :* gmd:resolution
                
- *Description :* Degré de détail dans le jeu de données de type raster
        
- *Information complémentaire :* Degré de spécification de la trame, c.-à-d. description d'une extension, d'une dimension ou d'un nombre de cellules.
        
        
                        
.. _iso19139-gmd-spatialResolution:
        
                        
**Résolution spatiale** 
                  
- *Code :* gmd:spatialResolution
                
- *Description :* Facteur qui donne une indication générale sur la densité de données spatiales dans le jeu de données
        
- *Information complémentaire :* Facteur donnant une indication générale de la résolution spatiale du jeu de données. Il est indiqué sous forme d'échelle ou d'élément de comparaison au sol. Ces informations sont gérées dans la classe MD_Resolution.
      La résolution spatiale se rapporte au niveau de détail de la série de données. Elle est exprimée comme un ensemble
      de valeurs de distance de résolution allant de zéro à plusieurs valeurs (normalement utilisé pour des données
      maillées et des produits dérivés d’imagerie) ou exprimée en échelles équivalentes (habituellement utilisées pour les
      cartes ou les produits dérivés de cartes).      
    
        
        
                        
.. _iso19139-gmd-result:
        
                        
**Résultat** 
                  
- *Code :* gmd:result
                
- *Description :* Valeur (ou jeu de valeur) obtenue en appliquant une mesure de qualité de donnée ou par le résultat d'une comparaison des valeurs (ou jeu de valeurs) obtenues avec un niveau de qualité spécifique
        
- *Information complémentaire :* Valeur (ou ensemble de valeurs) déduite de la mesure de qualité utilisée ou résultats provenant de la comparaison de ces valeurs avec un indicateur de qualité spécifié.
        
        
                        
.. _iso19139-gmd-DQ-ConformanceResult:
        
                        
**Résultat de conformité** 
                  
- *Code :* gmd:DQ_ConformanceResult
                
- *Description :* Classe qui contient l'information sur les résultats des évaluations des valeurs (ou jeu de valeurs) obtenues en comparaison avec une valeur de qualité spécifique
        
- *Information complémentaire :* Classe contenant des informations relatives aux résultats d'exploitation se déduisant de la comparaison des valeurs obtenues avec un indicateur de qualité spécifié.
        
        
                        
.. _iso19139-gmd-DQ-QuantitativeResult:
        
                        
**Résultat quantitatif** 
                  
- *Code :* gmd:DQ_QuantitativeResult
                
- *Description :* Classe contenant l'information des valeurs (ou jeu de valeurs) obtenu en appliquant sur les données une mesure de qualité
        
- *Information complémentaire :* Informations relatives aux résultats des tests livrés par l'application d'une mesure de qualité.
        
        
                        
.. _iso19139-gmd-abstract:
        
                        
**Résumé** 
                  
- *Code :* gmd:abstract
                
- *Description :* Court résumé explicatif du contenu des ressources
        
- *Information complémentaire :* Court résumé descriptif du contenu du jeu de données. Cet attribut est du type PT_FreeText et est géré dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-pass:
        
                        
**Réussi** 
                  
- *Code :* gmd:pass
                
- *Description :* Indication sur le résultat de conformance où 0 = échoué et 1= réussi
        
- *Information complémentaire :* Indication du résultat du test de concordance (0 = échec, 1= succès).
        
        
                        
.. _iso19139-gmd-role:
        
                        
**Rôle** 
                  
- *Code :* gmd:role
                
- *Description :* Fonction accomplie par le service responsable
        
- *Information complémentaire :* Rôle endossé par le service responsable (prestataire, gestionnaire, propriétaire, utilisateur, distributeur, créateur de données, instance compétente, évaluateur de données, responsable de leur traitement ou de leur publication, auteur, éditeur ou partenaire commercial).
        
        
                        
.. _iso19139-gmd-schemaAscii:
        
                        
**Schéma en ASCII** 
                  
- *Code :* gmd:schemaAscii
                
- *Description :* Schéma d'application entièrement donné dans un fichier ASCII
        
- *Information complémentaire :* Schéma d'application dans un fichier ASCII.
        
        
                        
.. _iso19139-srv-SV-ParameterDirection:
        
                        
**Sens du paramètre** 
                  
- *Code :* srv:SV_ParameterDirection
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-seriesMetadata:
        
                        
**SeriesMetadata** 
                  
- *Code :* gmd:seriesMetadata
                
- *Description :* SeriesMetadata
        
        
        
                        
.. _iso19139-gmd-LI-Source:
        
                        
**Source** 
                  
- *Code :* gmd:LI_Source
                
- *Description :* Classe contenant l'information sur les données sources utilisées pour créer les données spécifiées par l'attribut scope (79) du domaine d'applicabilité
        
- *Information complémentaire :* Classe contenant des informations relatives aux données source dont le domaine de données est issu et auxquelles il est renvoyé dans la classe DQ_DataQuality (attribut concernant l'étendue des indications de qualité). Des informations complémentaires peuvent être trouvées sous DQ_Dataquality.
        
        
                        
.. _iso19139-gmd-source:
        
                        
**Source** 
                  
- *Code :* gmd:source
                
- *Description :* Informations sur la source de la donnée
        
- *Information complémentaire :* Informations relatives aux données source. Elles sont gérées dans la classe LI_Source.
        
        
                        
.. _iso19139-gmd-sourcegmd-MD-ExtendedElementInformation:
        
                        
**Source** (cf. `gmd:MD_ExtendedElementInformation <#iso19139-gmd-md-extendedelementinformation>`_)
                  
- *Code :* gmd:source
                
- *Description :* Nom de la personne ou de l'organisation ayant créé les extensions
        
- *Information complémentaire :* Nom de la personne ou de l'organisation ayant procédé à l'extension de la norme.
        
        
                        
.. _iso19139-gmd-onLine:
        
                        
**Sources en ligne** 
                  
- *Code :* gmd:onLine
                
- *Description :* Information sur les sources en ligne depuis lesquelles la ressource peut être obtenue
        
- *Information complémentaire :* Informations relatives à la source en ligne via laquelle le jeu de données peut être acquis. Ces informations sont du type CI_OnlineRessource et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-offLine:
        
                        
**Sources hors connexion** 
                  
- *Code :* gmd:offLine
                
- *Description :* Information sur les médias off-line depuis lesquels la ressource peut être obtenue
        
- *Information complémentaire :* Informations concernant le support hors ligne sur lequel le jeu de données peut être obtenu. Ces informations sont gérées dans la classe MD_Medium.
        
        
                        
.. _iso19139-gmd-specificationgmd-DQ-ConformanceResult:
        
                        
**Spécification** (cf. `gmd:DQ_ConformanceResult <#iso19139-gmd-dq-conformanceresult>`_)
                  
- *Code :* gmd:specification
                
- *Description :* Informations de référence des spécifications de produits ou des exigences des utilisateurs avec lesquelles les données sont comparées
        
- *Information complémentaire :* Description des spécifications du produit ou des exigences de l'utilisateur avec lesquelles les données ont été comparées.
        
        
                        
.. _iso19139-gmd-specificationgmd-MD-Format:
        
                        
**Spécification** (cf. `gmd:MD_Format <#iso19139-gmd-md-format>`_)
                  
- *Code :* gmd:specification
                
- *Description :* Nom d'une spécification de sous-ensemble, profil ou produit du format
        
- *Information complémentaire :* Nom d'une spécification partielle, de profil ou de produit du format.
        
        
                        
.. _iso19139-gmd-subset:
        
                        
**Subset** 
                  
- *Code :* gmd:subset
                
- *Description :* Subset
        
        
        
                        
.. _iso19139-gmd-southBoundLatitude:
        
                        
**Sud** 
                  
- *Code :* gmd:southBoundLatitude
                
- *Description :* Coordonnée la plus au sud de la limite de l'étendue du jeu de données, exprimée en latitude avec des degrés décimaux (NORD positif)
        
- *Information complémentaire :* Limite sud de l'extension du jeu de données, exprimée en latitude géographique (degrés décimaux) comptée positivement vers le nord.
        
        
                        
.. _iso19139-gmd-superset:
        
                        
**Superset** 
                  
- *Code :* gmd:superset
                
- *Description :* Superset
        
        
        
                        
.. _iso19139-gmd-classificationSystem:
        
                        
**Système de classification** 
                  
- *Code :* gmd:classificationSystem
                
- *Description :* Nom du système de classification
        
- *Information complémentaire :* Le nom du système de classification peut être indiqué ici, s'il en existe un.
        
        
                        
.. _iso19139-gmd-MD-ReferenceSystem:
        
                        
**Système de référence** 
                  
- *Code :* gmd:MD_ReferenceSystem
                
- *Description :* Classe pour l'information sur le système de référence
        
- *Information complémentaire :* La classe MD_ReferenceSystem décrit le système de référence spatial et temporel utilisé pour le jeu de données. Dans cette classe, le lien avec le système géodésique de référence est établi à l'aide de l'attribut "referenceSystemIdentifier". Seuls le nom du système de référence et l'organisation associée sont saisis, aucun paramètre concret n''est entré.
        
        
                        
.. _iso19139-gmd-sourceReferenceSystem:
        
                        
**Système de référence spatiale des données sources** 
                  
- *Code :* gmd:sourceReferenceSystem
                
- *Description :* Système de référence spatiale utilisé par les données sources
        
- *Information complémentaire :* Indications relatives au système de référence. Elles sont gérées dans la classe MD_ReferenceSystem.
        
        
                        
.. _iso19139-gmd-CI-Series:
        
                        
**Séries** 
                  
- *Code :* gmd:CI_Series
                
- *Description :* Type de données pour l'information sur les séries ou le jeu de données global, à qui appartient le jeu de données
        
- *Information complémentaire :* Type de données réservé aux informations relatives à la série ou au jeu de données composé dont le jeu de données fait partie. Il s'agit d'informations concernant le nom, l'édition et (en cas de disponibilité) le numéro de la page sur laquelle cette série du jeu de données peut être trouvée. l'ensemble des feuilles de la carte au 1:25''000 de swisstopo constitue par exemple une série.
        
        
                        
.. _iso19139-gmd-series:
        
                        
**Séries** 
                  
- *Code :* gmd:series
                
- *Description :* Information sur la série (ou sur le jeu de données global) de laquelle le jeu de données est une partie
        
- *Information complémentaire :* Information relative à la série ou au jeu de données composé dont le jeu de données est issu. 
      Exemple : la série de toutes les cartes nationales au 1:25'000. 
      Ces informations sont du type CI_Series et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-transferSize:
        
                        
**Taille transfer** 
                  
- *Code :* gmd:transferSize
                
- *Description :* Taille estimée d'une unité dans le format de transfert spécifié, exprimé en mégabits. La taille de transfert est > 0.0
        
- *Information complémentaire :* Taille approchée d'un fichier (exprimée en mégaoctets) dans le format spécifié. La taille de transfert est > 0.0
        
        
                        
.. _iso19139-gmd-fees:
        
                        
**Taxes** 
                  
- *Code :* gmd:fees
                
- *Description :* Taxes et conditions pour accéder à la ressource. Les unités monétaires sont incluses (comme spécifié dans ISO 4217)
        
- *Information complémentaire :* Emoluments relatifs à l'obtention ou à l'utilisation des données. Indications fournies dans les unités monétaires définies dans la norme ISO 4217.
        
        
                        
.. _iso19139-gmd-fileDecompressionTechnique:
        
                        
**Technique de décompression du fichier** 
                  
- *Code :* gmd:fileDecompressionTechnique
                
- *Description :* Recommandations sur les algorithmes et les processus qui peuvent être appliqués pour lire ou ouvrir une ressource, à laquelle des techniques de compressions ont été appliquées.
        
- *Information complémentaire :* Remarques relatives aux algorithmes ou aux processus à mettre en ?uvre pour la lecture ou l'extension de la ressource en cas de compression de cette dernière.
        
        
                        
.. _iso19139-gmd-turnaround:
        
                        
**Temps d'attente** 
                  
- *Code :* gmd:turnaround
                
- *Description :* Temps d'attente usuel pour la préparation d'une commande
        
- *Information complémentaire :* Temps de traitement usuel d'une commande.
        
        
                        
.. _iso19139-gco-CharacterString:
        
                        
**Texte** 
                  
- *Code :* gco:CharacterString
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-PT-FreeText:
        
                        
**Texte libre** 
                  
- *Code :* gmd:PT_FreeText
                
- *Description :* Classe pour la description d'un texte libre multilingue sur des éléments de métadonnées
        
- *Information complémentaire :* Classe réservée à la description d'un élément textuel de métadonnées, libre et multilingue. Le type de données PT_FreeText permet la gestion d'informations textuelles dans différentes langues. Le multilinguisme est accepté en cinq endroits différents du modèle CH-Profil : la description du contenu des données (résumé dans la classe MD_Identification), le titre d'indications de sources (titre dans CI_Citation), le nom et l'abréviation de services compétents (organisationName et organisationAcronym dans CI_ResponsibleParty), le code d'identifiants (code dans MD_Identifier) et les mots clés (keyword dans MD_Keyword).
        
        
                        
.. _iso19139-gmd-topicCategory:
        
                        
**Thématique** 
                  
- *Code :* gmd:topicCategory
                
- *Description :* Thème(s) principal(aux) du jeu de données
        
- *Information complémentaire :* Thème principal (ou thèmes principaux) du jeu de données. 
      Ce thème ou groupe de thèmes permet de trouver un jeu de données. 
      Cet attribut se fondant sur une liste de codes de la norme ISO (MD_TopicCategoryCode), une recherche standardisée 
      par thèmes est possible au plan international. 
      Les thèmes suivants sont répertoriés : agriculture, biologie, limites, climatologie et météorologie, économie, indications altimétriques, environnement, sciences de la Terre, santé, images et cartes de base, armée et renseignement, cours d'eau intérieurs, indications de lieux, mers et océans, aménagement du territoire et cadastre, société, subdivisions, transport, infrastructures de transport.
    
        
        
                        
.. _iso19139-gmd-MD-TopicCategoryCode:
        
                        
**Thématique** 
                  
- *Code :* gmd:MD_TopicCategoryCode
                
- *Description :* Code de la catégorie du sujet
        
        
        
                        
.. _iso19139-gmd-title:
        
                        
**Titre** 
                  
- *Code :* gmd:title
                
- *Description :* Nom avec lequel la ressource en question est connue
        
- *Information complémentaire :* 
      
      Nom caractéristique et souvent unique sous lequel la ressource est connue.
      
      Titre/nom. Ces informations sont du type PT_FreeText et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-collectiveTitle:
        
                        
**Titre collectif** 
                  
- *Code :* gmd:collectiveTitle
                
- *Description :* Titre commun avec indication d'une appartenance. Note : le titre identifie des éléments d'une série collective, combiné avec l'information sur quels volumes sont à disposition à la source citée
        
- *Information complémentaire :* Cet élement est utilisé en Suisse pour designer le nom d'une géodonnées de base qui correspond à l'entrée du catalogue Annexe I de la OGéo, car il est possible que plusieurs jeux de données "physiques" soient attribuée à une entrée "juridique". Ex.: L'entrée no. 47 "Cartes géophysiques" consiste de 3 jeux de données "Cartes géophysiques 1:500000", "Cartes géophysisques spéciales" et "Atlas gravimétrique 1:100000"
        
        
                        
.. _iso19139-gmd-alternateTitle:
        
                        
**Titre court** 
                  
- *Code :* gmd:alternateTitle
                
- *Description :* Nom raccourci ou autre façon d'écrire le nom, sous lequel l'information des informations de références est connue. Exemple : DCW pour "Digital Chart of the World"
        
- *Information complémentaire :* Nom abrégé ou orthographe du titre/nom différente de celle sous laquelle l'information correspondante est connue. Exemple : DCW pour "Digital Chart of the World"
        
        
                        
.. _iso19139-gmd-associationType:
        
                        
**Type d'association** 
                  
- *Code :* gmd:associationType
                
- *Description :* Type d'association sur le jeu de données rassemblé
        
- *Information complémentaire :* Type de relation entre jeux de données de rang inférieur, autrement dit, nature du lien unissant les deux jeux de données. La sélection s'opère dans la liste suivante : transfert à titre de comparaison, comparaison avec le jeu de données de rang supérieur, partie d'une structure de données identique, renvoi à une source, couple stéréoscopique.
        
        
                        
.. _iso19139-gco-attributeType:
        
                        
**Type d'attribut** 
                  
- *Code :* gco:attributeType
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-contentType:
        
                        
**Type d'information** 
                  
- *Code :* gmd:contentType
                
- *Description :* Type d'information représenté par la valeur de la cellule
        
- *Information complémentaire :* Type d'information représenté par la valeur de la cellule. La cellule est une image, une classification thématique ou une mesure physique.
        
        
                        
.. _iso19139-gmd-initiativeType:
        
                        
**Type d'initiative** 
                  
- *Code :* gmd:initiativeType
                
- *Description :* Type d'initiative pour laquelle le jeu de données rassemblé a été produit
        
- *Information complémentaire :* Type de l'initiative ou du projet ayant motivé la saisie des données de rang inférieur (exemple : campagne, collecte, analyse, mission, étude, etc.)
        
        
                        
.. _iso19139-gmd-geometricObjectType:
        
                        
**Type d'objets géométriques** 
                  
- *Code :* gmd:geometricObjectType
                
- *Description :* Nom des objets spatiaux point et vecteur utilisés pour localiser les positions à zéro, une, deux et trois dimensions dans le jeu de données
        
- *Information complémentaire :* Nom du type d'objet géométrique. Sélection dans la liste suivante : complexe, combinaison, ligne ouverte, point, primitive en 3D, ligne fermée.
        
        
                        
.. _iso19139-srv-couplingType:
        
                        
**Type de couplage** 
                  
- *Code :* srv:couplingType
                
- *Description :* Type de couplage
        
        
        
                        
.. _iso19139-gmd-dateType:
        
                        
**Type de date** 
                  
- *Code :* gmd:dateType
                
- *Description :* Evénements en relations avec la date
        
- *Information complémentaire :* Evénement auquel la date se rapporte (création, publication, traitement), respectant le type de la date.
        
        
                        
.. _iso19139-gmd-type:
        
                        
**Type de mot clé** 
                  
- *Code :* gmd:type
                
- *Description :* Thèmes utilisés pour grouper des mots clés similaires
        
- *Information complémentaire :* Thème utilisé pour grouper des mots clés similaires. Les thèmes suivants sont disponibles : discipline, lieu, couche, intervalle de temps, thème.
        
        
                        
.. _iso19139-gmd-cellGeometry:
        
                        
**Type de raster** 
                  
- *Code :* gmd:cellGeometry
                
- *Description :* Identification du type de raster (point ou cellule)
        
- *Information complémentaire :* Définition du type de trame (point ou cellule).
        
        
                        
.. _iso19139-gmd-spatialRepresentationType:
        
                        
**Type de représentation spatiale** 
                  
- *Code :* gmd:spatialRepresentationType
                
- *Description :* Méthode utilisée pour représenter spatialement l'information géographique
        
- *Information complémentaire :* Méthode utilisée pour la représentation spatiale des informations géographiques par des vecteurs, un quadrillage, des cartes, des tableaux, ou d'autres moyens similaires.
        
        
                        
.. _iso19139-srv-serviceType:
        
                        
**Type de service** 
                  
- *Code :* srv:serviceType
                
- *Description :* 
      Classification qui permet de rechercher les services de données géographiques disponibles. Un service donné ne
      peut être classé que dans une seule catégorie.
        
        
- Liste de suggestions :

        
   - OGC Web Map Service (OGC:WMS)

        
   - OGC Web Feature Service (OGC:WFS)

        
   - OGC Web Coverage Service (OGC:WCS)

        
   - Téléchargement (W3C:HTML:DOWNLOAD)

        
   - Information (W3C:HTML:LINK)

        
        
                        
.. _iso19139-gmd-dataType:
        
                        
**Type de valeur** 
                  
- *Code :* gmd:dataType
                
- *Description :* Code qui identifie le genre de valeur fourni dans l'élément étendu
        
- *Information complémentaire :* Code définissant le type de données de l'élément étendu.
        
        
                        
.. _iso19139-gmd-valueType:
        
                        
**Type de valeur** 
                  
- *Code :* gmd:valueType
                
- *Description :* Valeur quantitative, ou domaine de valeur, pour l'appréciation de qualité
        
- *Information complémentaire :* Valeur (ou domaine de valeurs) quantitative pour l'appréciation de la qualité.
        
        
                        
.. _iso19139-srv-valueType:
        
                        
**Type de valeur** 
                  
- *Code :* srv:valueType
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-featureTypes:
        
                        
**Types d'objet** 
                  
- *Code :* gmd:featureTypes
                
- *Description :* Sous-ensemble de types d'objets, tirés du catalogue d'objets cité, intervenant dans le jeu de données
        
- *Information complémentaire :* Sous-ensemble des types d'objets du catalogue cité, présents dans le jeu de données.
        
        
                        
.. _iso19139-gmd-CI-Telephone:
        
                        
**Téléphone** 
                  
- *Code :* gmd:CI_Telephone
                
- *Description :* Type de données pour le numéro de téléphone de la personne ou du service responsable
        
- *Information complémentaire :* Type de données pour les numéros de téléphone.
        
        
                        
.. _iso19139-gmd-phone:
        
                        
**Téléphone** 
                  
- *Code :* gmd:phone
                
- *Description :* Numéro de téléphone avec lequel la personne ou l'organisation responsable peut être contactée
        
- *Information complémentaire :* Numéro de téléphone. Ces informations sont du type CI_Telephone et sont gérées dans la classe du même nom.
        
        
                        
.. _iso19139-gmd-phone:
        
                        
**Téléphone** 
                  
- *Code :* gmd:phone
                
- *Description :* Type de données pour le numéro de téléphone de la personne ou du service responsable
        
- *Information complémentaire :* Type de données pour les numéros de téléphone.
        
        
                        
.. _iso19139-src:
        
                        
**URL** 
                  
- *Code :* src
                
- *Description :* URL du document.
        
        
        
                        
.. _iso19139-gmd-URL:
        
                        
**URL libre** 
                  
- *Code :* gmd:URL
                
- *Description :* Classe destinée à la description d'un élément de métadonnées URL libre multilingue
        
- *Information complémentaire :* Classe destinée à la description d'un élément de métadonnées URL libre multilingue. Le type de données PT_FreeText permet de gérer des informations textuelles en plusieurs langues.
        
        
                        
.. _iso19139-uuidref:
        
                        
**Unique identifier** 
                  
- *Code :* uuidref
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-units:
        
                        
**Unité** 
                  
- *Code :* gmd:units
                
- *Description :* Unité dans laquelle les longueurs d'ondes du senseur sont indiquées
        
- *Information complémentaire :* Unité dans laquelle la longueur d'onde du capteur est exprimée.
        
        
                        
.. _iso19139-unit:
        
                        
**Unité** 
                  
- *Code :* unit
                
- *Description :* 
        
        
        
                        
.. _iso19139-uom:
        
                        
**Unité** 
                  
- *Code :* uom
                
- *Description :* Unité de mesure
        
        
- Liste de suggestions :

        
   - mètres (m)

        
        
                        
.. _iso19139-gmd-densityUnits:
        
                        
**Unité de densité** 
                  
- *Code :* gmd:densityUnits
                
- *Description :* Unité de mesure pour la densité d'enregistrement
        
- *Information complémentaire :* Unité de mesure de la densité d'écriture.
        
        
                        
.. _iso19139-gco-Measuregmd-EX-VerticalExtent:
        
                        
**Unité de mesure** (cf. `gmd:EX_VerticalExtent <#iso19139-gmd-ex-verticalextent>`_)
                  
- *Code :* gco:Measure
                
- *Description :* Unité de l'indication de l'étendue verticale. Exemple : mètre, pied, millimètre, hectopascal
        
- *Information complémentaire :* Unité altimétrique. Exemples : mètre, pied, millimètre, hectopascal
        
        
                        
.. _iso19139-gmd-valueUnit:
        
                        
**Unité de valeur** 
                  
- *Code :* gmd:valueUnit
                
- *Description :* Unité de valeur pour la documentation des résultats de qualité sur les données
        
- *Information complémentaire :* Unité de valeur pour la documentation du résultat de la qualité des données.
        
        
                        
.. _iso19139-gmd-unitsOfDistribution:
        
                        
**Unités de distribution** 
                  
- *Code :* gmd:unitsOfDistribution
                
- *Description :* Parties, niveaux, surfaces géographiques, etc., dans lesquelles les données sont à disposition
        
- *Information complémentaire :* Découpage en carroyages, couches, zones géographiques, etc. permettant l'obtention des données.
        
        
                        
.. _iso19139-gmd-MD-Usage:
        
                        
**Utilisation** 
                  
- *Code :* gmd:MD_Usage
                
- *Description :* Classe pour une brève description de la manière dont les ressources sont actuellement utilisées ou ont été utilisées
        
- *Information complémentaire :* Brève description de la manière dont les ressources sont actuellement utilisées ou l'ont été, avant tout lorsqu''il s'agit d'applications spéciales poursuivant un objectif particulier. Exemple : un forestier saisit une clairière avec une précision, des signes conventionnels et des attributs bien différents de ceux qu''emploierait un biologiste.
        
        
                        
.. _iso19139-gmd-specificUsage:
        
                        
**Utilisation** 
                  
- *Code :* gmd:specificUsage
                
- *Description :* Brève description de l'utilisation actuelle de la ressource et/ou de la série de ressource
        
- *Information complémentaire :* Brève description de l'utilisation de la ressource et/ou de la série de ressources sous forme de texte.
        
        
                        
.. _iso19139-gmd-resourceSpecificUsage:
        
                        
**Utilisation spécifique de la ressource** 
                  
- *Code :* gmd:resourceSpecificUsage
                
- *Description :* Informations sur des applications spécifiques pour lesquelles la ressource est ou sera utilisée
        
- *Information complémentaire :* Informations concernant des applications particulières pour lesquelles la ou les ressources sont ou ont été utilisées. Ces informations sont gérées dans la classe MD_Usage.
        
        
                        
.. _iso19139-gmd-valuegmd-DQ-QuantitativeResult:
        
                        
**Valeur** (cf. `gmd:DQ_QuantitativeResult <#iso19139-gmd-dq-quantitativeresult>`_)
                  
- *Code :* gmd:value
                
- *Description :* Valeurs quantitatives, déterminées par la procédure d'évaluation utilisée.
        
- *Information complémentaire :* Valeurs quantitatives définies par le processus d'exploitation.
        
        
                        
.. _iso19139-gmd-value:
        
                        
**Valeur** 
                  
- *Code :* gmd:value
                
- *Description :* 
        
        
        
                        
.. _iso19139-gmd-domainValue:
        
                        
**Valeur Domaine** 
                  
- *Code :* gmd:domainValue
                
- *Description :* Valeurs valides qui peuvent être assignées à l'élément étendu
        
- *Information complémentaire :* Valeurs licites pouvant être affectées à l'élément étendu.
        
        
                        
.. _iso19139-gmd-maxValue:
        
                        
**Valeur maximale** 
                  
- *Code :* gmd:maxValue
                
- *Description :* Longueur d'onde la plus longue que le senseur est capable de collecter à l'intérieur d'une bande donnée
        
- *Information complémentaire :* Longueur d'onde la plus élevée à laquelle le capteur est capable d'effectuer une mesure au sein de la gamme mentionnée.
        
        
                        
.. _iso19139-gmd-maximumValue:
        
                        
**Valeur maximale** 
                  
- *Code :* gmd:maximumValue
                
- *Description :* Indication de l'altitude du point le plus haut du jeu de données
        
- *Information complémentaire :* Altitude du point le plus élevé du jeu de données
        
        
                        
.. _iso19139-gmd-minimumValue:
        
                        
**Valeur minimale** 
                  
- *Code :* gmd:minimumValue
                
- *Description :* Indication de l'altitude du point le plus bas du jeu de données
        
- *Information complémentaire :* Altitude du point le plus bas du jeu de données.
        
        
                        
.. _iso19139-gmd-minValue:
        
                        
**Valeur minimum** 
                  
- *Code :* gmd:minValue
                
- *Description :* Longueur d'onde la plus courte que le senseur est capable de collecter à l'intérieur d'une bande donnée
        
- *Information complémentaire :* Longueur d'onde la plus courte à laquelle le capteur est capable d'effectuer une mesure au sein de la gamme mentionnée.
        
        
                        
.. _iso19139-gmd-DQ-TemporalValidity:
        
                        
**Validité temporelle** 
                  
- *Code :* gmd:DQ_TemporalValidity
                
- *Description :* Classe pour la description de la validité des données du domaine d'applicabilité, en regard de l'aspect temporel
        
- *Information complémentaire :* Classe destinée à la description de la validité des données définies dans le domaine de qualité associé.
        
        
                        
.. _iso19139-gmd-versiongmd-RS-Identifier:
        
                        
**Version** (cf. `gmd:RS_Identifier <#iso19139-gmd-rs-identifier>`_)
                  
- *Code :* gmd:version
                
- *Description :* Identification de la version pour la domaine de valeurs
        
- *Information complémentaire :* Numéro de version de l'espace nominal / de l'identifiant (alphanumérique).
        
        
                        
.. _iso19139-gmd-version:
        
                        
**Version** 
                  
- *Code :* gmd:version
                
- *Description :* Version du format (date,nombre,etc)
        
- *Information complémentaire :* Version du format de données, par exemple 6.0.
        
        
                        
.. _iso19139-gmd-metadataStandardVersion:
        
                        
**Version du standard de métadonnées** 
                  
- *Code :* gmd:metadataStandardVersion
                
- *Description :* Version (du profil) du standard de métadonnées utilisé.
        
- *Information complémentaire :* Version (du profil) de la norme sur les métadonnées utilisée.
        
        
                        
.. _iso19139-srv-serviceTypeVersion:
        
                        
**Version du type de service** 
                  
- *Code :* srv:serviceTypeVersion
                
- *Description :* Version du type de service
        
        
        
                        
.. _iso19139-gmd-city:
        
                        
**Ville** 
                  
- *Code :* gmd:city
                
- *Description :* Ville de l'emplacement
        
- *Information complémentaire :* Ville, localité
        
        
                        
.. _iso19139-gmd-graphicOverview:
        
                        
**Vue générale graphique** 
                  
- *Code :* gmd:graphicOverview
                
- *Description :* Vue générale graphique illustrant les ressources (y inclus une légende)
        
- *Information complémentaire :* Vue d'ensemble graphique de la ressource (légende incluse). Ces informations sont gérées dans la classe MD_BrowseGraphic.
        
        
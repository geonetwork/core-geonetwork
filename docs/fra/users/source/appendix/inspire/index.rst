.. include:: ../../substitutions.txt

.. _inspire:

La directive INSPIRE et |project_name|
======================================


Cette section explique les options de configuration permmettant la
mise en place d'un catalogue suivant les recommandations de `la directive INSPIRE <http://inspire.jrc.ec.europa.eu/index.cfm>`_.

Il est possible d'activer tout ou partie de ces options. Certains packages (tel que GéoSource)
est pré-configuré avec une partie de ces options.


Activation des règles de validation Schématron pour INSPIRE
-----------------------------------------------------------

Après l'installation, l'administrateur du catalogue peut ajouter les règles de validations en réalisant les 
actions suivantes :

- Version 2.6.x : copier manuellement les règles INSPIRE depuis le code source ou lancer dans le répertoire schématron 
  la commande ::
	
	ant inspire
	
	
	
- Version 2.7.x : renommer le fichier *schematron-rules-inspire.xsl.disabled* en *schematron-rules-inspire.xsl*


Une fois la modification faite, il est nécessaire de relancer l'application.


Ajouter le thésaurus GEMET, les thèmes INSPIRE et la taxonomie des services
---------------------------------------------------------------------------

Depuis la version 2.7.x, il est possible d'ajouter ces thésaurus depuis l'interface d'administration des thésaurus.

.. figure:: thesaurus-install.png


Activer la vue INSPIRE
----------------------

Dans le fichier *config-gui.xml*, modifier la configuration des vues de la manière suivante ::

	 <metadata-tab>
	  <simple flat="true"  default="true"/>
	  <advanced/><!-- This view should not be removed as this is the only view to be able 
	  to edit all elements defined in a schema. -->
	  <iso/>
	  <!-- This view display all INSPIRE recommended elements
	  in a view -->
	  <inspire flat="true"/>
	  <xml/>
	 </metadata-tab>



Activer le formulaire de recherche et le CSW INSPIRE
----------------------------------------------------

Depuis l'interface d'administration, configuration du système, activer l'option INSPIRE.
Il est nécessaire de reconstruire l'index après activation de cette option.
Cette option active également le CSW INSPIRE tel que décrit dans le `guide technique sur les
service de découverte <http://inspire.jrc.ec.europa.eu/index.cfm/pageid/5>`_


Affiner la configuration des suggestions dans l'éditeur
-------------------------------------------------------

|project_name| permet de contrôler quels éléments sont ajoutés lorsque l'utilisateur ajoutes des éléments
dans l'éditeur. Par défaut, tous les éléments obligatoires sont ajoutés automatiquement. Les autres (les optionnels)
peuvent être contrôlé via le mécanisme de suggestion.

Par exemple, INSPIRE demande la présence d'une organisation et d'un email dans les informations sur un contact.
Ceci est possible, en configurant le fichier *xml/schemas/iso19139/schema-suggestion.xml* de la manière suivante ::

	<!-- INSPIRE Suggestion for contact suggest 
		organisation name and email address as defined in metadata IR.
		ie. less fields than in |project_name| default behaviour.-->
	<field name="gmd:CI_ResponsibleParty">
		<suggest name="gmd:organisationName"/>
		<suggest name="gmd:contactInfo"/>
	</field>
	
	<field name="gmd:CI_Contact">
		<suggest name="gmd:address"/>
	</field>
	
	<field name="gmd:CI_Address">
		<suggest name="gmd:electronicMailAddress"/>
	</field>

De la même manière pour la section distribution ::

	<!-- Add a distribution format / INSPIRE requirement -->
	<field name="gmd:distributionInfo">
		<suggest name="gmd:MD_Distribution"/>
	</field>
	<field name="gmd:MD_Distribution">
		<suggest name="gmd:distributionFormat"/>
		<suggest name="gmd:transferOptions"/>
	</field>
	<field name="gmd:distributionFormat">
		<suggest name="gmd:MD_Format"/>
	</field>

Cette configuration permet une saisie plus rapide de l'essentiel des informations dans l'éditeur.



Indexation de l'étendue temporelle
----------------------------------

Par défaut, |project_name| n'indexe pas les dates de publication, révision, création en tant qu'étendue temporelle.

Pour activer cette option, modifier le paramètre *useDateAsTemporalExtent* dans le fichier *xml/schemas/iso19139/index-fields.xml* ::

	<!-- If identification creation, publication and revision date
          should be indexed as a temporal extent information (eg. in INSPIRE 
          metadata implementing rules, those elements are defined as part
          of the description of the temporal extent). -->
	<xsl:variable name="useDateAsTemporalExtent" select="false()"/>


Configurer les listes de valeurs de suggestions
-----------------------------------------------

|project_name| dispose d'un mécanisme permettant d'ajouter des listes de propositions à côté des champs en édition.
Par example, pour le champ dénominateur de l'échelle, une liste composée des valeurs 10000, 25000, 50000, ... est proposée.

INSPIRE demande la saisie de certaine valeur pour le champ type de service. Pour cela, modifier le fichier 
*xml/schemas/iso19139/loc/en/labels.xml* de la manière suivante ::

 	<element name="srv:serviceType">
        <label>Service Type</label>
        <description>Service type name from a registry of services. For example, the values of the
            nameSpace and name attributes of GeneralName may be 'OGC' and 'catalogue'</description>
        <helper>
            <!--<option value="OGC:WMS">OGC Web Map Service (OGC:WMS)</option>
            <option value="OGC:WFS">OGC Web Feature Service (OGC:WFS)</option>
            <option value="OGC:WCS">OGC Web Coverage Service (OGC:WCS)</option>
            <option value="W3C:HTML:DOWNLOAD">Download (W3C:HTML:DOWNLOAD)</option>
            <option value="W3C:HTML:LINK">Information (W3C:HTML:LINK)</option>-->
            <!-- INSPIRE Service type defined in MD IR / 1.3.1 Spatial data service type -->
            <option value="discovery">Discovery Service (discovery)</option>
            <option value="view">View Service (view)</option>
            <option value="download">Download Service (download)</option>
            <option value="transformation">Transformation Service (transformation)</option>
            <option value="other">Other Services (other)</option> 
        </helper>
    </element>


Cacher des valeurs dans les listes de choix
-------------------------------------------

Afin de simplifier la saisie tout en restant conforme aux demandes de la directive INSPIRE, 
il est possible de cacher certaines valeurs dans les listes de choix (ie. codeList).
Pour cela il faut utiliser l'attribut *hideInEditMode* dans les fichiers de liste de valeur

Par example, pour cacher les valeurs *model* et *attribut* dans le niveau de hiérarchie, modifier le fichier
*xml/schemas/iso19139/loc/en/codelists.xml* ::


	<codelist name="gmd:MD_ScopeCode">
	  <entry hideInEditMode="true">
			<code>attribute</code>
			<label>Attribute</label>
			<description>Information applies to the attribute class</description>
		</entry>
		<entry>
			<code>service</code>
			<label>Service</label>
			<description>Information applies to a capability which a service provider entity makes available to a service user entity through a set of interfaces that define a behaviour, such as a use case</description>
		</entry>
		<entry hideInEditMode="true">
			<code>model</code>
			<label>Model</label>
			<description>Information applies to a copy or imitation of an existing or hypothetical object</description>
		</entry>







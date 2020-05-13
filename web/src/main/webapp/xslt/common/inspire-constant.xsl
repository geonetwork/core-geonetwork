<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright 2014-2016 European Environment Agency

    Licensed under the EUPL, Version 1.1 or – as soon
    they will be approved by the European Commission -
    subsequent versions of the EUPL (the "Licence");
    You may not use this work except in compliance
    with the Licence.
    You may obtain a copy of the Licence at:

    https://joinup.ec.europa.eu/community/eupl/og_page/eupl

    Unless required by applicable law or agreed to in
    writing, software distributed under the Licence is
    distributed on an "AS IS" basis,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.
    See the Licence for the specific language governing
    permissions and limitations under the Licence.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <!-- Define if search for regulation title should be strict or light. -->
  <xsl:variable name="inspireRegulationLaxCheck" select="false()"/>

  <xsl:variable name="inspireThemesMap">
    <map theme="Coordinate reference systems"
         monitoring="coordinateReferenceSystems" annex="I"/>
    <map theme="Elevation"
         monitoring="elevation" annex="II"/>
    <map theme="Land cover"
         monitoring="landCover" annex="II"/>
    <map theme="Orthoimagery"
         monitoring="orthoimagery" annex="II"/>
    <map theme="Geology"
         monitoring="geology" annex="II"/>
    <map theme="Statistical units"
         monitoring="statisticalUnits" annex="III"/>
    <map theme="Buildings"
         monitoring="buildings" annex="III"/>
    <map theme="Soil"
         monitoring="soil" annex="III"/>
    <map theme="Land use"
         monitoring="landUse" annex="III"/>
    <map theme="Human health and safety"
         monitoring="humanHealthAndSafety" annex="III"/>
    <map theme="Utility and governmental services"
         monitoring="utilityAndGovernmentalServices" annex="III"/>
    <map theme="Geographical grid systems"
         monitoring="geographicalGridSystems" annex="I"/>
    <map theme="Environmental monitoring facilities"
         monitoring="environmentalMonitoringFacilities" annex="III"/>
    <map theme="Production and industrial facilities"
         monitoring="productionAndIndustrialFacilities" annex="III"/>
    <map theme="Agricultural and aquaculture facilities"
         monitoring="agriculturalAndAquacultureFacilities" annex="III"/>
    <!--<map theme="Population distribution — demography"-->
    <map theme="Population distribution.*"
         monitoring="populationDistributionDemography" annex="III"/>
    <map theme="Area management/restriction/regulation zones and reporting units"
         monitoring="areaManagementRestrictionRegulationZonesAndReportingUnits" annex="III"/>
    <map theme="Natural risk zones"
         monitoring="naturalRiskZones" annex="III"/>
    <map theme="Atmospheric conditions"
         monitoring="atmosphericConditions" annex="III"/>
    <map theme="Meteorological geographical features"
         monitoring="meteorologicalGeographicalFeatures" annex="III"/>
    <map theme="Oceanographic geographical features"
         monitoring="oceanographicGeographicalFeatures" annex="III"/>
    <map theme="Sea regions"
         monitoring="seaRegions" annex="III"/>
    <map theme="Geographical names"
         monitoring="geographicalNames" annex="I"/>
    <map theme="Bio-geographical regions"
         monitoring="bioGeographicalRegions" annex="III"/>
    <map theme="Habitats and biotopes"
         monitoring="habitatsAndBiotopes" annex="III"/>
    <map theme="Species distribution"
         monitoring="speciesDistribution" annex="III"/>
    <map theme="Energy resources"
         monitoring="energyResources" annex="III"/>
    <map theme="Mineral resources"
         monitoring="mineralResources" annex="III"/>
    <map theme="Administrative units"
         monitoring="administrativeUnits" annex="I"/>
    <map theme="Addresses"
         monitoring="addresses" annex="I"/>
    <map theme="Cadastral parcels"
         monitoring="cadastralParcels" annex="I"/>
    <map theme="Transport networks"
         monitoring="transportNetworks" annex="I"/>
    <map theme="Hydrography"
         monitoring="hydrography" annex="I"/>
    <map theme="Protected sites"
         monitoring="protectedSites" annex="I"/>
  </xsl:variable>

  <xsl:variable name="eu10892010">
    <xsl:choose>
      <xsl:when test="$inspireRegulationLaxCheck">
        <cr>commission regulation (eu)</cr>
        <re>reglement (ue)</re>
        <in>inspire</in>
      </xsl:when>
      <xsl:otherwise>
        <en>Commission Regulation (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services</en>
        <bg>Регламент (ЕС) № 1089/2010 на Комисията от 23 ноември 2010 година за прилагане на Директива 2007/2/ЕО на Европейския парламент и на Съвета по отношение на оперативната съвместимост на масиви от пространствени данни и услуги за пространствени данни</bg>
        <es>Reglamento (UE) n ° 1089/2010 de la Comisión, de 23 de noviembre de 2010 , por el que se aplica la Directiva 2007/2/CE del Parlamento Europeo y del Consejo en lo que se refiere a la interoperabilidad de los conjuntos y los servicios de datos espaciales</es>
        <cs>Nařízení Komise (EU) č. 1089/2010 ze dne 23. listopadu 2010 , kterým se provádí směrnice Evropského parlamentu a Rady 2007/2/ES, pokud jde o interoperabilitu sad prostorových dat a služeb prostorových dat</cs>
        <da>Kommissionens forordning (EU) nr. 1089/2010 af 23. november 2010 om gennemførelse af Europa-Parlamentets og Rådets direktiv 2007/2/EF for så vidt angår interoperabilitet for geodatasæt og -tjenester</da>
        <de>Verordnung (EG) Nr. 1089/2010 der Kommission vom 23. November 2010 zur Durchführung der Richtlinie 2007/2/EG des Europäischen Parlaments und des Rates hinsichtlich der Interoperabilität von Geodatensätzen und -diensten</de>
        <et>Komisjoni määrus (EL) nr 1089/2010, 23. november 2010 , millega rakendatakse Euroopa Parlamendi ja nõukogu direktiivi 2007/2/EÜ seoses ruumiandmekogumite ja -teenuste ristkasutatavusega</et>
        <el>Κανονισμός (ΕΕ) αριθ. 1089/2010 της Επιτροπής, της 23ης Νοεμβρίου 2010 , σχετικά με την εφαρμογή της οδηγίας 2007/2/ΕΚ του Ευρωπαϊκού Κοινοβουλίου και του Συμβουλίου όσον αφορά τη διαλειτουργικότητα των συνόλων και των υπηρεσιών χωρικών δεδομένων</el>
        <fr>Règlement (UE) n ° 1089/2010 de la Commission du 23 novembre 2010 portant modalités d'application de la directive 2007/2/CE du Parlement européen et du Conseil en ce qui concerne l'interopérabilité des séries et des services de données géographiques</fr>
        <hr>Uredba Komisije (EU) br. 1089/2010 od 23. studenoga 2010. o provedbi Direktive 2007/2/EZ Europskog parlamenta i Vijeća o međuoperativnosti skupova prostornih podataka i usluga u vezi s prostornim podacima</hr>
        <it>Regolamento (UE) n. 1089/2010 della Commissione, del 23 novembre 2010 , recante attuazione della direttiva 2007/2/CE del Parlamento europeo e del Consiglio per quanto riguarda l'interoperabilità dei set di dati territoriali e dei servizi di dati territoriali</it>
        <lv>Komisijas Regula (ES) Nr. 1089/2010 ( 2010. gada 23. novembris ), ar kuru īsteno Eiropas Parlamenta un Padomes Direktīvu 2007/2/EK attiecībā uz telpisko datu kopu un telpisko datu pakalpojumu savstarpējo izmantojamību</lv>
        <lt>2010 m. lapkričio 23 d. Komisijos reglamentas (ES) Nr. 1089/2010, kuriuo įgyvendinamos Europos Parlamento ir Tarybos direktyvos 2007/2/EB nuostatos dėl erdvinių duomenų rinkinių ir paslaugų sąveikumo</lt>
        <hu>A Bizottság 1089/2010/EU rendelete ( 2010. november 23. ) a 2007/2/EK európai parlamenti és tanácsi irányelv téradatkészletek és -szolgáltatások interoperabilitására vonatkozó rendelkezéseinek végrehajtásáról</hu>
        <mt>Regolament tal-Kummissjoni (UE) Nru 1089/2010 tat- 23 ta' Novembru 2010 li jimplimenta d-Direttiva 2007/2/KE tal-Parlament Ewropew u tal-Kunsill fir-rigward tal-interoperabbiltà tas-settijiet ta’ dejta u servizzi ġeografiċi</mt>
        <nl>Verordening (EU) nr. 1089/2010 van de Commissie van 23 november 2010 ter uitvoering van Richtlijn 2007/2/EG van het Europees Parlement en de Raad betreffende de interoperabiliteit van verzamelingen ruimtelijke gegevens en van diensten met betrekking tot ruimtelijke gegevens</nl>
        <pl>Rozporządzenie Komisji (UE) nr 1089/2010 z dnia 23 listopada 2010 r. w sprawie wykonania dyrektywy 2007/2/WE Parlamentu Europejskiego i Rady w zakresie interoperacyjności zbiorów i usług danych przestrzennych</pl>
        <pt>Regulamento (UE) n. ° 1089/2010 da Comissão, de 23 de Novembro de 2010 , que estabelece as disposições de execução da Directiva 2007/2/CE do Parlamento Europeu e do Conselho relativamente à interoperabilidade dos conjuntos e serviços de dados geográficos</pt>
        <ro>Regulamentul (UE) nr. 1089/2010 al Comisiei din 23 noiembrie 2010 de punere în aplicare a Directivei 2007/2/CE a Parlamentului European și a Consiliului în ceea ce privește interoperabilitatea seturilor și serviciilor de date spațiale</ro>
        <sk>Nariadenie Komisie (EÚ) č. 1089/2010 z  23. novembra 2010 , ktorým sa vykonáva smernica Európskeho parlamentu a Rady 2007/2/ES, pokiaľ ide o interoperabilitu súborov a služieb priestorových údajov</sk>
        <sl>Uredba Komisije (EU) št. 1089/2010 z dne 23. novembra 2010 o izvajanju Direktive 2007/2/ES Evropskega parlamenta in Sveta glede medopravilnosti zbirk prostorskih podatkov in storitev v zvezi s prostorskimi podatki</sl>
        <fi>Komission asetus (EU) N:o 1089/2010, annettu 23 päivänä marraskuuta 2010 , Euroopan parlamentin ja neuvoston direktiivin 2007/2/EY täytäntöönpanosta paikkatietoaineistojen ja -palvelujen yhteentoimivuuden osalta</fi>
        <sv>Kommissionens förordning (EU) nr 1089/2010 av den 23 november 2010 om genomförande av Europaparlamentets och rådets direktiv 2007/2/EG vad gäller interoperabilitet för rumsliga datamängder och datatjänster</sv>
        <!-- Translation http://eur-lex.europa.eu/legal-content/SV/TXT/?uri=CELEX:32010R1089&qid=1418298723943  -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="eu9762009">
    <xsl:choose>
      <xsl:when test="$inspireRegulationLaxCheck">
        <cr>commission regulation (eu)</cr>
        <re>reglement (ue)</re>
        <in>inspire</in>
      </xsl:when>
      <xsl:otherwise>
        <en>Commission Regulation (EC) No 976/2009 of 19 October 2009 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards the Network Services</en>
        <bg>Регламент (ЕО) № 976/2009 на Комисията от 19 октомври 2009 година за прилагане на Директива 2007/2/ЕО на Европейския парламент и на Съвета по отношение на мрежовите услуги</bg>
        <cs>Nařízení Komise (ES) č. 976/2009 ze dne 19. října 2009 , kterým se provádí směrnice Evropského parlamentu a Rady 2007/2/ES, pokud jde o síťové služby</cs>
        <da>Kommissionens forordning (EF) nr. 976/2009 af 19. oktober 2009 om gennemførelse af Europa-Parlamentets og Rådets direktiv 2007/2/EF for så vidt angår nettjenesterne</da>
        <de>Verordnung (EG) Nr. 976/2009 der Kommission vom 19. Oktober 2009 zur Durchführung der Richtlinie 2007/2/EG des Europäischen Parlaments und des Rates hinsichtlich der Netzdienste</de>
        <et>Komisjoni määrus (EÜ) nr 976/2009, 19. oktoober 2009 , millega rakendatakse Euroopa Parlamendi ja nõukogu direktiivi 2007/2/EÜ seoses võrguteenustega</et>
        <el>Κανονισμός (ΕΚ) αριθ. 976/2009 της Επιτροπής, της 19ης Οκτωβρίου 2009 , για την υλοποίηση της οδηγίας 2007/2/ΕΚ του Ευρωπαϊκού Κοινοβουλίου και του Συμβουλίου όσον αφορά τις δικτυακές υπηρεσίες</el>
        <fr>Règlement (CE) n o  976/2009 de la Commission du 19 octobre 2009 portant modalités d’application de la directive 2007/2/CE du Parlement européen et du Conseil en ce qui concerne les services en réseau</fr>
        <hr>Uredba Komisije (EZ) br. 976/2009 od 19. listopada 2009. o provedbi Direktive 2007/2/EZ Europskog parlamenta i Vijeća u vezi s mrežnim uslugama</hr>
        <it>Regolamento (CE) n. 976/2009 della Commissione, del 19 ottobre 2009 , recante attuazione della direttiva 2007/2/CE del Parlamento europeo e del Consiglio per quanto riguarda i servizi di rete</it>
        <lv>Komisijas Regula (EK) Nr. 976/2009 ( 2009. gada 19. oktobris ), ar kuru īsteno Eiropas Parlamenta un Padomes Direktīvu 2007/2/EK attiecībā uz tīkla pakalpojumiem</lv>
        <lt>2009 m. spalio 19 d. Komisijos reglamentas (EB) Nr. 976/2009, kuriuo įgyvendinamos Europos Parlamento ir Tarybos direktyvos 2007/2/EB nuostatos dėl tinklo paslaugų</lt>
        <hu>A Bizottság 976/2009/EK rendelete ( 2009. október 19. ) a 2007/2/EK európai parlamenti és tanácsi irányelv hálózati szolgáltatásokra vonatkozó rendelkezéseinek végrehajtásáról</hu>
        <mt>Regolament tal-Kummissjoni (KE) Nru 976/2009 tad- 19 ta’ Ottubru 2009 li jimplimenta d-Direttiva 2007/2/KE tal-Parlament Ewropew u tal-Kunsill fir-rigward tas-Servizzi ta’ Netwerk</mt>
        <nl>Verordening (EG) nr. 976/2009 van de Commissie van 19 oktober 2009 tot uitvoering van Richtlijn 2007/2/EG van het Europees Parlement en de Raad wat betreft de netwerkdiensten</nl>
        <pl>Rozporządzenie Komisji (WE) nr 976/2009 z dnia 19 października 2009 r. w sprawie wykonania dyrektywy 2007/2/WE Parlamentu Europejskiego i Rady w zakresie usług sieciowych</pl>
        <pt>Regulamento (CE) n. o  976/2009 da Comissão, de 19 de Outubro de 2009 , que estabelece as disposições de execução da Directiva 2007/2/CE do Parlamento Europeu e do Conselho no que respeita aos serviços de rede</pt>
        <ro>Regulamentul (CE) nr. 976/2009 al Comisiei din 19 octombrie 2009 de aplicare a Directivei 2007/2/CE a Parlamentului European și a Consiliului în ceea ce privește serviciile de rețea</ro>
        <sk>Nariadenie Komisie (ES) č. 976/2009 z  19. októbra 2009 , ktorým sa vykonáva smernica Európskeho parlamentu a Rady 2007/2/ES, pokiaľ ide o sieťové služby</sk>
        <sl>Uredba Komisije (ES) št. 976/2009 z dne 19. oktobra 2009 o izvajanju Direktive 2007/2/ES Evropskega parlamenta in Sveta glede omrežnih storitev</sl>
        <fi>Komission asetus (EY) N:o 976/2009, annettu 19 päivänä lokakuuta 2009 , Euroopan parlamentin ja neuvoston direktiivin 2007/2/EY täytäntöönpanosta verkkopalvelujen osalta</fi>
        <sv>Kommissionens förordning (EG) nr 976/2009 av den 19 oktober 2009 om genomförande av Europaparlamentets och rådets direktiv 2007/2/EG med avseende på nättjänster</sv>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
</xsl:stylesheet>

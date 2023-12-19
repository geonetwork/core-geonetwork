<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
    <sch:ns prefix="gco" uri="https://schemas.isotc211.org/19103/-/gco/1.2"/>
    <sch:ns prefix="cit" uri="https://schemas.isotc211.org/19115/-1/cit/1.3"/>
    <sch:ns prefix="mcc" uri="https://schemas.isotc211.org/19115/-1/mcc/1.3"/>
    <sch:ns prefix="mda" uri="https://schemas.isotc211.org/19115/-1/mda/1.3"/>
    <sch:ns prefix="mrc" uri="https://schemas.isotc211.org/19115/-1/mrc/1.3"/>
    <sch:ns prefix="mrd" uri="https://schemas.isotc211.org/19115/-1/mrd/1.3"/>
    <sch:ns prefix="dqc" uri="https://schemas.isotc211.org/19157/-1/dqc/1.0"/>
    <sch:ns prefix="mdq" uri="https://schemas.isotc211.org/19157/-1/mdq/1.0"/>
    <!--
    ISO 19157-1 
    -->
    
    <!-- 
    Rule: MeasureReference
    Ref: {count(measureIdentification) > 0) OR (count (nameOfMeasure) AND count (measureDescription)>0}
    -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-failure-en"
            xml:lang="en">There is neither a measure identifier nor both a measure name and description.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-failure-fr"
            xml:lang="fr">Il n'y a ni identifiant de mesure, ni nom et description de la mesure.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-success-en"
            xml:lang="en">measure identifier is  
            "<sch:value-of select="normalize-space($measureId)"/>"
            or measure name is
            "<sch:value-of select="normalize-space($measureName)"/>"
            and measure description is
            "<sch:value-of select="normalize-space($measureDesc)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-success-fr"
            xml:lang="fr">Le identifiant de mesure est  
            "<sch:value-of select="normalize-space($measureId)"/>"
            ,ou le nom de la mesure est 
            "<sch:value-of select="normalize-space($measureName)"/>"
            et la description de la mesure est
            "<sch:value-of select="normalize-space($measureDesc)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription">
        <sch:title xml:lang="en">Measure reference MUST have either an identifier or both a name and description</sch:title>
        <sch:title xml:lang="fr">La référence de la mesure DOIT avoir soit un identifiant, soit à la fois un nom et une description</sch:title>
        
        <sch:rule context="//mdq:MeasureReference">
            
            <sch:let name="measureId" value="mdq:measureIdentification"/>
            <sch:let name="measureName" value="mdq:nameOfMeasure"/>
            <sch:let name="measureDesc" value="mdq:measureDescription"/>
            <sch:let name="hasMeasureId" 
                value="normalize-space($measureId) != ''"/>
            <sch:let name="hasMeasureName" 
                value="normalize-space($measureName) != ''"/>
            <sch:let name="hasMeasureDesc" 
                value="normalize-space($measureDesc) != ''"/>
            
            <sch:assert test="$hasMeasureId or ($hasMeasureName and $hasMeasureDesc)"
                diagnostics="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-failure-en 
                rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-failure-fr"/>
            
            <sch:report test="$hasMeasureId or ($hasMeasureName and $hasMeasureDesc)"
                diagnostics="rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-success-en 
                rule.mdq.measureIdentificationORnameOfMeasureANDmeasureDescription-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
    
    
    
    <!-- 
    Rule: QualityEvaluationReportInformation
    Ref: {count(reportReference) + count(onlineResource) > 0}
  -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.reportReferenceandonlineResource-failure-en"
            xml:lang="en">The quality evaluation report has neither report reference nor a online link to report.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.reportReferenceandonlineResource-failure-fr"
            xml:lang="fr">Le rapport d'évaluation de la qualité n'a ni référence au rapport ni lien en ligne vers le rapport.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.reportReferenceandonlineResource-success-en"
            xml:lang="en">Report reference is  
            "<sch:value-of select="normalize-space($reportRef)"/>"
            the online resource is 
            "<sch:value-of select="normalize-space($onlineRes)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.reportReferenceandonlineResource-success-fr"
            xml:lang="fr">La référence du rapport est  
            "<sch:value-of select="normalize-space($reportRef)"/>"
            la ressource en ligne est
            "<sch:value-of select="normalize-space($onlineRes)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.reportReferenceandonlineResource">
        <sch:title xml:lang="en">QualityEvaluationReportInformation MUST have either a reference or an online resource</sch:title>
        <sch:title xml:lang="fr">Une QualityEvaluationReportInformation DOIT avoir soit une référence, soit une ressource en ligne</sch:title>
        
        <sch:rule context="//mdq:QualityEvaluationReportInformation">
            
            <sch:let name="reportRef" value="mdq:reportReference"/>
            <sch:let name="onlineRes" value="mdq:onlineRes"/>
            <sch:let name="hasRef" 
                value="normalize-space($reportRef) != ''"/>
            <sch:let name="hasRes" 
                value="normalize-space($onlineRes) != ''"/>
            
            <sch:assert test="$hasRef or $hasRes"
                diagnostics="rule.mdq.reportReferenceandonlineResource-failure-en 
                rule.mdq.reportReferenceandonlineResource-failure-fr"/>
            
            <sch:report test="$hasRef or $hasRes"
                diagnostics="rule.mdq.reportReferenceandonlineResource-success-en 
                rule.mdq.reportReferenceandonlineResource-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
    
    
    
    <!-- 
    Rule: AggregationDerivation
    Ref: {evaluationMethodType.oclAsType(indirect) implies count(deductiveSource)>0}
  -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-failure-en"
            xml:lang="en">Deductive source not present although evaluation method is indirect.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-failure-fr"
            xml:lang="fr">Source déductive absente bien que la méthode d'évaluation soit indirect.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-success-en"
            xml:lang="en">Evaluation method type is  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            the deductive siurce is 
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-success-fr"
            xml:lang="fr">Le type de méthode d'évaluation est  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            la source déductive est
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource">
        <sch:title xml:lang="en">EvaluationMethod MUST have a deductive source if the evaluation method is indirect</sch:title>
        <sch:title xml:lang="fr">EvaluationMethod DOIT avoir une source déductive si la méthode d'évaluation est indirect</sch:title>
        
        <sch:rule context="//mdq:SampleBasedInspection[(mdq:evaluationMethodType/@codeListValue = 'indirect')]">
            
            <sch:let name="evalMethType" value="mdq:evaluationMethodType/@codeListValue"/>
            <sch:let name="deductSorce" value="mdq:deductiveSource"/>
            <sch:let name="hasDeductSource" 
                value="normalize-space($deductSorce) != ''"/>
            
            <sch:assert test="$hasDeductSource"
                diagnostics="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-failure-en 
                rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-failure-fr"/>
            
            <sch:report test="$hasDeductSource"
                diagnostics="rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-success-en 
                rule.mdq.AggregationindirectEvaluationMethodANDdeductiveSource-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
    <!-- 
    Rule: SampleBasedInspection
    Ref: {evaluationMethodType.oclAsType(indirect) implies count(deductiveSource)>0}
  -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-failure-en"
            xml:lang="en">Deductive source not present although evaluation method is indirect.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-failure-fr"
            xml:lang="fr">Source déductive absente bien que la méthode d'évaluation soit indirect.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-success-en"
            xml:lang="en">Evaluation method type is  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            the deductive siurce is 
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-success-fr"
            xml:lang="fr">Le type de méthode d'évaluation est  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            la source déductive est
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource">
        <sch:title xml:lang="en">EvaluationMethod MUST have a deductive source if the evaluation method is indirect</sch:title>
        <sch:title xml:lang="fr">EvaluationMethod DOIT avoir une source déductive si la méthode d'évaluation est indirect</sch:title>
        
        <sch:rule context="//mdq:SampleBasedInspection[(mdq:evaluationMethodType/@codeListValue = 'indirect')]">
            
            <sch:let name="evalMethType" value="mdq:evaluationMethodType/@codeListValue"/>
            <sch:let name="deductSorce" value="mdq:deductiveSource"/>
            <sch:let name="hasDeductSource" 
                value="normalize-space($deductSorce) != ''"/>
            
            <sch:assert test="$hasDeductSource"
                diagnostics="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-failure-en 
                rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-failure-fr"/>
            
            <sch:report test="$hasDeductSource"
                diagnostics="rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-success-en 
                rule.mdq.SampleindirectEvaluationMethodANDdeductiveSource-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
    <!-- 
    Rule: FullInspection
    Ref: {evaluationMethodType.oclAsType(indirect) implies count(deductiveSource)>0}
  -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-failure-en"
            xml:lang="en">Deductive source not present although evaluation method is indirect.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-failure-fr"
            xml:lang="fr">Source déductive absente bien que la méthode d'évaluation soit indirect.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-success-en"
            xml:lang="en">Evaluation method type is  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            the deductive siurce is 
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-success-fr"
            xml:lang="fr">Le type de méthode d'évaluation est  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            la source déductive est
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource">
        <sch:title xml:lang="en">EvaluationMethod MUST have a deductive source if the evaluation method is indirect</sch:title>
        <sch:title xml:lang="fr">EvaluationMethod DOIT avoir une source déductive si la méthode d'évaluation est indirect</sch:title>
        
        <sch:rule context="//mdq:FullInspection[(mdq:evaluationMethodType/@codeListValue = 'indirect')]">
            
            <sch:let name="evalMethType" value="mdq:evaluationMethodType/@codeListValue"/>
            <sch:let name="deductSorce" value="mdq:deductiveSource"/>
            <sch:let name="hasDeductSource" 
                value="normalize-space($deductSorce) != ''"/>
            
            <sch:assert test="$hasDeductSource"
                diagnostics="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-failure-en 
                rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-failure-fr"/>
            
            <sch:report test="$hasDeductSource"
                diagnostics="rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-success-en 
                rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
    <!-- 
    Rule: IndirectEvaluation
    Ref: {evaluationMethodType.oclAsType(indirect) implies count(deductiveSource)>0}
  -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource-failure-en"
            xml:lang="en">Deductive source not present although evaluation method is indirect.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource-failure-fr"
            xml:lang="fr">Source déductive absente bien que la méthode d'évaluation soit indirect.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource-success-en"
            xml:lang="en">Evaluation method type is  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            the deductive siurce is 
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource-success-fr"
            xml:lang="fr">Le type de méthode d'évaluation est  
            "<sch:value-of select="normalize-space($evalMethType)"/>"
            la source déductive est
            "<sch:value-of select="normalize-space($deductSorce)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource">
        <sch:title xml:lang="en">EvaluationMethod MUST have a deductive source if the evaluation method is indirect</sch:title>
        <sch:title xml:lang="fr">EvaluationMethod DOIT avoir une source déductive si la méthode d'évaluation est indirect</sch:title>
        
        <sch:rule context="//mdq:FullInspection[(mdq:evaluationMethodType/@codeListValue = 'indirect')]">
            
            <sch:let name="evalMethType" value="mdq:evaluationMethodType/@codeListValue"/>
            <sch:let name="deductSorce" value="mdq:deductiveSource"/>
            <sch:let name="hasDeductSource" 
                value="normalize-space($deductSorce) != ''"/>
            
            <sch:assert test="$hasDeductSource"
                diagnostics="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource-failure-en 
                rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-failure-fr"/>
            
            <sch:report test="$hasDeductSource"
                diagnostics="rule.mdq.IndirectindirectEvaluationMethodANDdeductiveSource-success-en 
                rule.mdq.FullindirectEvaluationMethodANDdeductiveSource-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
    
    
    
    <!-- 
    Rule: CoverageResult
    Ref: {count(resultContent) + count(resultFormat) > 0}
  -->
    <sch:diagnostics>
        <sch:diagnostic id="rule.mdq.resultContentORresultFormat-failure-en"
            xml:lang="en">The coverage result has neither result content nor result format.</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.resultContentORresultFormat-failure-fr"
            xml:lang="fr">Le résultat de la couverture n'a ni contenu ni format de résultat.</sch:diagnostic>
        
        <sch:diagnostic id="rule.mdq.resultContentORresultFormat-success-en"
            xml:lang="en">Coverage result is  
            "<sch:value-of select="normalize-space($covResult)"/>"
            the result format is 
            "<sch:value-of select="normalize-space($resFormat)"/>"
            .</sch:diagnostic>
        <sch:diagnostic id="rule.mdq.resultContentORresultFormat-success-fr"
            xml:lang="fr">Le résultat de la couverture est  
            "<sch:value-of select="normalize-space($covResult)"/>"
            format de résultat est
            "<sch:value-of select="normalize-space($resFormat)"/>"
            .</sch:diagnostic>
    </sch:diagnostics>
    
    <sch:pattern id="rule.mdq.resultContentORresultFormat">
        <sch:title xml:lang="en">QualityEvaluationReportInformation MUST have either a reference or an online resource</sch:title>
        <sch:title xml:lang="fr">Une QualityEvaluationReportInformation DOIT avoir soit une référence, soit une ressource en ligne</sch:title>
        
        <sch:rule context="//mdq:CoverageResult">
            
            <sch:let name="covResult" value="mdq:resultContent"/>
            <sch:let name="resFormat" value="mdq:resultFormat"/>
            <sch:let name="hasCovRes" 
                value="normalize-space($covResult) != ''"/>
            <sch:let name="hasResForm" 
                value="normalize-space($resFormat) != ''"/>
            
            <sch:assert test="$hasCovRes or $hasResForm"
                diagnostics="rule.mdq.resultContentORresultFormat-failure-en 
                rule.mdq.resultContentORresultFormat-failure-fr"/>
            
            <sch:report test="$hasCovRes or $hasResForm"
                diagnostics="rule.mdq.resultContentORresultFormat-success-en 
                rule.mdq.resultContentORresultFormat-success-fr"/>
        </sch:rule>
    </sch:pattern>
    
</sch:schema>
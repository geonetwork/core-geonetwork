<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">
    <!-- Rendering templates for creating HTML elements. -->

    <xsl:import href="common/base-variables-metadata.xsl"/>

    <xsl:import href="common/utility-tpl-metadata.xsl"/>

    <xsl:import href="layout-xml.xsl"/>

    <!-- 
    Render an element with a label and a value
  -->
    <xsl:template name="render-element">
        <xsl:param name="label" as="xs:string"/>
        <xsl:param name="value"/>
        <!-- cls may define custom CSS class in order to activate
    custom widgets on client side -->
        <xsl:param name="cls" required="no" as="xs:string"/>
        <!-- widget may define custom information in order to activate
    custom widgets on client side. Eg. calendar, bboxMap -->
        <xsl:param name="widget" required="no" as="xs:string" select="''"/>
        <xsl:param name="widgetParams" required="no" as="xs:string" select="''"/>
        <!-- XPath is added as data attribute for client side references 
    to get help or inline editing ? -->
        <xsl:param name="xpath" required="no" as="xs:string" select="''"/>

        <!-- For editing -->
        <xsl:param name="name" required="no" as="xs:string" select="generate-id()"/>
        <xsl:param name="type" required="no" as="xs:string" select="'input'"/>
        <xsl:param name="hidden" required="no" as="xs:boolean" select="false()"/>
        <xsl:param name="editInfo" required="no"/>
        <xsl:param name="parentEditInfo" required="no"/>
        <xsl:param name="attributesSnippet" required="no"/>

        <!-- Required status is defined in parent element for
    some profiles like ISO19139. If not set, the element
    editing information is used. 
    In view mode, always set to false.
    -->
        <xsl:variable name="isRequired" as="xs:boolean">
            <xsl:choose>
                <xsl:when test="$isEditing">
                    <xsl:choose>
                        <xsl:when
                            test="$parentEditInfo and $parentEditInfo/@min = 1 and $parentEditInfo/@max = 1">
                            <xsl:value-of select="true()"/>
                        </xsl:when>
                        <xsl:when
                            test="not($parentEditInfo) and $editInfo and $editInfo/@min = 1 and $editInfo/@max = 1">
                            <xsl:value-of select="true()"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="false()"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="false()"/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:variable>


        <!-- The HTML element to remove when action remove is called -->
        <div class="form-group" id="gn-el-{$editInfo/@ref}">
            <label for="gn-TODO"
                class="col-lg-2 control-label {if ($isRequired) then 'gn-required' else ''}">
                <xsl:if test="$xpath and $withXPath">
                    <xsl:attribute name="data-gn-xpath" select="$xpath"/>
                </xsl:if>
                <xsl:if test="$widget != ''">
                    <xsl:attribute name="data-gn-widget" select="$widget"/>
                    <xsl:if test="$widgetParams != ''">
                        <xsl:attribute name="data-gn-widget-params" select="$widgetParams"/>
                    </xsl:if>
                </xsl:if>
                <xsl:value-of select="$label"/>
            </label>



            <xsl:choose>
                <xsl:when test="$isEditing">
                    <!-- TODO : Add custom fields -->
                    <div class="col-lg-8 gn-value">
                        <xsl:call-template name="render-form-field">
                            <xsl:with-param name="name" select="$name"/>
                            <xsl:with-param name="value" select="$value"/>
                            <xsl:with-param name="type" select="$type"/>
                            <xsl:with-param name="isRequired" select="$isRequired"/>
                            <xsl:with-param name="editInfo" select="$editInfo"/>
                            <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                        </xsl:call-template>
                    </div>
                    <div class="col-lg-2 gn-control">
                        <xsl:call-template name="render-form-field-control">
                            <xsl:with-param name="name" select="name(.)"/>
                            <xsl:with-param name="isRequired" select="$isRequired"/>
                            <xsl:with-param name="editInfo" select="$editInfo"/>
                            <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                        </xsl:call-template>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <div class="col-lg-10 gn-value">
                        <xsl:value-of select="$value"/>
                    </div>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="$attributesSnippet">
                <xsl:copy-of select="$attributesSnippet"/>
            </xsl:if>
        </div>
    </xsl:template>


    <!-- 
    Render a boxed element in a fieldset
  -->
    <xsl:template name="render-boxed-element">
        <xsl:param name="label" as="xs:string"/>
        <xsl:param name="value"/>
        <!-- The content to put into the box -->
        <xsl:param name="subTreeSnippet" required="yes" as="node()"/>
        <!-- cls may define custom CSS class in order to activate
    custom widgets on client side -->
        <xsl:param name="cls" required="no"/>
        <!-- XPath is added as data attribute for client side references 
    to get help or inline editing ? -->
        <xsl:param name="xpath" required="no"/>
        <xsl:param name="attributesSnippet" required="no"/>


        <fieldset>
            <legend class="{$cls}">
                <xsl:if test="$xpath and $withXPath">
                    <xsl:attribute name="data-gn-xpath" select="$xpath"/>
                </xsl:if>

                <xsl:value-of select="$label"/>

                <xsl:if test="$attributesSnippet">
                    <xsl:copy-of select="$attributesSnippet"/>
                </xsl:if>
            </legend>

            <xsl:if test="$subTreeSnippet">
                <xsl:copy-of select="$subTreeSnippet"/>
            </xsl:if>
        </fieldset>
    </xsl:template>



    <!-- Form utils -->
    <xsl:template name="render-form-field">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:param name="hidden"/>
        <xsl:param name="type"/>
        <xsl:param name="isRequired"/>
        <xsl:param name="editInfo"/>
        <xsl:param name="parentEditInfo"/>

        <xsl:variable name="valueToEdit" select="normalize-space($value/text())"/>

        <xsl:choose>
            <xsl:when test="$type = 'textarea'">
                <textarea class="form-control" name="_{$name}">
                    <xsl:if test="$isRequired">
                        <xsl:attribute name="required" select="'required'"/>
                    </xsl:if>
                    <xsl:if test="$hidden">
                        <xsl:attribute name="display" select="'none'"/>
                    </xsl:if>
                    <xsl:value-of select="$valueToEdit"/>
                </textarea>
            </xsl:when>
            <xsl:when test="$type = 'select'">
                <select class="form-control" name="_{$name}">
                    <xsl:if test="$isRequired">
                        <xsl:attribute name="required" select="'required'"/>
                    </xsl:if>
                    <xsl:if test="$hidden">
                        <xsl:attribute name="display" select="'none'"/>
                    </xsl:if>
                    <!-- TODO: Build list from ... -->
                    <option value="{$valueToEdit}">
                        <xsl:value-of select="$value"/>
                    </option>
                </select>

            </xsl:when>
            <xsl:otherwise>
                <input class="form-control" name="_{$name}" value="{$valueToEdit}">
                    <xsl:if test="$isRequired">
                        <xsl:attribute name="required" select="'required'"/>
                    </xsl:if>
                    <xsl:if test="$type != ''">
                        <xsl:attribute name="type" select="$type"/>
                    </xsl:if>
                    <xsl:if test="$hidden">
                        <xsl:attribute name="hidden"/>
                    </xsl:if>
                </input>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


    <xsl:template name="render-form-field-control">
        <xsl:param name="name"/>
        <xsl:param name="isRequired"/>
        <xsl:param name="editInfo"/>
        <xsl:param name="parentEditInfo"/>

        <!--<textarea><xsl:copy-of select="$editInfo"/></textarea>
        <textarea><xsl:copy-of select="$parentEditInfo"/></textarea>
        -->
        <xsl:if test="$parentEditInfo and $parentEditInfo/@del = 'true'">
            <button class="btn icon-remove" data-ng-click="remove({$editInfo/@ref}, {$editInfo/@parent})"/>
        </xsl:if>


        <!-- Add icon for last element of its kind -->
        <xsl:if
            test="$parentEditInfo and $parentEditInfo/@add = 'true' and not($parentEditInfo/@down)">
            <button class="btn icon-plus"
                data-ng-click="add({$parentEditInfo/@parent}, '{$name}', {$editInfo/@ref})"/>
        </xsl:if>

    </xsl:template>



    <!-- Nav bars -->
    <xsl:template name="scroll-spy-nav-bar">
        <div id="navbarExample" class="navbar navbar-static navbar-fixed-bottom">
            <div class="navbar-inner">
                <div class="container" style="width: auto;">
                    <ul class="nav">
                        <li class="active">
                            <a href="#identificationInfo">@gmd:identificationInfo</a>
                        </li>
                        <li>
                            <a href="#spatialRepresentationInfo">@gmd:spatialRepresentationInfo</a>
                        </li>
                        <li>
                            <a href="#distributionInfo">@gmd:distributionInfo</a>
                        </li>
                        <li>
                            <a href="#dataQualityInfo">@gmd:dataQualityInfo</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </xsl:template>
</xsl:stylesheet>

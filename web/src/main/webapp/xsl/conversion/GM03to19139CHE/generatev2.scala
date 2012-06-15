import java.io._
import io._
import util.matching._

val Excludes = """[:\(\)\.-]""".r
case class Expr(expr:Regex) {
  def replace(line:String) = expr.replaceAllIn(line, {
    (_:Regex.Match) match {
      case m if m.group(2) != null && Excludes.findFirstIn(m.group(2)).isEmpty=>
        m.matched.patch(m.start(2) - m.start,"int:",0)
      case m =>
        m.matched
      }
    })
}

val specifics = Map(
  "CHE03-to-19139.xsl" -> Map(
    "match=\"CodeISO.LanguageCodeISO_" -> "match=\"int:CodeISO.LanguageCodeISO_",
    "Metadata/language" -> "Metadata/int:language",
    "normalize-space(plainURL" -> "normalize-space(int:plainURL",
    "translate(language" -> "translate(int:language",
    "xmlns:comp=\"http://www.geocat.ch/2003/05/gateway/GM03Comprehensive\"" -> "xmlns:comp=\"http://toignore\"",
    "Core.Core.PT_FreeText/textGroup/" -> "Core.Core.PT_FreeText/int:textGroup/"
  ),
  "extent.xsl" -> Map(
    "string(description" -> "string(int:description",
    "Core.Core.EX_Extent/description" -> "Core.Core.EX_Extent/int:description",
    "EX_ExtenttemporalElement/temporalElement" -> "EX_ExtenttemporalElement/int:temporalElement"
  ),
  "content.xsl" -> Map(
     "|baseDomain" -> "|int:baseDomain",
     "|processingLevelCode" -> "|int:processingLevelCode",
     "GenericName_/value" -> "GenericName_/int:value",
     "document('units.xml')" -> "document('../units.xml')",
     "MD_Type/type" -> "MD_Type/int:type"
   ),
  "legislation.xsl" -> Map(
    "match=\"CodeISO." -> "match=\"int:CodeISO.",
     "CodeISO.CountryCodeISO_|CodeISO.Country_" -> "CodeISO.CountryCodeISO_|int:CodeISO.Country_",
     "language/CodeISO.LanguageCodeISO_|language/CodeISO.LanguageCode_" -> "language/int:CodeISO.LanguageCodeISO_|int:language/int:CodeISO.LanguageCode_",
     "codeListValue=\"{value}\"" -> "codeListValue=\"{int:value}\""
   ),
  "metadata.xsl" -> Map(
     "codeListValue=\"{value}" -> "codeListValue=\"{int:value}",
     "ortrayalCatalogueInfoMD_Metadata/portrayalCatalogueInfo\"/>" -> "ortrayalCatalogueInfoMD_Metadata/int:portrayalCatalogueInfo\"/>"
   ),
  "resp-party.xsl" -> Map(
     "codeListValue=\"{value}" -> "codeListValue=\"{int:value}",
     "Contact/role" -> "Contact/int:role",
     "country|address" -> "country|address",
     "CI_Address/country" -> "CI_Address/int:country",
     "MD_MaintenanceInformationcontact/role" -> "MD_MaintenanceInformationcontact/int:role",
     "Contact/hoursOfService" -> "Contact/int:hoursOfService",
     "Telephone[numberType" -> "Telephone[int:numberType",
     "Address/city" -> "Address/int:city",
     "Address/administrativeArea" -> "Address/int:administrativeArea",
     "Address/postalCode" -> "Address/int:postalCode",
     "Address/electronicMailAddress" -> "Address/int:electronicMailAddress",
     "Address|electronicalMailAddress" -> "Address|int:electronicalMailAddress",
     "Address/streetName" -> "Address/int:streetName",
     "Address/streetNumber" -> "Address/int:streetNumber",
     "Address/addressLine" -> "Address/int:addressLine",
     "Address/postBox" -> "Address/int:postBox",
     "URL_/value" -> "URL_/int:value",
     "../../../role" -> "../../../int:role",
     "|contactInfo" -> "|int:contactInfo",
     "Core.Core.CI_Contact/contactInstructions" -> "Core.Core.CI_Contact/int:contactInstructions",
     " distributorContact |" -> " int:distributorContact |",
     "../role |" -> "../int:role |"
   ),
  "citation.xsl" -> Map(
     "Code_/value" -> "Code_/int:value",
     "Core.Core.CI_Citation|citation" -> "Core.Core.CI_Citation|int:citation",
     "Comprehensive.Comprehensive.CI_Citationidentifier/identifier" -> "Comprehensive.Comprehensive.CI_Citationidentifier/int:identifier",
     "and not(alternateTitle/" -> "and not(int:alternateTitle/"
   ),
  "data-quality.xsl" -> Map(
     "normalize-space(dateTime" -> "normalize-space(int:dateTime",
     "normalize-space(description" -> "normalize-space(int:description",
     "test=\".//XMLBLBOX" -> "test=\".//int:XMLBLBOX",
     "elect=\".//XMLBLBOX" -> "elect=\".//int:XMLBLBOX",
     "elect=\".//value" -> "elect=\".//int:value",
     "attributes|features|featureInstances|attributeInstances" -> "attributes|int:features|int:featureInstances|int:attributeInstances"
   ),
  "identification.xsl" -> Map(
     "Code_/value" -> "Code_/int:value",
     "Comprehensive.Comprehensive.gml_CodeType/code" -> "Comprehensive.Comprehensive.gml_CodeType/int:code",
     "Resolution/distance" -> "Resolution/int:distance",
     "Resolution/equivalentScale" -> "Resolution/int:equivalentScale",
     "Identification/revision" -> "Identification/revision",
     "GM03Comprehensive.Comprehensive.formatDistributordistributorFormat[distributorFormat" -> "GM03Comprehensive.Comprehensive.formatDistributordistributorFormat[int:distributorFormat",
     "Identification/revision" -> "Identification/int:revision",
     "and basicGeodataID" -> "and int:basicGeodataID",
     "<xsl:for-each select=\".//value\">" -> "<xsl:for-each select=\".//int:value\">"
  )
)


val dir = new File("version2/")
val shaFile = "lastModification.sha"

def rmtree(f:File) : Boolean = {
  if(f.isDirectory) f.listFiles.foreach(rmtree)
  f.delete
}

rmtree(dir)
dir.mkdirs

val exprs = List(
  Expr(("""select\s*=\s*"\s*(int:)?([\w"""+Excludes+"""]+)""").r),
  Expr(("""match\s*=\s*"\s*(int:)?([\w"""+Excludes+"""]+)""").r),
  Expr(("""test\s*=\s*"\s*(int:)?([\w"""+Excludes+"""]+)""").r)
)

for (file <- new File(".").listFiles;
     if file.getName.endsWith(".xsl") ) {

  val found = collection.mutable.Set[String]()
  var styleSheetTag = false
  var wroteExcludes = false

  val out = new FileWriter(new File(dir, file.getName))
  Source.fromFile(file) getLines() foreach { l =>
    var line = l
    var write = true

    if (line contains "<xsl:stylesheet") styleSheetTag = true
    if (line contains "exclude-result-prefixes") {
      val Array(prefix, att) = line split ("\"",2)
      line = "%s\"int %s".format(prefix,att)
      wroteExcludes = true
    }

    if(styleSheetTag && (line contains '>')) {
      styleSheetTag = false
      out.write("                xmlns:int=\"http://www.interlis.ch/INTERLIS2.3\"\n")
      if (!wroteExcludes) out.write("                exclude-result-prefixes=\"int\"\n")
    }
    if (line contains "<xsl:include href=\"version2") write=false

    for {map <- specifics.get(file.getName)
         (toReplace,replacement) <- map
         if line contains toReplace } {
           line = line.replace(toReplace, replacement)
           found += toReplace
    }

    line = line.replace("GM03","int:GM03_2").
        replace("TRANSFER", "int:TRANSFER").
        replace("DATASECTION", "int:DATASECTION").
        replace("comp:int", "comp")


    for (expr <- exprs) line = expr.replace(line)

    if(write) out.write(line+"\n")
  }
  out.close()

  specifics.get(file.getName) foreach {m =>
    if (m.size != found.size)
      println ("ERROR:  --- Not all replacements were matched:"+" filename="+file.getName+" -> "+(m.keySet -- found mkString ", "))
  }
}

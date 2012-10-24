package org.geonetwork

import org.apache.log4j.Logger

package object config {
  val Log = Logger.getLogger("jeeves")
  implicit def addAtt(n: xml.Node) = new {
    def att(name: String) =
      n.attributes.find(_.key == name).map(_.value.text).getOrElse("")
  }
}
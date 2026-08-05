//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package net.sf.saxon;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Evaluate;
import net.sf.saxon.functions.StandardFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.VendorFunctionLibrary;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Value;
import net.sf.saxon.expr.StaticProperty;

public class PatchedConfiguration extends Configuration {

    private DisabledVendorFunctionLibrary disabledVendorFunctionLibrary;
    private XPathContext disabledConversionContext;

    public PatchedConfiguration(Configuration c) {
        c.copyTo(this);
        init();
    }

    public VendorFunctionLibrary getVendorFunctionLibrary() {
        if (disabledVendorFunctionLibrary == null) {
            disabledVendorFunctionLibrary = new DisabledVendorFunctionLibrary();
        }
        return disabledVendorFunctionLibrary;
    }

    private class DisabledVendorFunctionLibrary extends VendorFunctionLibrary {
        protected void init() {
            super.init();
            //override
            StandardFunction.Entry e = this.register("evaluate", DisabledEvaluate.class, Evaluate.EVALUATE, 1, 10, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE);
            StandardFunction.arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, (Value)null);
            e = this.register("evaluate-node", DisabledEvaluate.class, Evaluate.EVALUATE_NODE, 1, 1, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE);
            StandardFunction.arg(e, 0, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, (Value)null);
            e = this.register("eval", DisabledEvaluate.class, Evaluate.EVAL, 1, 10, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE);
            StandardFunction.arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.EXACTLY_ONE, (Value)null);
            e = this.register("expression", DisabledEvaluate.class, Evaluate.EXPRESSION, 1, 2, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.EXACTLY_ONE);
            StandardFunction.arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, (Value)null);
            StandardFunction.arg(e, 1, NodeKindTest.ELEMENT, StaticProperty.EXACTLY_ONE, (Value)null);
        }

    }

    private class DisabledEvaluate extends SystemFunction {
    }
}

// $ANTLR 2.7.7 (2006-11-01): "xml.g" -> "XMLParser.java"$
package fr.loria.ecoo.so6.antlr;

import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.impl.BitSet;
import fr.loria.ecoo.so6.xml.exception.AttributeAlreadyExist;
import fr.loria.ecoo.so6.xml.exception.ParseException;
import fr.loria.ecoo.so6.xml.node.CDataNode;
import fr.loria.ecoo.so6.xml.node.CommentNode;
import fr.loria.ecoo.so6.xml.node.DocTypeNode;
import fr.loria.ecoo.so6.xml.node.Document;
import fr.loria.ecoo.so6.xml.node.ElementNode;
import fr.loria.ecoo.so6.xml.node.ProcessingInstructionNode;
import fr.loria.ecoo.so6.xml.node.TextNode;
import fr.loria.ecoo.so6.xml.node.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class XMLParser extends antlr.LLkParser implements XMLLexerTokenTypes {

    public static void joinSiblingTextNodes(TreeNode node) {
        if(node.hasChildren()) {
            int numChildren = node.getChildren().size();
            TreeNode aux;
            List<TreeNode> removedNodes = new ArrayList<TreeNode>();
            int startPos = - 1;
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < numChildren; i++) {
                aux = node.getChild(i);

                if(aux instanceof TextNode && ! (aux instanceof CommentNode)) {
                    if(startPos == - 1) {
                        startPos = i;
                    }
                    else {
                        sb.append(((TextNode) aux).getContent());
                        removedNodes.add(aux);
                    }
                }
                else {
                    if(startPos != - 1 && sb.length() != 0) {
                        ((TextNode) node.getChild(startPos)).appendContent(sb.toString());
                        sb = new StringBuffer();
                    }

                    startPos = - 1;

                    if(aux instanceof ElementNode) {
                        joinSiblingTextNodes(aux);
                    }
                }
            }

            if(startPos != - 1 && sb.length() != 0) {
                ((TextNode) node.getChild(startPos)).appendContent(sb.toString());
            }

            for(TreeNode removedNode : removedNodes) {
                node.removeChild(removedNode);
            }
        }
    }

    protected XMLParser(TokenBuffer tokenBuf, int k) {
        super(tokenBuf, k);
        tokenNames = _tokenNames;
    }

    public XMLParser(TokenBuffer tokenBuf) {
        this(tokenBuf, 1);
    }

    protected XMLParser(TokenStream lexer, int k) {
        super(lexer, k);
        tokenNames = _tokenNames;
    }

    public XMLParser(TokenStream lexer) {
        this(lexer, 1);
    }

    public XMLParser(ParserSharedInputState state) {
        super(state, 1);
        tokenNames = _tokenNames;
    }

    public final Document document() throws RecognitionException, TokenStreamException, Exception {
        Document doc = new Document();

        TreeNode n = null, e = null;
        String vers = null;

        {
            if((LA(1) == PI_S)) {
                prolog(doc);
            }
            else if((_tokenSet_0.member(LA(1)))) {
            }
            else {
                throw new NoViableAltException(LT(1), getFilename());
            }

        }
        {
            _loop53:
            do {
                if((_tokenSet_1.member(LA(1)))) {
                    n = node();
                    if(n != null) {
                        doc.appendChild(n);
                    }
                }
                else {
                    break _loop53;
                }

            }
            while(true);
        }
        match(Token.EOF_TYPE);
        joinSiblingTextNodes(doc);
        return doc;
    }

    public final void prolog(
            Document d
    ) throws RecognitionException, TokenStreamException, Exception {

        String[] anAtt = null;

        match(PI_S);
        match(XML);
        match(WS);
        {
            int _cnt58 = 0;
            _loop58:
            do {
                if((LA(1) == XML || LA(1) == NAME_START)) {
                    anAtt = attribute();

                    if("version".equals(anAtt[0])) {
                        d.setVersion(anAtt[1]);
                    }
                    else if("encoding".equals(anAtt[0])) {
                        d.setEncoding(anAtt[1]);
                    }
                    else if("standalone".equals(anAtt[0])) {
                        d.setStandalone(anAtt[1]);
                    }
                    else {
                        throw new Exception("unsupported attribute in xml declaration:" + anAtt[0] + "=" + anAtt[1]);
                    }


                    {
                        switch(LA(1)) {
                            case WS: {
                                match(WS);
                                break;
                            }
                            case XML:
                            case PI_E:
                            case NAME_START: {
                                break;
                            }
                            default: {
                                throw new NoViableAltException(LT(1), getFilename());
                            }
                        }
                    }
                }
                else {
                    if(_cnt58 >= 1) {
                        break _loop58;
                    }
                    else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt58++;
            }
            while(true);
        }
        match(PI_E);
    }

    public final TreeNode node() throws RecognitionException, TokenStreamException, ParseException,
                                        AttributeAlreadyExist {
        TreeNode n = null;

        switch(LA(1)) {
            case ELEM_S: {
                n = elem();
                break;
            }
            case QUOTES:
            case DQUOTES:
            case XML:
            case COMMENT:
            case CDATA:
            case DOCTYPE_S:
            case BOPEN:
            case BCLOSE:
            case DIESE:
            case ESCAPE_LONGELEM_EE:
            case PI_S:
            case NAME_START:
            case NAME_SUITE:
            case SHORTELEM_E:
            case LONGELEM_EE:
            case EQ:
            case WS:
            case TEXT:
            case QUOTE:
            case DQUOTE: {
                n = misc();
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
        return n;
    }

    public final ElementNode elem() throws RecognitionException, TokenStreamException, ParseException,
                                           AttributeAlreadyExist {
        ElementNode elt = null;
        ;

        String name = null;

        match(ELEM_S);
        name = name();
        {
        }

        elt = new ElementNode(name);

        esuite(elt);
        return elt;
    }

    public final TreeNode misc() throws RecognitionException, TokenStreamException {
        TreeNode n = null;

        Token c = null;
        Token d = null;
        Token q = null;
        Token dq = null;
        Token comment = null;
        Token pi_s = null;
        Token pi_e = null;
        Token cdata = null;
        Token s = null;
        TextNode t = null;
        String x = null;
        String y = null;


        switch(LA(1)) {
            case COMMENT: {
                c = LT(1);
                match(COMMENT);

                if(! "SPLIT".equals(c.getText())) {
                    n = new CommentNode(c.getText());
                }

                break;
            }
            case PI_S: {
                n = processingInstruction();
                {
                }
                break;
            }
            case CDATA: {
                d = LT(1);
                match(CDATA);

                n = new CDataNode(d.getText());

                break;
            }
            case DOCTYPE_S: {
                match(DOCTYPE_S);
                t = new DocTypeNode();
                {
                    int _cnt92 = 0;
                    _loop92:
                    do {
                        switch(LA(1)) {
                            case QUOTES: {
                                q = LT(1);
                                match(QUOTES);
                                t.appendContent(q.getText());
                                break;
                            }
                            case DQUOTES: {
                                dq = LT(1);
                                match(DQUOTES);
                                t.appendContent(dq.getText());
                                break;
                            }
                            default:
                                if((_tokenSet_2.member(LA(1)))) {
                                    text_wo_bracket(t);
                                }
                                else {
                                    if(_cnt92 >= 1) {
                                        break _loop92;
                                    }
                                    else {
                                        throw new NoViableAltException(LT(1), getFilename());
                                    }
                                }
                        }
                        _cnt92++;
                    }
                    while(true);
                }
                {
                    switch(LA(1)) {
                        case BOPEN: {
                            match(BOPEN);
                            t.appendContent("[");
                            {
                                int _cnt95 = 0;
                                _loop95:
                                do {
                                    switch(LA(1)) {
                                        case XML:
                                        case DIESE:
                                        case ESCAPE_LONGELEM_EE:
                                        case NAME_START:
                                        case NAME_SUITE:
                                        case EQ:
                                        case WS:
                                        case TEXT:
                                        case QUOTE:
                                        case DQUOTE: {
                                            text_wo_bracket(t);
                                            break;
                                        }
                                        case QUOTES:
                                        case DQUOTES: {
                                            string(t);
                                            break;
                                        }
                                        case ELEM_S: {
                                            match(ELEM_S);
                                            t.appendContent("<");
                                            break;
                                        }
                                        case LONGELEM_EE: {
                                            match(LONGELEM_EE);
                                            t.appendContent(">");
                                            break;
                                        }
                                        case LONGELEM_ES: {
                                            match(LONGELEM_ES);
                                            t.appendContent("</");
                                            break;
                                        }
                                        case SHORTELEM_E: {
                                            match(SHORTELEM_E);
                                            t.appendContent("/>");
                                            break;
                                        }
                                        case COMMENT: {
                                            comment = LT(1);
                                            match(COMMENT);
                                            t.appendContent("<!--");
                                            t.appendContent(comment.getText());
                                            t.appendContent("-->");
                                            break;
                                        }
                                        case PI_S: {
                                            pi_s = LT(1);
                                            match(PI_S);
                                            t.appendContent("<?");
                                            break;
                                        }
                                        case PI_E: {
                                            pi_e = LT(1);
                                            match(PI_E);
                                            t.appendContent("?>");
                                            break;
                                        }
                                        case CDATA: {
                                            cdata = LT(1);
                                            match(CDATA);
                                            t.appendContent("<!CDATA[");
                                            t.appendContent(cdata.getText());
                                            t.appendContent("]]>");
                                            break;
                                        }
                                        default: {
                                            if(_cnt95 >= 1) {
                                                break _loop95;
                                            }
                                            else {
                                                throw new NoViableAltException(LT(1), getFilename());
                                            }
                                        }
                                    }
                                    _cnt95++;
                                }
                                while(true);
                            }
                            match(BCLOSE);
                            t.appendContent("]");
                            break;
                        }
                        case LONGELEM_EE:
                        case WS: {
                            break;
                        }
                        default: {
                            throw new NoViableAltException(LT(1), getFilename());
                        }
                    }
                }
                {
                    switch(LA(1)) {
                        case WS: {
                            s = LT(1);
                            match(WS);
                            t.appendContent(s.getText());
                            break;
                        }
                        case LONGELEM_EE: {
                            break;
                        }
                        default: {
                            throw new NoViableAltException(LT(1), getFilename());
                        }
                    }
                }
                match(LONGELEM_EE);
                n = t;
                break;
            }
            case QUOTES:
            case DQUOTES:
            case XML:
            case BOPEN:
            case BCLOSE:
            case DIESE:
            case ESCAPE_LONGELEM_EE:
            case NAME_START:
            case NAME_SUITE:
            case SHORTELEM_E:
            case LONGELEM_EE:
            case EQ:
            case WS:
            case TEXT:
            case QUOTE:
            case DQUOTE: {

                t = new TextNode();

                text(t);

                n = t;

                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
        return n;
    }

    public final String[] attribute() throws RecognitionException, TokenStreamException {
        String[] result = new String[2];

        String v = null;
        String n = null;

        n = name();
        {
        }
        {
            switch(LA(1)) {
                case WS: {
                    match(WS);
                    break;
                }
                case EQ: {
                    break;
                }
                default: {
                    throw new NoViableAltException(LT(1), getFilename());
                }
            }
        }
        match(EQ);
        {
            switch(LA(1)) {
                case WS: {
                    match(WS);
                    break;
                }
                case QUOTES:
                case DQUOTES: {
                    break;
                }
                default: {
                    throw new NoViableAltException(LT(1), getFilename());
                }
            }
        }
        v = attributeSuite();

        result[0] = n;
        result[1] = v;

        return result;
    }

    public final String name_suite() throws RecognitionException, TokenStreamException {
        String name = null;

        Token x = null;
        Token ns = null;
        Token xs = null;
        StringBuffer buf = new StringBuffer();

        {
            int _cnt61 = 0;
            _loop61:
            do {
                if((LA(1) == XML)) {
                    x = LT(1);
                    match(XML);
                    buf.append(x.getText());
                }
                else if((LA(1) == NAME_SUITE)) {
                    ns = LT(1);
                    match(NAME_SUITE);
                    buf.append(ns.getText());
                }
                else if((LA(1) == NAME_START)) {
                    xs = LT(1);
                    match(NAME_START);
                    buf.append(xs.getText());
                }
                else {
                    if(_cnt61 >= 1) {
                        break _loop61;
                    }
                    else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt61++;
            }
            while(true);
        }
        name = buf.toString();
        return name;
    }

    public final String name() throws RecognitionException, TokenStreamException {
        String name = null;

        Token x = null;
        Token s = null;
        StringBuffer buf = new StringBuffer();
        String n = null;


        {
            switch(LA(1)) {
                case XML: {
                    x = LT(1);
                    match(XML);
                    buf.append(x.getText());
                    {
                        if((LA(1) == XML || LA(1) == NAME_START || LA(1) == NAME_SUITE)) {
                            n = name_suite();
                            {
                            }
                            buf.append(n);
                        }
                        else if((_tokenSet_3.member(LA(1)))) {
                        }
                        else {
                            throw new NoViableAltException(LT(1), getFilename());
                        }

                    }
                    break;
                }
                case NAME_START: {
                    s = LT(1);
                    match(NAME_START);
                    buf.append(s.getText());
                    {
                        if((LA(1) == XML || LA(1) == NAME_START || LA(1) == NAME_SUITE)) {
                            n = name_suite();
                            {
                            }
                            buf.append(n);
                        }
                        else if((_tokenSet_3.member(LA(1)))) {
                        }
                        else {
                            throw new NoViableAltException(LT(1), getFilename());
                        }

                    }
                    break;
                }
                default: {
                    throw new NoViableAltException(LT(1), getFilename());
                }
            }
        }
        name = buf.toString();
        return name;
    }

    public final String name_processing() throws RecognitionException, TokenStreamException {
        String name = null;

        Token s = null;
        Token x = null;
        StringBuffer buf = new StringBuffer();
        String ss = null;


        {
            switch(LA(1)) {
                case NAME_START: {
                    s = LT(1);
                    match(NAME_START);
                    buf.append(s.getText());
                    {
                        switch(LA(1)) {
                            case XML:
                            case NAME_START:
                            case NAME_SUITE: {
                                ss = name_suite();
                                {
                                }
                                buf.append(ss);
                                break;
                            }
                            case PI_E:
                            case WS: {
                                break;
                            }
                            default: {
                                throw new NoViableAltException(LT(1), getFilename());
                            }
                        }
                    }
                    break;
                }
                case XML: {
                    x = LT(1);
                    match(XML);
                    ss = name_suite();
                    {
                    }

                    buf.append(x.getText());
                    buf.append(ss);

                    break;
                }
                default: {
                    throw new NoViableAltException(LT(1), getFilename());
                }
            }
        }
        name = buf.toString();
        return name;
    }

    public final String attributeSuite() throws RecognitionException, TokenStreamException {
        String result = null;

        String x, y;

        switch(LA(1)) {
            case DQUOTES: {
                match(DQUOTES);
                x = attributeValueWithoutDQuotes();
                match(DQUOTES);

                result = x;

                break;
            }
            case QUOTES: {
                match(QUOTES);
                y = attributeValueWithoutQuotes();
                match(QUOTES);

                result = y;

                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
        return result;
    }

    public final String attributeValueWithoutDQuotes() throws RecognitionException, TokenStreamException {
        String result = null;

        Token q = null;
        StringBuffer buf = new StringBuffer();
        String cav = null;

        {
            _loop88:
            do {
                switch(LA(1)) {
                    case XML:
                    case BOPEN:
                    case BCLOSE:
                    case DIESE:
                    case PI_E:
                    case NAME_START:
                    case NAME_SUITE:
                    case SHORTELEM_E:
                    case LONGELEM_EE:
                    case EQ:
                    case WS:
                    case TEXT: {
                        cav = commonAttributeValue();
                        {
                        }
                        buf.append(cav);
                        break;
                    }
                    case QUOTES: {
                        q = LT(1);
                        match(QUOTES);
                        buf.append(q.getText());
                        break;
                    }
                    default: {
                        break _loop88;
                    }
                }
            }
            while(true);
        }
        result = buf.toString();
        return result;
    }

    public final String attributeValueWithoutQuotes() throws RecognitionException, TokenStreamException {
        String result = null;

        Token dq = null;
        StringBuffer buf = new StringBuffer();
        String cav = null;

        {
            _loop84:
            do {
                switch(LA(1)) {
                    case XML:
                    case BOPEN:
                    case BCLOSE:
                    case DIESE:
                    case PI_E:
                    case NAME_START:
                    case NAME_SUITE:
                    case SHORTELEM_E:
                    case LONGELEM_EE:
                    case EQ:
                    case WS:
                    case TEXT: {
                        cav = commonAttributeValue();
                        {
                        }
                        buf.append(cav);
                        break;
                    }
                    case DQUOTES: {
                        dq = LT(1);
                        match(DQUOTES);
                        buf.append(dq.getText());
                        break;
                    }
                    default: {
                        break _loop84;
                    }
                }
            }
            while(true);
        }
        result = buf.toString();
        return result;
    }

    public final String commonAttributeValue() throws RecognitionException, TokenStreamException {
        String result = null;

        Token t = null;
        Token x = null;
        Token ws = null;
        Token diese = null;
        Token lee = null;
        Token bopen = null;
        Token bclose = null;
        Token se_e = null;
        Token pi_e = null;
        Token eq = null;
        StringBuffer buf = new StringBuffer();
        String n = null;

        {
            switch(LA(1)) {
                case TEXT: {
                    t = LT(1);
                    match(TEXT);
                    buf.append(t.getText());
                    break;
                }
                case WS: {
                    ws = LT(1);
                    match(WS);
                    buf.append(ws.getText());
                    break;
                }
                case DIESE: {
                    diese = LT(1);
                    match(DIESE);
                    buf.append(diese.getText());
                    break;
                }
                case LONGELEM_EE: {
                    lee = LT(1);
                    match(LONGELEM_EE);
                    buf.append(lee.getText());
                    break;
                }
                case BOPEN: {
                    bopen = LT(1);
                    match(BOPEN);
                    buf.append(bopen.getText());
                    break;
                }
                case BCLOSE: {
                    bclose = LT(1);
                    match(BCLOSE);
                    buf.append(bclose.getText());
                    break;
                }
                case SHORTELEM_E: {
                    se_e = LT(1);
                    match(SHORTELEM_E);

                    buf.append(se_e.getText());

                    break;
                }
                case PI_E: {
                    pi_e = LT(1);
                    match(PI_E);

                    buf.append(pi_e.getText());

                    break;
                }
                case EQ: {
                    eq = LT(1);
                    match(EQ);

                    buf.append(eq.getText());

                    break;
                }
                default:
                    if((LA(1) == XML || LA(1) == NAME_START || LA(1) == NAME_SUITE)) {
                        n = name_suite();
                        {
                        }
                        buf.append(n);
                    }
                    else if((LA(1) == XML)) {
                        x = LT(1);
                        match(XML);
                        buf.append(x.getText());
                    }
                    else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
            }
        }
        result = buf.toString();
        return result;
    }

    public final ProcessingInstructionNode processingInstruction() throws RecognitionException, TokenStreamException {
        ProcessingInstructionNode pi = null;

        String name = null;

        match(PI_S);

        pi = new ProcessingInstructionNode();

        name = name_processing();
        {
        }

        pi.setTarget(name);

        piContent(pi);
        match(PI_E);
        return pi;
    }

    public final void text_wo_bracket(TextNode n) throws RecognitionException, TokenStreamException {

        Token t = null;
        Token ns = null;
        Token s = null;
        Token eq = null;
        Token diese = null;
        Token ele = null;
        Token q = null;
        Token dq = null;
        String na = null;

        {
            int _cnt115 = 0;
            _loop115:
            do {
                if((LA(1) == TEXT)) {
                    t = LT(1);
                    match(TEXT);

                    n.appendContent(t.getText());

                }
                else if((LA(1) == NAME_SUITE)) {
                    ns = LT(1);
                    match(NAME_SUITE);

                    n.appendContent(ns.getText());

                }
                else if((LA(1) == XML || LA(1) == NAME_START)) {
                    na = name();
                    {
                    }

                    n.appendContent(na);

                }
                else if((LA(1) == WS)) {
                    s = LT(1);
                    match(WS);

                    n.appendContent(s.getText());

                }
                else if((LA(1) == EQ)) {
                    eq = LT(1);
                    match(EQ);

                    n.appendContent(eq.getText());

                }
                else if((LA(1) == DIESE)) {
                    diese = LT(1);
                    match(DIESE);

                    n.appendContent(diese.getText());

                }
                else if((LA(1) == ESCAPE_LONGELEM_EE)) {
                    ele = LT(1);
                    match(ESCAPE_LONGELEM_EE);

                    n.appendContent(ele.getText());

                }
                else if((LA(1) == QUOTE)) {
                    q = LT(1);
                    match(QUOTE);
                    n.appendContent(q.getText());
                }
                else if((LA(1) == DQUOTE)) {
                    dq = LT(1);
                    match(DQUOTE);
                    n.appendContent(dq.getText());
                }
                else {
                    if(_cnt115 >= 1) {
                        break _loop115;
                    }
                    else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt115++;
            }
            while(true);
        }
    }

    public final void string(TextNode n) throws RecognitionException, TokenStreamException {

        Token dq = null;
        Token q = null;

        {
            int _cnt103 = 0;
            _loop103:
            do {
                if((LA(1) == DQUOTES)) {
                    dq = LT(1);
                    match(DQUOTES);
                    n.appendContent(dq.getText());
                    {
                        _loop100:
                        do {
                            if((_tokenSet_4.member(LA(1)))) {
                                string_wo_dquotes(n);
                            }
                            else {
                                break _loop100;
                            }

                        }
                        while(true);
                    }
                    match(DQUOTES);
                    n.appendContent(dq.getText());
                }
                else if((LA(1) == QUOTES)) {
                    q = LT(1);
                    match(QUOTES);
                    n.appendContent(q.getText());
                    {
                        _loop102:
                        do {
                            if((_tokenSet_5.member(LA(1)))) {
                                string_wo_quotes(n);
                            }
                            else {
                                break _loop102;
                            }

                        }
                        while(true);
                    }
                    match(QUOTES);
                    n.appendContent(q.getText());
                }
                else {
                    if(_cnt103 >= 1) {
                        break _loop103;
                    }
                    else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt103++;
            }
            while(true);
        }
    }

    public final void text(TextNode n) throws RecognitionException, TokenStreamException {

        Token bopen = null;
        Token bclose = null;
        Token lee = null;
        Token se_e = null;
        Token dq = null;
        Token q = null;

        {
            int _cnt118 = 0;
            _loop118:
            do {
                if((_tokenSet_2.member(LA(1)))) {
                    text_wo_bracket(n);
                }
                else if((LA(1) == BOPEN)) {
                    bopen = LT(1);
                    match(BOPEN);

                    n.appendContent(bopen.getText());

                }
                else if((LA(1) == BCLOSE)) {
                    bclose = LT(1);
                    match(BCLOSE);

                    n.appendContent(bclose.getText());

                }
                else if((LA(1) == LONGELEM_EE)) {
                    lee = LT(1);
                    match(LONGELEM_EE);

                    n.appendContent(lee.getText());

                }
                else if((LA(1) == SHORTELEM_E)) {
                    se_e = LT(1);
                    match(SHORTELEM_E);

                    n.appendContent(se_e.getText());

                }
                else if((LA(1) == DQUOTES)) {
                    dq = LT(1);
                    match(DQUOTES);

                    n.appendContent(dq.getText());

                }
                else if((LA(1) == QUOTES)) {
                    q = LT(1);
                    match(QUOTES);

                    n.appendContent(q.getText());

                }
                else {
                    if(_cnt118 >= 1) {
                        break _loop118;
                    }
                    else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt118++;
            }
            while(true);
        }
    }

    public final void string_wo_dquotes(
            TextNode n
    ) throws RecognitionException, TokenStreamException {

        Token bopen = null;
        Token bclose = null;
        Token elt_s = null;
        Token les = null;
        Token lee = null;
        Token se_e = null;
        Token q = null;
        Token c = null;
        Token cd = null;
        Token pi_s = null;
        Token pi_e = null;

        switch(LA(1)) {
            case XML:
            case DIESE:
            case ESCAPE_LONGELEM_EE:
            case NAME_START:
            case NAME_SUITE:
            case EQ:
            case WS:
            case TEXT:
            case QUOTE:
            case DQUOTE: {
                text_wo_bracket(n);
                break;
            }
            case BOPEN: {
                bopen = LT(1);
                match(BOPEN);

                n.appendContent(bopen.getText());

                break;
            }
            case BCLOSE: {
                bclose = LT(1);
                match(BCLOSE);

                n.appendContent(bclose.getText());

                break;
            }
            case ELEM_S: {
                elt_s = LT(1);
                match(ELEM_S);

                n.appendContent(elt_s.getText());

                break;
            }
            case LONGELEM_ES: {
                les = LT(1);
                match(LONGELEM_ES);

                n.appendContent(les.getText());

                break;
            }
            case LONGELEM_EE: {
                lee = LT(1);
                match(LONGELEM_EE);

                n.appendContent(lee.getText());

                break;
            }
            case SHORTELEM_E: {
                se_e = LT(1);
                match(SHORTELEM_E);

                n.appendContent(se_e.getText());

                break;
            }
            case QUOTES: {
                q = LT(1);
                match(QUOTES);

                n.appendContent(q.getText());

                break;
            }
            case COMMENT: {
                c = LT(1);
                match(COMMENT);

                n.appendContent("<!--");
                n.appendContent(c.getText());
                n.appendContent("-->");

                break;
            }
            case CDATA: {
                cd = LT(1);
                match(CDATA);

                n.appendContent("<![CDATA[");
                n.appendContent(cd.getText());
                n.appendContent("]]>");

                break;
            }
            case PI_S: {
                pi_s = LT(1);
                match(PI_S);
                n.appendContent("<?");
                break;
            }
            case PI_E: {
                pi_e = LT(1);
                match(PI_E);
                n.appendContent("?>");
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
    }

    public final void string_wo_quotes(
            TextNode n
    ) throws RecognitionException, TokenStreamException {

        Token bopen = null;
        Token bclose = null;
        Token elt_s = null;
        Token les = null;
        Token lee = null;
        Token se_e = null;
        Token dq = null;
        Token c = null;
        Token cd = null;
        Token pi_s = null;
        Token pi_e = null;

        switch(LA(1)) {
            case XML:
            case DIESE:
            case ESCAPE_LONGELEM_EE:
            case NAME_START:
            case NAME_SUITE:
            case EQ:
            case WS:
            case TEXT:
            case QUOTE:
            case DQUOTE: {
                text_wo_bracket(n);
                break;
            }
            case BOPEN: {
                bopen = LT(1);
                match(BOPEN);

                n.appendContent(bopen.getText());

                break;
            }
            case BCLOSE: {
                bclose = LT(1);
                match(BCLOSE);

                n.appendContent(bclose.getText());

                break;
            }
            case ELEM_S: {
                elt_s = LT(1);
                match(ELEM_S);

                n.appendContent(elt_s.getText());

                break;
            }
            case LONGELEM_ES: {
                les = LT(1);
                match(LONGELEM_ES);

                n.appendContent(les.getText());

                break;
            }
            case LONGELEM_EE: {
                lee = LT(1);
                match(LONGELEM_EE);

                n.appendContent(lee.getText());

                break;
            }
            case SHORTELEM_E: {
                se_e = LT(1);
                match(SHORTELEM_E);

                n.appendContent(se_e.getText());

                break;
            }
            case DQUOTES: {
                dq = LT(1);
                match(DQUOTES);

                n.appendContent(dq.getText());

                break;
            }
            case COMMENT: {
                c = LT(1);
                match(COMMENT);

                n.appendContent("<!--");
                n.appendContent(c.getText());
                n.appendContent("-->");

                break;
            }
            case CDATA: {
                cd = LT(1);
                match(CDATA);

                n.appendContent("<![CDATA[");
                n.appendContent(cd.getText());
                n.appendContent("]]>");

                break;
            }
            case PI_S: {
                pi_s = LT(1);
                match(PI_S);
                n.appendContent("<?");
                break;
            }
            case PI_E: {
                pi_e = LT(1);
                match(PI_E);
                n.appendContent("?>");
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
    }

    public final void piContent(
            ProcessingInstructionNode pi
    ) throws RecognitionException, TokenStreamException {

        Token w = null;
        Token t = null;
        Token ns = null;
        Token n = null;
        Token dq = null;
        Token q = null;
        Token bo = null;
        Token bc = null;
        Token e_s = null;
        Token le_ee = null;
        Token le_es = null;
        Token dt_s = null;
        Token pi_s = null;
        Token ws = null;
        Token eq = null;

        {
            switch(LA(1)) {
                case WS: {
                    w = LT(1);
                    match(WS);
                    pi.appendContent(w.getText());
                    {
                        _loop111:
                        do {
                            switch(LA(1)) {
                                case TEXT: {
                                    t = LT(1);
                                    match(TEXT);
                                    pi.appendContent(t.getText());
                                    break;
                                }
                                case NAME_START: {
                                    ns = LT(1);
                                    match(NAME_START);
                                    pi.appendContent(ns.getText());
                                    break;
                                }
                                case NAME_SUITE: {
                                    n = LT(1);
                                    match(NAME_SUITE);
                                    pi.appendContent(n.getText());
                                    break;
                                }
                                case DQUOTES: {
                                    dq = LT(1);
                                    match(DQUOTES);
                                    pi.appendContent(dq.getText());
                                    break;
                                }
                                case QUOTES: {
                                    q = LT(1);
                                    match(QUOTES);
                                    pi.appendContent(q.getText());
                                    break;
                                }
                                case BOPEN: {
                                    bo = LT(1);
                                    match(BOPEN);
                                    pi.appendContent(bo.getText());
                                    break;
                                }
                                case BCLOSE: {
                                    bc = LT(1);
                                    match(BCLOSE);
                                    pi.appendContent(bc.getText());
                                    break;
                                }
                                case ELEM_S: {
                                    e_s = LT(1);
                                    match(ELEM_S);
                                    pi.appendContent(e_s.getText());
                                    break;
                                }
                                case LONGELEM_EE: {
                                    le_ee = LT(1);
                                    match(LONGELEM_EE);
                                    pi.appendContent(le_ee.getText());
                                    break;
                                }
                                case LONGELEM_ES: {
                                    le_es = LT(1);
                                    match(LONGELEM_ES);
                                    pi.appendContent(le_es.getText());
                                    break;
                                }
                                case DOCTYPE_S: {
                                    dt_s = LT(1);
                                    match(DOCTYPE_S);
                                    pi.appendContent(dt_s.getText());
                                    break;
                                }
                                case PI_S: {
                                    pi_s = LT(1);
                                    match(PI_S);
                                    pi.appendContent(pi_s.getText());
                                    break;
                                }
                                case WS: {
                                    ws = LT(1);
                                    match(WS);
                                    pi.appendContent(ws.getText());
                                    break;
                                }
                                case EQ: {
                                    eq = LT(1);
                                    match(EQ);
                                    pi.appendContent(eq.getText());
                                    break;
                                }
                                default: {
                                    break _loop111;
                                }
                            }
                        }
                        while(true);
                    }
                    break;
                }
                case PI_E: {
                    break;
                }
                default: {
                    throw new NoViableAltException(LT(1), getFilename());
                }
            }
        }
    }

    public final void esuite(
            ElementNode elt
    ) throws RecognitionException, TokenStreamException, ParseException, AttributeAlreadyExist {


        switch(LA(1)) {
            case SHORTELEM_E: {
                match(SHORTELEM_E);
                break;
            }
            case LONGELEM_EE: {
                match(LONGELEM_EE);
                long_suite(elt);
                break;
            }
            case WS: {
                match(WS);
                esuite2(elt);
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
    }

    public final void long_suite(ElementNode elt) throws RecognitionException, TokenStreamException, ParseException,
                                                         AttributeAlreadyExist {
        TreeNode t = null;
        ElementNode e = null;
        String s = null;

        {
            _loop126:
            do {
                if((_tokenSet_1.member(LA(1)))) {
                    t = node();
                    if(t != null) {
                        elt.appendChild(t);
                    }
                }
                else {
                    break _loop126;
                }

            }
            while(true);
        }
        match(LONGELEM_ES);
        s = name();
        {
        }
        if(! elt.getElementName().equals(s)) {
            throw new ParseException("The tag name must be equal");
        }
        {
            switch(LA(1)) {
                case WS: {
                    match(WS);
                    break;
                }
                case LONGELEM_EE: {
                    break;
                }
                default: {
                    throw new NoViableAltException(LT(1), getFilename());
                }
            }
        }
        match(LONGELEM_EE);
    }

    public final void esuite2(ElementNode elt) throws RecognitionException, TokenStreamException, ParseException,
                                                      AttributeAlreadyExist {

        String[] anAtt = null;
        String s;


        switch(LA(1)) {
            case SHORTELEM_E: {
                match(SHORTELEM_E);
                break;
            }
            case LONGELEM_EE: {
                match(LONGELEM_EE);
                long_suite(elt);
                break;
            }
            case XML:
            case NAME_START: {
                anAtt = attribute();

                s = (String) elt.getAttribute(anAtt[0]);
                if(s != null) {
                    throw new AttributeAlreadyExist(anAtt[0]);
                }

                elt.setAttribute(anAtt[0], anAtt[1]);

                {
                    switch(LA(1)) {
                        case WS: {
                            match(WS);
                            break;
                        }
                        case XML:
                        case NAME_START:
                        case SHORTELEM_E:
                        case LONGELEM_EE: {
                            break;
                        }
                        default: {
                            throw new NoViableAltException(LT(1), getFilename());
                        }
                    }
                }
                esuite2(elt);
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
    }


    public static final String[] _tokenNames = {
            "<0>",
            "EOF",
            "<2>",
            "NULL_TREE_LOOKAHEAD",
            "QUOTES",
            "DQUOTES",
            "XML",
            "COMMENT_S",
            "COMMENT_E",
            "COMMENT",
            "CDATA_S",
            "CDATA_E",
            "CDATA",
            "DOCTYPE_S",
            "BOPEN",
            "BCLOSE",
            "DIESE",
            "ESCAPE_LONGELEM_EE",
            "PI_S",
            "PI_E",
            "LETTER",
            "IDEOGRAPHIC",
            "BASECHAR",
            "DIGIT",
            "COMBININGCHAR",
            "EXTENDER",
            "NAMECHAR",
            "NAME_START",
            "NAME_SUITE",
            "ELEM_S",
            "SHORTELEM_E",
            "LONGELEM_ES",
            "LONGELEM_EE",
            "EQ",
            "WS",
            "TEXT",
            "QUOTE",
            "DQUOTE"
    };

    private static final long[] mk_tokenSet_0() {
        long[] data = {272596726386L, 0L};
        return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

    private static final long[] mk_tokenSet_1() {
        long[] data = {272596726384L, 0L};
        return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

    private static final long[] mk_tokenSet_2() {
        long[] data = {266690822208L, 0L};
        return data;
    }

    public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

    private static final long[] mk_tokenSet_3() {
        long[] data = {274744734322L, 0L};
        return data;
    }

    public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());

    private static final long[] mk_tokenSet_4() {
        long[] data = {274744726096L, 0L};
        return data;
    }

    public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

    private static final long[] mk_tokenSet_5() {
        long[] data = {274744726112L, 0L};
        return data;
    }

    public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());

}
// XMLReaderAdapter.java - adapt an SAX2 XMLReader to a SAX1 Parser
// Written by David Megginson, sax@megginson.com
// NO WARRANTY!  This class is in the public domain.

// $Id: XMLReaderAdapter.java,v 1.2 2000/01/22 16:28:23 david Exp $

package org.xml.sax.helpers;

import java.io.IOException;
import java.util.Locale;

import org.xml.sax.Parser;	// deprecated
import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.AttributeList; // deprecated
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler; // deprecated
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXNotSupportedException;


/**
 * Adapt a SAX2 XMLReader as a SAX1 Parser.
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * </blockquote>
 *
 * <p>This class wraps a SAX2 XMLReader and makes it act as a SAX1
 * Parser.  The XMLReader must support a true value for the
 * http://xml.org/sax/features/raw-names property or parsing will fail
 * with a SAXException; if the XMLReader supports a false value
 * for the http://xml.org/sax/features/namespaces property, that will
 * also be used to improve efficiency.</p>
 *
 * @since SAX 2.0
 * @author David Megginson, 
 *         <a href="mailto:sax@megginson.com">sax@megginson.com</a>
 * @version 2.0beta
 * @see org.xml.sax.Parser
 * @see org.xml.sax.XMLReader
 */
public class XMLReaderAdapter implements Parser, ContentHandler
{


    ////////////////////////////////////////////////////////////////////
    // Constructor.
    ////////////////////////////////////////////////////////////////////


    /**
     * Create a new adapter.
     *
     * <p>Create a new adapter, wrapped around a SAX2 XMLReader.
     * The adapter will make the XMLReader act like a SAX1
     * Parser.</p>
     *
     * @param xmlReader The SAX2 XMLReader to wrap.
     * @exception java.lang.NullPointerException If the argument is null.
     */
    public XMLReaderAdapter (XMLReader xmlReader)
    {
	if (xmlReader == null) {
	    throw new NullPointerException("XMLReader must not be null");
	}
	this.xmlReader = xmlReader;
	rawAtts = new AttributesAdapter();
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Parser.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set the locale for error reporting.
     *
     * <p>This is not supported in SAX2, and will always throw
     * an exception.</p>
     *
     * @param The locale for error reporting.
     * @see org.xml.sax.Parser#setLocale
     */
    public void setLocale (Locale locale)
	throws SAXException
    {
	throw new SAXNotSupportedException("setLocale not supported");
    }


    /**
     * Register the entity resolver.
     *
     * @param resolver The new resolver.
     * @see org.xml.sax.Parser#setEntityResolver
     */
    public void setEntityResolver (EntityResolver resolver)
    {
	xmlReader.setEntityResolver(resolver);
    }


    /**
     * Register the DTD event handler.
     *
     * @param handler The new DTD event handler.
     * @see org.xml.sax.Parser#setDTDHandler
     */
    public void setDTDHandler (DTDHandler handler)
    {
	xmlReader.setDTDHandler(handler);
    }


    /**
     * Register the SAX1 document event handler.
     *
     * <p>Note that the SAX1 document handler has no Namespace
     * support.</p>
     *
     * @param handler The new SAX1 document event handler.
     * @see org.xml.sax.Parser#setDocumentHandler
     */
    public void setDocumentHandler (DocumentHandler handler)
    {
	documentHandler = handler;
    }


    /**
     * Register the error event handler.
     *
     * @param handler The new error event handler.
     * @see org.xml.sax.Parser#setErrorHandler
     */
    public void setErrorHandler (ErrorHandler handler)
    {
	xmlReader.setErrorHandler(handler);
    }


    /**
     * Parse the document.
     *
     * <p>This method will throw an exception if the embedded
     * XMLReader does not support the 
     * http://xml.org/sax/features/raw-names property.</p>
     *
     * @param systemId The absolute URL of the document.
     * @exception java.io.IOException If there is a problem reading
     *            the raw content of the document.
     * @exception org.xml.sax.SAXException If there is a problem
     *            processing the document.
     * @see #parse(org.xml.sax.InputSource)
     * @see org.xml.sax.Parser#parse(java.lang.String)
     */
    public void parse (String systemId)
	throws IOException, SAXException
    {
	setupXMLReader();
	xmlReader.parse(systemId);
    }


    /**
     * Parse the document.
     *
     * <p>This method will throw an exception if the embedded
     * XMLReader does not support the 
     * http://xml.org/sax/features/raw-names property.</p>
     *
     * @param input An input source for the document.
     * @exception java.io.IOException If there is a problem reading
     *            the raw content of the document.
     * @exception org.xml.sax.SAXException If there is a problem
     *            processing the document.
     * @see #parse(java.lang.String)
     * @see org.xml.sax.Parser#parse(org.xml.sax.InputSource)
     */
    public void parse (InputSource input)
	throws IOException, SAXException
    {
	setupXMLReader();
	xmlReader.parse(input);
    }


    /**
     * Set up the XML reader.
     */
    private void setupXMLReader ()
	throws SAXException
    {
	xmlReader.setFeature("http://xml.org/sax/features/raw-names", true);
	try {
	    xmlReader.setFeature("http://xml.org/sax/features/namespaces",
	                         false);
	} catch (SAXException e) {
	    // NO OP
	}
	xmlReader.setContentHandler(this);
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ContentHandler.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set a document locator.
     *
     * @param locator The document locator.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     */
    public void setDocumentLocator (Locator locator)
    {
	documentHandler.setDocumentLocator(locator);
    }


    /**
     * Start document event.
     *
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument ()
	throws SAXException
    {
	documentHandler.startDocument();
    }


    /**
     * End document event.
     *
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument ()
	throws SAXException
    {
	documentHandler.endDocument();
    }


    /**
     * Adapt a SAX2 start prefix mapping event.
     *
     * @param prefix The prefix being mapped.
     * @param uri The Namespace URI being mapped to.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping (String prefix, String uri)
    {
    }


    /**
     * Adapt a SAX2 end prefix mapping event.
     *
     * @param prefix The prefix being mapped.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping (String prefix)
    {
    }


    /**
     * Adapt a SAX2 start element event.
     *
     * @param uri The Namespace URI.
     * @param localName The Namespace local name.
     * @param rawName The raw XML 1.0 name.
     * @param atts The SAX2 attributes.
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void startElement (String uri, String localName,
			      String rawName, Attributes atts)
	throws SAXException
    {
	rawAtts.setAttributes(atts);
	documentHandler.startElement(rawName, rawAtts);
    }


    /**
     * Adapt a SAX2 end element event.
     *
     * @param uri The Namespace URI.
     * @param localName The Namespace local name.
     * @param rawName The raw XML 1.0 name.
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement (String uri, String localName,
			    String rawName)
	throws SAXException
    {
	documentHandler.endElement(rawName);
    }


    /**
     * Adapt a SAX2 characters event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use.
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters (char ch[], int start, int length)
	throws SAXException
    {
	documentHandler.characters(ch, start, length);
    }


    /**
     * Adapt a SAX2 ignorable whitespace event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use.
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */
    public void ignorableWhitespace (char ch[], int start, int length)
	throws SAXException
    {
	documentHandler.ignorableWhitespace(ch, start, length);
    }


    /**
     * Adapt a SAX2 processing instruction event.
     *
     * @param target The processing instruction target.
     * @param data The remainder of the processing instruction
     * @exception org.xml.sax.SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void processingInstruction (String target, String data)
	throws SAXException
    {
	documentHandler.processingInstruction(target, data);
    }


    /**
     * Adapt a SAX2 skipped entity event.
     *
     * @param name The name of the skipped entity.
     * @see org.xml.sax.ContentHandler#skippedEntity
     */
    public void skippedEntity (String name)
	throws SAXException
    {
    }



    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    XMLReader xmlReader;
    DocumentHandler documentHandler;
    AttributesAdapter rawAtts;



    ////////////////////////////////////////////////////////////////////
    // Internal class.
    ////////////////////////////////////////////////////////////////////


    /**
     * Internal class to wrap a SAX2 Attributes object for SAX1.
     */
    final class AttributesAdapter implements AttributeList
    {
	AttributesAdapter ()
	{
	}


	/**
	 * Set the embedded Attributes object.
	 *
	 * @param The embedded SAX2 Attributes.
	 */ 
	void setAttributes (Attributes attributes)
	{
	    this.attributes = attributes;
	}


	/**
	 * Return the number of attributes.
	 *
	 * @return The length of the attribute list.
	 * @see org.xml.sax.AttributeList#getLength
	 */
	public int getLength ()
	{
	    return attributes.getLength();
	}


	/**
	 * Return the raw name of an attribute by position.
	 *
	 * @return The raw name.
	 * @see org.xml.sax.AttributeList#getName
	 */
	public String getName (int i)
	{
	    return attributes.getRawName(i);
	}


	/**
	 * Return the type of an attribute by position.
	 *
	 * @return The type.
	 * @see org.xml.sax.AttributeList#getType(int)
	 */
	public String getType (int i)
	{
	    return attributes.getType(i);
	}


	/**
	 * Return the value of an attribute by position.
	 *
	 * @return The value.
	 * @see org.xml.sax.AttributeList#getValue(int)
	 */
	public String getValue (int i)
	{
	    return attributes.getValue(i);
	}


	/**
	 * Return the type of an attribute by raw name.
	 *
	 * @return The type.
	 * @see org.xml.sax.AttributeList#getType(java.lang.String)
	 */
	public String getType (String rawName)
	{
	    return attributes.getType(rawName);
	}


	/**
	 * Return the value of an attribute by raw name.
	 *
	 * @return The value.
	 * @see org.xml.sax.AttributeList#getValue(java.lang.String)
	 */
	public String getValue (String rawName)
	{
	    return attributes.getValue(rawName);
	}

	private Attributes attributes;
    }

}

// end of XMLReaderAdapter.java

/***************************************************************
Copyright © 2012 52°North Initiative for Geospatial Open Source Software GmbH

 Author: < >

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.aviation.aixm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.parser.XMLHandlingException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.x52North.wps.feature.aixm.geometryFeature.SurfaceFeatureDocument;

import aero.aixm.schema.x51.AirspaceDocument;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.RouteSegmentDocument;
import aero.aixm.schema.x51.RouteSegmentType;

/**
 * Parser for AIXM 5.1 aviation feature data.
 * 
 * @author matthes rieke
 *
 */
public class AIXM51BasicParser extends AbstractParser {
	
	private static final Logger logger = Logger.getLogger(AIXM51BasicParser.class);
	
	public static final String GML32_SCHEMA_LOCATION = "http://www.opengis.net/gml/3.2 http://schemas.opengis.net/gml/3.2.1/gml.xsd";
	public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	public static String featureWrapperSchemaPath;
	public static String featureWrapperSchemaLocation;

	static {
		try {
			String xsd = SurfaceFeatureDocument.type.getSourceName();
			InputStream input = SurfaceFeatureDocument.type.getTypeSystem().getSourceAsStream(xsd);
			File temp = File.createTempFile("featureWrapperSchema", ".xsd");
			temp.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(temp);
			InputStreamReader isr = new InputStreamReader(input);

			while (isr.ready()) {
				fos.write(isr.read());
			}

			fos.flush();
			fos.close();
			isr.close();

			featureWrapperSchemaPath = temp.toURI().toString();
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		if (featureWrapperSchemaPath != null) {
			featureWrapperSchemaLocation = SurfaceFeatureDocument.type.getDocumentElementName().getNamespaceURI() +
				" " +featureWrapperSchemaPath;
		}
	}
	
	public AIXM51BasicParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}

	@Override
	public IData parse(InputStream input, String mimeType, String schema) {
		XmlObject request = null;
		try {
			request = XMLBeansParser.parse(input);
		} catch (XMLHandlingException e) {
			throw new RuntimeException(e);
		}
		
		if (request == null) return null;
		
		return handleInput(request);
	}

	private IData handleInput(XmlObject request) {
		if (request instanceof AirspaceDocument) {
			return parseAirspace(((AirspaceDocument) request).getAirspace());
		} else if (request instanceof AirspaceType) {
			return parseAirspace((AirspaceType) request);
		}
		
		else if (request instanceof RouteSegmentDocument) {
			return parseRouteSegment(((RouteSegmentDocument) request).getRouteSegment());
		} else if (request instanceof RouteSegmentType) {
			return parseRouteSegment((RouteSegmentType) request);
		}
		
		throw new UnsupportedOperationException("Only Airspace and RouteSegment are currently supported.");
	}

	private IData parseRouteSegment(RouteSegmentType routeSegment) {
		return new RouteSegmentParser(routeSegment).parse();
	}

	private IData parseAirspace(AirspaceType airspace) {
		return new AirspaceParser(airspace).parse();
	}
	
	public static void appendSchemaLocation(XmlObject doc,
			String schemaLocation) {
		XmlCursor cursor = doc.newCursor();
		if (cursor.toFirstChild()) {
			cursor.setAttributeText(new QName(XSI_NAMESPACE, "schemaLocation"), schemaLocation);
		}
		cursor.dispose();
	}

}

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

import javax.xml.namespace.QName;

import net.opengis.gml.x32.CurveDocument;
import net.opengis.gml.x32.CurvePropertyDocument;
import net.opengis.gml.x32.FeatureCollectionDocument;
import net.opengis.gml.x32.FeatureCollectionType;
import net.opengis.gml.x32.FeaturePropertyType;

import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.GML32BasicParser;
import org.x52North.wps.feature.aixm.geometryFeature.CurveFeatureDocument;
import org.x52North.wps.feature.aixm.geometryFeature.CurveFeatureType;

import aero.aixm.schema.x51.CurvePropertyType;
import aero.aixm.schema.x51.CurveType;
import aero.aixm.schema.x51.RouteSegmentTimeSlicePropertyType;
import aero.aixm.schema.x51.RouteSegmentType;

public class RouteSegmentParser {

	private static GML32BasicParser gmlParser = GML32BasicParser.getInstanceForConfiguration(new AIXM51Configuration());
	private RouteSegmentType routeSegment;

	public RouteSegmentParser(RouteSegmentType routeSegment) {
		this.routeSegment = routeSegment;
	}

	public IData parse() {
		return parseTimeSlices(this.routeSegment.getTimeSliceArray());
	}

	private IData parseTimeSlices(RouteSegmentTimeSlicePropertyType[] timeSliceArray) {

		if (timeSliceArray != null && timeSliceArray.length > 0) {
			return parseTimeSlice(timeSliceArray[0]);
		}
		
		return null;
	}

	private IData parseTimeSlice(RouteSegmentTimeSlicePropertyType timeSlice) {
		if (timeSlice.getRouteSegmentTimeSlice().isSetCurveExtent()) {
			return parseCurveExtent(timeSlice.getRouteSegmentTimeSlice().getCurveExtent());
		} else {
			throw new UnsupportedOperationException("Currently only geometries encoded as CurveExtent are supported.");
		}
	}

	private IData parseCurveExtent(CurvePropertyType curveExtent) {
		FeatureCollectionDocument coll = createWrapperFeatureCollection(curveExtent.getCurve());
		
		String[] split = AIXM51BasicParser.GML32_SCHEMA_LOCATION.split(" ");
		return gmlParser.parse(coll.newInputStream(), new QName(split[0].trim(), split[1].trim()));
	}
	
	private FeatureCollectionDocument createWrapperFeatureCollection(
			CurveType curveType) {
		FeatureCollectionDocument coll = FeatureCollectionDocument.Factory.newInstance();
		FeatureCollectionType collection = coll.addNewFeatureCollection();
		FeaturePropertyType member = collection.addNewFeatureMember();
		CurveFeatureType feature = CurveFeatureType.Factory.newInstance();
		feature.setType("Airspace");
		
		CurvePropertyDocument surfacePropDoc = CurvePropertyDocument.Factory.newInstance();
		net.opengis.gml.x32.CurvePropertyType surfaceProp = surfacePropDoc.addNewCurveProperty();
		surfaceProp.setAbstractCurve(curveType);
		XmlUtil.qualifySubstitutionGroup(surfaceProp.getAbstractCurve(), CurveDocument.type.getDocumentElementName(), net.opengis.gml.x32.CurvePropertyType.type);
		
		feature.setGeometry(surfaceProp);
		member.setAbstractFeature(feature);
		XmlUtil.qualifySubstitutionGroup(member.getAbstractFeature(), CurveFeatureDocument.type.getDocumentElementName());
		
		AIXM51BasicParser.appendSchemaLocation(coll, AIXM51BasicParser.GML32_SCHEMA_LOCATION + " " + AIXM51BasicParser.featureWrapperSchemaLocation);
		return coll;
	}

}

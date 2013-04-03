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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.FeatureCollectionDocument;
import net.opengis.gml.x32.FeatureCollectionType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.SurfaceDocument;
import net.opengis.gml.x32.SurfacePropertyDocument;

import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GML32BasicParser;
import org.x52North.wps.feature.aixm.geometryFeature.SurfaceFeatureDocument;
import org.x52North.wps.feature.aixm.geometryFeature.SurfaceFeatureType;

import aero.aixm.schema.x51.AirspaceGeometryComponentPropertyType;
import aero.aixm.schema.x51.AirspaceTimeSlicePropertyType;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.AirspaceVolumePropertyType;
import aero.aixm.schema.x51.SurfacePropertyType;
import aero.aixm.schema.x51.SurfaceType;

public class AirspaceParser {

	private AirspaceType airspace;
	private static GML32BasicParser gmlParser = GML32BasicParser.getInstanceForConfiguration(new AIXM51Configuration());
	

	public AirspaceParser(AirspaceType airspace) {
		this.airspace = airspace;
	}

	public IData parse() {
		if (this.airspace.getTimeSliceArray() == null || this.airspace.getTimeSliceArray().length == 0) {
			return null;
		}

		return parseTimeSlice(this.airspace.getTimeSliceArray(0));
	}

	private IData parseTimeSlice(AirspaceTimeSlicePropertyType timeSlice) {
		AirspaceGeometryComponentPropertyType[] geom = timeSlice.getAirspaceTimeSlice().getGeometryComponentArray();

		if (geom == null || geom.length == 0) {
			return null;
		}

		return parseGeometryComponentArray(geom);
	}

	private IData parseGeometryComponentArray(
			AirspaceGeometryComponentPropertyType[] geom) {
		List<GTVectorDataBinding> geometries = new ArrayList<GTVectorDataBinding>();

		for (AirspaceGeometryComponentPropertyType g : geom) {
			geometries.addAll(parseGeometryComponent(g));
		}

		return combineGeometriesToData(geometries);
	}


	private List<GTVectorDataBinding> parseGeometryComponent(
			AirspaceGeometryComponentPropertyType g) {

		if (g.getAirspaceGeometryComponent() != null && g.getAirspaceGeometryComponent().isSetTheAirspaceVolume()) {
			return parseAirspaceVolume(g.getAirspaceGeometryComponent().getTheAirspaceVolume());
		}

		return null;
	}

	private List<GTVectorDataBinding> parseAirspaceVolume(
			AirspaceVolumePropertyType theAirspaceVolume) {

		if (theAirspaceVolume.getAirspaceVolume().isSetHorizontalProjection()) {
			return parseHoriontalProjection(theAirspaceVolume.getAirspaceVolume().getHorizontalProjection());
		}

		return null;
	}

	private List<GTVectorDataBinding> parseHoriontalProjection(
			SurfacePropertyType horizontal) {
		List<GTVectorDataBinding> result = new ArrayList<GTVectorDataBinding>();

		FeatureCollectionDocument coll = createWrapperFeatureCollection(horizontal.getSurface());
		
		String[] split = AIXM51BasicParser.GML32_SCHEMA_LOCATION.split(" ");
		result.add(gmlParser.parse(coll.newInputStream(), new QName(split[0].trim(), split[1].trim())));
		return result;
	}

	private FeatureCollectionDocument createWrapperFeatureCollection(
			SurfaceType surface) {
		FeatureCollectionDocument coll = FeatureCollectionDocument.Factory.newInstance();
		FeatureCollectionType collection = coll.addNewFeatureCollection();
		FeaturePropertyType member = collection.addNewFeatureMember();
		SurfaceFeatureType feature = SurfaceFeatureType.Factory.newInstance();
		feature.setType("Airspace");
		
		SurfacePropertyDocument surfacePropDoc = SurfacePropertyDocument.Factory.newInstance();
		net.opengis.gml.x32.SurfacePropertyType surfaceProp = surfacePropDoc.addNewSurfaceProperty();
		surfaceProp.setAbstractSurface(surface);
		XmlUtil.qualifySubstitutionGroup(surfaceProp.getAbstractSurface(), SurfaceDocument.type.getDocumentElementName(), net.opengis.gml.x32.SurfaceType.type);
		
		feature.setGeometry(surfaceProp);
		member.setAbstractFeature(feature);
		XmlUtil.qualifySubstitutionGroup(member.getAbstractFeature(), SurfaceFeatureDocument.type.getDocumentElementName());
		
		AIXM51BasicParser.appendSchemaLocation(coll, AIXM51BasicParser.GML32_SCHEMA_LOCATION + " " + AIXM51BasicParser.featureWrapperSchemaLocation);
		return coll;
	}


	private IData combineGeometriesToData(List<GTVectorDataBinding> geometries) {
		/*
		 * TODO find a solution for multiple airspace geometry components
		 */
		if (geometries != null && geometries.size() > 0) {
			return geometries.get(0);
		}
		
		return null;
	}

}

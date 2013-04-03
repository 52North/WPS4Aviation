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

import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.gml3.XSDIdRegistry;
import org.geotools.gml3.bindings.CurvePropertyTypeBinding;
import org.geotools.gml3.bindings.GML3EncodingUtils;
import org.geotools.gml3.bindings.MultiPolygonTypeBinding;
import org.geotools.gml3.bindings.PolygonPatchTypeBinding;
import org.geotools.gml3.bindings.SurfacePropertyTypeBinding;
import org.geotools.gml3.v3_2.GML;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.Binding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;

public class AIXM51Configuration extends GMLConfiguration {
	
	public static final QName AIXM_SURFACE_TYPE = new QName("http://www.aixm.aero/schema/5.1", "SurfaceType");
	
	@Override
	protected void configureBindings(Map bindings) {
		super.configureBindings(bindings);
		bindings.put(GML.SurfaceType, GML32SurfaceBinding.class);
		bindings.put(GML.PolygonPatchType, GML32PolygonPatchBinding.class);
		bindings.put(GML.SurfacePropertyType, GML32SurfacePropertyTypeBinding.class);
		bindings.put(GML.CurveType, GML32CurvePropertyTypeBinding.class);
	}
	
	/**
	 * The corresponding workaround binding.
	 * 
	 * @author matthes rieke
	 *
	 */
	public static class GML32SurfaceBinding extends AbstractComplexBinding {

		private GeometryFactory gf;

		public GML32SurfaceBinding(GeometryFactory gf) {
			this.gf = gf;
		}
		
		@Override
		public QName getTarget() {
			return GML.SurfaceType;
		}

		@Override
		public Class<?> getType() {
			return MultiPolygon.class;
		}

		@Override
		public int getExecutionMode() {
			return Binding.OVERRIDE;
		}

		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			return new MultiPolygonTypeBinding(gf).parse(instance, node, value);
		}
		

	}
	
	public static class GML32PolygonPatchBinding extends PolygonPatchTypeBinding {

		public GML32PolygonPatchBinding(GeometryFactory gf) {
			super(gf);
		}

		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			// TODO Auto-generated method stub
			return super.parse(instance, node, value);
		}
		
	}
	
	public static class GML32SurfacePropertyTypeBinding extends SurfacePropertyTypeBinding {

		public GML32SurfacePropertyTypeBinding(GML3EncodingUtils encodingUtils,
				XSDIdRegistry idRegistry) {
			super(encodingUtils, idRegistry);
		}

		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			Object result = node.getChildValue(MultiPolygon.class);
			
			if (result != null) {
				return result;
			}
			
			return super.parse(instance, node, value);
		}
		
		
		
	}
	
	public static class GML32CurvePropertyTypeBinding extends CurvePropertyTypeBinding {

		public GML32CurvePropertyTypeBinding(GML3EncodingUtils encodingUtils,
				XSDIdRegistry idRegistry) {
			super(encodingUtils, idRegistry);
		}

		@Override
		public Object parse(ElementInstance instance, Node node, Object value)
				throws Exception {
			Object result = searchForSegments(node);
			
			if (result != null) {
				return result;
			}
			
			return super.parse(instance, node, value);
		}

		private Object searchForSegments(Node node) {
			for (Object n : node.getChildren()) {
				if (n instanceof Node) {
					if (((Node) n).getValue().getClass() == super.getGeometryType()) {
						return ((Node) n).getValue();
					}
					return searchForSegments((Node) n);
				}
			}
			
			return null;
		}
		
		
		
	}


}

package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gml3.bindings.PolygonTypeBinding;
import org.geotools.gml3.bindings.ext.MultiSurfaceTypeBinding;
import org.geotools.gml3.v3_2.GML;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.XSD;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.Feature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GML32MultiGeometryGenerator extends AbstractGenerator {

	public static final String TARGET_NAMESPACE = "http://www.opengis.net/ows9/aviation/wps";
	public static final String SCHEMA_LOCATION = "http://test.schemas.opengis.net/ows-9/aviation/wps/aixmIntersectionResult.xsd";
	private static final QName ROOT_ELEMENT = new QName(TARGET_NAMESPACE, "MultiGeometry");
	private static final QName GML_ID_NAME = new QName(GML.NAMESPACE, "id");
	private static final Random RANDOM = new Random();

	public GML32MultiGeometryGenerator() {
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
	throws IOException {
		FeatureCollection<?, ?> features = ((GTVectorDataBinding) data).getPayload();

		FeatureIterator<?> it = features.features();
		Feature feature;
		List<Element> geoms = new ArrayList<Element>();
		MultiGeometryEncoder encoder = createEncoder();
		try {
			while (it.hasNext()) {
				feature = it.next();
				if (feature != null && feature.getDefaultGeometryProperty() != null) {
					encoder.encodeAsXmlObject(feature.getDefaultGeometryProperty().getValue(), geoms);
				}
			}
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (TransformerException e) {
			throw new IOException(e);
		}

		if (!geoms.isEmpty()) {
			try {
				return encoder.encodeGeometriesAsStream(geoms);
			} catch (TransformerException e) {
				throw new IOException(e);
			}
		} else {
			return null;
		}
	}


	private MultiGeometryEncoder createEncoder() {
		MultiGeometryEncoder encoder = new MultiGeometryEncoder();
		return encoder;
	}

	private class MultiGeometryEncoder {

		private Encoder encoder;
		private XSD xsd;
		private GMLConfiguration config;

		public MultiGeometryEncoder() {
			this.config = new GMLWithIDConfiguration();
			this.xsd = new XSD() {

				@Override
				public String getSchemaLocation() {
					return SCHEMA_LOCATION;
				}

				@Override
				public String getNamespaceURI() {
					return TARGET_NAMESPACE;
				}
			};
		}

		public void encodeAsXmlObject(Object geometry, List<Element> geoms) throws IOException, TransformerException, SAXException {
			this.encoder = new Encoder(config);
			this.encoder.setOmitXMLDeclaration(true);
			if (geometry instanceof MultiPolygon) {
				encodeMultiPolygon((MultiPolygon) geometry, geoms);
			} else if (geometry instanceof Polygon) {
				encodePolygon((Polygon) geometry, geoms);
			} else if (geometry instanceof LinearRing) {
				encodeLinearRing((LinearRing) geometry, geoms);
			} else if (geometry instanceof LineString) {
				encodeLineString((LineString) geometry, geoms);
			} else if (geometry instanceof MultiLineString) {
				encodeMultiLineString((MultiLineString) geometry, geoms);
			} else if (geometry instanceof Point) {
				encodePoint((Point) geometry, geoms);
			} else if (geometry instanceof MultiPoint) {
				encodeMultiPoint((MultiPoint) geometry, geoms);
			}
		}

		private void encodeMultiPoint(MultiPoint geometry, List<Element> geoms) throws IOException, SAXException, TransformerException {
			if (geometry.getNumGeometries() == 1) {
				encodePoint((Point) geometry.getGeometryN(0), geoms);
			}
			else {
				geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "MultiPoint")).getDocumentElement());
			}
		}

		private void encodePoint(Point geometry, List<Element> geoms) throws IOException, SAXException, TransformerException {
			geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "Point")).getDocumentElement());			
		}

		private void encodeMultiLineString(MultiLineString geometry,
				List<Element> geoms) throws IOException, TransformerException, SAXException {
			if (geometry.getNumGeometries() == 1) {
				encodeLineString((LineString) geometry.getGeometryN(0), geoms);
			} else {
				geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "MultiLineString")).getDocumentElement());
			}
		}

		private void encodeLineString(LineString geometry, List<Element> geoms) throws IOException, TransformerException, SAXException {
			geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "LineString")).getDocumentElement());
		}

		private void encodeLinearRing(LinearRing geometry, List<Element> geoms) throws IOException, SAXException, TransformerException {
			geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "LinearRing")).getDocumentElement());
		}

		private void encodePolygon(Polygon geometry, List<Element> geoms) throws IOException, SAXException, TransformerException {
			geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "Polygon")).getDocumentElement());
		}

		private void encodeMultiPolygon(MultiPolygon geometry, List<Element> geoms) throws IOException, SAXException, TransformerException {
			geoms.add(encoder.encodeAsDOM(geometry, new QName(GML.NAMESPACE, "MultiSurface")).getDocumentElement());
		}

		public InputStream encodeGeometriesAsStream(List<Element> geoms) throws IOException, TransformerException {
			Document doc = createEmptyDocument();  
			Element root = createRootElement(this.xsd.getSchema().getElementDeclarations(), doc);

			if (root == null) return null;

			appendGeometries(geoms, root);

			doc.appendChild(root);
			
			return createStreamFromDocument(doc);
		}

		private InputStream createStreamFromDocument(Document doc)
				throws TransformerException, TransformerConfigurationException,
				TransformerFactoryConfigurationError,
				UnsupportedEncodingException {
			DOMSource source = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(writer));
			return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
		}

		private Document createEmptyDocument() throws IOException {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new IOException(e);
			}
			Document doc = builder.newDocument();
			
			return doc;
		}

		private void setSchemaLocation(Element doc) {
			doc.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
					TARGET_NAMESPACE +" "+ SCHEMA_LOCATION);
			
		}

		private void appendGeometries(List<Element> geoms, Element root) {
			for (Element geom : geoms) {
				Element container = root.getOwnerDocument().createElementNS(GML.NAMESPACE, "gml:geometryMember");
				container.appendChild(root.getOwnerDocument().importNode(geom, true));
				root.appendChild(container);	
			}
		}

		private Element createRootElement(EList<XSDElementDeclaration> elements, Document doc) {
			QName qn;
			for (XSDElementDeclaration element : elements) {
				qn = new QName(element.getTargetNamespace(), element.getName());
				if (qn.equals(ROOT_ELEMENT)) {
					Element elem = doc.createElementNS(qn.getNamespaceURI(), "ows9:"+ qn.getLocalPart());
					addGmlId(elem);
					setSchemaLocation(elem);
					return elem;
				}
			}
			return null;
		}

		private void addGmlId(Element elem) {
			elem.setAttributeNS(GML_ID_NAME.getNamespaceURI(), "gml:"+GML_ID_NAME.getLocalPart(), createGmlId(null, GML_ID_NAME).toString());			
		}

	}
	
	private static class GMLWithIDConfiguration extends GMLConfiguration {

		private static GeometryFactory gf = new GeometryFactory();
		
		@Override
		protected void configureBindings(Map bindings) {
			super.configureBindings(bindings);
			bindings.put(GML.PolygonType, new PolygonWithIDBinding(gf));
			bindings.put(GML.MultiSurfaceType, new MultiSurfaceWithIDBinding(gf));
		}
		
	}
	
	private static class PolygonWithIDBinding extends PolygonTypeBinding {

		public PolygonWithIDBinding(GeometryFactory gFactory) {
			super(gFactory);
		}
		
		@Override
		public Object getProperty(Object object, QName name) throws Exception {
			Object result = super.getProperty(object, name);
			return createGmlId(result, name);
		}
		
	}
	
	private static class MultiSurfaceWithIDBinding extends MultiSurfaceTypeBinding {

		public MultiSurfaceWithIDBinding(GeometryFactory gFactory) {
			super(gFactory);
		}
		
		@Override
		public Object getProperty(Object object, QName name) throws Exception {
			Object result = super.getProperty(object, name);
			return createGmlId(result, name);
		}
		
	}
	
	private static Object createGmlId(Object result, QName name) {
		if (result == null && name.equals(GML_ID_NAME)) {
			StringBuilder sb = new StringBuilder();
			sb.append("id-");
			sb.append(RANDOM.nextInt(100000));
			return sb.toString();
		}
		return result;
	}
	

}

52°North Web Processing Service for Aviation
================================

This project includes algorithms to supports spatial operations on
AIXM 5.1 features.

For information on the 52°North WPS visit the
[52°North Processing Community](http://52north.org/communities/geoprocessing/).

This implementation includes parsers for specific AIXM features, currently limited
to Airspace and RouteSegment features. The developed processes and algorithms provide
classic geospatial computations such as the evaluation of spatial relations (e.g.
*Airspace A* intersects *RouteSegment B*) and the calculation of the intersection
geometries.

All developed processes are backed by an
[ArcGIS Server 10.0](http://www.esri.com/software/arcgis/arcgisserver)
to support robust and precise geometry calculations. A useful by-product of the
OWS-9 activities was the enhanced maturity of the ArcGIS Server backend. The WPS
AGS module now supports caching of ServerContext objects to improve the performance
of the AGS bridge.

*A demo instance of the Aviation-enabled WPS is available at
[the 52°North demo server](http://geoprocessing.demo.52north.org:8080/aviation-wps).*

Project Structure
================================

This maven reactor project contains three submodules.

<dl>
  <dt>aviation-processes</dt>
  <dd>The algorithm and parser implementations</dd>
  
  <dt>aixm-feature-wrapper</dt>
  <dd>Provides bindings for an additional schema, supporting the bridge to AGS</dd>
  
  <dt>52n-wps-4-aviation</dt>
  <dd>A dedicated module to create a deployable .WAR file with the aviation processes
  included. It is disabled by default. Use "mvn clean install -Dcreate-webapp=true"
  to enable it.</dd>
</dl>

Geotools version
================================
The previous releases of 52°North WPS use Geotools version 2.7.x. As these versions has
various bugs in the GML3 module this project replaces the Geotools libraries with version 8.6.
In particular, the old versions are excluded in the 52n-wps-webapp war overlay and the 8.6
libraries are defined as dependecies of the 52n-wps-4-aviation module.

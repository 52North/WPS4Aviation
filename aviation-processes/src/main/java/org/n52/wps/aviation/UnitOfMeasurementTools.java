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
package org.n52.wps.aviation;

import java.util.HashMap;
import java.util.Map;

/**
 * Static tool class for unit of measurement concerns.
 * 
 * @author matthes rieke
 *
 */
public class UnitOfMeasurementTools {
	
	private static Map<String, String> ucumToAGS;
	
	static {
		ucumToAGS = new HashMap<String, String>();
		ucumToAGS.put("km", "Kilometers");
		ucumToAGS.put("m", "Meters");
		ucumToAGS.put("dm", "Decimeters");
		ucumToAGS.put("cm", "Centimeters");
		ucumToAGS.put("mm", "Millimeters");
		ucumToAGS.put("[mi_us]", "Miles");
		ucumToAGS.put("[yd_us]", "Yards");
		ucumToAGS.put("[ft_us]", "Feet");
		ucumToAGS.put("[in_us]", "Inches");
		ucumToAGS.put("[nmi_i]", "NauticalMiles");
	}
	
	/**
	 * UCUM to ArcGIS Server Linear Unit mapping.
	 * 
	 * @param ucum the unit as "unified code for unit of measurement"
	 * @return the ArcGIS Server equivalent
	 */
	public static String ucumToAGSLinearUnitString(String ucum) {
		if (ucumToAGS.containsKey(ucum)) {
			return ucumToAGS.get(ucum);
		}
		return ucum;
	}

}

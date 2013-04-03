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

import org.n52.wps.io.data.IData;

/**
 * interface for the spatial analysis backend.
 * 
 * @author matthes rieke
 *
 */
public interface SpatialAnalysisProvider {
	
	/**
	 * calculates the intersection geometry
	 * of the provided features
	 */
	public IData intersection(IData... features);
	
	/**
	 * calculate a buffer around the given geometry.
	 * 
	 * @param feature the feature to apply the buffer to
	 * @param bufferDistance the buffer distance
	 * @param distanceUom the distance unit of measurement
	 * @return
	 */
	public IData buffer(IData feature, double bufferDistance, String distanceUom);
	
	/**
	 * feature1 intersects feature2
	 */
	public boolean intersects(IData feature1, IData feature2);
	
	/**
	 * feature1 touches feature2
	 */
	public boolean touches(IData feature1, IData feature2);

	/**
	 * feature1 completely contains feature2
	 */
	public boolean contains(IData feature1, IData feature2);

	/**
	 * feature1 covers feature2
	 */
	public boolean covers(IData feature1, IData feature2);
	
	/**
	 * feature1 crosses feature2
	 */
	public boolean crosses(IData feature1, IData feature2);
	
	/**
	 * feature1 is disjoint to feature2 (not intersects)
	 */
	public boolean disjoint(IData feature1, IData feature2);
	
	/**
	 * feature1 equals feature2
	 */
	public boolean equals(IData feature1, IData feature2);
	
	/**
	 * feature1 overlaps feature2
	 */
	public boolean overlaps(IData feature1, IData feature2);
	
	/**
	 * feature1 is within feature2
	 */
	public boolean within(IData feature1, IData feature2);


	/**
	 * @return true if this provider can be used (e.g. backend is initialized properly)
	 */
	public boolean isReady();
	
	/**
	 * free resources
	 */
	public void shutdown();

}

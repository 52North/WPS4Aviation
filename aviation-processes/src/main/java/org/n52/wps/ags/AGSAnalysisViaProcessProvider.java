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
package org.n52.wps.ags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.aviation.SpatialAnalysisProvider;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation makes direct use of the AGS workspace
 * without the overhead of pushing the data through the AGS Algorithms.
 * 
 * @author matthes rieke
 *
 */
public class AGSAnalysisViaProcessProvider implements SpatialAnalysisProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(AGSAnalysisViaProcessProvider.class);
	
	private static final String INTERSECTION = "Intersect_analysis";
	private static final String TOUCHES = "Touches_analysis";
	private IAlgorithmRepository agsRepository;
	private String[] requiredProcesses  = new String[]{
		INTERSECTION, TOUCHES
	};
	private List<String> availableProcesses = new ArrayList<String>();

	public static void main(String[] args) {
		new AGSAnalysisViaProcessProvider().intersection(null, null);
	}
	
	public AGSAnalysisViaProcessProvider() {
		this.agsRepository = RepositoryManager.getInstance().getAlgorithmRepository(AGSProcessRepository.class.getName());
		if (this.agsRepository == null) {
			this.agsRepository = new AGSProcessRepository();
		}
		
		for (String processId : requiredProcesses) {
			if (this.agsRepository.getAlgorithm(processId) != null) {
				this.availableProcesses.add(processId);
			}
		}
	}

	@Override
	public IData intersection(IData... features) {
		if (this.availableProcesses.contains(INTERSECTION)) {
			IAlgorithm algo = this.agsRepository.getAlgorithm(INTERSECTION);
			Map<String, List<IData>> map = prepareFeatures(features);
			try {
				return processResult(algo.run(map), INTERSECTION);
			} catch (ExceptionReport e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private IData processResult(Map<String, IData> result, String process) {
		if (process.equals(INTERSECTION)) {
			return result.get("out_feature_class");
		} else if (process.equals(TOUCHES)) {
			
		}
		return null;
	}

	private Map<String, List<IData>> prepareFeatures(IData... features) {
		List<IData> featureList = new ArrayList<IData>();
		try {
			for (IData data : features) {
				featureList.add(convertToGenericFileData(data));
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		HashMap<String, List<IData>> map = new HashMap<String, List<IData>>();
		map.put("in_features", featureList);
		return map;
	}

	private IData convertToGenericFileData(IData data) throws IOException {
		if (data instanceof GTVectorDataBinding) {
			return new GenericFileDataBinding(new GenericFileData(((GTVectorDataBinding) data).getPayload()));	
		}
		return null;
	}

	@Override
	public boolean intersects(IData feature1, IData feature2) {
		return false;
	}

	@Override
	public boolean touches(IData feature1, IData feature2) {
		return false;
	}
	
	@Override
	public boolean contains(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean covers(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean crosses(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disjoint(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean equals(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean overlaps(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean within(IData feature1, IData feature2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		Collection<String> algos = this.agsRepository.getAlgorithmNames();
		return algos != null && !algos.isEmpty();
	}

	@Override
	public IData buffer(IData feature, double bufferDistance, String distanceUom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
	}

}

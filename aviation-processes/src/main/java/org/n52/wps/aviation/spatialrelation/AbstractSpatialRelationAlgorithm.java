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
package org.n52.wps.aviation.spatialrelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.ags.AGSProperties;
import org.n52.wps.aviation.AviationConfiguration;
import org.n52.wps.aviation.SpatialAnalysisProvider;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

public abstract class AbstractSpatialRelationAlgorithm extends AbstractSelfDescribingAlgorithm {

	private static final String INPUT_FIRST_FEATURE_IDENTIFIER = "Feature1";
	private static final String INPUT_SECOND_FEATURE_IDENTIFIER = "Feature2";
	private static final String OUTPUT_RESULT_IDENTIFIER = "Result";

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		AGSProperties.getInstance().bootstrapArcobjectsJar();
		
		Map<String,IData> result = new HashMap<String, IData>();
		List<IData> data = inputData.get(INPUT_FIRST_FEATURE_IDENTIFIER);
		
		if (data == null || data.size() > 1) {
			throw new IllegalArgumentException("Only one input with identifier "+
					INPUT_FIRST_FEATURE_IDENTIFIER +" allowed.");
		}
		IData feature1 = data.get(0);
		
		data = inputData.get(INPUT_SECOND_FEATURE_IDENTIFIER);
		if (data == null || data.size() > 1) {
			throw new IllegalArgumentException("Only one input with identifier "+
					INPUT_SECOND_FEATURE_IDENTIFIER +" allowed.");
		}
		IData feature2 = data.get(0);
		
		LiteralBooleanBinding output = new LiteralBooleanBinding(
				computeSpatialRelation(AviationConfiguration.getInstance().getAnalysisProvider(), feature1, feature2)); 
		result.put(OUTPUT_RESULT_IDENTIFIER, output);
		return result;
	}
	
	/**
	 * @param provider the analysis provider for convenience
	 * @param feature1 the first feature
	 * @param feature2 the second feature
	 * @return the implementing spatial relation result
	 */
	protected abstract boolean computeSpatialRelation(SpatialAnalysisProvider provider,
			IData feature1, IData feature2);

	@Override
	public boolean processDescriptionIsValid() {
		ProcessDescriptionType desc = getDescription();
		
		//TODO actually validate the contents
		return desc.validate();
	}

	@Override
	public Class<?> getInputDataType(String id) {
		return GTVectorDataBinding.class;
	
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		return LiteralBooleanBinding.class;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(INPUT_FIRST_FEATURE_IDENTIFIER);
		identifierList.add(INPUT_SECOND_FEATURE_IDENTIFIER);
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(OUTPUT_RESULT_IDENTIFIER);
		return identifierList;
	}

}

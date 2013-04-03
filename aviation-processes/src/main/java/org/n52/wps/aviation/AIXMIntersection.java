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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.ags.AGSProperties;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

@AlgorithmPublicName({"AIXMIntersection"})
public class AIXMIntersection extends AbstractSelfDescribingAlgorithm {

	private static final String INPUT_FEATURE_IDENTIFIER = "Feature";
	private static final String OUTPUT_RESULT_IDENTIFIER = "Result";

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		AGSProperties.getInstance().bootstrapArcobjectsJar();
		
		Map<String,IData> result = new HashMap<String, IData>();
		List<IData> data = inputData.get(INPUT_FEATURE_IDENTIFIER);
		
		IData[] dataArray = new IData[data.size()];
		for (int i = 0; i < dataArray.length; i++) {
			dataArray[i] = data.get(i);
		}
		
		IData output = AviationConfiguration.getInstance().getAnalysisProvider().intersection(dataArray);
		result.put(OUTPUT_RESULT_IDENTIFIER, output);
		return result;
	}

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
		return GTVectorDataBinding.class;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(INPUT_FEATURE_IDENTIFIER);
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(OUTPUT_RESULT_IDENTIFIER);
		return identifierList;
	}

}

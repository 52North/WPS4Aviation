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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AviationProcessRepository implements IAlgorithmRepository {

	private static final Logger logger = LoggerFactory.getLogger(AviationProcessRepository.class);
	private Map<String,Class<?>> algorithmNames;
	private Map<String, ProcessDescriptionType> processDescriptions;

	public AviationProcessRepository() {
		this.algorithmNames = new HashMap<String, Class<?>>();
		this.processDescriptions = new HashMap<String, ProcessDescriptionType>();

		Set<String> algos = AviationConfiguration.getInstance().getAlgorithms();

		try {
			for (String a : algos) {
				Class<?> clazz = Class.forName(a, false, getClass().getClassLoader());
				AlgorithmPublicName publicName = clazz.getAnnotation(AlgorithmPublicName.class);
				this.algorithmNames.put(publicName != null ? publicName.value()[0] : a, clazz);	
			}
		} catch (ClassNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}

	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return this.algorithmNames.keySet();
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		if (!algorithmNames.keySet().contains(processID)) {
			return null;
		}

		IAlgorithm result = null;
		try {
			result = (IAlgorithm) algorithmNames.get(processID).newInstance();
		} catch (InstantiationException e) {
			logger.warn(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage(), e);
		}

		return result;
	}


	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		if (!algorithmNames.keySet().contains(processID)) {
			return null;
		}

		return resolveProcessDescription(processID);
	}

	private synchronized ProcessDescriptionType resolveProcessDescription(String processID) {
		if (!this.processDescriptions.keySet().contains(processID)) {
			Class<?> clazz = algorithmNames.get(processID);
			XmlObject xo = null;
			try {
				xo = XmlObject.Factory.parse(clazz.getResource(clazz.getSimpleName()+".xml"));
			} catch (XmlException e) {
				logger.warn(e.getMessage(), e);
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
			if (xo != null && xo instanceof ProcessDescriptionsDocument) {
				this.processDescriptions.put(processID, ((ProcessDescriptionsDocument) xo).getProcessDescriptions().getProcessDescriptionArray(0));
			} else {
				this.processDescriptions.put(processID, null);
			}
		}

		return this.processDescriptions.get(processID);
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return algorithmNames.containsKey(processID);
	}

	@Override
	public void shutdown() {

	}


}

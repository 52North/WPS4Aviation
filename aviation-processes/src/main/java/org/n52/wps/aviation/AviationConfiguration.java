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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Configuration for the Aviation processes.
 * 
 * @author matthes rieke
 *
 */
public class AviationConfiguration extends Properties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(AviationConfiguration.class);
	private static final String ANALYSIS_PROVIDER = "SpatialAnalysisProviderClassName";
	private static final Object ALGORITHM_KEY = "Algorithm";
	private static AviationConfiguration _instance;

	public static synchronized AviationConfiguration getInstance() {
		if (_instance == null) {
			_instance = new AviationConfiguration();
		}
		return _instance;
	}

	private Set<String> activeProperties;
	private SpatialAnalysisProvider analysisProvider;
	private Set<String> algorithms;
	
	private AviationConfiguration() {
		this.activeProperties = new HashSet<String>();
		this.algorithms = new HashSet<String>();
		
		for (Property prop : WPSConfig.getInstance().getPropertiesForRepositoryClass(AviationProcessRepository.class.getName())) {
			if (prop.getActive() && prop.getName().equals(ALGORITHM_KEY)) {
				this.algorithms.add(prop.getStringValue());
			}
			else {
				this.setProperty(prop.getName(), prop.getStringValue());
				if (prop.getActive()) this.activeProperties.add(prop.getName());				
			}
			
		}
		
		initializeComponents();
	}
	
	private void initializeComponents() {
		this.analysisProvider = instantiateAnalysisProvider();
	}

	private SpatialAnalysisProvider instantiateAnalysisProvider() {
		String clazzName = getProperty(ANALYSIS_PROVIDER);
		try {
			Class<?> clazz = Class.forName(clazzName);
			if (SpatialAnalysisProvider.class.isAssignableFrom(clazz)) {
				return (SpatialAnalysisProvider) clazz.newInstance();
			}
		} catch (ClassNotFoundException e) {
			logger.warn(e.getMessage(), e);
		} catch (InstantiationException e) {
			logger.warn(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage(), e);
		}
		return null;
	}

	public boolean isPropertyActive(String name) {
		return this.activeProperties.contains(name);
	}

	public SpatialAnalysisProvider getAnalysisProvider() {
		return analysisProvider;
	}

	public void setAnalysisProvider(SpatialAnalysisProvider analysisProvider) {
		this.analysisProvider = analysisProvider;
	}

	public Set<String> getAlgorithms() {
		return algorithms;
	}

	
	
}

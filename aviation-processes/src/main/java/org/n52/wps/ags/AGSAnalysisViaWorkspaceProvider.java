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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.n52.wps.ags.workspace.AGSWorkspace;
import org.n52.wps.ags.workspace.feature.SpatialRelation;
import org.n52.wps.aviation.SpatialAnalysisProvider;
import org.n52.wps.aviation.UnitOfMeasurementTools;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.ShapefileBinding;
import org.n52.wps.io.datahandler.generator.GTBinZippedSHPGenerator;


/**
 * This makes use of the AGSProcessRepository and its registered processes.
 * Note that process descriptions must be available in the workspace directory
 * as defined by the {@link AGSProcessRepository}.
 * 
 * @author matthes rieke
 *
 */
public class AGSAnalysisViaWorkspaceProvider implements SpatialAnalysisProvider {

	private static final String INPUT_SEPARATOR = " ; ";
	private static final String INTERSECTION = "Intersect_analysis";
	private static final String BUFFER = "Buffer_analysis";
	private AGSWorkspace workspace;
	private static final Logger logger = Logger.getLogger(AGSAnalysisViaWorkspaceProvider.class);

	public AGSAnalysisViaWorkspaceProvider() {
		/*
		 * ensure that arcobjects.jar is available
		 */
		AGSProperties.getInstance().bootstrapArcobjectsJar();
		try {
			this.workspace = new AGSWorkspace(new File(AGSProperties.getInstance().getWorkspaceBase()));
		} catch (NoClassDefFoundError e) {
		}
	}


	public AGSWorkspace getWorkspace() {
		return workspace;
	}

	@Override
	public IData intersection(IData... features) {
		try {
			List<File> shpDirs = createShapefileDirectories(features);
			List<File> shapeFiles = resolveShapeFilesFromDirectories(shpDirs);
			String inputParameters = prepareFeaturesAsInputParameters(shapeFiles);
			String outputShape = createOutputShapefileName(this.workspace.getWorkspace());
			return parseGeoProcessorResult(INTERSECTION, outputShape, new String[] {inputParameters, outputShape});
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing intersection.", e);
		}
	}

	@Override
	public IData buffer(IData feature, double bufferDistance, String distanceUom) {
		try {
			List<File> shpDirs = createShapefileDirectories(feature);
			List<File> shapeFiles = resolveShapeFilesFromDirectories(shpDirs);
			String inputParameters = prepareFeaturesAsInputParameters(shapeFiles);
			String outputShape = createOutputShapefileName(this.workspace.getWorkspace());
			IData result = parseGeoProcessorResult(BUFFER, outputShape, new String[] {inputParameters, outputShape, createLinearUnitString(bufferDistance, distanceUom)});
			deleteFilesAndParentDirectories(shapeFiles);
			return result;
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing buffer.", e);
		}
	}

	private String createLinearUnitString(double value,	String uom) {
		StringBuilder sb = new StringBuilder();
		sb.append(value);
		sb.append(" ");
		sb.append(UnitOfMeasurementTools.ucumToAGSLinearUnitString(uom));
		return sb.toString();
	}

	@Override
	public boolean intersects(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.INTERSECTS, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing intersects.", e);
		}
	}


	@Override
	public boolean touches(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.TOUCHES, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing touches.", e);
		}
	}

	@Override
	public boolean contains(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.CONTAINS, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing contains.", e);
		}
	}

	@Override
	public boolean covers(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.COVERS, feature1, feature2);

		} catch (IOException e) {
			throw new IllegalStateException("Error while executing covers.", e);
		}
	}


	@Override
	public boolean crosses(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.CROSSES, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing crosses.", e);
		}
	}

	@Override
	public boolean disjoint(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.DISJOINT, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing disjoint.", e);
		}
	}

	@Override
	public boolean equals(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.EQUALS, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing equals.", e);
		}
	}

	@Override
	public boolean overlaps(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.OVERLAPS, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing overlaps.", e);
		}
	}

	@Override
	public boolean within(IData feature1, IData feature2) {
		try {
			return this.evaluateSpatialRelation(SpatialRelation.WITHIN, feature1, feature2);
		} catch (IOException e) {
			throw new IllegalStateException("Error while executing within.", e);
		}
	}
	
	private boolean evaluateSpatialRelation(SpatialRelation relation,
			IData feature1, IData feature2) throws IOException {
		checkWorkspaceState();
		
		List<File> shpFiles = createShapefilesForFeatures(feature1, feature2);
		boolean result = this.workspace.evaluateSpatialRelation(relation, shpFiles);
		deleteFilesAndParentDirectories(shpFiles);
		return result;
	}

	private void checkWorkspaceState() {
		if (this.workspace == null)
			throw new IllegalStateException("Workspace is null!");
		
		try {
			if (!this.workspace.isReady())
					throw new IllegalStateException("Workspace is not properly initialized!");
		} catch (IOException e) {
			throw new IllegalStateException("Workspace is not properly initialized!", e);
		}
	}

	private void deleteFilesAndParentDirectories(List<File> shpFiles) {
		for (File file : shpFiles) {
			if (file != null && file.exists()) {
				deleteFileAndAssociates(file, false);
				File parent = file.getAbsoluteFile().getParentFile();
				if (parent != null && parent.isDirectory()) {
					parent.delete();
				}
			}
		}
	}

	private void deleteFileAndAssociates(File file, boolean onExit) {
			if (file != null) {
				final String baseName = file.getName().substring(0,
						file.getName().lastIndexOf("."));
				File[] list = file.getAbsoluteFile().getParentFile().listFiles(
						new FileFilter() {
							@Override
							public boolean accept(File pathname) {
								return pathname.getName().startsWith(baseName);
							}
						});
				for (File f : list) {
					if (onExit) f.deleteOnExit();
					else f.delete();
				}
		}		
	}

	private List<File> createShapefilesForFeatures(IData feature1, IData feature2) throws IOException {
		List<File> shpDirs = createShapefileDirectories(new IData[] {feature1, feature2});
		List<File> shapeFiles = resolveShapeFilesFromDirectories(shpDirs);
		return shapeFiles;
	}

	private IData parseGeoProcessorResult(String toolName, String outputShape, String... inputParameters) throws IOException {
		checkWorkspaceState();
		
		this.workspace.executeGPTool(toolName, null, inputParameters);
		return convertShapefileToData(outputShape);
	}


	private IData convertShapefileToData(String outputShape) throws FileNotFoundException {
		File resultShape = new File(outputShape);
		if (!resultShape.exists()) {
			throw new IllegalStateException("Internal Geoprocessing failed with no result.");
		}
		ShapefileBinding binding = new ShapefileBinding(resultShape);
		GTVectorDataBinding result = binding.getPayloadAsGTVectorDataBinding();
		deleteFileAndAssociates(resultShape, true);
		return result;
	}

	private String createOutputShapefileName(File parent) {
		File output = new File(parent, removeDashesFromUUID(UUID.randomUUID()) +".shp");
		return output.getAbsolutePath();
	}

	private String prepareFeaturesAsInputParameters(List<File> shapeFiles) {
		StringBuilder sb = new StringBuilder();
		for (File file : shapeFiles) {
			sb.append(file.getAbsolutePath());
			sb.append(INPUT_SEPARATOR);
		}
		sb.delete(sb.length() - INPUT_SEPARATOR.length(), sb.length());
		return sb.toString();
	}

	private List<File> resolveShapeFilesFromDirectories(List<File> shpDirs) {
		FileFilter shpFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				String extension = name.substring(name.lastIndexOf("."), name.length());
				return extension.equalsIgnoreCase(".shp");
			}
		};
		ArrayList<File> result = new ArrayList<File>();
		for (File dir : shpDirs) {
			result.addAll(Arrays.asList(dir.listFiles(shpFilter)));
		}
		return result;
	}

	private List<File> createShapefileDirectories(IData... feature) throws IOException {
		ArrayList<File> result = new ArrayList<File>();

		for (IData data : feature) {
			result.add(saveToUniqueTempDirectory(data));
		}

		return result;
	}

	private String removeDashesFromUUID(UUID randomUUID) {
		return randomUUID.toString().replace("-", "");
	}

	private File saveToUniqueTempDirectory(IData data) throws IOException {
		/*
		 * TODO Consider revision of GTBinDirectorySHPGenerator to get rid
		 * of performance-killing zip/unzip combination. Currently, it locks files
		 * so that AGS cannot access them.
		 */
		
		GTBinZippedSHPGenerator gen = new GTBinZippedSHPGenerator();
		File zipped = IOUtils.writeStreamToFile(gen.generateStream(data, null, null), "zip");
		File targetDir = new File(this.workspace.getWorkspace(), UUID.randomUUID().toString());
		targetDir.mkdir();
		IOUtils.unzip(zipped, "zip", targetDir);
		zipped.delete();
		return targetDir;
	}

	@Override
	public boolean isReady() {
		try {
			checkWorkspaceState();
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public void shutdown() {
		AGSWorkspace.shutdown();
	}

}

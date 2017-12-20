/*
 * Copyright 2017 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.masi.mdm.imagetransformer;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.impl.util.DataOrganizationTreeBuilder;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.util.Constants;
import edu.kit.masi.mdm.imagetransformer.model.ImageResizerProperties;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor for resizing all images with a specific extension.
 *
 * @author hartmann-v
 */
public class ImageResizerProcessor extends AbstractStagingProcessor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageResizerProcessor.class);
  /**
   * Maximal width of the image(s).
   */
  private static final String WIDTH = "Width";
  /**
   * Maximal height of the image(s).
   */
  private static final String HEIGHT = "Height";
  /**
   * Viewnames of the images.
   */
  private static final String VIEWNAME = "Viewname";
  /**
   * Possible extensions of the images.
   */
  private static final String EXTENSIONS = "File extensions";
  /**
   * Output format of the created images.
   */
  private static final String OUTPUT_FORMAT = "Output format";
  /**
   * Description for all properties.
   */
  private static final String DESCRIPTION_EXTENSION = "\n"
          + "For multiple images make a list of comma separated values. The number of values has to be identical in "
          + "all fields (" + WIDTH + "/" + HEIGHT + "/" + VIEWNAME + ").";
  /**
   * Description for property 'Width'.
   */
  private static final String WIDTH_DESCRIPTION = "Max. value for the width of the image or '-1' if not limited." + DESCRIPTION_EXTENSION;
  /**
   * Description for property 'Height'.
   */
  private static final String HEIGHT_DESCRIPTION = "Max. value for the height of the image or '-1' if not limited." + DESCRIPTION_EXTENSION;
  /**
   * Description for property 'Viewname'.
   */
  private static final String VIEWNAME_DESCRIPTION = "Viewname of the corresponding image." + DESCRIPTION_EXTENSION;
  /**
   * Description for property 'Viewname'.
   */
  private static final String EXTENSIONS_DESCRIPTION = "Comma separated list of all possible images. (e.g.: jpg, png, gif)";
  /**
   * Description for property 'Viewname'.
   */
  private static final String OUTPUT_FORMAT_DESCRIPTION = "Output format of the scaled images. Only one of PNG,JPG,GIF is allowed.";
  /**
   * Placeholder for values which are not set.
   */
  private static final int NOT_SPECIFIED = -1;
  /**
   * Map holding all properties.
   */
  private final Map<String, String> propertyMap = new HashMap();
  /**
   * Set holding all resize properties.
   */
  private Set<ImageResizerProperties> resizeImageProperties = new HashSet<>();
  /**
   * Set of all possible extensions.
   */
  private Set<String> imageExtensions = new HashSet<>();
  /**
   * output format of the created images.
   */
  private static String outputFormat;

  /**
   * Default constructor
   *
   * @param pUniqueIdentifier The unique identifier of this OP. This identifier
   * should be used to name generated output files associated with this OP.
   */
  public ImageResizerProcessor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
    // Initializing property map.
    propertyMap.put(WIDTH, WIDTH_DESCRIPTION);
    propertyMap.put(HEIGHT, HEIGHT_DESCRIPTION);
    propertyMap.put(VIEWNAME, VIEWNAME_DESCRIPTION);
    propertyMap.put(EXTENSIONS, EXTENSIONS_DESCRIPTION);
    propertyMap.put(OUTPUT_FORMAT, OUTPUT_FORMAT_DESCRIPTION);
  }

  @Override
  public String getName() {
    return "ImageResizerProcessor";
  }

  @Override
  public void performPreTransferProcessing(TransferTaskContainer ttc) throws StagingProcessorException {
    LOGGER.debug("Start performPreTransferProcessing...");
  }

  @Override
  public void finalizePreTransferProcessing(TransferTaskContainer ttc) throws StagingProcessorException {
    LOGGER.debug("Start finalizePreTransferProcessing...");
  }

  @Override
  public void performPostTransferProcessing(TransferTaskContainer ttc) throws StagingProcessorException {
    LOGGER.debug("Start performPostTransferProcessing...");
    IFileTree fileTree = ttc.getFileTree();
    ICollectionNode rootNode = fileTree.getRootNode();
    IDataOrganizationNode dataNode = Util.getNodeByName(rootNode, Constants.STAGING_DATA_FOLDER_NAME);
    Map<String, DataOrganizationTreeBuilder> fileTreeView = new HashMap();
    for (ImageResizerProperties views : resizeImageProperties) {
      DataOrganizationTreeBuilder treeBuilder = new DataOrganizationTreeBuilder().
              create(fileTree.getDigitalObjectId(), views.getViewName());
      fileTreeView.put(views.getViewName(), treeBuilder);
    }
    StringBuilder patternBuilder = new StringBuilder(".*(");
    for (String extension : imageExtensions) {
      patternBuilder.append(extension).append("|");
      LOGGER.debug("Search for extension: '{}'", extension);
    }
    patternBuilder.deleteCharAt(patternBuilder.length() - 1);
    patternBuilder.append(")");
    Set<IDataOrganizationNode> nodesByRegex = Util.getNodesByRegex((ICollectionNode) dataNode, patternBuilder.toString());
    for (IDataOrganizationNode node : nodesByRegex) {
      LOGGER.debug("Found node: '{}'", node.getName());
      for (ImageResizerProperties item : resizeImageProperties) {
        File theFile;
        try {
          theFile = new File(new URL(((IFileNode) node).getLogicalFileName().asString()).toURI());
          if (theFile.exists()) {
            // do resizing
            ImagePlus ip = new ImagePlus(theFile.getAbsolutePath());
            int width = ip.getWidth();
            int height = ip.getHeight();
            String filename = theFile.getName();
            LOGGER.debug("Filename: '{} ('{}'x'{}')", filename, width, height);
            String[] split = filename.split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int index2 = 0; index2 < split.length - 1; index2++) {
              sb.append(split[index2]).append(".");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("_").append(item.getViewName()).append(".");
            sb.append(outputFormat);
            double relWidth = (double) item.getMaxWidth() / (double) width;
            double relHeight = (double) item.getMaxHeight() / (double) height;
            double scaling = Double.min(relWidth, relHeight);
            int newWidth = (int) ((double) width * scaling);
            int newHeight = (int) ((double) height * scaling);
            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("Max values: '{}'x'{}'", item.getMaxWidth(), item.getMaxHeight());
              LOGGER.trace("Resize '{}' to '{}'x'{}' -> '{}'", filename, newWidth, newHeight, sb.toString());
            }
            ImageProcessor imgProc = ip.getProcessor().resize(newWidth, newHeight);
            File outputFile = new File(theFile.getParent() + File.separator + sb.toString());
            LOGGER.debug("Output file: '{}'", outputFile.getAbsolutePath());
            // Write image
            ImageIO.write(imgProc.getBufferedImage(), outputFormat, outputFile);
            // Write dataOrganization
            fileTreeView.get(item.getViewName()).addFile(new LFNImpl(outputFile.toURI().toURL()), outputFile.getName());
          }
        } catch (MalformedURLException | URISyntaxException ex) {
          LOGGER.error("Error resolving URL!", ex);
        } catch (IOException ex) {
          LOGGER.error("Error writing scaled image!", ex);
        }
      }
    }

    DataOrganizer dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();

    for (DataOrganizationTreeBuilder builder : fileTreeView.values()) {
      FileTreeImpl buildTree = builder.leaveCollection().buildTree();
      try {
        dataOrganizer.createFileTree(buildTree);
      } catch (EntityExistsException ex) {
        LOGGER.error("View '" + buildTree.getViewName() + "' already exists for DigitalObjectId '" + buildTree.getDigitalObjectId().getStringRepresentation() + "'!", ex);
      }
    }
  }

  @Override
  public void finalizePostTransferProcessing(TransferTaskContainer ttc) throws StagingProcessorException {
    LOGGER.debug("Start finalizePostTransferProcessing...");
  }

  @Override
  public String[] getInternalPropertyKeys() {
    return propertyMap.keySet().toArray(new String[propertyMap.size()]);
  }

  @Override
  public String getInternalPropertyDescription(String string) {
    return propertyMap.get(string);
  }

  @Override
  public String[] getUserPropertyKeys() {
    LOGGER.trace("getUserPropertyKeys - No user properties defined!");
    return null;
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    LOGGER.trace("getUserPropertyDescription - Nothing to do");
    return null;
  }

  @Override
  public void validateProperties(Properties prprts) throws PropertyValidationException {
    // Test for array length
    String regex = ",";
    String[] widthArray = prprts.getProperty(WIDTH).split(regex);
    String[] heightArray = prprts.getProperty(HEIGHT).split(regex);
    String[] viewnameArray = prprts.getProperty(VIEWNAME).split(regex);

    HashSet<String> viewnameSet = new HashSet();

    int numberOfImages = widthArray.length;
    LOGGER.debug("Width: '{}', Height: '{}', View: '{}'", prprts.getProperty(WIDTH), prprts.getProperty(HEIGHT), prprts.getProperty(VIEWNAME));
    if ((numberOfImages != heightArray.length) || (numberOfImages != viewnameArray.length)) {
      for (int index = 0; index < numberOfImages; index++) {
        LOGGER.error(widthArray[index] + "/" + heightArray[index] + "/" + viewnameArray[index]);
      }
      throw new PropertyValidationException("The number of properties should be identical in the fields(" + WIDTH + "/" + HEIGHT + "/" + VIEWNAME + ")");
    }
    for (int index1 = 0; index1 < numberOfImages; index1++) {
      int width = Integer.parseInt(widthArray[index1].trim());
      int height = Integer.parseInt(heightArray[index1].trim());
      if ((width == NOT_SPECIFIED) && (height == NOT_SPECIFIED)) {
        throw new PropertyValidationException("Image #" + (index1 + 1) + ": At least width or height has to be different of '" + NOT_SPECIFIED + "'");
      }
      if (width == NOT_SPECIFIED) {
        width = Integer.MAX_VALUE;
      }
      if (height == NOT_SPECIFIED) {
        height = Integer.MAX_VALUE;
      }
      if ((width <= 0) || (height <= 0)) {
        throw new PropertyValidationException("Image #" + (index1 + 1) + ": Width and height has to be greater than '0'! Found: " + width + "/" + height + "!");
      }
      viewnameSet.add(viewnameArray[index1].trim());
    }
    // Test for unique viewnames
    if (viewnameSet.contains("default")) {
      throw new PropertyValidationException("Viewname 'default' is not allowed!");
    }
    if (viewnameSet.size() < numberOfImages) {
      throw new PropertyValidationException("There is at least one doubled viewname!");
    }
    // Test for valid output format
    String outputFormat = prprts.getProperty(OUTPUT_FORMAT);
    switch (outputFormat) {
      case "PNG":
      case "JPG":
      case "GIF":
        break;
      default:
        throw new PropertyValidationException("'" + outputFormat + "' is not a valid output format!");
    }

  }

  @Override
  public void configure(Properties prprts) throws PropertyValidationException, ConfigurationException {
    validateProperties(prprts);

    resizeImageProperties.clear();
    imageExtensions.clear();
    String regex = ",";
    // Test for array length
    String[] widthArray = prprts.getProperty(WIDTH).split(regex);
    String[] heightArray = prprts.getProperty(HEIGHT).split(regex);
    String[] viewnameArray = prprts.getProperty(VIEWNAME).split(regex);
    String[] extensionArray = prprts.getProperty(EXTENSIONS).split(regex);

    int numberOfImages = widthArray.length;
    for (int index1 = 0; index1 < numberOfImages; index1++) {
      int width = Integer.parseInt(widthArray[index1].trim());
      int height = Integer.parseInt(heightArray[index1].trim());
      ImageResizerProperties irp = new ImageResizerProperties(width, height, viewnameArray[index1].trim());
      resizeImageProperties.add(irp);
    }
    for (String extension : extensionArray) {
      imageExtensions.add(extension.trim());
    }
    outputFormat = prprts.getProperty(OUTPUT_FORMAT).trim();
  }

}

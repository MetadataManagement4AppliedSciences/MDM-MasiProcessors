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
package edu.kit.dama.mdm.content.mets.tikaextractor;

import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.content.mets.MetsMetadataExtractor;
import edu.kit.dama.mdm.content.mets.tikaextractor.model.TikaMetadata;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.fzk.tools.xml.JaxenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Processor for extracting metadata from any files using Apache Tika.
 *
 * @author hartmann-v
 */
public class TikaExtractorProcessor extends MetsMetadataExtractor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TikaExtractorProcessor.class);

  /**
   * Default constructor
   *
   * @param pUniqueIdentifier The unique identifier of this OP. This identifier
   * should be used to name generated output files associated with this OP.
   */
  public TikaExtractorProcessor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
    // Initializing property map.
  }

  @Override
  public String getName() {
    return "TikaExtractorProcessor";
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
  protected String[] getExtractorPropertyKeys() {
    LOGGER.trace("getExtractorPropertyKeys - Nothing to do");
    return null;
  }

  @Override
  protected String getExtractorPropertyDescription(String pProperty) {
    LOGGER.trace("getExtractorPropertyDescription - Nothing to do");
    return null;
  }

  @Override
  protected void validateExtractorProperties(Properties pProperties) throws PropertyValidationException {
    LOGGER.trace("validateExtractorProperties - Nothing to do");
  }

  @Override
  protected void configureExtractor(Properties pProperties) {
    LOGGER.trace("configureExtractor - Nothing to do");
  }

  @Override
  protected Document createCommunitySpecificDocument(TransferTaskContainer pContainer) throws MetaDataExtractionException {
    LOGGER.debug("Start createCommunitySpecificDocument...");
    IFileTree fileTree = pContainer.getFileTree();
    ICollectionNode rootNode = fileTree.getRootNode();
    IDataOrganizationNode dataNode = Util.getNodeByName(rootNode, Constants.STAGING_DATA_FOLDER_NAME);
    // Find all files!
    StringBuilder patternBuilder = new StringBuilder(".*");
    Set<IDataOrganizationNode> nodesByRegex = Util.getNodesByRegex((ICollectionNode) dataNode, patternBuilder.toString());
    TikaMetadata tm = new TikaMetadata(true);
    Parser parser = new AutoDetectParser();
    BodyContentHandler handler = new BodyContentHandler();
    ParseContext context = new ParseContext();
    for (IDataOrganizationNode node : nodesByRegex) {
      try {
        if (node instanceof CollectionNodeImpl){
          LOGGER.trace("Skip found directory '{}'", ((CollectionNodeImpl)node).getLogicalFileName().asString());
          continue;
        }
        File theFile = new File(new URL(((IFileNode) node).getLogicalFileName().asString()).toURI());

        LOGGER.trace("Parse file '{}' with tika!", theFile.getAbsolutePath());
        //Parser method parameters
        Metadata metadata = new Metadata();
        FileInputStream inputstream;
        inputstream = new FileInputStream(theFile);

        parser.parse(inputstream, handler, metadata, context);
        if (LOGGER.isTraceEnabled()) {
          for (String names: metadata.names()) {
            LOGGER.trace("Found: {} = '{}'", names, metadata.get(names));
          }
        }
        tm.addMetadata(metadata);

      } catch (FileNotFoundException ex) {
        LOGGER.error(null, ex);
      } catch (IOException | SAXException | TikaException ex) {
        LOGGER.error(null, ex);

      } catch (URISyntaxException ex) {
        LOGGER.error(null, ex);
      } catch (Throwable tw) {
        LOGGER.error("Unknown error while parsing file with TIKA!", tw);
      }
    }
    Document w3CDocument = null;
    try {
      w3CDocument = JaxenUtil.getW3CDocument(IOUtils.toInputStream(tm.metadataToXml()));
    } catch (Exception ex) {
      LOGGER.error("Error creating XML", ex);
    }

    return w3CDocument;
  }

}

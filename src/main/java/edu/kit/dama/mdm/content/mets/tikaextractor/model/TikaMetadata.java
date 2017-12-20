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
package edu.kit.dama.mdm.content.mets.tikaextractor.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance holding all extracted metadata of a digital object.
 *
 * @author hartmann-v
 */
public class TikaMetadata {

  /**
   * Logger for all logging messages.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(TikaMetadata.class);
  /**
   * Namespace of the XML and Prefix for all subordinate namespaces.
   */
  private static final String NAMESPACE_PREFIX = "http://datamanager.kit.edu/masi/tika/1.0/";
  /**
   * Element name of the root node.
   */
  private static final String ROOT_NODE = "metadata";
  /**
   * Element name of the file node.
   */
  private static final String FILE_NODE = "file";
  /**
   * Map containing all namespaces.
   */
  Map<String, String> namespaces = new HashMap<>();
  /**
   * Set containing metadata of all files.
   */
  Set<Metadata> metadata = new HashSet<>();
  
  boolean prettyPrint = false;
  /** Constructor for pretty printing output.
   * For debugging only.
   * @param prettyPrint Print with indent true/false.
   */
  public TikaMetadata(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }
  
  

  /**
   * Add namespace for given prefix if not already registered.
   *
   * @param pPrefix prefix of namespace.
   */
  public void addNamespace(String pPrefix) {
    // check if already registered.
    if (!namespaces.containsKey(pPrefix)) {
      LOGGER.debug("Add namespace for prefix '{}'", pPrefix);
      namespaces.put(pPrefix, NAMESPACE_PREFIX + pPrefix);
    }
  }

  /**
   * Add metadata of one file to instance.
   *
   * @param pMetadata instance holding metadata of one file.
   */
  public void addMetadata(Metadata pMetadata) {
    metadata.add(pMetadata);
    //getting the list of all meta data elements 
    String[] metadataNames = pMetadata.names();
    for (String names : metadataNames) {
      String[] split = names.split(":");
      if (split.length == 2) {
        addNamespace(split[0]);
      }
    }
  }

  /**
   * Print metadata from all files as XML
   *
   * @return XML representation of metadata.
   */
  public String metadataToXml() {
    StringBuilder sb = new StringBuilder();
    sb.append("<metadata xmlns=\"").append(NAMESPACE_PREFIX);
      sb.append("\" ");
    // Add all namespaces. 
    for (String prefix : namespaces.keySet()) {
      sb.append("xmlns:").append(prefix);
      sb.append("=\"").append(namespaces.get(prefix));
      sb.append("\" ");
    }
    sb.append(">");

    for (Metadata fileMetadata : metadata) {
      addMetadataFromFile(sb, fileMetadata);
    }
    addStopElement(sb, ROOT_NODE);
    return sb.toString();
  }

  /**
   * Add metadata from single file to string builder.
   *
   * @param pStringBuilder string builder.
   * @param pMetadata instance holding metadata of file.
   */
  private void addMetadataFromFile(StringBuilder pStringBuilder, Metadata pMetadata) {
    addStartElement(pStringBuilder, FILE_NODE);
    for (String elements : pMetadata.names()) {
      addElement(pStringBuilder, elements, pMetadata.get(elements));
    }
    addStopElement(pStringBuilder, FILE_NODE);

  }

  /**
   * Append element to StringBuilder.
   *
   * @param pStringBuilder string builder
   * @param pKey element name.
   * @param pValue value of the element.
   */
  private void addElement(StringBuilder pStringBuilder, String pKey, String pValue) {
    addStartElement(pStringBuilder, pKey);
    pStringBuilder.append(pValue);
    addStopElement(pStringBuilder, pKey);
  }

  /**
   * Append start element to StringBuilder.
   *
   * @param pStringBuilder string builder
   * @param pKey element name.
   */
  private void addStartElement(StringBuilder pStringBuilder, String pKey) {
    pStringBuilder.append("<").append(normalizeElementName(pKey)).append(">");
  }

  /**
   * Append stop element to StringBuilder.
   *
   * @param pStringBuilder string builder
   * @param pKey element name.
   */
  private void addStopElement(StringBuilder pStringBuilder, String pKey) {
    pStringBuilder.append("</").append(normalizeElementName(pKey)).append(">");
    if (prettyPrint) {
      pStringBuilder.append("\n");
    }
  }

  /**
   * Normalize element name. Remove leading and trailing spaces and replace all
   * spaces in between by '_'.
   *
   * @param pElementName name of the element.
   * @return normalized element name.
   */
  private String normalizeElementName(String pElementName) {
    String trim = pElementName.trim();
    String replace = trim.replace(' ', '_').replace('/', '_');
    return replace;
  }
}

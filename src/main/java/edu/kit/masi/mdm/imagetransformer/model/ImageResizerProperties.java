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
package edu.kit.masi.mdm.imagetransformer.model;

/**
 * Class holding all properties for image resizing.
 *
 * @author hartmann-v
 */
public class ImageResizerProperties {

  /**
   * max width.
   */
  private int maxWidth;
  /**
   * max height.
   */
  private int maxHeight;
  /**
   * viewname.
   */
  private String viewName;

  /**
   * Constructor setting all properties.
   *
   * @param maxWidth maximal width of image.
   * @param maxHeight maximal height of image.
   * @param viewName viewname of image.
   */
  public ImageResizerProperties(int maxWidth, int maxHeight, String viewName) {
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.viewName = viewName;
    if (this.maxHeight < 1) {
      this.maxHeight = Integer.MAX_VALUE;
    }
    if (this.maxWidth < 1) {
      this.maxWidth = Integer.MAX_VALUE;
    }
  }

  /**
   * Get max width.
   *
   * @return the maxWidth
   */
  public int getMaxWidth() {
    return maxWidth;
  }

  /**
   * Get max height.
   *
   * @return the maxHeight
   */
  public int getMaxHeight() {
    return maxHeight;
  }

  /**
   * Get viewname.
   *
   * @return the viewName
   */
  public String getViewName() {
    return viewName;
  }

}

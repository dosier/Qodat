/*
 * Copyright (C) 2015 Square, Inc.
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
package stan.qodat.scene.control.export.gif.encoder;

import java.util.Set;

public interface Ditherer {
  /**
   * Dither the given image, producing a new image which only contains colors from the given color
   * set.
   *
   * @param image the original, unquantized image
   * @param newColors the quantized set of colors to be used in the new image
   * @param transColor color that is transparent and shouldn't be dithered with
   * @return a new image containing only of colors from {@code newColors}
   */
  Image dither(Image image, Set<Color> newColors, Color transColor);
}

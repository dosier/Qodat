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

import java.util.*;

/**
 * Implements median cut quantization.
 *
 * <p>The algorithm works as follows:
 *
 * <ul>
 *   <li>Begin with one cluster containing all the original colors.</li>
 *   <li>Find the cluster containing the greatest spread along a single color component (red, green
 *   or blue).</li>
 *   <li>Find the median of that color component among colors in the cluster.</li>
 *   <li>Split the cluster into two halves, using that median as a threshold.</li>
 *   <li>Repeat this process until the desired number of clusters is reached.</li>
 * </ul>
 */
public final class MedianCutQuantizer implements ColorQuantizer {
    public static final MedianCutQuantizer INSTANCE = new MedianCutQuantizer();

    private MedianCutQuantizer() {
    }

    @Override
    public Set<Color> quantize(Multiset<Color> originalColors, int maxColorCount) {
        TreeSet<Cluster> clusters = new TreeSet<>(new ClusterSpreadComparator());
        clusters.add(new Cluster(originalColors));

        while (clusters.size() < maxColorCount) {
            Cluster clusterWithLargestSpread = clusters.pollFirst();
            clusters.addAll(clusterWithLargestSpread.split());
        }

        Set<Color> clusterCentroids = new HashSet<>();
        for (Cluster cluster : clusters) {
            clusterCentroids.add(Color.getCentroid(cluster.colors));
        }
        return clusterCentroids;
    }

    private static final class Cluster {
        final Multiset<Color> colors;
        double largestSpread;
        int componentWithLargestSpread;

        Cluster(Multiset<Color> colors) {
            this.colors = colors;
            this.largestSpread = -1;
            for (int component = 0; component < 3; ++component) {
                double componentSpread = getComponentSpread(component);
                if (componentSpread > largestSpread) {
                    largestSpread = componentSpread;
                    componentWithLargestSpread = component;
                }
            }
        }

        double getComponentSpread(int component) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (Color color : colors) {
                min = Math.min(min, color.getComponent(component));
                max = Math.max(max, color.getComponent(component));
            }
            return max - min;
        }

        Collection<Cluster> split() {
            List<Color> orderedColors = new ArrayList<>(colors);
            orderedColors.sort(new ColorComponentComparator(componentWithLargestSpread));
            int medianIndex = orderedColors.size() / 2;
            return Arrays.asList(
                    new Cluster(new HashMultiset<>(orderedColors.subList(0, medianIndex))),
                    new Cluster(
                            new HashMultiset<>(orderedColors.subList(medianIndex, orderedColors.size()))));
        }
    }

    /**
     * Orders clusters according to their maximum spread, in descending order.
     */
    static final class ClusterSpreadComparator implements Comparator<Cluster> {
        @Override
        public int compare(Cluster a, Cluster b) {
            double spreadDifference = b.largestSpread - a.largestSpread;
            if (spreadDifference == 0) {
                return ArbitraryComparator.INSTANCE.compare(a, b);
            }
            return (int) Math.signum(spreadDifference);
        }
    }

    /**
     * Orders colors according to the value of one particular component, in ascending order.
     */
    static final class ColorComponentComparator implements Comparator<Color> {
        final int component;

        ColorComponentComparator(int component) {
            this.component = component;
        }

        @Override
        public int compare(Color a, Color b) {
            double componentDifference = a.getComponent(component) - b.getComponent(component);
            if (componentDifference == 0) {
                return ArbitraryComparator.INSTANCE.compare(a, b);
            }
            return (int) Math.signum(componentDifference);
        }
    }
}

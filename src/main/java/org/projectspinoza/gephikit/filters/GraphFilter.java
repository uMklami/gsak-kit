package org.projectspinoza.gephikit.filters;
import java.util.Arrays;
import java.util.Comparator;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.attribute.AttributeRangeBuilder;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.openide.util.Lookup;
public class GraphFilter {
	public GraphView filterBasedOnRange(AttributeModel attributeModel, String coloumn, double range) {

		AttributeColumn pagerankCol = attributeModel.getNodeTable().getColumn(coloumn);
		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

		AttributeRangeBuilder.AttributeRangeFilter attributeRangeFilter = 
				new AttributeRangeBuilder.AttributeRangeFilter(pagerankCol);
		Query pagerankQuery = filterController.createQuery(attributeRangeFilter);

		// Set the filters parameters - Keep nodes above 0.010
		attributeRangeFilter.setRange(new Range(range, Double.MAX_VALUE));

		// Execute the filter query
		return filterController.filter(pagerankQuery);
	}

	public GraphView degreerangefilter(Graph graph) {
		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

		DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
		degreeFilter.init(graph);
		degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));
		// Remove nodes with degree < 30
		Query query = filterController.createQuery(degreeFilter);
		return filterController.filter(query);
	}

	public Graph removePercentageNodes(Graph graph,	AttributeColumn column, double threshhold, String columnValue) {
		int nodestoberemoved = 0;
		Node[] nodesN = sortgraphBasedonColoumnValue(graph, column);

		nodestoberemoved = (int) Math.ceil(nodesN.length * threshhold);
		for (int i = 0; i < nodestoberemoved; i++) {
			graph.removeNode(nodesN[i]);
		}
		return graph;
	}

	@SuppressWarnings("rawtypes")
	public void rankSize(String column, AttributeModel attributeModel, RankingController rankingController) {
		AttributeColumn Column = attributeModel.getNodeTable().getColumn(column);

		Ranking PageRankRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Column.getId());

		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController
				.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(1);
		sizeTransformer.setMaxSize(10);
		rankingController.transform(PageRankRanking, sizeTransformer);
	}

	public Node[] sortgraphBasedonColoumnValue(Graph graph,	final AttributeColumn column) {
		Node[] nodesN = graph.getNodes().toArray();
		if (column != null) {
			Arrays.sort(nodesN, new Comparator<Node>() {
				public int compare(Node o1, Node o2) {
					Number n1 = (Number) o1.getAttributes().getValue(column.getIndex());
					Number n2 = (Number) o2.getAttributes().getValue(column.getIndex());
					if (n1.doubleValue() < n2.doubleValue()) {
						return -1;
					} else if (n1.doubleValue() > n2.doubleValue()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
		}
		return nodesN;
	}
}

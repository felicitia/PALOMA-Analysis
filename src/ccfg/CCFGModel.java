package ccfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import util.FileUtil;

public class CCFGModel {

	static Graph<String, DefaultWeightedEdge> weightedCCFG = null;
	final static String logDir = "/Users/felicitia/Documents/Research/PALOMA/FSE2018/App10(Mcdelivery)/CallbackLogs";
	private static PrintWriter pwNodes = null;
	private static PrintWriter pwEdges = null;
	private static final String CSV_SEPARATOR = "#";

	public static void main(String[] args) {
		graph2CSV();
	}

	public static void graph2CSV() {
		Graph<String, DefaultWeightedEdge> weightedCCFG = getWeightedCCFG();
		try {
			pwNodes = new PrintWriter("nodes.csv");
			pwEdges = new PrintWriter("edges.csv");
			pwNodes.println("nid:ID" + CSV_SEPARATOR + "callbackType");
			pwEdges.println(":START_ID" + CSV_SEPARATOR + ":END_ID"
					+ CSV_SEPARATOR + ":TYPE");

			// print nodes
			for (String node : weightedCCFG.vertexSet()) {
				String sig = node.substring(node.indexOf("<") + 1,
						node.lastIndexOf(">"));
				String type = node.substring(node.indexOf("#") + 1,
						node.lastIndexOf("#"));
				pwNodes.print(sig + CSV_SEPARATOR + type);
				pwNodes.println();
			}
			// print edges
			for (DefaultWeightedEdge edge : weightedCCFG.edgeSet()) {
				String srcNode = weightedCCFG.getEdgeSource(edge);
				String tgtNode = weightedCCFG.getEdgeTarget(edge);

				pwEdges.print(srcNode.substring(srcNode.indexOf("<") + 1,
						srcNode.lastIndexOf(">"))
						+ CSV_SEPARATOR
						+ tgtNode.substring(tgtNode.indexOf("<") + 1, tgtNode.lastIndexOf(">"))
						+ CSV_SEPARATOR + weightedCCFG.getEdgeWeight(edge));
				pwEdges.println();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pwNodes.close();
		pwEdges.close();
		System.out.println("graph to CSV files done :)");
	}

	public static Graph<String, DefaultWeightedEdge> getWeightedCCFG() {
		if (weightedCCFG == null) {
			weightedCCFG = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(
					DefaultWeightedEdge.class);
			File[] logFiles = FileUtil.getFilesWithExtension(logDir, ".log");
			BufferedReader br = null;
			FileReader fr = null;
			String lastNode = null;
			for (File logFile : logFiles) {
				try {
					fr = new FileReader(logFile.getAbsolutePath());
					br = new BufferedReader(fr);
					String sCurrentLine;
					while ((sCurrentLine = br.readLine()) != null) {
						if (sCurrentLine.contains("trigger")) {
							String currentNode = sCurrentLine.substring(
									sCurrentLine.indexOf("<"),
									sCurrentLine.length());
							weightedCCFG.addVertex(currentNode);
							if (lastNode != null
									&& !lastNode.equals(currentNode)) {
								if (weightedCCFG.containsEdge(lastNode,
										currentNode)) {
									// update weight
									DefaultWeightedEdge edge = weightedCCFG
											.getEdge(lastNode, currentNode);
									int currentWeight = (int) weightedCCFG
											.getEdgeWeight(edge);
									weightedCCFG.setEdgeWeight(edge,
											currentWeight + 1);
								} else {
									// add a new edge with weight 1
									DefaultWeightedEdge edge = weightedCCFG
											.addEdge(lastNode, currentNode);
									weightedCCFG.setEdgeWeight(edge, 1);
								}
							}
							lastNode = currentNode;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						if (br != null)
							br.close();
						if (fr != null)
							fr.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		return weightedCCFG;
	}
}

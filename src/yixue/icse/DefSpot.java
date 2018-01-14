package yixue.icse;

public class DefSpot {

	String jimple;
	String nodeId;
	String pkgName;
	String body; //the method that contains the def spot, not the method in statistic.txt!
	int subStrPos; // the position of the unknown substring in the URLMap (pkgname.json), count from 0
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getJimple() {
		return jimple;
	}
	public void setJimple(String jimple) {
		this.jimple = jimple;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getPkgName() {
		return pkgName;
	}
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	public int getSubStrPos() {
		return subStrPos;
	}
	public void setSubStrPos(int subStrPos) {
		this.subStrPos = subStrPos;
	}
	
}

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FordFulkerson;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class BaseballElimination {
	private final int n;
	private final Map<String,Integer> map = new HashMap<>();
	private final int[] w,l,r;
	private final int[][] g;
	private int maxWin;
	private FordFulkerson ff;
	private FlowNetwork fn;
	
	public BaseballElimination(String filename) {                    // create a baseball division from given filename in format specified below
		In input = new In(filename);
		n = input.readInt();
		w = new int[n];
		l = new int[n];
		r = new int[n];
		g = new int[n][n];
		
		for(int i=0;i<n;i++) {
			map.put(input.readString(), i);
			w[i] = input.readInt();
			maxWin = Math.max(maxWin, w[i]);
			l[i] = input.readInt();
			r[i] = input.readInt();
			for(int j=0;j<n;j++) {
				g[i][j] = input.readInt();
			}
		}
	}
		
	public int numberOfTeams() {                        // number of teams
		return n;
	}	
		
	public Iterable<String> teams(){                                // all teams
		return map.keySet();
	}
	
	public int wins(String team) {                      // number of wins for given team
		if(!map.containsKey(team)) throw new IllegalArgumentException();
		int index = map.get(team);
		return w[index];
	}
	
	public int losses(String team) {                    // number of losses for given team
		if(!map.containsKey(team)) throw new IllegalArgumentException();
		int index = map.get(team);
		return l[index];
	}
	
	public int remaining(String team) {                 // number of remaining games for given team
		if(!map.containsKey(team)) throw new IllegalArgumentException();
		
		int index = map.get(team);
		return r[index];
	}
	
	public int against(String team1, String team2) {    // number of remaining games between team1 and team2
		if(!map.containsKey(team1) || !map.containsKey(team2) ) throw new IllegalArgumentException();
		
		int i1 = map.get(team1);
		int i2 = map.get(team2);
		return g[i1][i2];
	}
	
	public boolean isEliminated(String team) {              // is given team eliminated?
		if(!map.containsKey(team)) throw new IllegalArgumentException();
		
		int id = map.get(team);
		if(trivialEliminated(id)) {
			return true;  // Trivial elimination
		}
		
		int total = n+(n-1)*(n-2)/2+1;  // total n
		int t = id, s = total-1;
		
		/* create flowNetwork */
		createFlowNetwork(id);
		/* finish creating flowNetwork */
		
		ff = new FordFulkerson(fn,s,t);
		
		int sum=0;
		for(int i=0;i<n;i++) {     // first team
			if(i==id) continue;
			for(int j=i+1;j<n;j++) {   // second team
				if(j==id) continue;
				sum += g[i][j];
			}
		}
		if(ff.value()<sum) return true;
		
		return false;
	}
	
	private boolean trivialEliminated(int id) {
		if(w[id]+r[id]<maxWin) return true;
		else return false;
	}
	
	private void createFlowNetwork(int id){
		int total = n+(n-1)*(n-2)/2+1;  // total number of vertices in graph
		int t = id, s = total-1;
		
		/* create flowNetwork */
		fn  = new FlowNetwork(total);
		int cur = n;
		for(int i=0;i<n;i++) {     // first team
			if(i==id) continue;
			for(int j=i+1;j<n;j++) {   // second team
				if(j==id) continue;
				fn.addEdge(new FlowEdge(s,cur,g[i][j]));
				fn.addEdge(new FlowEdge(cur,i,Integer.MAX_VALUE));
				fn.addEdge(new FlowEdge(cur,j,Integer.MAX_VALUE));
				cur++;
			}
		}
		for(int i=0;i<n;i++) {
			if(i==id) continue;
			fn.addEdge(new FlowEdge(i,t, w[id]+r[id]-w[i] ));
		}
		
	}
	
	public Iterable<String> certificateOfElimination(String team){  // subset R of teams that eliminates given team; null if not eliminated
		if(!map.containsKey(team)) throw new IllegalArgumentException();
		
		if(!isEliminated(team)) return null;    // not eliminated
			
		List<String> rlist = new ArrayList<>();
		int id = map.get(team);
		
		if(trivialEliminated(id)) {             // trivial eliminated
			for(String s : map.keySet()) {
				if(w[map.get(s)]==maxWin) {
					rlist.add(s);
					return rlist;
				}
			}
		}
		
		for(String str : map.keySet()) {
			if(str.equals(team)) continue;
			if(ff.inCut( map.get(str) )) {
				rlist.add(str);
			}
		}
		return rlist;		
	}
	
	public static void main(String[] args) {
	    BaseballElimination division = new BaseballElimination(args[0]);
	    for (String team : division.teams()) {
	        if (division.isEliminated(team)) {
	            StdOut.print(team + " is eliminated by the subset R = { ");
	            for (String t : division.certificateOfElimination(team)) {
	                StdOut.print(t + " ");
	            }
	            StdOut.println("}");
	        }
	        else {
	            StdOut.println(team + " is not eliminated");
	        }
	    }
	}
	
}

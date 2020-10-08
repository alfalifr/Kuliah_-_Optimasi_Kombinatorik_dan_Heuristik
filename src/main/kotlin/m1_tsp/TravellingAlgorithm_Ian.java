package travellingalgorithm;
import java.util.Scanner;

/**
 * Punya Aurelius Ian
 */
public class TravellingAlgorithm_Ian
{
    
    public static void main(String[] args) 
    {
     Scanner in = new Scanner(System.in);
        
        System.out.println("Berapa ukuran matriks yang diinginkan:");
        int n = in.nextInt();
       
        int[][] jarak = new int[n][n];
        //Populate array
        for(int i = 0; i<jarak.length; i++)
        {
            for(int j=0; j<jarak[i].length; j++)
            {
                //Kotanya sama jaraknya 0
                if(i==j)
                {
                    jarak[i][j] = 0;
                }
                //Jarak kota a ke b udah ada maka jarak b ke a sama
                else if(i>j)
                {
                    jarak[i][j] = jarak[j][i];
                }
                else
                {
                    jarak[i][j] = (int)Math.floor((Math.random()*10))+1;
                }
            }
        }
        System.out.println("");
        System.out.println("Informasi jarak antar kota:");
        for(int i = 0; i<jarak.length; i++)
        {
            for(int j=0; j<jarak[i].length; j++)
            {
                if( i != j)
                System.out.println("Jarak " + i + " ke " + j + " =" + jarak[i][j]);
            }
        }
        System.out.println("");
        System.out.println("Rute dan Jarak yang memungkinkkan:");
        // Boolean array to check if a node
        // has been visited or not
        boolean[] v = new boolean[n];

        // Mark 0th node as visited
        v[0] = true;
        int ans = Integer.MAX_VALUE;
        // Find the minimum weight Hamiltonian Cycle
        ans = tsp(jarak, v, 0, n, 1, 0, ans);

        // ans is the minimum weight Hamiltonian Cycle
        System.out.println("Jarak paling pendek:");
        System.out.println(ans);
    }

    // Function to find the minimum weight
    // Hamiltonian Cycle
    static int tsp(int[][] graph, boolean[] v,
                   int currPos, int n,
                   int count, int cost, int ans)
    {

        // If last node is reached and it has a link
        // to the starting node i.e the source then
        // keep the minimum value out of the total cost
        // of traversal and "ans"
        // Finally return to check for more possible values
        
        if (count == n && graph[currPos][0] > 0)
        {

            System.out.println("Positions: " + currPos +", Jarak: "+ cost);
            System.out.println("Positions: " + 0 +", Jarak: "+ (cost + graph[currPos][0]));
      
            ans = Math.min(ans, cost + graph[currPos][0]);
            System.out.println("");
            return ans;
        }

        // BACKTRACKING STEP
        // Loop to traverse the adjacency list
        // of currPos node and increasing the count
        // by 1 and cost by graph[currPos,i] value
        for (int i = 0; i < n; i++)
        {
            if (v[i] == false && graph[currPos][i] > 0)
            {

                // Mark as visited
                v[i] = true;
                System.out.println("Positions: " + currPos +", Jarak: "+cost);
                ans = tsp(graph, v, i, n, count + 1,
                        cost + graph[currPos][i], ans);

                // Mark ith node as unvisited
                v[i] = false;
            }
        }
        return ans;
    }

}
   

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class SHA256 {

	static byte[] getSHA(String input) throws NoSuchAlgorithmException {
		// Static getInstance method is called with hashing SHA
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		// digest() method called
		// to calculate message digest of an input
		// and return array of byte
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}

	static String toHexString(byte[] hash) {
		// Convert byte array into signum representation
		BigInteger number = new BigInteger(1, hash);
		// Convert message digest into hex value
		StringBuilder hexString = new StringBuilder(number.toString(16));
		// Pad with leading zeros
		while (hexString.length() < 32) {
			hexString.insert(0, '0');
		}
		return hexString.toString();
	}

}

class Tuple {

	Boolean is_Left;
	String Hash;

	Tuple(String S) {
		this.Hash = S;
		this.is_Left = null;
	}

	Tuple(String S, boolean b) {
		this.is_Left = b;
		this.Hash = S;
	}

}

class Block {

	String Merkle_Root;
	String Previous_Block_Hash;
	Block next;

	Block() {
		this.Merkle_Root = "eh27fd";
		this.Previous_Block_Hash = this.Merkle_Root;
		this.next = null;
	}

	Block(String Merkle_Root) {
		this.Merkle_Root = Merkle_Root;
		next = null;
	}

}

class Block_Chain {

	Block head = new Block();

	Block_Chain() {
		head.Merkle_Root = "eh27fd";
		head.Previous_Block_Hash = head.Merkle_Root;
		head.next = null;
	}

	void add_block(String Root) {
		Block node = new Block(Root);
		Block n = head;
		while (n.next != null) {
			n = n.next;
		}
		n.next = node;
		n.next.Previous_Block_Hash = n.Merkle_Root;
	}

	void Chain_Verify() {
		Block n = head;
		while (n != null) {
			if (n.next != null && !(n.Merkle_Root.equals(n.next.Previous_Block_Hash))) {
				n.next = null;
			}
			n = n.next;
		}
	}

	void print() {
		Block n = head;
		while (n != null) {
			System.out.println("Previous_Block_Hash : " + n.Previous_Block_Hash);
			System.out.println("Merkle_Root : " + n.Merkle_Root);
			if (n.next != null) {
				System.out.println("|");
				System.out.println("|");
			}
			n = n.next;
		}
	}

}

class Merkle_Node {

	Merkle_Node parent;
	boolean is_Left_of_Parent;
	boolean is_Right_of_Parent;
	Merkle_Node left;
	Merkle_Node right;
	boolean leaf;
	String H;

	Merkle_Node(String H) {
		this.H = H;
		this.left = null;
		this.right = null;
		this.leaf = true;
	}

	Merkle_Node(Merkle_Node O1, Merkle_Node O2) {
		O1.parent = this;
		O1.is_Left_of_Parent = true;
		O1.is_Right_of_Parent = false;
		this.left = O1;
		O2.parent = this;
		O2.is_Left_of_Parent = false;
		O2.is_Right_of_Parent = true;
		this.right = O2;
		this.leaf = false;
		try {
			this.H = SHA256.toHexString(SHA256.getSHA(O1.H + O2.H));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	Merkle_Node(Merkle_Node O1) {
		O1.parent = this;
		O1.is_Left_of_Parent = true;
		O1.is_Right_of_Parent = false;
		this.left = O1;
		this.right = new Merkle_Node(O1.H);
		this.right.is_Left_of_Parent = false;
		this.right.is_Right_of_Parent = true;
		this.right.parent = this;
		this.leaf = false;
		try {
			this.H = SHA256.toHexString(SHA256.getSHA(O1.H + O1.H));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}

class Merkle_Tree {

	Merkle_Node root;
	int Height, k = 0;

	Merkle_Tree(int n) {
		root = null;
		Height = n;
	}

	Merkle_Tree(List<String> L) {
		root = null;
		int Tree_Height = 0;
		for (int i = 0; i < L.size(); i++) {
			if (L.size() <= Math.pow(2, i)) {
				Tree_Height = i + 1;
				break;
			}
		}
		Height = Tree_Height;
	}

	List<Merkle_Node> Leaf_Nodes(List<String> L) {
		List<Merkle_Node> ML = new ArrayList<>();
		for (int i = 0; i < L.size(); i++) {
			ML.add(new Merkle_Node(L.get(i)));
		}
		return ML;
	}

	void Print_Leaf_Nodes(List<Merkle_Node> L) {
		for (int i = 0; i < L.size(); i++) {
			System.out.println(L.get(i));
		}
	}

	void Internal_Nodes(List<Merkle_Node> L) throws NoSuchAlgorithmException {
		List<Merkle_Node> temp = new ArrayList<>();
		k++;

		if (L.size() == 2 && k + 1 == Height) {
			root = new Merkle_Node(L.get(0), L.get(1));
		}

		else if (L.size() == 1 && k + 1 == Height) {
			root = new Merkle_Node(L.get(0));
		}

		else {
			for (int i = 0; i < L.size(); i = i + 2) {
				if (L.size() - i == 1) {
					Merkle_Node n = new Merkle_Node(L.get(i));
					temp.add(n);
					break;
				}
				Merkle_Node n = new Merkle_Node(L.get(i), L.get(i + 1));
				temp.add(n);
			}
			Internal_Nodes(temp);
		}
	}

	void InOrder(Merkle_Node root) {
		if (root == null)
			return;
		InOrder(root.left);
		System.out.println(root + "-->" + root.H + " ---> parent is : " + root.parent);
		InOrder(root.right);
	}

	List<Tuple> Hashes_For_Verification(Merkle_Node n, List<Tuple> trail) {
		if (n == this.root) {
			trail.add(new Tuple(n.H));
		}
		if (n.is_Left_of_Parent) {
			trail.add(new Tuple(n.parent.right.H, false));
			Hashes_For_Verification(n.parent, trail);
		} else if (n.is_Right_of_Parent) {
			trail.add(new Tuple(n.parent.left.H, true));
			Hashes_For_Verification(n.parent, trail);
		}
		return trail;
	}

}

class Transactions {
	String Sender_Name;
	String Reciever_Name;
	String Date;
	String Time;
	float BitCoins;
	String Transaction_ID;

	Transactions(String Sender_Name, String Reciever_Name, String Date, String Time, float BitCoins) {
		this.Sender_Name = Sender_Name;
		this.Reciever_Name = Reciever_Name;
		this.Date = Date;
		this.Time = Time;
		this.BitCoins = BitCoins;
		try {
			this.Transaction_ID = SHA256
					.toHexString(SHA256.getSHA(Sender_Name + Reciever_Name + Date + Time + BitCoins));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	static List<String> TX_ID(List<Transactions> L) {
		List<String> ID = new ArrayList<>();
		for (int i = 0; i < L.size(); i++) {
			ID.add(L.get(i).Transaction_ID);
			;
		}
		return ID;
	}

	static void print(List<Transactions> L) {
		System.out.println("Sender\tReciever\tDate\t        Time\tBitcoins\tTX_ID");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------");
		for (int i = 0; i < L.size(); i++) {
			System.out.println(L.get(i).Sender_Name + "\t" + L.get(i).Reciever_Name + "        \t" + L.get(i).Date
					+ "\t" + L.get(i).Time + "\t" + L.get(i).BitCoins + "\t" + L.get(i).Transaction_ID);
		}
	}

}

public class sem3project {
	public static void main(String args[]) throws NoSuchAlgorithmException {

		Scanner sc = new Scanner(System.in);

		// SERVER

		System.out.println("Transactions Present in SERVER : \n");

		// BLOCK1

		List<Transactions> BLOCK1 = new ArrayList<>();
		BLOCK1.add(new Transactions("Ashrith", "Satwik", "24/12/2019", "15:30", 2));
		BLOCK1.add(new Transactions("Satwik", "Ashrith", "24/12/2019", "9:30", 2));
		BLOCK1.add(new Transactions("Ashrith", "Vishnu", "24/12/2019", "18:30", 2));
		BLOCK1.add(new Transactions("Vishnu", "Satwik", "24/12/2019", "7:30", 2));
		List<String> List_of_TX_ID_in_Block1_at_Server = Transactions.TX_ID(BLOCK1);
		System.out.println("Transactions Present in BLOCK1 : ");
		Transactions.print(BLOCK1);

		// Merkle Tree For SERVER BLOCK1 TX's

		Merkle_Tree Merkle_Tree_of_Block1_at_Server = new Merkle_Tree(List_of_TX_ID_in_Block1_at_Server);
		List<Merkle_Node> Leaf_Nodes_of_Block1_at_Server = Merkle_Tree_of_Block1_at_Server
				.Leaf_Nodes(List_of_TX_ID_in_Block1_at_Server);
		Merkle_Tree_of_Block1_at_Server.Internal_Nodes(Leaf_Nodes_of_Block1_at_Server);
		System.out.println("\nServer Block1 Merkle Tree Leaf Nodes are : ");
		Merkle_Tree_of_Block1_at_Server.Print_Leaf_Nodes(Leaf_Nodes_of_Block1_at_Server);
		System.out.println("\nServer Block1 Merkle Tree Inorder is : ");
		Merkle_Tree_of_Block1_at_Server.InOrder(Merkle_Tree_of_Block1_at_Server.root);
		System.out.println("\nServer Block1 Merkle Tree Root is : " + Merkle_Tree_of_Block1_at_Server.root);

		// BLOCK2

		List<Transactions> BLOCK2 = new ArrayList<>();
		BLOCK2.add(new Transactions("Ashrith", "Satwik", "25/12/2019", "13:30", 2));
		BLOCK2.add(new Transactions("Satwik", "Ashrith", "25/12/2019", "9:30", 2));
		BLOCK2.add(new Transactions("Ashrith", "Vishnu", "25/12/2019", "18:30", 2));
		BLOCK2.add(new Transactions("Vishnu", "Satwik", "25/12/2019", "7:30", 2));
		List<String> List_of_TX_ID_in_Block2_at_Server = Transactions.TX_ID(BLOCK2);
		System.out.println("\nTransactions Present in BLOCK2 : ");
		Transactions.print(BLOCK2);

		// Merkle Tree For SERVER BLOCK2 TX's

		Merkle_Tree Merkle_Tree_of_Block2_at_Server = new Merkle_Tree(List_of_TX_ID_in_Block2_at_Server);
		List<Merkle_Node> Leaf_Nodes_of_Block2_at_Server = Merkle_Tree_of_Block2_at_Server
				.Leaf_Nodes(List_of_TX_ID_in_Block2_at_Server);
		Merkle_Tree_of_Block2_at_Server.Internal_Nodes(Leaf_Nodes_of_Block2_at_Server);
		System.out.println("\nServer Block2 Merkle Tree Leaf Nodes are : ");
		Merkle_Tree_of_Block2_at_Server.Print_Leaf_Nodes(Leaf_Nodes_of_Block2_at_Server);
		System.out.println("\nServer Block2 Merkle Tree Inorder is : ");
		Merkle_Tree_of_Block2_at_Server.InOrder(Merkle_Tree_of_Block2_at_Server.root);
		System.out.println("\nServer Block2 Merkle Tree Root is : " + Merkle_Tree_of_Block2_at_Server.root);

		// TREES

		List<Merkle_Tree> MT = new ArrayList<>();
		MT.add(Merkle_Tree_of_Block1_at_Server);
		MT.add(Merkle_Tree_of_Block2_at_Server);

		ArrayList<ArrayList<Merkle_Node>> All_Leaf_Nodes = new ArrayList<ArrayList<Merkle_Node>>();
		All_Leaf_Nodes.add((ArrayList<Merkle_Node>) Leaf_Nodes_of_Block1_at_Server);
		All_Leaf_Nodes.add((ArrayList<Merkle_Node>) Leaf_Nodes_of_Block2_at_Server);

		// SERVER BLOCK-CHAIN

		Block_Chain Server_Block_Chain = new Block_Chain();
		Server_Block_Chain.add_block(MT.get(0).root.H);
		Server_Block_Chain.add_block(MT.get(1).root.H);
		System.out.println("\nServer Block-Chain : ");
		Server_Block_Chain.print();

		// CLIENT

		System.out.println("\nCLIENT SIDE");

		// CLIENT BLOCK1

		List<Transactions> C_BLOCK1 = new ArrayList<>();
		C_BLOCK1.add(new Transactions("Ashrith", "Satwik", "24/12/2019", "13:30", 2));
		C_BLOCK1.add(new Transactions("Satwik", "Ashrith", "24/12/2019", "9:30", 2));
		C_BLOCK1.add(new Transactions("Ashrith", "Vishnu", "24/12/2019", "15:30", 2));
		C_BLOCK1.add(new Transactions("Ashrith", "Vishnu", "24/12/2019", "18:30", 2));
		List<String> List_of_TX_ID_in_Block1_at_Client = Transactions.TX_ID(C_BLOCK1);

		// CLIENT BLOCK2

		List<Transactions> C_BLOCK2 = new ArrayList<>();
		C_BLOCK2.add(new Transactions("Ashrith", "Satwik", "25/12/2019", "13:30", 2));
		C_BLOCK2.add(new Transactions("Satwik", "Ashrith", "25/12/2019", "9:30", 2));
		C_BLOCK2.add(new Transactions("Ashrith", "Vishnu", "25/12/2019", "17:30", 2));
		C_BLOCK2.add(new Transactions("Vishnu", "Satwik", "25/12/2019", "7:30", 2));
		List<String> List_of_TX_ID_in_Block2_at_Client = Transactions.TX_ID(C_BLOCK2);

		// All Transactions

		ArrayList<ArrayList<String>> All_Transactions = new ArrayList<ArrayList<String>>();
		All_Transactions.add((ArrayList<String>) List_of_TX_ID_in_Block1_at_Client);
		All_Transactions.add((ArrayList<String>) List_of_TX_ID_in_Block2_at_Client);

		// CLIENT BLOCK-CHAIN

		Block_Chain Client_Block_Chain = new Block_Chain();
		Client_Block_Chain = Server_Block_Chain;

		// Verfification

		do {
			System.out.println("\nEnter details of Transaction as in your block that you want to verify : ");
			System.out.print("Enter Sender Name : ");
			String S_N = sc.next();
			System.out.print("Enter Reciever Name : ");
			String R_N = sc.next();
			System.out.print("Enter Date of Transaction : ");
			String D = sc.next();
			System.out.print("Enter Time of Transaction in 24 hr format (xx:xx) : ");
			String T = sc.next();
			System.out.print("Enter BitCoins Transfered : ");
			int B = sc.nextInt();
			Transactions query = new Transactions(S_N, R_N, D, T, B);
			String Query_ID_Hash = query.Transaction_ID;
			boolean Query_ID_Availablility_In_Client_Block = false;
			int Block_no = 0, Pos_In_Block = 0;

			for (int i = 0; i < All_Transactions.size(); i++) {
				for (int j = 0; j < All_Transactions.get(i).size(); j++) {
					if (All_Transactions.get(i).get(j).equals(Query_ID_Hash)) {
						Block_no = i;
						Pos_In_Block = j;
						Query_ID_Availablility_In_Client_Block = true;
					}
				}
			}

			List<Tuple> Req_Info_For_Verification_From_Server = new ArrayList<>();

			if (Query_ID_Availablility_In_Client_Block) {
				Req_Info_For_Verification_From_Server = MT.get(Block_no).Hashes_For_Verification(
						All_Leaf_Nodes.get(Block_no).get(Pos_In_Block), Req_Info_For_Verification_From_Server);
				System.out.println("\nUse these Hashes and Information Returned From Server to Verify your Tx : ");
				for (int i = 0; i < Req_Info_For_Verification_From_Server.size(); i++) {
					System.out.println(Req_Info_For_Verification_From_Server.get(i).Hash + "--->"
							+ Req_Info_For_Verification_From_Server.get(i).is_Left);
				}

				for (int i = 0; i < Req_Info_For_Verification_From_Server.size() - 1; i++) {
					if (!Req_Info_For_Verification_From_Server.get(i).is_Left) {
						Query_ID_Hash = Query_ID_Hash + Req_Info_For_Verification_From_Server.get(i).Hash;
					} else {
						Query_ID_Hash = Req_Info_For_Verification_From_Server.get(i).Hash + Query_ID_Hash;
					}
					Query_ID_Hash = SHA256.toHexString(SHA256.getSHA(Query_ID_Hash));
				}

				if (Query_ID_Hash.equals((Req_Info_For_Verification_From_Server
						.get(Req_Info_For_Verification_From_Server.size() - 1)).Hash)) {
					System.out.println("\nNot Tampered");
					int i = 0;
					Block n = Client_Block_Chain.head.next;
					while (n != null) {
						if (i == Block_no) {
							n.Merkle_Root = Query_ID_Hash;
						}
						i++;
						n = n.next;
					}
					Client_Block_Chain.Chain_Verify();
					System.out.println("\nClient Block-Chain after updating and verify a particular transaction");
					Client_Block_Chain.print();
					break;
				} else {
					System.out.println("\nTampered");
					int i = 0;
					Block n = Client_Block_Chain.head.next;
					while (n != null) {
						if (i == Block_no) {
							n.Merkle_Root = Query_ID_Hash;
						}
						i++;
						n = n.next;
					}
					Client_Block_Chain.Chain_Verify();
					System.out.println("\nClient Block-Chain after updating and verify a particular transaction");
					Client_Block_Chain.print();
					break;
				}

			} else {
				System.out.println("\nEnter the Data from your Database to Verify with the Server for In-Consistency");
			}
		} while (true);

		sc.close();

	}
}
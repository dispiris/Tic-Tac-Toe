import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.Timer;

public class Board {
	
	public static final int GRID_SIZE = 100;
	public static final boolean debug = false;
	
	private Mark[][] board;
	private JFrame frame;
	private JButton[][] buttons;
	private boolean nextMoveLegal;
	private int stepCount;
	private boolean gameover;
	private final int THINKING_DEPTH;
	private int bestX;
	private int bestY;
	
	public Board(int level, boolean humanFirst) {
		board = new Mark[3][3];
		nextMoveLegal = true;
		THINKING_DEPTH = level;
		frame = new JFrame("Tic-Tac-Toe");
		frame.setLayout(new GridLayout(3, 3));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		buttons = new JButton[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				JButton button = new JButton();
				button.setPreferredSize(new Dimension(GRID_SIZE, GRID_SIZE));
				button.setFont(new Font("Arials", Font.BOLD, 100));
				int i2 = i;
				int j2 = j;
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (nextMoveLegal && validPosition(i2, j2)) {
							set(i2, j2, Mark.x);
							nextMoveLegal = false;
							if (!gameover) {
								Timer timer = new Timer(500, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										setNext();
										nextMoveLegal = true;
									}
								});
								timer.setRepeats(false);
								timer.start();
							} 
						} 
					}
				});
				buttons[i][j] = button;
				frame.add(button);
			}
		}
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
		if (!humanFirst) {
			setNext();
		}
	}
	
	public void set(int x, int y, Mark m) {
		if (!validPosition(x, y)) {
			System.out.println("invalid position");
		} else {
			board[x][y] = m;
			buttons[x][y].setText("" + m);
			stepCount++;
			checkGameStatus();
		}
	}
	
	private void checkGameStatus() {
		if (stepCount == 9) {
			System.out.println("DRAW");
			for (JButton[] line : buttons) {
				for (JButton but : line) {
					but.setEnabled(false);
				}
			}
			gameover = true;
		} else {
			Mark winner = winner(this.board);
			if (winner != null) {
				System.out.println("winner is " + winner);
				for (JButton[] line : buttons) {
					for (JButton but : line) {
						but.setEnabled(false);
					}
				}
				gameover = true;
			} 
			gameover = false;
		}
	}

	private boolean validPosition(int x, int y) {
		return board[x][y] == null;
	}
	
	private static Mark winner(Mark[][] board) {
		for (int i = 0; i < 3; i++) {
			Mark mark = threeConsecutive(board[i][0], board[i][1], board[i][2]);
			if (mark != null) {
				return mark;
			}
		}
		for (int i = 0; i < 3; i++) {
			Mark mark = threeConsecutive(board[0][i], board[1][i], board[2][i]);
			if (mark != null) {
				return mark;
			}
		}
		Mark mark = threeConsecutive(board[0][0], board[1][1], board[2][2]);
		if (mark != null) 	return mark;
		mark = threeConsecutive(board[0][2], board[1][1], board[2][0]);
		if (mark != null) 	return mark;
		
		for (Mark[] marks : board) {
			for (Mark mark1 : marks) {
				if (mark1 == null) {
					return null;
				}
			}
		}
		
		return Mark.draw;
	}
	
	// returns the winner mark. 
	private static Mark threeConsecutive(Mark a, Mark b, Mark c) {
		if (a == null || b == null || c == null) {
			return null;
		} 
		if (a == b && b == c) {
			return a;
		}
		return null;
	}
	
	private static int evaluate(Mark[][] board) {
		int total = 0;
		total += evaluateThree(board[0][0], board[0][1], board[0][2]);
		total += evaluateThree(board[1][0], board[1][1], board[1][2]);
		total += evaluateThree(board[2][0], board[2][1], board[2][2]);
		total += evaluateThree(board[0][0], board[1][0], board[2][0]);
		total += evaluateThree(board[0][1], board[1][1], board[2][1]);
		total += evaluateThree(board[0][2], board[1][2], board[2][2]);
		total += evaluateThree(board[0][0], board[1][1], board[2][2]);
		total += evaluateThree(board[0][2], board[1][1], board[2][0]);
		return total;
	}
	
	private int minimax(Mark[][] board, int depth, int original, int alpha, int beta, boolean maxPlayer) {
		if (depth == 0 || winner(board) != null) {
			int eval = evaluate(board);
			if (debug)	System.out.println("this is a leaf and eval = " + eval);
			return eval;
		}
		
		if (maxPlayer) {
			int maxEval = -10000000;
			outerloop:
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (board[i][j] == null) {
						if (debug)	System.out.println("maxplayer/o at depth "+depth+" at position (" + i + ", " + j + "); ");
						board[i][j] = Mark.o;
						int eval = minimax(board, depth - 1, original, alpha, beta, false);
						board[i][j] = null;
						if (eval > maxEval) {
							maxEval = eval;
							if (debug)	System.out.println("! maxEval updated to " + maxEval);
							if (depth == original) {
								bestX = i;
								bestY = j;
								if (debug)	System.out.println("!!!best x & y updated to: " + bestX + " " + bestY);
							}
						}
						
						alpha = Math.max(alpha, eval);
						if (beta <= alpha) {
							if (debug)	System.out.println("break the loop");
							break outerloop;
						} 
					} 
				}
			}
			return maxEval;
		} else {
			int minEval = 10000000;
			outerloop:
			for (int i = 0; i < 3; i++) {	
				for (int j = 0; j < 3; j++) {
					if (board[i][j] == null) {
						if (debug)	System.out.println("minplayer/x at depth "+depth+" at position (" + i + ", " + j + "); ");
						board[i][j] = Mark.x;
						int eval = minimax(board, depth - 1, original, alpha, beta, true);
						board[i][j] = null;
						if (eval < minEval) {
							minEval = eval;
							if (debug)	System.out.println("! minEval updated to " + minEval);
						}
						beta = Math.min(beta, eval);
						if (beta <= alpha) {
							if (debug)	System.out.println("break the loop");
							break outerloop;
						} 
					} 
				}
			}
			return minEval;
		}
	}
	
	private void setNext() {
		if (debug) {
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println("	current eval: " + evaluate(board));
			System.out.println("minimax eval: "
					+ minimax(board, THINKING_DEPTH, THINKING_DEPTH, -100000000, 100000000, true));
		} else {
			minimax(board, THINKING_DEPTH, THINKING_DEPTH, -100000000, 100000000, true);
		}
		set(bestX, bestY, Mark.o);
	}
	
	private static int evaluateThree(Mark a, Mark b, Mark c) {
		int xCount = 0;
		int oCount = 0;
		Mark[] three = {a, b, c};
		for (Mark m : three) {
			if (m == Mark.o)	oCount++;
			else if (m == Mark.x)	xCount++;
		}
		
		if (oCount == 0 && xCount == 0) {
			return 0;
		} else if (oCount > 0 && xCount > 0) {
			return 0;
		} else if (oCount == 3) {
			return 100_000;
		} else if (oCount == 2) {
			return 10_000;
		} else if (oCount == 1) {
//			return 1_000;
			return 0;
		} else if (xCount == 3) {
			return -100_000;
		} else if (xCount == 2) {
			return -10_000;
		} else {	// xCount == 1
			return 0;
		} 
	}
	
	public static void main(String[] args) {
//		JFrame frame1 = new JFrame("setup");
//		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame1.setLocationRelativeTo(null);
//		frame1.setLayout(new GridLayout(3,1));
//		JButton into = new JButton("who's first?");
//		into.setFont(new Font("Arial", Font.PLAIN, 100));
//		into.setEnabled(false);
//		frame1.add(into);
//		JButton me = new JButton("me");
//		me.setFont(new Font("Arial", Font.PLAIN, 100));
//		me.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				meFirst = true;
//			}
//		});
//		
		
		JFrame frame = new JFrame("choose difficulty");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new GridLayout(4,1));
		
		JButton b1 = new JButton("easy");
		b1.setFont(new Font("Arial", Font.PLAIN, 100));
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Board(1, false);
				frame.setVisible(false);
			}
		});
		
		JButton b2 = new JButton("normal");
		b2.setFont(new Font("Arial", Font.PLAIN, 100));
		b2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Board(2, false);
				frame.setVisible(false);
			}
		});
		
		JButton b3 = new JButton("difficult");
		b3.setFont(new Font("Arial", Font.PLAIN, 100));
		b3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Board(6, false);
				frame.setVisible(false);
			}
		});
		
		JButton b4 = new JButton("impossible");
		b4.setFont(new Font("Arial", Font.PLAIN, 100));
		b4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Board(9, false);
				frame.setVisible(false);
			}
		});
		
		frame.add(b1);
		frame.add(b2);
		frame.add(b3);
		frame.add(b4);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}

}

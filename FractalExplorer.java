/*
@author Alexander Joukov
*/
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

public class FractalExplorer extends JFrame {

	static final int WIDTH = 600;
	static final int HEIGHT = 600;
	double maxIter = 300.0;
	static final double DEFAULT_ZOOM = 100.0;
	static final double DEFAULT_TOP_LEFT_X = -3.0;
	static final double DEFAULT_TOP_LEFT_Y = 3.0;
	double zoomFactor = DEFAULT_ZOOM;
	double topLeftX = DEFAULT_TOP_LEFT_X;
	double topLeftY = DEFAULT_TOP_LEFT_Y;



	Canvas canvas;
	BufferedImage fractalImage;

	public FractalExplorer() {
		setInitialGUIProperties();
		addCanvas();
		canvas.addKeyStrokeEvents();
		updateFractal();
	}

	public void updateFractal() {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				double c_r = getXPos(x);
				double c_i = getYPos(y);

				int count = computeIterations(c_r, c_i);

				int pixelColor = makeColor(count);
				fractalImage.setRGB(x, y, pixelColor);

			}
		}
		canvas.repaint();
	}

	private int makeColor(int count) {
		/*
		int color = 0b011011100001100101101000;
		int mask = 0b000000000000010101110111;
		int shiftMag = count / 13;
		*/
		if (count == maxIter) {
			return 0;
		}
//		return color | (mask << shiftMag);
		double val = Math.toRadians(90.0 * count / maxIter);
		int r = 0;
		int g = 35 + (int)(220 * Math.sin(val));
		int b = 35 + (int)(220 * Math.sin(val*2));
		return (new Color(r,g,b).getRGB());
	}


	private double getXPos(double x) {
		return x/zoomFactor + topLeftX;
	}

	private double getYPos(double y) {
		return y/zoomFactor - topLeftY;
	}
	private int computeIterations(double c_r, double c_i) {
		//c = c_r + c_i
		//z = z_r + z_i

		//z' = z*z + c
		//   = (z_r + z_i)*(z_r + z_i) + c_r + c_i
		//   = (z_r*z_r) + 2z_r*z_i - z_i*z_i + c_r + c_i
		//z_i' = 2z_r*z_i + c_i
		//z_r' = (z_r*z_r) - z_i*z_i + c_r

		double z_r = 0.0;
		double z_i = 0.0;

		int count = 0;

		//distance = rad(a*a + b*b)
		//distance <= 2.0
		//distance^2 <= 4.0

		while (z_r*z_r + z_i*z_i <= 4.0) {
			double z_r_temp = z_r;
			z_r = (z_r*z_r) - z_i*z_i + c_r;
			z_i = 2*z_r_temp*z_i + c_i;

			//if true then pt was in set
			if (count >= maxIter) {
				return (int)maxIter;
			}
			count++;
		}
		return count;
	}

	public static void main(String[] args) {
		new FractalExplorer();
	}

	private void addCanvas() {
		canvas = new Canvas();
		fractalImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		canvas.setVisible(true);
		this.add(canvas, BorderLayout.CENTER);
	}

	public void setInitialGUIProperties() {
		setTitle("Fractal Explorer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WIDTH,HEIGHT);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void moveUp() {
		double curHeight = HEIGHT / zoomFactor;
		topLeftY += curHeight/8;
		updateFractal();
	}
	private void moveDown() {
		double curHeight = HEIGHT / zoomFactor;
		topLeftY -= curHeight/8;
		updateFractal();
	}
	private void moveLeft() {
		double curWidth = WIDTH / zoomFactor;
		topLeftX -= curWidth/8;
		updateFractal();
	}
	private void moveRight() {
		double curWidth = WIDTH / zoomFactor;
		topLeftX += curWidth/8;
		updateFractal();
	}

	private void adjustZoom(double X, double Y, double newZoomFactor) {
		topLeftX += X/zoomFactor;
		topLeftY -= Y/zoomFactor;

		zoomFactor = newZoomFactor;

		topLeftX -= (WIDTH/2)/zoomFactor;
		topLeftY += (HEIGHT/2)/zoomFactor;

		updateFractal();
	}

	private class Canvas extends JPanel implements MouseListener {
		public Canvas() {
			addMouseListener(this);
		}


		@Override public Dimension getPreferredSize() {
			return new Dimension(WIDTH, HEIGHT);
		}
		@Override public void paintComponent(Graphics g) {
			g.drawImage(fractalImage, 0, 0, null);
		}
		@Override public void mousePressed(MouseEvent mouse) {

			double x = (double) mouse.getX();
			double y = (double) mouse.getY();

			switch(mouse.getButton() ) {
				//left
			case (MouseEvent.BUTTON1):
				adjustZoom(x,y,zoomFactor*2);
				//maxIter*=1.33;
				break;
				//right
			case (MouseEvent.BUTTON3):
				adjustZoom(x,y,zoomFactor*0.5);
				//maxIter*=0.75;
				break;
			}

		}

		public void addKeyStrokeEvents() {
			KeyStroke wKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
			KeyStroke dKey = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
			KeyStroke sKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
			KeyStroke aKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);


			Action wPressed = new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) {
					moveUp();
				}
			};
			Action dPressed = new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) {
					moveRight();
				}
			};
			Action aPressed = new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) {
					moveLeft();
				}
			};
			Action sPressed = new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) {
					moveDown();
				}
			};

			this.getInputMap().put(wKey, "w_key");
			this.getInputMap().put(aKey, "a_key");
			this.getInputMap().put(sKey, "s_key");
			this.getInputMap().put(dKey, "d_key");

			this.getActionMap().put( "w_key", wPressed );
			this.getActionMap().put( "d_key", dPressed );
			this.getActionMap().put( "s_key", sPressed );
			this.getActionMap().put( "a_key", aPressed );

		}

		@Override public void mouseReleased(MouseEvent mouse) {}
		@Override public void mouseClicked(MouseEvent mouse) {}
		@Override public void mouseEntered(MouseEvent mouse) {}
		@Override public void mouseExited(MouseEvent mouse) {}
	}
}
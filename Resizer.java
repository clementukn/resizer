import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import java.awt.Color;


public class Resizer {
    private Picture picture;
	private int width;
	private int height;
    
    private static final boolean HORIZONTAL = true;
    private static final boolean VERTICAL = false;
	
	// create a resizer object based on the given picture
	public Resizer(Picture picture) {
		this.picture = picture;
		width = picture.width();
		height = picture.height();
	}
	
	// current picture
	public Picture picture() {
		return picture;
	}
	
	// width of current picture
	public     int width() {
		return width;
	}
	
	// height of current picture
	public     int height() {
		return height;
	}
	
	// compute x gradient
	private double xgradient(int x, int y) {
		Color cright = picture.get(x + 1, y);
		Color cleft = picture.get(x - 1, y);
		int dRed = cright.getRed() - cleft.getRed();
		int dGreen = cright.getGreen() - cleft.getGreen();
		int dBlue = cright.getBlue() - cleft.getBlue();
		return dRed * dRed + dGreen * dGreen + dBlue * dBlue;
	}
	
	// compute y gradient
	private double ygradient(int x, int y) {
		Color cright = picture.get(x, y + 1);
		Color cleft = picture.get(x, y - 1);
		int dRed = cright.getRed() - cleft.getRed();
		int dGreen = cright.getGreen() - cleft.getGreen();
		int dBlue = cright.getBlue() - cleft.getBlue();
		return dRed * dRed + dGreen * dGreen + dBlue * dBlue;
	}
	
	// energy of pixel at column x and row y
	// use the dual-gradient function
	public  double energy(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new java.lang.IndexOutOfBoundsException();
		if (x == 0 || x == width - 1) return 1000;
		if (y == 0 || y == height - 1) return 1000;
		return Math.sqrt(xgradient(x, y) + ygradient(x, y));
	}
	
    // compute the acyclic shortest path (using topological order)
    private void computeShortPath(double[][] energyTable, double[][] distTable, int[][] rootTable) {
        // set first line up
        for (int w = 0; w < width(); ++w) {
            distTable[0][w] = energyTable[0][w];
        }
        
        // /!\ WARNING no need to compute on the side but to check whether w is in bounds
        for (int h = 1; h < height(); ++h) {
            for (int w = 0; w < width(); ++w) {
                // by default set the distance to be from the top
                distTable[h][w] = distTable[h - 1][w] + energyTable[h][w];
                rootTable[h][w] = w;
                
                // up-left
                if ((w - 1 >= 0) && (distTable[h - 1][w - 1] + energyTable[h][w] < distTable[h][w])) {
                    distTable[h][w] = distTable[h - 1][w - 1] + energyTable[h][w];
                    rootTable[h][w] = w - 1;
                }
                
                // up-right
                if ((w + 1 < width()) && (distTable[h - 1][w + 1] + energyTable[h][w] < distTable[h][w])) {
                    distTable[h][w] = distTable[h - 1][w + 1] + energyTable[h][w];
                    rootTable[h][w] = w + 1;
                }
            }
        }
    }
    
    private void printTable(double[][] table) {
        for (int h = 0;  h < height(); h++) {
            for (int w = 0; w < width(); w++) {
                StdOut.printf("%7.2f ", table[h][w]);
            }
            StdOut.println();
        }
    }
    
    private void printTableInt(int[][] table) {
        for (int h = 0;  h < height(); h++) {
            for (int w = 0; w < width(); w++) {
                StdOut.printf("%d ", table[h][w]);
            }
            StdOut.println();
        }
    }
    
    // return true if the seam is valid, false otherwise
    private boolean checkSeamValidity(int[] seam, boolean direction) {
        int maxLength = direction == VERTICAL? height : width;
        int maxWidth = direction == VERTICAL? width : height;
        
        if (seam.length != maxLength)
            return false;
        
        int previous = seam[0];
        for (int elt : seam) {
            int diff = elt - previous;
            if (Math.abs(diff) > 1 || elt < 0 || elt >= maxWidth)
                return false;
            previous = elt;
        }
        return true;
    }
    
	// sequence of indices for vertical seam
	// first compute the energy matrix of the image and find the min path
	public   int[] findVerticalSeam() {
        double[][] energyTable = new double[height()][width()];
        for (int h = 0; h < height(); ++h) {
            for (int w = 0; w < width(); ++w) {
                energyTable[h][w] = energy(w, h);
            }
        }
		
        double[][] distTable = new double[height()][width()];
        int[][] rootTable = new int[height()][width()];
        
        computeShortPath(energyTable, distTable, rootTable);
        
        // search for the min distance
        double min = Double.MAX_VALUE;
        int lastRow = height() - 1;
        int indexMin = -1;
        for (int i = 1; i < width(); ++i) {
            if (distTable[lastRow][i] < min) {
                min = distTable[lastRow][i];
                indexMin = i;
            }
        }
        
        // once we get the min go backward to find entire path
        int[] verticalSeam = new int[height()];
        int currIndex = indexMin;
        for (int i = height() - 1; i >= 0; --i) {
            verticalSeam[i] = currIndex;
            currIndex = rootTable[i][currIndex];
        }
        return verticalSeam;
	}
    
	
    // transpose the picture
    private void transposePicture() {
        Picture transPicture = new Picture(picture.height(), picture.width());
        for (int x = 0; x < picture.width(); x++) {
            for (int y = 0; y < picture.height(); y++) {
                transPicture.set(y, x, picture.get(x, y));
            }
        }
        width = transPicture.width();
        height = transPicture.height();
        picture = transPicture;
    }
    
    // sequence of indices for horizontal seam
	public   int[] findHorizontalSeam() {
        transposePicture();
        int[] sol = findVerticalSeam();
        transposePicture();
        return sol;
    }
    
    // remove horizontal seam from current picture
	public    void removeVerticalSeam(int[] seam) {
        if (seam == null)
            throw new java.lang.NullPointerException();
        if (width <= 1 || !checkSeamValidity(seam, VERTICAL))
            throw new java.lang.IllegalArgumentException();
        
        Picture newPic = new Picture(width() - 1, height());
        for (int row = 0; row < height(); row++) {
            int newCol = 0;
            for (int col = 0; col < width(); col++) {
                if (col == seam[row])
                    continue;
                newPic.set(newCol++, row, picture.get(col, row));
            }
        }
        
        width = newPic.width();
        height = newPic.height();
        picture = newPic;
    }

    // remove vertical seam from current picture
	// actually transpose the picture and deal with it as if it were verticalSeam
	public    void removeHorizontalSeam(int[] seam) {
        if (seam == null)
            throw new java.lang.NullPointerException();
        if (height() <= 1 || !checkSeamValidity(seam, HORIZONTAL))
            throw new java.lang.IllegalArgumentException();
        transposePicture();
        removeVerticalSeam(seam);
        transposePicture();
    }
	
	public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.out.println("usage : java resize filename new-width new-height");
            System.exit(0);
        }

        String filename = args[0];
		Picture pic = new Picture(filename);
        Resizer sc = new Resizer(pic);
        int newWidth = Integer.parseInt(args[1]);
        int newHeight = Integer.parseInt(args[2]);

        if (newWidth > sc.width() || newHeight > sc.height()) {
            System.out.println("new size can't be larger.");
            System.exit(0);
        }

        int deltaWidth = sc.width() - newWidth;
        int deltaHeight = sc.height() - newHeight; 
        for (int w = 0; w < deltaWidth; w++) {
            sc.removeVerticalSeam(sc.findVerticalSeam());
        }
        for (int h = 0; h < deltaHeight; h++) {
            sc.removeHorizontalSeam(sc.findHorizontalSeam());
        }
        
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        //sc.picture().save("output" + suffix);

        sc.picture().show();
	}
}
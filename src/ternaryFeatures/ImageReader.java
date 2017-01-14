package ternaryFeatures;
import java.io.*;
import java.util.ArrayList;

public class ImageReader {
	
	/* # of rows and columns in a single image */
	int rows;
	int columns;
	
	/* Files as Data */
	ArrayList<char[][]> images;
	int [] labels;
	
	/* Constructor */
	public ImageReader(int numImages, String directory, String labelsFilename, String imagesFilename, int rows, int columns) throws FileNotFoundException, IOException{
		this.rows = rows;
		this.columns = columns;
		readTrainingLabels(numImages, directory, labelsFilename);
        readTrainingImages(numImages, directory, imagesFilename);
        //printImages(images);
	}
	
	/* Convert the file represented by "labelsFilename" into our "labels" variable */
	private void readTrainingLabels(int numImages, String directory, String labelsFilename) throws IOException{
		labels = new int[numImages];
		BufferedReader br = new BufferedReader(new FileReader(directory + "/" + labelsFilename));	//can throw FileNotFoundException
		String line;
		int counter = 0;
        while ((line = br.readLine()) != null) { //can throw IOException
        	int firstDigit = Character.getNumericValue(line.charAt(0));
        	labels[counter++] = firstDigit;
        }
        br.close();
	}
	
	/* Convert the file represented by "imagesFilename" into our "images" variable */
	private void readTrainingImages(int numImages, String directory, String imagesFilename) throws IOException{
		images = new ArrayList<char[][]>(numImages);
		BufferedReader br = new BufferedReader(new FileReader(directory + "/" + imagesFilename));	//can throw FileNotFoundException
        String line;
        int counter = 0;
        char[][] currentImage = new char[rows][columns];
		while ((line = br.readLine()) != null) { //can throw IOException
			int rowInImage = counter % rows;

			for (int j = 0; j < columns; j++){
        		currentImage[rowInImage][j] = line.charAt(j);
        	}
			if (rowInImage == rows - 1){
				images.add(currentImage);
				currentImage = new char[rows][columns];
			}
        	counter++;
        }
        br.close();
	}
	
	/* Used for debugging */
	private void printImages(ArrayList<char[][]> images){
        for (char[][] image : images){
        	for (int i = 0; i < rows; i++){
        		for (int j = 0; j < columns; j++){
        			System.out.print(image[i][j]);
        		}
        		System.out.println();
        	}
        }
	}
}

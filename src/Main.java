
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
	public static void main (String [] args) throws FileNotFoundException, IOException{
		/* Test on Digit Data */
		ImageReader trainingData = new ImageReader(5000, "digitdata", "traininglabels", "trainingimages", 28, 28);
		ImageReader testData = new ImageReader(1000, "digitdata", "testlabels", "testimages", 28, 28);
		MAP map = new MAP(trainingData.images, trainingData.labels, testData.images, testData.labels, trainingData.rows, trainingData.columns, 10);
		map.calculateStuff(trainingData.images, trainingData.labels, testData.images, testData.labels);
		map.printResults();
		
		/* Test on Face Data: This code currently doesn't work properly. */
//		ImageReader trainingData2 = new ImageReader(451, "facedata", "facedatatrainlabels", "facedatatrain", 70, 60);
//		ImageReader testData2 = new ImageReader(150, "facedata", "facedatatestlabels", "facedatatest", 70, 60);
//		MAP map2 = new MAP(trainingData2.images, trainingData2.labels, testData2.images, testData2.labels, trainingData2.rows, trainingData2.columns, 2);
//		map2.calculateStuff(trainingData2.images, trainingData2.labels, testData2.images, testData2.labels);
//		map2.printResults();
	}
}

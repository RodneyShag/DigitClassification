import java.util.ArrayList;

public class MAP {
	
	/* Final data */
	final int rows;
	final int columns;
	final int numClasses;
	
	/* Data */
	int numTrainingImages;
	int numTestImages;
	int[] numTrainingDigits;	//used in calculatelikelihoods()
	int[] numTestDigits;		//used in evaluate()

	
	/* Priors, Likelihoods, Posteriors */
	double[] priors;
	double[][][] likelihoods;
	double[][] posteriors;
	
	/* Other Stuff */
	double[][] confusionMatrix;
	
	/* Constructor */
	public MAP(ArrayList<char[][]> trainingImages, int [] trainingLabels, ArrayList<char[][]> testImages, int [] testLabels, int rows, int columns, int numClasses){
		/* Initialize Final Data */
		this.rows = rows;
		this.columns = columns;
		this.numClasses = numClasses;
		
		/* Initialize Data */
		numTrainingImages = trainingImages.size();
		numTestImages = testImages.size();
		numTrainingDigits = new int[numClasses];
		numTestDigits = new int[numClasses];
		countNumTestDigits(testLabels);
		countNumTrainingDigits(trainingLabels);
		
		/* Initialize Priors, Likelihoods, Posteriors */
		priors = new double[numClasses];
		likelihoods = new double[numClasses][rows][columns];
		posteriors = new double[numTestImages][numClasses];

		/* Initialize Other Stuff */
		confusionMatrix = new double[numClasses][numClasses];
	}
	
	public void calculateStuff(ArrayList<char[][]> trainingImages, int [] trainingLabels, ArrayList<char[][]> testImages, int [] testLabels){
        calculatePriors(trainingLabels);
	    calculateLikelihoods(trainingImages, trainingLabels, 1); // can try "k" values 1 to 50 using a loop
	    calculatePosteriors(testImages);
	    evaluate(testLabels);
	    minMaxPosteriors(testImages);
	    
	    /* Odds Ratios for Digit Data */
	    mapOddsRatio(4, 9); // 0.168 in confusion matrix
	    mapOddsRatio(8, 3); // 0.136 in confusion matrix
	    mapOddsRatio(7, 9); // 0.132 in confusion matrix
	    mapOddsRatio(5, 3); // 0.130 in confusion matrix
	    
	    /* Odds Ratios for Face Data */
//	    mapOddsRatio(0, 1);
//	    mapOddsRatio(1, 0);	    
	}
	
	private void calculatePriors(int [] trainingLabels){
		for (int i = 0; i < numTrainingImages; i++){
			priors[trainingLabels[i]]++;
		}
        int totalFrequency = 0;
        for (int i = 0; i < numClasses; i++){
        	totalFrequency += priors[i];
        }
        for (int i = 0; i < numClasses; i++){
        	priors[i] /= totalFrequency;
        }
	}
	
	private void calculateLikelihoods(ArrayList<char[][]> trainingImages, int [] trainingLabels, int k){
		for (int imageNum = 0; imageNum < numTrainingImages; imageNum++){
			int digit = trainingLabels[imageNum];
			char[][] currentImage = trainingImages.get(imageNum);
			for (int i = 0; i < rows; i++){
				for (int j = 0; j < columns; j++){
					if (currentImage[i][j] == '+' || currentImage[i][j] == '#'){
						likelihoods[digit][i][j]++;
					}
				}
			}
		}
		
		int V = 2;	// for Laplace Smoothing
		for (int digit = 0; digit < numClasses; digit++){
			for (int i = 0; i < rows; i++){
				for (int j = 0; j < columns; j++){
					likelihoods[digit][i][j] = (likelihoods[digit][i][j] + k) / (numTrainingDigits[digit] + k * V);
				}
			}
		}
	}
	
	private void calculatePosteriors(ArrayList<char[][]> testImages){
		double likelihood;
		for (int image = 0; image < numTestImages; image++){
			char[][] currentImage = testImages.get(image);
			for (int digit = 0; digit < numClasses; digit++){
				posteriors[image][digit] = Math.log(priors[digit]);
				for (int i = 0; i < rows; i++){
					for (int j = 0; j < columns; j++){
						if (currentImage[i][j] == '+' || currentImage[i][j] == '#')
							likelihood = likelihoods[digit][i][j];
						else
							likelihood = 1 - likelihoods[digit][i][j];
						posteriors[image][digit] += Math.log(likelihood);
					}
				}
			}
		}
	}
	
	/* Calculates and outputs "Classification Rates", "Confusion Matrix" */
	private void evaluate(int [] testLabels){
		/* Calculate "Classification Rates", and Confusion Matrix */
		double overallPrediction = 0;
		double[] digitPrediction = new double[numClasses];
		for (int image = 0; image < numTestImages; image++){
			int actualDigit = testLabels[image];
			int likelyDigit = max(posteriors[image]);
			confusionMatrix[actualDigit][likelyDigit]++;
			if (likelyDigit == actualDigit){
				overallPrediction++;
				digitPrediction[likelyDigit]++;
			}
		}
		
		/* Get Percentages from raw counts */
		overallPrediction /= numTestImages;
		for (int i = 0; i < numClasses; i++){
			digitPrediction[i] /= numTestDigits[i];
		}
		for (int i = 0; i < numClasses; i++){
			for (int j = 0; j < numClasses; j++){
				confusionMatrix[i][j] /= numTestDigits[i];
			}
		}
		
		/* Print info */
		printClassificationRate(overallPrediction, digitPrediction);
		printConfusionMatrix();
	}
	
	/* Calculates and outputs digits with highest/lowest posterior probabilities */
	private void minMaxPosteriors(ArrayList<char[][]> testImages){
		for (int classNum = 0; classNum < numClasses; classNum++){
			int minIndex = 0;
			int maxIndex = 0;
			double minPosterior = posteriors[0][classNum];
			double maxPosterior = posteriors[0][classNum];
			for (int imageNum = 1; imageNum < numTestImages; imageNum++){
				if (minPosterior > posteriors[imageNum][classNum]){
					minPosterior = posteriors[imageNum][classNum];
					minIndex = imageNum;
				}
				if (maxPosterior < posteriors[imageNum][classNum]){
					maxPosterior = posteriors[imageNum][classNum];
					maxIndex = imageNum;
				}
			}
			System.out.println("\n****** Class: " + classNum + " - Lowest Posterior Probability ******");
			printDigit(testImages, minIndex);
			System.out.println("\n****** Class: " + classNum + " - Highest Posterior Probability ******");
			printDigit(testImages, maxIndex);
		}
	}
	
	/* Calculates and prints Odds Ratio */
	public void mapOddsRatio(int digit1, int digit2){
	    printFeatureLikelihood(digit1);
	    printFeatureLikelihood(digit2);
	    double [][] oddsRatio = new double[rows][columns];
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < columns; j++){
				oddsRatio[i][j] = likelihoods[digit1][i][j] / likelihoods[digit2][i][j];
			}
		}
		printOddsRatio(oddsRatio);
	}

	/* Gets max from array */
	private int max(double[] tenPosteriors){
		int likelyDigit = 0;
		double maxPosterior = tenPosteriors[0];
		for (int i = 1; i < numClasses; i++){
			if (tenPosteriors[i] > maxPosterior){
				maxPosterior = tenPosteriors[i];
				likelyDigit = i;
			}
		}
		return likelyDigit;
	}
	
	/********************************/
	/* Constructor Helper Functions */
	/********************************/
	
	private void countNumTestDigits(int [] testLabels){
		for (int i = 0; i < numTestImages; i++){
			numTestDigits[testLabels[i]]++;
		}
	}
	
	private void countNumTrainingDigits(int [] trainingLabels){
		for (int i = 0; i < numTrainingImages; i++){
			numTrainingDigits[trainingLabels[i]]++;
		}
	}
	
	/*****************************/
	/* Necessary Print Functions */
	/*****************************/
	
	public void printClassificationRate(double overallPrediction, double[] digitPrediction){
		System.out.println("Overall Prediction: " + overallPrediction + "\n");
		System.out.println("*** Classification Rates ***");
		for (int i = 0; i < numClasses; i++){
			System.out.println(i + ": " + digitPrediction[i]);
		}
	}
	
	public void printConfusionMatrix(){
		System.out.println("\n*** Confusion Matrix ***\n" + "     0      1      2      3      4      5      6      7      8      9");
		for (int i = 0; i < numClasses; i++){
			System.out.print(i + ": ");
			for (int j = 0; j < numClasses; j++){
				System.out.printf("%.3f  ", confusionMatrix[i][j]);
			}
			System.out.println();
		}
	}
	
	private void printDigit(ArrayList<char[][]> testImages, int imageNum){
		char[][] image = testImages.get(imageNum);
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < columns; j++){
				System.out.print(image[i][j]);
			}
			System.out.println();
		}
	}
	
	public void printFeatureLikelihood(int digit){
		System.out.println("\n*** Map: Feauture Likelihood: " + digit + " ***");
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < columns; j++){
				if (likelihoods[digit][i][j] > 0.4)
					System.out.printf("+ ");
				else if (likelihoods[digit][i][j] > 0.2)
					System.out.printf(". ");
				else
					System.out.printf("  ");
			}
			System.out.println();
		}
	}
	
	public void printOddsRatio(double[][] oddsRatio){
		System.out.println("\n*** Map: Odds Ratio ***");
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < columns; j++){
				double logOfOddsRatio = Math.log(oddsRatio[i][j]);
				if (logOfOddsRatio > 0.4)
					System.out.printf("+ ");
				else if (logOfOddsRatio < -0.4)
					System.out.printf("- ");
				else
					System.out.printf("  ");
			}
			System.out.println();
		}
	}
	
	/****************************/
	/* Optional Print Functions */
	/****************************/
	
	public void printResults(){
		/* Optional Prints */
	    //printPriors();
        //printLikelihoods();
        //printPosteriors();
	}
	
	public void printPriors(){
		System.out.println("\n*** Priors ***");
        for (int i = 0; i < numClasses; i++){
        	System.out.println(" priors[" + i + "] = " + priors[i]);
        }
        System.out.println();
	}

	public void printLikelihoods(){
		System.out.println("\n*** Likelihoods ***");
		for (int digit = 0; digit < numClasses; digit++){
			for (int i = 0; i < rows; i++){
				for (int j = 0; j < columns; j++){
					System.out.printf("%.2f ", likelihoods[digit][i][j]);
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	
	public void printPosteriors(){
		System.out.println("\n*** Posteriors ***");
		for (int image = 0; image < numTestImages; image++){
			for (int digit = 0; digit < numClasses; digit++){
				System.out.printf("%.2f ", posteriors[image][digit]);
			}
			System.out.println();
		}
	}
}

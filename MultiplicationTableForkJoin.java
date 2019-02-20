import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiplicationTableForkJoin {

	final static int NUM = 10;

	public static void main(String[] args) {

		int np = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of Available Processors: " + np);

		int[] numArray1 = new int[NUM];
		for (int i = 0; i < numArray1.length; i++) {
			numArray1[i] = i + 1;
		}

		int[] numArray2 = new int[NUM];
		for (int i = 0; i < numArray2.length; i++) {
			numArray2[i] = i + 1;
		}

		for (int numOfThreads = 1; numOfThreads <= np; numOfThreads++) {

			long startTime = System.currentTimeMillis();
			int[][] resultMatrix = parallel(numArray1, numArray2, numOfThreads);
			long endTime = System.currentTimeMillis();

			System.out.println("\nTime with " + numOfThreads + " threads: " + (endTime - startTime) + "  ms.");

			if (NUM <= 10) {
				System.out.println("\nTable multiplication is ");
				printResult(resultMatrix);
			}
		}
	}

	public static void printResult(int[][] resultMatrix) {
		for (int[] row : resultMatrix) {
			for (int i : row) {
				System.out.print(i);
				System.out.print("\t");
			}
			System.out.println();
		}
	}

	public static int[][] parallel(int[] numArray1, int[] numArray2, int numOfThreads) {

		int[][] result = new int[numArray1.length][numArray2.length];

		RecursiveAction task = new MultTask(numArray2, numArray2, numOfThreads, result);

		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(task);

		return result;
	}

	private static class MultTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;

		private int[] numArray1;
		private int[] numArray2;
		private int[][] resultMatrix;
		private int numOfThreads;

		private static Lock lock = new ReentrantLock();

		public MultTask(int[] numArray1, int[] numArray2, int numOfThreads, int[][] resultMatrix) {
			this.numArray1 = numArray1;
			this.numArray2 = numArray2;
			this.resultMatrix = resultMatrix;
			this.numOfThreads = numOfThreads;
		}

		@Override
		public void compute() {

			RecursiveAction[] tasks = new RecursiveAction[numOfThreads];

			for (int i = 0; i < numOfThreads; i++) {
				if (i == numOfThreads - 1) {
					tasks[i] = (new Mult(i * (int) (numArray1.length / numOfThreads), (int) (numArray1.length)));
				} else {
					tasks[i] = (new Mult(i * (int) (numArray1.length / numOfThreads),
							(int) (numArray1.length / numOfThreads) * (i + 1)));
				}
			}

			invokeAll(tasks);

		}

		public class Mult extends RecursiveAction {
			private static final long serialVersionUID = 1L;

			private int start;
			private int end;

			public Mult(int start, int end) {
				this.start = start;
				this.end = end;
			}

			@Override
			public void compute() {
				lock.lock();

				try {
					for (int i = start; i < end; i++) {
						for (int j = 0; j < resultMatrix.length; j++) {
							for (int k = 0; k < resultMatrix[0].length; k++) {
								resultMatrix[i][j] = numArray1[i] * numArray2[j];
							}
						}
					}

				} finally {
					lock.unlock();
				}

			}
		}
	}

}

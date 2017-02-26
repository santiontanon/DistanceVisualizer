package tsne;

import tsne.java.com.jujutsu.tsne.*;
import static tsne.java.com.jujutsu.utils.EjmlOps.addRowVector;
import static tsne.java.com.jujutsu.utils.EjmlOps.assignAllLessThan;
import static tsne.java.com.jujutsu.utils.EjmlOps.assignAtIndex;
import static tsne.java.com.jujutsu.utils.EjmlOps.biggerThan;
import static tsne.java.com.jujutsu.utils.EjmlOps.colMean;
import static tsne.java.com.jujutsu.utils.EjmlOps.maximize;
import static tsne.java.com.jujutsu.utils.EjmlOps.replaceNaN;
import static tsne.java.com.jujutsu.utils.EjmlOps.setData;
import static tsne.java.com.jujutsu.utils.EjmlOps.setDiag;
import static tsne.java.com.jujutsu.utils.EjmlOps.tile;
import static tsne.java.com.jujutsu.utils.MatrixOps.abs;
import static tsne.java.com.jujutsu.utils.MatrixOps.equal;
import static tsne.java.com.jujutsu.utils.MatrixOps.negate;
import static tsne.java.com.jujutsu.utils.MatrixOps.rnorm;
import static org.ejml.ops.CommonOps.add;
import static org.ejml.ops.CommonOps.addEquals;
import static org.ejml.ops.CommonOps.divide;
import static org.ejml.ops.CommonOps.elementDiv;
import static org.ejml.ops.CommonOps.elementLog;
import static org.ejml.ops.CommonOps.elementMult;
import static org.ejml.ops.CommonOps.elementPower;
import static org.ejml.ops.CommonOps.elementSum;
import static org.ejml.ops.CommonOps.mult;
import static org.ejml.ops.CommonOps.multAddTransB;
import static org.ejml.ops.CommonOps.scale;
import static org.ejml.ops.CommonOps.subtract;
import static org.ejml.ops.CommonOps.subtractEquals;
import static org.ejml.ops.CommonOps.sumRows;
import static org.ejml.ops.CommonOps.transpose;

import org.ejml.data.DenseMatrix64F;

import tsne.java.com.jujutsu.utils.MatrixOps;
import static tsne.java.com.jujutsu.utils.MatrixOps.assignValuesToRow;
import static tsne.java.com.jujutsu.utils.MatrixOps.concatenate;
import static tsne.java.com.jujutsu.utils.MatrixOps.exp;
import static tsne.java.com.jujutsu.utils.MatrixOps.fillMatrix;
import static tsne.java.com.jujutsu.utils.MatrixOps.getValuesFromRow;
import static tsne.java.com.jujutsu.utils.MatrixOps.mean;
import static tsne.java.com.jujutsu.utils.MatrixOps.range;
import static tsne.java.com.jujutsu.utils.MatrixOps.scalarDivide;
import static tsne.java.com.jujutsu.utils.MatrixOps.scalarInverse;
import static tsne.java.com.jujutsu.utils.MatrixOps.scalarMult;
import static tsne.java.com.jujutsu.utils.MatrixOps.sqrt;
import static tsne.java.com.jujutsu.utils.MatrixOps.sum;

/**
 *
 * Author: Santiago Ontañón Based on code from: Leif Jonsson
 * (leif.jonsson@gmail.com)
 *
 */
public class TSneFromDistanceMatrix {

    MatrixOps mo = new MatrixOps();

    public double[][] tsne(double[][] D, int no_dims, double perplexity, int max_iter) {
        String IMPLEMENTATION_NAME = this.getClass().getSimpleName();
        System.out.println("D:Shape is = " + D.length + " x " + D[0].length);
        System.out.println("Running " + IMPLEMENTATION_NAME + ".");
        long end = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        int n = D.length;
        double momentum = .5;
        double initial_momentum = 0.5;
        double final_momentum = 0.8;
        int eta = 500;
        double min_gain = 0.01;
        DenseMatrix64F Y = new DenseMatrix64F(rnorm(n, no_dims));
        DenseMatrix64F Ysqlmul = new DenseMatrix64F(Y.numRows, Y.numRows); // Ysqlmul = n x n
        DenseMatrix64F dY = new DenseMatrix64F(fillMatrix(n, no_dims, 0.0));
        DenseMatrix64F iY = new DenseMatrix64F(fillMatrix(n, no_dims, 0.0));
        DenseMatrix64F gains = new DenseMatrix64F(fillMatrix(n, no_dims, 1.0));
        DenseMatrix64F btNeg = new DenseMatrix64F(n, no_dims);
        DenseMatrix64F bt = new DenseMatrix64F(n, no_dims);

        // Compute P-values
        DenseMatrix64F P = new DenseMatrix64F(d2p(D, 1e-5, perplexity).P); // P = n x n
        DenseMatrix64F Psized = new DenseMatrix64F(P.numRows, P.numCols);        // L = n x n
        DenseMatrix64F diag = new DenseMatrix64F(fillMatrix(Psized.numRows, Psized.numCols, 0.0));

        transpose(P, Psized);
        addEquals(P, Psized);
        divide(P, elementSum(P));
        replaceNaN(P, Double.MIN_VALUE);
        scale(4.0, P);					// early exaggeration
        maximize(P, 1e-12);

        System.out.println("Using perplexity: " + perplexity);
        System.out.println("Y:Shape is = " + Y.getNumRows() + " x " + Y.getNumCols());

        DenseMatrix64F sqed = new DenseMatrix64F(Y.numRows, Y.numCols);  // sqed = n x n
        DenseMatrix64F sum_Y = new DenseMatrix64F(1, Y.numRows);
        DenseMatrix64F Q = new DenseMatrix64F(P.numRows, P.numCols);  // Q = n x n

        for (int iter = 0; iter < max_iter; iter++) {
            // Compute pairwise affinities
            elementPower(Y, 2, sqed);
            sumRows(sqed, sum_Y);
            multAddTransB(-2.0, Y, Y, Ysqlmul);
            addRowVector(Ysqlmul, sum_Y);
            transpose(Ysqlmul);
            addRowVector(Ysqlmul, sum_Y);

            add(Ysqlmul, 1.0);
            divide(1.0, Ysqlmul);
            assignAtIndex(Ysqlmul, range(n), range(n), 0);
            divide(Ysqlmul, elementSum(Ysqlmul), Q);

            maximize(Q, 1e-12);

            // Compute gradient
            subtract(P, Q, Psized);
            elementMult(Psized, Ysqlmul);
            DenseMatrix64F rowsum = sumRows(Psized, null); // rowsum = nx1
            double[] rsum = new double[rowsum.numRows];
            for (int i = 0; i < rsum.length; i++) {
                rsum[i] = rowsum.get(i, 0);
            }
            setDiag(diag, rsum);
            subtract(diag, Psized, Psized);
            mult(Psized, Y, dY);
            scale(4.0, dY);

            // Perform the update
            if (iter < 20) {
                momentum = initial_momentum;
            } else {
                momentum = final_momentum;
            }

            boolean[][] boolMtrx = equal(biggerThan(dY, 0.0), biggerThan(iY, 0.0));

            setData(btNeg, abs(negate(boolMtrx)));
            setData(bt, abs(boolMtrx));

            DenseMatrix64F gainsSmall = new DenseMatrix64F(gains);
            DenseMatrix64F gainsBig = new DenseMatrix64F(gains);
            add(gainsSmall, 0.2);
            scale(0.8, gainsBig);

            elementMult(gainsSmall, btNeg);
            elementMult(gainsBig, bt);
            add(gainsSmall, gainsBig, gains);

            assignAllLessThan(gains, min_gain, min_gain);

            scale(momentum, iY);
            DenseMatrix64F gainsdY = new DenseMatrix64F(gains.numRows, dY.numCols);
            elementMult(gains, dY, gainsdY);
            scale(eta, gainsdY);
            subtractEquals(iY, gainsdY);
            addEquals(Y, iY);
            DenseMatrix64F colMeanY = colMean(Y, 0);
            DenseMatrix64F meanTile = tile(colMeanY, n, 1);
            subtractEquals(Y, meanTile);

            // Compute current value of the cost function
            if (iter % 50 == 0) {
                DenseMatrix64F Pdiv = new DenseMatrix64F(P);
                elementDiv(Pdiv, Q);
                elementLog(Pdiv, Psized);
                replaceNaN(Psized, Double.MIN_VALUE);
                elementMult(Psized, P);
                replaceNaN(Psized, Double.MIN_VALUE);
                double C = elementSum(Psized);
                end = System.currentTimeMillis();
                System.out.printf("Iteration %d: error is %f (50 iterations in %4.2f seconds)\n", iter, C, (end - start) / 1000.0);
                if (C < 0) {
                    System.err.println("Warning: Error is negative, this is usually a very bad sign!");
                }
                start = System.currentTimeMillis();
            } else if (iter % 10 == 0) {
                end = System.currentTimeMillis();
                System.out.printf("Iteration %d: (10 iterations in %4.2f seconds)\n", iter, (end - start) / 1000.0);
                start = System.currentTimeMillis();
            }

            // Stop lying about P-values
            if (iter == 100) {
                divide(P, 4);
            }
        }

        // Return solution
        return MatrixOps.extractDoubleArray(Y);
    }

    public TSne.R d2p(double[][] D, double tol, double perplexity) {
        int n = D.length;
        // D seems correct at this point compared to Python version
        double[][] P = fillMatrix(n, n, 0.0);
        double[] beta = fillMatrix(n, n, 1.0)[0];
        double logU = Math.log(perplexity);
        System.out.println("Starting d2p...");
        for (int i = 0; i < n; i++) {
            if (i % 500 == 0) {
                System.out.println("Computing P-values for point " + i + " of " + n + "...");
            }
            double betamin = Double.NEGATIVE_INFINITY;
            double betamax = Double.POSITIVE_INFINITY;
            double[][] Di = getValuesFromRow(D, i, concatenate(range(0, i), range(i + 1, n)));

            TSne.R hbeta = Hbeta(Di, beta[i]);
            double H = hbeta.H;
            double[][] thisP = hbeta.P;

            // Evaluate whether the perplexity is within tolerance
            double Hdiff = H - logU;
            int tries = 0;
            while (Math.abs(Hdiff) > tol && tries < 50) {
                if (Hdiff > 0) {
                    betamin = beta[i];
                    if (Double.isInfinite(betamax)) {
                        beta[i] = beta[i] * 2;
                    } else {
                        beta[i] = (beta[i] + betamax) / 2;
                    }
                } else {
                    betamax = beta[i];
                    if (Double.isInfinite(betamin)) {
                        beta[i] = beta[i] / 2;
                    } else {
                        beta[i] = (beta[i] + betamin) / 2;
                    }
                }

                hbeta = Hbeta(Di, beta[i]);
                H = hbeta.H;
                thisP = hbeta.P;
                Hdiff = H - logU;
                tries = tries + 1;
            }
            assignValuesToRow(P, i, concatenate(range(0, i), range(i + 1, n)), thisP[0]);
        }

        TSne.R r = new TSne.R();
        r.P = P;
        r.beta = beta;
        double sigma = mean(sqrt(scalarInverse(beta)));

        System.out.println("Mean value of sigma: " + sigma);

        return r;
    }

    public TSne.R Hbeta(double[][] D, double beta) {
        double[][] P = exp(scalarMult(scalarMult(D, beta), -1));
        double sumP = sum(P);   // sumP confirmed scalar
        double H = Math.log(sumP) + beta * sum(mo.scalarMultiply(D, P)) / sumP;
        P = scalarDivide(P, sumP);
        TSne.R r = new TSne.R();
        r.H = H;
        r.P = P;
        return r;
    }

}

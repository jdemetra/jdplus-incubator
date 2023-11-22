/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public interface ParameterInterpreter {

    public static int parametersDim(Stream<ParameterInterpreter> blocks) {
        return blocks.map(block -> block.dim())
                .reduce(0, (a, b) -> a + b);
    }

    public static int functionDim(Stream<ParameterInterpreter> blocks) {
        return blocks.filter(block -> !block.isFixed())
                .map(block -> block.getDomain().getDim())
                .reduce(0, (a, b) -> a + b);
    }

    /**
     * From function parameters to model parameters.
     *
     * @param blocks
     * @param inparams
     * @return Contains fixed model parameters, initialized with default values
     */
    public static double[] decode(List<ParameterInterpreter> blocks, DoubleSeq inparams) {
        double[] buffer = new double[parametersDim(blocks.stream())];
        int pos = 0;
        DoubleSeqCursor reader = inparams.cursor();
        for (ParameterInterpreter p : blocks) {
            pos = p.decode(reader, buffer, pos);
        }
        return buffer;
    }

    /**
     * Fix some parameters, using given (full) model parameters and a selection
     * criterion. This function will change existing default values
     *
     * @param blocks
     * @param test
     * @param inparams
     */
    public static void fixModelParameters(List<ParameterInterpreter> blocks, Predicate<ParameterInterpreter> test, DoubleSeq inparams) {
        DoubleSeqCursor reader = inparams.cursor();
        for (ParameterInterpreter p : blocks) {
            if (test.test(p)) {
                p.fixModelParameter(reader);
            } else {
                reader.skip(p.getDomain().getDim());
            }
        }
    }

    /**
     * From model parameters to function (transformed) parameters Fixed
     * parameters are not included in the output
     *
     * @param blocks
     * @param inparams
     * @return
     */
    public static double[] encode(List<ParameterInterpreter> blocks, DoubleSeq inparams) {
        double[] buffer = new double[functionDim(blocks.stream())];
        int pos = 0;
        DoubleSeqCursor reader = inparams.cursor();
        for (ParameterInterpreter p : blocks) {
            pos = p.encode(reader, buffer, pos);
        }
        return buffer;
    }

    public static double[] defaultFunctionParameters(List<ParameterInterpreter> blocks) {
        double[] buffer = new double[functionDim(blocks.stream())];
        int pos = 0;
        for (ParameterInterpreter p : blocks) {
            pos = p.fillDefault(buffer, pos);
        }
        return buffer;
    }

    ParameterInterpreter duplicate();

    String getName();

    boolean isFixed();

    boolean isScaleSensitive(boolean variance);
    
    int dim();

    /**
     * Reads the parameters and rescale them if possible. 
     * Default values should NOT be modified
     *
     * @param factor The rescaling factor (stdev of the variances)
     * @param buffer The buffer with the parameters. Rescaling must be done
     * in-place
     * @param pos The current position in the buffer
     * @param check Condition to apply the rescaling
     * @return New position in the buffer
     */
    int rescale(double factor, double[] buffer, int pos, Predicate<ParameterInterpreter> check);

    /**
     * Reads the parameters and fix them.
     *
     * @param reader The current parameters
     */
    void fixModelParameter(DoubleSeqCursor reader);

    void free();

    /**
     * Reads the parameters and transforms them into a suitable input for the
     * builders.
     *
     * @param reader The current parameters
     * @param buffer The buffer with the transformed + fixed parameters
     * @param pos The current position in the buffer
     * @return The new position in the buffer
     */
    int decode(DoubleSeqCursor reader, double[] buffer, int pos);

    /**
     * Transforms true parameters into function parameters (skipping fixed
     * parameters)
     *
     * @param reader
     * @param buffer
     * @param pos
     * @return
     */
    int encode(DoubleSeqCursor reader, double[] buffer, int pos);

    IParametersDomain getDomain();

    /**
     * Fill the default parameters (without fixed parameters)
     *
     * @param buffer
     * @param pos
     * @return
     */
    int fillDefault(double[] buffer, int pos);

}
